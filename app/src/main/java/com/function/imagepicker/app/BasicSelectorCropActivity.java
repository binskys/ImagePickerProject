package com.function.imagepicker.app;

import androidx.core.app.ActivityCompat;
import androidx.viewbinding.ViewBinding;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;

import com.function.imagepicker.dialog.BaseDialogFragment;
import com.function.imagepicker.utils.PhotoUtils;
import com.function.imagepicker.dialog.SelectorPhotoDialog;
import com.function.imagepicker.permission.EasyPermission;
import com.function.imagepicker.permission.GrantResult;
import com.function.imagepicker.permission.NextAction;
import com.function.imagepicker.permission.Permission;
import com.function.imagepicker.permission.PermissionRequestListener;
import com.function.imagepicker.permission.RequestPermissionRationalListener;
import com.hjq.toast.ToastUtils;
import com.function.imagepicker.R;
import com.function.imagepicker.databinding.SelectorPhotoMainBinding;
import com.luck.picture.lib.compress.OnCompressListener;
import com.luck.picture.lib.entity.LocalMedia;
import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.UCropActivity;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * @author Zero degree
 * @create 2021/1/4 8:57
 * @Describe 基础系统单选+裁剪
 */
public class BasicSelectorCropActivity extends BaseActivity<SelectorPhotoMainBinding> {
    private boolean isCompress = true;
    private boolean isCrop = true;

    @Override
    public ViewBinding bindingView() {
        binding = SelectorPhotoMainBinding.inflate(LayoutInflater.from(this));
        return binding;
    }

    @Override
    public void initOnCreate(Bundle bundle) {
        binding.tvSelector.setOnClickListener(v -> {
            //判断是否已经授予了权限
            if (EasyPermission.isPermissionGrant(this, Permission.CAMERA, Permission.READ_EXTERNAL_STORAGE, Permission.WRITE_EXTERNAL_STORAGE)) {
                showPhotoDialog();
            } else {
                checkPermission();
            }
        });
        binding.ivPhoto.setImageResource(R.drawable.ic_launcher_background);

    }

    private void checkPermission() {
        EasyPermission.with(this)
                .addPermission(Permission.READ_EXTERNAL_STORAGE)
                .addPermission(Permission.WRITE_EXTERNAL_STORAGE)
                .addPermission(Permission.CAMERA)
                //1.第一次请求权限时，用户拒绝了，调用shouldShowRequestPermissionRationale()后返回true，
                // 应该显示一些为什么需要这个权限的说明
                // 2.用户在第一次拒绝某个权限后，下次再次申请时，
                // 授权的dialog中将会出现“不再提醒”选项，一旦选中勾选了，那么下次申请将不会提示用户
                // 3.第二次请求权限时，用户拒绝了，并选择了“不在提醒”的选项，调用shouldShowRequestPermissionRationale()后返回false
                .addRequestPermissionRationaleHandler(Permission.CAMERA, new RequestPermissionRationalListener() {
                    @Override
                    public void onRequestPermissionRational(String permission, boolean requestPermissionRationaleResult, final NextAction nextAction) {
                        //这里处理具体逻辑，如弹窗提示用户等,但是在处理完自定义逻辑后必须调用nextAction的next方法
                        Log.e("", "onRequestPermissionRational() 申请权限操作");
                        checkPermission();

                    }
                })
                .request(new PermissionRequestListener() {
                    @Override
                    public void onGrant(Map<String, GrantResult> result) {
                        //权限申请返回
                        Log.e("", "onGrant()");


                    }

                    @Override
                    public void onCancel(String stopPermission) {
                        //在addRequestPermissionRationaleHandler的处理函数里面调用了NextAction.next(NextActionType.STOP,就会中断申请过程，直接回调到这里来
                        Log.e("", "onCancel()");
                    }
                });
    }


    SelectorPhotoDialog photoDialog;

    private void showPhotoDialog() {
        if (photoDialog == null) {
            photoDialog = SelectorPhotoDialog.newInstance("SelectorPhotoDialog");
        }
        binding.tvSize1.setText("");
        binding.tvSize2.setText("");
        photoDialog.setDialogInterface((BaseDialogFragment.DialogInterface<Integer>) object -> {
            if (object == 1) {
                //相机
                ToastUtils.show("--相机--");
                PhotoUtils.openCamera(this);
            }
            if (object == 2) {
                //相册
                ToastUtils.show("--相册--");
                PhotoUtils.openAlbum(this);
            }
        });
        photoDialog.show(getSupportFragmentManager(), "SelectorPhotoDialog");
    }

    Uri cropImageUri = null;
    Uri selectorImageUri = null;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case PhotoUtils.CODE_CAMERA:
                //拍照返回
                try {
                    selectorImageUri = PhotoUtils.cameraImageUri;
                    if (resultCode == RESULT_CANCELED) {
                        PhotoUtils.deleteImageUri(this, selectorImageUri);
                    } else {
                        if (isCrop) {
                            crop(selectorImageUri);
                        } else {
                            photoResult(PhotoUtils.getImagePath(this, selectorImageUri));
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case PhotoUtils.CODE_ALBUM:
                //相册选择返回
                try {
                    selectorImageUri = data.getData();
                    if (resultCode == RESULT_CANCELED) {
                        PhotoUtils.deleteImageUri(this, selectorImageUri);
                    } else {
                        if (isCrop) {
                            crop(selectorImageUri);
                        } else {
                            photoResult(PhotoUtils.getImagePath(this, selectorImageUri));
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case UCrop.REQUEST_CROP:
                //裁剪返回
                try {
                    cropImageUri = UCrop.getOutput(data);
                    photoResult(PhotoUtils.getNull(cropImageUri).getPath());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            default:
                break;
        }
    }

    private void crop(Uri imageUri) {
        // startActivityForResult(PhotoUtils.photoCrop( PhotoUtils.imageUri,cropImageUri), PhotoUtils.CODE_CROP);
        String name = "cropImageUri" + System.currentTimeMillis() + ".png";

        //裁剪后保存到文件中
        Uri cropUri = Uri.fromFile(new File(getCacheDir(), name));
        //图片裁剪

        UCrop.Options options = new UCrop.Options();
        //设置裁剪图片可操作的手势
        options.setAllowedGestures(UCropActivity.SCALE, UCropActivity.ROTATE, UCropActivity.ALL);
        //设置toolbar颜色
        options.setToolbarColor(ActivityCompat.getColor(this, R.color.photo_fff));
        //设置状态栏颜色
        options.setStatusBarColor(ActivityCompat.getColor(this, R.color.photo_fff));
        //是否能调整裁剪框
        options.setFreeStyleCropEnabled(true);
        //设置状态栏字体颜色
        options.isOpenWhiteStatusBar(true);
        //是否可以旋转
        options.setRotateEnabled(false);
        UCrop.of(imageUri, cropUri)
                .withAspectRatio(9, 9)
                .withMaxResultSize(200, 200)
                .withOptions(options)
                .start(BasicSelectorCropActivity.this);
    }

    /**
     * @author Zero degree
     * @create 2021/1/3 10:17
     * @Describe 照片结果
     */
    private void photoResult(String imagePath) {
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
        binding.ivPhoto.setImageBitmap(bitmap);
        binding.tvSize1.setText(PhotoUtils.getFileSize(imagePath) + "");
        if (isCompress) {
            photoCompress(imagePath);
        }
    }

    /**
     * @author Zero degree
     * @create 2021/1/3 10:17
     * @Describe 照片压缩
     * 结果
     */
    private void photoCompress(String imagePath) {
        PhotoUtils.photoCompress(this, imagePath, new OnCompressListener() {
            @Override
            public void onStart() {

            }

            @Override
            public void onSuccess(List<LocalMedia> list) {
                String path=list.get(0).getPath();
                Bitmap bitmap = BitmapFactory.decodeFile(list.get(0).getPath());
                binding.ivPhoto.setImageBitmap(bitmap);
                binding.tvSize2.setText(PhotoUtils.getFileSize(path) + "");
            }

            @Override
            public void onError(Throwable e) {

            }
        });

    }
}
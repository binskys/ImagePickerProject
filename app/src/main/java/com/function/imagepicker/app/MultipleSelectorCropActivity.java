package com.function.imagepicker.app;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;

import androidx.viewbinding.ViewBinding;

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
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.compress.OnCompressListener;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.engine.GlideEngine;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.listener.OnResultCallbackListener;
import com.function.imagepicker.R;
import com.function.imagepicker.databinding.SelectorPhotoMainBinding;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.util.List;
import java.util.Map;


/**
 * @author Zero degree
 * @create 2021/1/4 8:57
 * @Describe 多选择+裁剪
 */
public class MultipleSelectorCropActivity extends BaseActivity<SelectorPhotoMainBinding> {
    private boolean isCompress = true;
    private boolean isCrop = true;

    @Override
    public ViewBinding bindingView() {
        binding = SelectorPhotoMainBinding.inflate(LayoutInflater.from(this));
        return binding;
    }

    @Override
    public void initOnCreate(Bundle bundle) {
        ToastUtils.init(getApplication());
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
                        showPhotoDialog();
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
                showCamera();
            }
            if (object == 2) {
                //相册
                ToastUtils.show("--相册--");
                showAlbum();
            }
        });
        photoDialog.show(getSupportFragmentManager(), "SelectorPhotoDialog");
    }

    /**
     * 拍照
     */
    private void showCamera() {
        PictureSelector.create(this)
                .openCamera(PictureMimeType.ofImage())
                .loadImageEngine(GlideEngine.createGlideEngine())
                .forResult(new OnResultCallbackListener<LocalMedia>() {
                    @Override
                    public void onResult(List<LocalMedia> result) {
                        // onResult Callback
                        multiCropHandleResult(result);
                    }

                    @Override
                    public void onCancel() {
                        // onCancel Callback
                    }
                });

    }

    /**
     * 从相册选择
     */
    private void showAlbum() {
        PictureSelector.create(this)
                .openGallery(PictureMimeType.ofImage())
                .loadImageEngine(GlideEngine.createGlideEngine())
                .forResult(new OnResultCallbackListener<LocalMedia>() {
                    @Override
                    public void onResult(List<LocalMedia> result) {
                        // onResult Callback
                        multiCropHandleResult(result);
                    }

                    @Override
                    public void onCancel() {
                        // onCancel Callback
                    }
                });
    }

    Uri cropImageUri = null;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case UCrop.REQUEST_CROP:
                    singleCropHandleResult(data);
                    break;
                default:
                    break;
            }
        } else if (resultCode == UCrop.RESULT_ERROR) {
            Throwable throwable = (Throwable) data.getSerializableExtra(UCrop.EXTRA_ERROR);
            ToastUtils.show(throwable.getMessage());
        }
    }


    /**
     * 多张图片裁剪
     */
    protected void multiCropHandleResult(List<LocalMedia> result) {
        if (result == null) {
            return;
        }
        if (result.size() > 0) {
            photoResult(result.get(0).getPath());
        }
    }

    /**
     * @author Zero degree
     * @create 2021/1/4 9:22
     * @Describe 单选
     */
    private void singleCropHandleResult(Intent data) {
        cropImageUri = UCrop.getOutput(data);
        photoResult(PhotoUtils.getNull(cropImageUri).getPath());
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
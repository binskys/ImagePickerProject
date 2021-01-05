package com.function.imagepicker.utils;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.text.TextUtils;
import androidx.fragment.app.Fragment;


import com.luck.picture.lib.compress.CompressionPredicate;
import com.luck.picture.lib.compress.Luban;
import com.luck.picture.lib.compress.OnCompressListener;

import java.io.File;
import java.util.Objects;


/**
 * @author Zero degree
 * @date 2021/1/3 9:46
 * @功能: 照片处理标识
 */

public class PhotoUtils {
    /*** 相机选择返回 */
    public static final int CODE_CAMERA = 0x1001;
    /*** 相册选择返回 */
    public static final int CODE_ALBUM = 0x1002;
    /*** 裁剪返回 */
    public static final int CODE_CROP = 0x1003;
    /*** 裁剪圆形 */
    public static final int CODE_CROP_CIRCLE = 0x1031;
    /*** 裁剪方形 */
    public static final int CODE_CROP_SQUARE = 0x1032;
    /*** 裁剪方形（长方行） */
    public static final int CODE_CROP_SQUARE_H = 0x1033;
    /*** 裁剪方形（竖方行） */
    public static final int CODE_CROP_SQUARE_V = 0x1034;
    /*** 结果返回 */
    public static final int CODE_RESULT = 0x1004;
    /*** 压缩标识 */
    public static final int CODE_COMPRESS = 0x1005;

    /*** 创建图片地址 */
    public static Uri cameraImageUri;
    /*** 裁剪后图片地址 */
    public static Uri cropImageUri;

    public static String TAG = PhotoUtils.class.getSimpleName();

    /**
     * @author Zero degree
     * @create 2021/1/3 9:48
     * @Describe 打开相机（Activity）
     */
    public static void openCamera(Activity activity) {
        cameraImageUri = createImageUri(activity);
        Intent intent = new Intent();
        intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
        //如果不设置EXTRA_OUTPUT getData()  获取的是bitmap数据  是压缩后的
        intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);
        activity.startActivityForResult(intent, CODE_CAMERA);
    }

    /**
     * @author Zero degree
     * @create 2021/1/3 9:48
     * @Describe 打开相机（Fragment）
     */
    public static void openCamera(Fragment activity) {
        cameraImageUri = createImageUri(Objects.requireNonNull(activity.getActivity()));
        Intent intent = new Intent();
        intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
        //如果不设置EXTRA_OUTPUT getData()  获取的是bitmap数据  是压缩后的
        intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);
        activity.startActivityForResult(intent, CODE_CAMERA);
    }

    /**
     * @author Zero degree
     * @create 2021/1/3 9:48
     * @Describe 打开相册（Activity）
     */
    public static void openAlbum(Activity activity) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_PICK);
        intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        activity.startActivityForResult(intent, CODE_ALBUM);
    }


    /**
     * @author Zero degree
     * @create 2021/1/3 9:48
     * @Describe 打开相册（Activity）
     */
    public static void openAlbum(Fragment activity) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_PICK);
        intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        activity.startActivityForResult(intent, CODE_ALBUM);
    }


    /**
     * @author Zero degree
     * @create 2021/1/3 9:48
     * @Describe 照片裁剪
     */
    public static Intent photoCrop(Uri imageUri, Uri cropImageUri) {
        // 创建File对象，用于存储裁剪后的图片，避免更改原图
        Intent intent = new Intent("com.android.camera.action.CROP");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        intent.setDataAndType(imageUri, "image/*");
        //裁剪图片的宽高比例
//        intent.putExtra("aspectX", 1);
//        intent.putExtra("aspectY", 1);
        //可裁剪
        intent.putExtra("crop", "true");
        // 裁剪后输出图片的尺寸大小
        //intent.putExtra("outputX", 400);
        //intent.putExtra("outputY", 200);
        //支持缩放
        intent.putExtra("scale", true);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, cropImageUri);
        //输出图片格式
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        //取消人脸识别
        intent.putExtra("noFaceDetection", true);
        intent.putExtra("return-data", false);
        return intent;
    }

    /**
     * @author Zero degree
     * @create 2021/1/3 9:48
     * @Describe 照片压缩
     */
    public static void photoCompress(Context activity, String loadPhotos, OnCompressListener compressListener) {
        Luban.with(activity)
                //传入原图
                .load(loadPhotos)
                //不压缩的阈值，单位为K 忽略不压缩图片的大小（小于就不压缩）
                .ignoreBy(100)
                //缓存压缩图片路径（AbsolutePath 绝对路径）
                .setTargetDir(activity.getCacheDir().getAbsolutePath())
                //设置开启压缩条件
                .filter(new CompressionPredicate() {
                    @Override
                    public boolean apply(String path) {
                        //空路径和gif格式不压缩
                        return !(TextUtils.isEmpty(path) || path.toLowerCase().endsWith(".gif"));
                    }
                })
                //压缩回调接口
                .setCompressListener(compressListener).launch();
    }

    /**
     * @param filePath 路径
     * @return 获取文件名称
     */
    public static String getFileName(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return filePath;
        }

        int filePosi = filePath.lastIndexOf(File.separator);
        return (filePosi == -1) ? filePath : filePath.substring(filePosi + 1);
    }

    /**
     * @param path 路径
     * @return 获取文件大小
     */
    public static long getFileSize(String path) {
        if (TextUtils.isEmpty(path)) {
            return -1;
        }

        File file = new File(path);
        return (file.exists() && file.isFile() ? file.length() : -1);
    }


    /**
     * 创建图片路径
     *
     * @param context
     * @return
     */
    private static Uri createImageUri(Context context) {
        String name = "takePhoto" + System.currentTimeMillis();
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, name);
        contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, name + ".jpeg");
        contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        Uri uri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
        return uri;
    }
    /**
     * 创建图片路径
     *
     * @param context
     * @return
     */
    public static Uri cropImageUri(Context context) {
        String name = "cropImageUri" + System.currentTimeMillis();
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, name);
        contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, name + ".jpeg");
        contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        Uri uri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
        return uri;
    }

    /**
     * 删除图片
     *
     * @param context
     * @param uri
     */
    public static void deleteImageUri(Context context, Uri uri) {
        if (uri == null) {
            return;
        }
        context.getContentResolver().delete(uri, null, null);
    }

    /**
     * @author Zero degree
     * @create 2021/1/3 10:12
     * @Describe 兼容10.0 路径 识别为空的问题
     */
    public static Uri getImageContentUri(Context context, String path) {
        Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Images.Media._ID}, MediaStore.Images.Media.DATA + "=? ",
                new String[]{path}, null);
        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
            Uri baseUri = Uri.parse("content://media/external/images/media");
            return Uri.withAppendedPath(baseUri, "" + id);
        } else {
            // 如果图片不在手机的共享图片数据库，就先把它插入。
            if (new File(path).exists()) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, path);
                return context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            } else {
                return null;
            }
        }
    }

    /**
     * @author Zero degree
     * @create 2021/1/3 10:15
     * @Describe 根据 Uri 获取照片路径
     */
    public static String getImagePath(Context context, Uri uri) {
        try {
            Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
                cursor.close();
                return path;
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static <T> T getNull(T obj) {
        if (obj == null) {
            throw new NullPointerException();
        }
        return obj;
    }
}

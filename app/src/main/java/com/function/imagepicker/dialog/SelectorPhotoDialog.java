package com.function.imagepicker.dialog;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;

import androidx.viewbinding.ViewBinding;

import com.function.imagepicker.databinding.SelectorPhotoDialogBinding;


/**
 * @author Zero degree
 * @date 2020/12/31 17:20
 * @功能: 照片选择/上传照片
 */

public class SelectorPhotoDialog extends BaseDialogFragment<SelectorPhotoDialogBinding> {

    public static SelectorPhotoDialog newInstance(String tittle) {
        SelectorPhotoDialog fragment = new SelectorPhotoDialog();
        Bundle bundle = new Bundle();
        bundle.putString("tittle", tittle);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public ViewBinding bindingView() {
        binding = SelectorPhotoDialogBinding.inflate(LayoutInflater.from(getActivity()));
        return binding;
    }

    @Override
    public int setAnimResId() {
        return DIALOG_IN_OUT;
    }

    @Override
    public int getShape() {
        return SHARE_TRA;
    }

    @Override
    public float setWidth() {
        return 0.92f;
    }

    @Override
    public int setGravity() {
        return Gravity.BOTTOM;
    }

    @Override
    public void initOnCreate(Bundle bundle) {
        //相机 1
        binding.tvCamera.setOnClickListener(v -> {
            mInterface.onDialogClick(1);
            dismissDialog();
        });
        //相册 2
        binding.tvAlbum.setOnClickListener(v -> {
            mInterface.onDialogClick(2);
            dismissDialog();
        });
        //取消 0
        binding.tvCancel.setOnClickListener(v -> {
            mInterface.onDialogClick(0);
            dismissDialog();
        });

    }
}

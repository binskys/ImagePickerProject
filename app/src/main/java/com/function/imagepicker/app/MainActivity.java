package com.function.imagepicker.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.viewbinding.ViewBinding;

import com.function.imagepicker.databinding.AppMainBinding;


/**
 * @author Zero degree
 * @date 2021/1/4 15:06
 * @功能:
 */

public class MainActivity extends BaseActivity<AppMainBinding> {
    @Override
    public ViewBinding bindingView() {
        binding = AppMainBinding.inflate(LayoutInflater.from(this));
        return binding;
    }

    @Override
    public void initOnCreate(Bundle bundle) {


    }

    public void onBasicSelector(View view) {
        startActivity(new Intent(this, BasicSelectorCropActivity.class));
    }

    public void onMultipleSelector(View view) {
        startActivity(new Intent(this, MultipleSelectorCropActivity.class));
    }
}

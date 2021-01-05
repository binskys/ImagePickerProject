package com.function.imagepicker.app;

import android.os.Bundle;
import android.view.LayoutInflater;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewbinding.ViewBinding;


public abstract class BaseActivity<V extends ViewBinding> extends AppCompatActivity {
   public V binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (bindingView()!=null) {
            setContentView(binding.getRoot());
            initOnCreate(savedInstanceState);
        }
    }

    public abstract ViewBinding bindingView();
    public abstract void initOnCreate(Bundle bundle);

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding=null;
    }
}
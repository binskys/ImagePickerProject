package com.function.imagepicker;

import android.app.Application;
import com.hjq.toast.ToastUtils;


/**
 * @author Zero degree
 * @date 2021/1/4 14:18
 * @功能:
 */

public class MyApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ToastUtils.init(this);
    }
}

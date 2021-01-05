package com.function.imagepicker.dialog;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.viewbinding.ViewBinding;


import com.function.imagepicker.R;

import java.util.Objects;

/**
 * @author benny
 * @date 2020/8/21 9:33
 * @功能: 启动页弹框·加载json动画
 */

public abstract class BaseDialogFragment<V extends ViewBinding> extends DialogFragment {
    public V binding;
    /*** 默认弹出*/
    public static final int DIALOG_DARK_FADE = R.style.dark_fade;

    /*** 向上弹起向下滑落*/
    public static final int DIALOG_IN_OUT = R.style.dialog_in_out;

    /*** 从左弹出从右关闭*/
    public static final int DIALOG_LEFT_RIGHT = R.style.dialog_left_right;

    /*** 背景圆角*/
    public static final int SHARE_CIRCLE = R.drawable.dialog_shape_circle;
    /*** 背景上圆下方*/
    public static final int SHARE_CIRCLE_TOP = R.drawable.dialog_shape_circle_top;
    /*** 背景方角*/
    public static final int SHARE_SQUARE = R.drawable.dialog_shape_square;
    /*** 背景透明*/
    public static final int SHARE_TRA = R.drawable.dialog_shape_transparent;

    public DialogInterface mInterface;

    public void setDialogInterface(DialogInterface mInterface) {
        this.mInterface = mInterface;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, R.style.Style_DialogFragment);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (bindingView() != null) {
            return binding.getRoot();
        } else {
            Log.e("BaseDialogFragment", "BaseDialogFragment view is null");
            return null;
        }
    }

    public abstract ViewBinding bindingView();

    public abstract void initOnCreate(Bundle bundle);

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        //将DialogFragment的样式设置为无标题
        //或 样式加： <item name="android:windowNoTitle">true</item>
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        super.onActivityCreated(savedInstanceState);
        initOnCreate(savedInstanceState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        //这里创建传统的dialog
        return super.onCreateDialog(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        Window window = null;
        if (dialog != null) {
            window = dialog.getWindow();
        }
        if (window != null) {
            window.getDecorView().setPadding(0, 0, 0, 0);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            DisplayMetrics dm = new DisplayMetrics();
            Display display = Objects.requireNonNull(getActivity()).getWindowManager().getDefaultDisplay();
            display.getMetrics(dm);
            WindowManager.LayoutParams attributes = window.getAttributes();

            //设置dialog显示位置
            attributes.gravity=setGravity();

            if (setAnimResId() != 0) {
                //设置动画效果
                attributes.windowAnimations=setAnimResId();
            } else {
                attributes.windowAnimations=android.R.style.Animation_InputMethod;
            }
            //绑定弹框属性
            window.setAttributes(attributes);
            //设置dialog背景风格
            window.setBackgroundDrawableResource(getShape());
            if (isClearFlags()) {
                //清理背景变暗
                window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            }
            int width=dm.widthPixels;
            int height=ViewGroup.LayoutParams.WRAP_CONTENT;
            if (setWidth() != 0) {
                //计算Dialog宽度
                width = (int) (dm.widthPixels * setWidth());
            }
            if (setHeight() != 0) {
                //计算Dialog高度
                height = (int) (dm.heightPixels * setHeight());
            }
            //设置Dialog宽、高
            window.setLayout(width,height);
            dialog.setCanceledOnTouchOutside(isCanceledOutside());
        }
    }

    public int getShape() {
        return SHARE_CIRCLE;
    }

    public boolean isClearFlags() {
        return false;
    }

    public int setAnimResId() {
        return DIALOG_DARK_FADE;
    }

    public float setWidth() {
        return 0.92f;
    }

    public float setHeight() {
        return 0;
    }

    /**
     * @author Zero degree
     * @create 2020/12/31 17:15
     * @Describe 点击屏幕消失
     */
    public boolean isCanceledOutside() {
        return true;
    }

    /**
     * @author Zero degree
     * @create 2020/12/31 17:15
     * @Describe dialog 显示位置
     */
    public int setGravity() {
        return Gravity.CENTER;
    }


    /**
     * @author Zero degree
     * @create 2020/12/31 17:13
     * @Describe 关闭弹框
     */
    public void dismissDialog() {
        dismiss();
    }

    /**
     * @author Zero degree
     * @create 2020/12/31 17:27
     * @Describe 事件回调封装
     */
    public interface DialogInterface<Object> {
        void onDialogClick(Object object);
    }
}

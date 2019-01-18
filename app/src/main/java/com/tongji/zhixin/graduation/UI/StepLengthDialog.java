package com.tongji.zhixin.graduation.UI;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.tongji.zhixin.graduation.R;

/**
 * Created by zhixin on 2018/5/25.
 */

public class StepLengthDialog extends Dialog {
    private Button setButton;
    public EditText stepLengthEditText;
    private View.OnClickListener listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.step_length_dialog);

        setButton = findViewById(R.id.steplengthbutton);
        stepLengthEditText = findViewById(R.id.steplengthedittext);
        setButton.setOnClickListener(listener);
        this.setCancelable(true);
    }

    public StepLengthDialog(@NonNull Context context) {
        super(context);
    }

    public StepLengthDialog(@NonNull Context context, int themeResId, View.OnClickListener listener) {
        super(context, themeResId);
        this.listener = listener;
    }

    public StepLengthDialog(@NonNull Context context, boolean cancelable, @Nullable
            OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }
}

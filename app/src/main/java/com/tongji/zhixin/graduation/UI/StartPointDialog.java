package com.tongji.zhixin.graduation.UI;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.tongji.zhixin.graduation.R;

/**
 * Created by zhixin on 2018/5/24.
 */

public class StartPointDialog extends Dialog {

    private Activity contex;
    private Button setButton;
    public EditText xEditText;
    public EditText yEditText;
    private View.OnClickListener listener;

    public StartPointDialog(@NonNull Context context) {
        super(context);
    }

    public StartPointDialog(@NonNull Activity context, int themeResId, View.OnClickListener listener) {
        super(context, themeResId);
        this.listener = listener;
        this.contex = context;
    }

    public StartPointDialog(@NonNull Context context, boolean cancelable, @Nullable
            OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.set_start_point_dialog);
        setButton = findViewById(R.id.setStartPoint);
        xEditText = findViewById(R.id.startX);
        yEditText = findViewById(R.id.startY);

        setButton.setOnClickListener(listener);
        this.setCancelable(true);
    }
}

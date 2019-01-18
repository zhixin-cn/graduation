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

public class SensivityDialog extends Dialog {
    private Button sensivityButton;
    public EditText sensivityEditText;
    private View.OnClickListener listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.sensivity_dialog);

        sensivityButton = findViewById(R.id.sensivityButton);
        sensivityEditText = findViewById(R.id.sensivityEditText);
        sensivityButton.setOnClickListener(listener);
    }

    public SensivityDialog(@NonNull Context context) {
        super(context);
    }

    public SensivityDialog(@NonNull Context context, int themeResId, View.OnClickListener listener) {
        super(context, themeResId);
        this.listener = listener;
    }

    public SensivityDialog(@NonNull Context context, boolean cancelable, @Nullable
            OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }
}

package com.tongji.zhixin.graduation.UI;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.tongji.zhixin.graduation.R;

/**
 * Created by zhixin on 2018/5/23.
 */

public class GetPointDialog extends Dialog {
    private Activity context;
    private Button okButton;
    public EditText x;
    public EditText y;
    private View.OnClickListener mClickListener;

    public GetPointDialog(Activity context) {
        super(context);
        this.context = context;
    }

    public GetPointDialog(Activity context, int theme, View.OnClickListener clickListener) {
        super(context, theme);
        this.context = context;
        this.mClickListener = clickListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.get_point_dialog);

        okButton = findViewById(R.id.ok);
        x = findViewById(R.id.x);
        y = findViewById(R.id.y);
//        Window dialogWindow = this.getWindow();
//        WindowManager windowManager = context.getWindowManager();
//        Display display = windowManager.getDefaultDisplay(); // 获取屏幕宽、高用
//        WindowManager.LayoutParams params = dialogWindow.getAttributes(); // 获取对话框当前的参数值
//        params.width = (int) (display.getWidth() * 0.8); // 宽度设置为屏幕的0.8
//        dialogWindow.setAttributes(params);

        okButton.setOnClickListener(mClickListener);
        this.setCancelable(true);
    }
}

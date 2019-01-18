package com.tongji.zhixin.graduation.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.tongji.zhixin.graduation.R;

public class StartActivity extends AppCompatActivity {

    private Button start;
    private RadioGroup choice;
    private String choiceState = "NoChoice";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        start = findViewById(R.id.start);
        choice = findViewById(R.id.choice);
        start.setOnClickListener(new MyButtonListener());
        choice.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton r = findViewById(checkedId);
                choiceState = String.valueOf(r.getText());
//                Log.d("cho", String.valueOf(r.getText()));
            }
        });
    }

    class MyButtonListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.start:
                    if(choiceState.equals("NoChoice")){
                        Toast.makeText(StartActivity.this, "未选择模式", Toast.LENGTH_SHORT).show();
                    }else {
                        Intent intent = new Intent(StartActivity.this, Graduation_Project.class);
                        intent.putExtra("mode", choiceState);
                        startActivity(intent);
                    }
                    break;
                default:
                    break;
            }
        }
    }
}

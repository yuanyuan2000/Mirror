package com.example.mirror.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.example.mirror.R;

public class HintActivity extends AppCompatActivity {

    private TextView know;                  //声明 “我知道”组件

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);  //不显示状态栏
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hint);                //设置对应的布局文件
        know = (TextView) findViewById(R.id.i_know);     //获取“我知道了”组件
        know.setOnClickListener(new View.OnClickListener() { //设置“我知道了”组件单击事件
            @Override
            public void onClick(View v) {
                finish();                  //结束窗体
            }
        });

    }
}
package com.example.mirror.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;

import com.example.mirror.R;

public class GuideActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide);  //用R.layout.activity_guide来获取layout目录中的activity_guide.xml布局文件
        handler.sendEmptyMessageDelayed(1,3000);// 指定3秒后发送消息what:1
    }

    //用回调函数接受消息，当msg.waht为1时，跳转到主页面
    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            if(msg.what == 1){
                Intent intent = new Intent(GuideActivity.this, MainActivity.class);  //创建跳转意图，传输数据
                startActivity(intent);  //跳转到intent指定的页面
                finish();  //关闭页面
            }
            return false;
        }
    });

    //屏蔽手机的返回键
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){     //如果按返回键
            return false;                  //返回否
        }
        return false;
    }
}
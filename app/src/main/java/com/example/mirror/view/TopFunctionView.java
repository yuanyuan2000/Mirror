package com.example.mirror.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.example.mirror.R;

//继承线性布局（因为这个自定义组件要使用线性布局），实现单击事件监听接口（几个按钮要使用）
public class TopFunctionView extends LinearLayout implements View.OnClickListener{

    private LayoutInflater mInflater;          //声明寻找XML文件类（因为这整个类本质就是一个线性布局，所以导入这个LayoutInflater很关键）
    private ImageView hint,choose,down,up;      //定义控件对象（包括：系统帮助、亮度、选择镜框），便于设置监听

    //构造方法
    public TopFunctionView(Context context) {
        this(context, null);
    }
    public TopFunctionView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    public TopFunctionView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mInflater = LayoutInflater.from(context);
        View view  = mInflater.inflate(R.layout.view_top_function,this);  //导入view_function布局文件
        hint = (ImageView) view.findViewById(R.id.hint);                //获取帮助按钮对象
        choose = (ImageView) view.findViewById(R.id.mirror_frame_choose);            //获取选择镜框按钮对象
        down = (ImageView) view.findViewById(R.id.light_down);                //获取减少亮度按钮对象
        up = (ImageView) view.findViewById(R.id.light_up);                    //获取增加亮度按钮对象
        hint.setOnClickListener(this);                    //设置帮助按钮按键监听事件
        choose.setOnClickListener(this);                //设置选择镜框按钮按键监听事件
        down.setOnClickListener(this);                    //设置减少亮度按钮按键监听事件
        up.setOnClickListener(this);                    //设置增加亮度按钮按键监听事件
    }

    public void setOnTopFunctionViewItemClickListener(onTopFunctionViewItemClickListener monFunctionViewItemClickListener){
        this.listener = monFunctionViewItemClickListener;    //设置监听对象
    }

    //定义回调接口，它能够实现调用4个按钮方法，这4个方法在MainActivity中被重写
    private onTopFunctionViewItemClickListener listener;
    public interface onTopFunctionViewItemClickListener{
        void hint();                             //系统帮助按钮方法
        void choose();                          //选择镜框按钮方法
        void down();                            //减少亮度按钮方法
        void up();                              //增加亮度按钮方法
    }

    //监控单击事件的函数
    @Override
    public void onClick(View v) {
        if (listener!= null){                 //监听不为空，表示有按钮按下
            switch (v.getId()){
                case R.id.hint:                 //帮助按钮
                    listener.hint();            //执行监听方法，实现功能
                    break;
                case R.id.mirror_frame_choose:                //选择镜框按钮
                    listener.choose();            //执行监听方法，实现功能
                    break;
                case R.id.light_down:                //减少亮度按钮
                    listener.down();            //执行监听方法，实现功能
                    break;
                case R.id.light_up:                //增加亮度
                    listener.up();            //执行监听方法，实现功能
                    break;
                default:
                    break;
            }
        }
    }
}

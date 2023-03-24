package com.example.mirror.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import com.example.mirror.R;
import com.example.mirror.utils.AudioRecordManger;
import com.example.mirror.utils.SetBrightness;
import com.example.mirror.view.DrawFogView;
import com.example.mirror.view.MirrorFrameView;
import com.example.mirror.view.TopFunctionView;
import com.zys.brokenview.BrokenCallback;
import com.zys.brokenview.BrokenTouchListener;
import com.zys.brokenview.BrokenView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback,
        SeekBar.OnSeekBarChangeListener, TopFunctionView.onTopFunctionViewItemClickListener,
        View.OnTouchListener, View.OnClickListener, DrawFogView.OnCaYiCaCompleteListener {

    private static final String TAG = MainActivity.class.getSimpleName(); //获得类名
    //权限申请
    String[] permissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO, Manifest.permission.WAKE_LOCK, Manifest.permission.WRITE_SETTINGS,
            Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS};
    List<String> mPermissionList = new ArrayList<>();
    private static final int PERMISSION_REQUEST = 1;
    //布局相关变量
    private SurfaceHolder holder;            //显示相机拍摄的内容
    private SurfaceView surfaceView;        //显示相机拍摄的内容
    private MirrorFrameView mirrorFrameView;        //显示镜框类
    private TopFunctionView topFunctionView;    //显示顶部功能栏类
    private SeekBar seekBar;                //控制焦距滑动条
    private ImageView add, minus;            //控制焦距按钮
    private LinearLayout bottom;            //调节焦距的按钮
    private ImageView save;                //保存图片的按钮
    private ProgressDialog dialog;        //弹窗
    private DrawFogView drawFogView;            //起雾绘画类
    //摄像头相关变量
    private boolean haveCamera;        //是否有相机设备
    private int mCurrentCamIndex;        //相机的指数
    private int ROTATE;                //旋转值
    private int minFocus;                //当前手机默认的焦距
    private int maxFocus;                //当前手机的最大焦距
    private int everyFocus;            //用于调整焦距
    private int nowFocus;                //当前的焦距值
    private Camera camera;             //声明相机变量
    //镜框选择相关变量
    private int frame_index;                //镜框类型
    private int[] frame_index_ID;            //图片资源ID的数组
    private static final int PHOTO = 1;      //镜框请求值
    //亮度调节相关变量
    private int brightnessValue;            //屏幕亮度值
    private boolean isAutoBrightness;        //屏幕是否为自动调节
    private int SegmentLengh;            //把亮度分为八段每段256的1/8
    //吹气起雾相关变量
    private AudioRecordManger audioRecordManger;        //调用话筒实现类
    private static final int RECORD = 2;            //监听话筒
    //碎屏相关变量
    private BrokenView brokenView;        //碎屏控件
    private boolean isBroken;                //开启或关闭碎屏功能
    private BrokenTouchListener brokenTouchListener;        //碎屏的单击监听
    private MyBrokenCallback brokenCallBack;            //BrokenView的生命周期
    private Paint brokenPaint;                //碎屏裂缝的画笔
    private GestureDetector gestureDetector;            //手势变量
    private MySimpleGestureListener mySimpleGestureListener;    //手势自定义子类

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        checkPermission();
        super.onCreate(savedInstanceState);
    }

    //权限四个函数：检查权限、权限回调、允许后、拒绝后
    private void checkPermission() {
        mPermissionList.clear();
        for (int i = 0; i < permissions.length; i++) {
            //判断哪些权限未授予
            if (ContextCompat.checkSelfPermission(this, permissions[i]) != PackageManager.PERMISSION_GRANTED) {
                mPermissionList.add(permissions[i]);
            }
        }
        if (mPermissionList.isEmpty()) {//未授予的权限为空，表示都授予了
            onAllow();
        } else {//请求权限方法
            String[] permissions = mPermissionList.toArray(new String[mPermissionList.size()]);//将List转为数组
            ActivityCompat.requestPermissions(MainActivity.this, permissions, PERMISSION_REQUEST);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for (int i = 0; i < grantResults.length; i++) {
            if (grantResults[i] == PackageManager.PERMISSION_DENIED) {// 拒绝权限
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[i])) {
                    // 选了"不再提醒"，一般提示跳转到权限设置页面
                    Log.e(TAG, permissions[i] + "需要到设置页面授予权限才能使用！ ");
                    onAllow();  //暂时这样写！
                } else {
                    //没有选择不再提醒，直接调用onReject关闭页面，下次打开还会询问
                    onReject();
                }
                return;
            }
        }
        onAllow();  //全部都点击同意方才进入allow
    }
    public void onAllow(){
        Log.e(TAG, "onAllow: ");
        setContentView(R.layout.activity_main);
        initViews();  //初始化控件
        holder = surfaceView.getHolder();          //获得预览对象
        holder.addCallback(this);                  //增加callback回调接口（这样后面才开始进行那三个回调函数）
        //设置默认镜框ID数组
        frame_index = 0;
        frame_index_ID = new int[]{R.mipmap.mag_0001, R.mipmap.mag_0003, R.mipmap.mag_0005,
                R.mipmap.mag_0006, R.mipmap.mag_0007, R.mipmap.mag_0008, R.mipmap.mag_0009,
                R.mipmap.mag_0011, R.mipmap.mag_0012, R.mipmap.mag_0014};
        seekBar.setOnSeekBarChangeListener(this);
        add.setOnTouchListener(this);        //放大焦距
        minus.setOnTouchListener(this);        //缩小焦距
        seekBar.setOnSeekBarChangeListener(this);    //进度条监听
        topFunctionView.setOnTopFunctionViewItemClickListener(this);    //调用按钮单击监听事件
        mirrorFrameView.setOnTouchListener(this);            //镜框图片单击事件监听（用于起雾擦除功能
        drawFogView.setOnCaYiCaCompleteListener(this);  //画布监听
        getBrightnessFromWindow();                //获取屏幕亮度的相关属性
        audioRecordManger = new AudioRecordManger(handler,RECORD);//实例化话筒实现类
        audioRecordManger.getNoiseLevel();//打开话筒监听声音
        mySimpleGestureListener = new MySimpleGestureListener();    //创建手势识别监听对象
        gestureDetector = new GestureDetector(this,mySimpleGestureListener);//创建手势识别对象
        brokenPaint = new Paint();                 //新建碎屏画笔
        brokenPaint.setStrokeWidth(5);             //设置空心线宽
        brokenPaint.setColor(Color.BLACK);            //设置颜色
        brokenPaint.setAntiAlias(true);            //抗锯齿
        brokenView = BrokenView.add2Window(this);        //碎屏视图加入窗体
        //碎屏按键监听事件
        brokenTouchListener = new BrokenTouchListener.Builder(brokenView).setPaint(brokenPaint).setBreakDuration(2000).setFallDuration(5000).build();
        brokenView.setEnable(true);                //默认不开启碎屏功能
        brokenCallBack = new MyBrokenCallback();            //碎屏生命周期
        brokenView.setCallback(brokenCallBack);            //执行碎屏
    }
    public void onReject(){
        Log.e(TAG, "onReject: ");
        finish();
    }

    private void initViews() {
        //根据R文件中的id和xml中的声明，将类和对应的布局联系起来
        surfaceView = (SurfaceView) findViewById(R.id.surface); //获得布局文件中Id为surface的组件
        mirrorFrameView = (MirrorFrameView) findViewById(R.id.mirror_frame);//获得布局文件中picture的组件
        topFunctionView = (TopFunctionView) findViewById(R.id.top_function);//获得布局文件中function组件
        seekBar = (SeekBar) findViewById(R.id.seekbar);           //获得布局文件中seekbar拖动条
        add = (ImageView) findViewById(R.id.add);                 //获得布局文件中add焦距放大组件
        minus = (ImageView) findViewById(R.id.minus);              //获得布局文件中minus焦距缩小组件
        bottom = (LinearLayout) findViewById(R.id.bottom_bar); //获得布局文件中底部线性布局
        drawFogView = (DrawFogView) findViewById(R.id.draw_fog); //获得布局文件中擦屏组件
    }

    //相机相关设置
    private void setCamera() {
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){  //判断手机是否有摄像头
            Camera mCamera = null;                                //声明相机变量
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();//创建相机信息对象
            //Part1：获取前置摄像头，得到mCurrentCamIndex和camera
            int cameraCount = Camera.getNumberOfCameras();           //获取摄像头个数
            for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
                Camera.getCameraInfo(camIdx, cameraInfo);            //获取相机对象
                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {  //判断是否是前置摄像头
                    try {
                        mCamera = Camera.open(camIdx);                //打开摄像头
                        mCurrentCamIndex = camIdx;                    //设置前置摄像头ID
                        break;
                    } catch (RuntimeException e) {
                        Log.e(TAG, "相机打开失败:" + e.getLocalizedMessage());
                    }
                }
            }
            camera = mCamera;
            //Part2：设置相机的拍摄角度，得到ROTATE
            int rotation = this.getWindowManager().getDefaultDisplay().getRotation();  //获得旋转角度
            int degrees = 0;
            switch (rotation) {
                case Surface.ROTATION_0: degrees = 0; break;
                case Surface.ROTATION_90: degrees = 90; break;
                case Surface.ROTATION_180: degrees = 180; break;
                case Surface.ROTATION_270: degrees = 270; break;
            }
            int result = 0;
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                result = (360 - (cameraInfo.orientation + degrees) % 360) % 360;  //前置摄像头旋转算法
            } else {
                result = (cameraInfo.orientation - degrees + 360) % 360;  //后置摄像头旋转算法
            }
            ROTATE = result + 180;
            camera.setDisplayOrientation(result);  //设置相机拍摄角度
            //Part3：设置相机其他参数，得到minFocus、maxFocus、everyFocus、nowFocus
            Camera.Parameters parameters = camera.getParameters();    //相机参数类
            parameters.setPictureFormat(ImageFormat.JPEG);            //设置图片格式JPG
            List<String> list = parameters.getSupportedFocusModes();    //获取支持的对焦模式
            for (String str : list) {
                Log.e(TAG, "支持的对焦的模式:"+ str);
            }
            List<Camera.Size> pictureList = parameters.getSupportedPictureSizes();  //手机支持的图片尺寸集合
            List<Camera.Size> previewList = parameters.getSupportedPreviewSizes();  //手机支持的预览尺寸集合
            parameters.setPictureSize(pictureList.get(0).width, pictureList.get(0).height);  //设置为当前使用手机的最大尺寸
            parameters.setPreviewSize(previewList.get(0).width, previewList.get(0).height);  //设置为当前使用手机的最大尺寸
            minFocus = parameters.getZoom();                //获得最小焦距
            maxFocus = parameters.getMaxZoom();            //获得最大焦距
            everyFocus = 1;                    //每次增加及减少值
            nowFocus = minFocus;                    //当前焦距为最小值
            seekBar.setMax(maxFocus);                //设置滑动条最右边为最大焦距
            Log.e(TAG, "当前镜头距离:"+ minFocus + "\t\t获取最大距离:"+ maxFocus);//日志打印
            camera.setParameters(parameters);                //设置相机参数
        }
    }

    //surfaceView创建时的回调函数
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.e("surfaceCreated","surface绘制开始");
        try {
            setCamera();            //设置摄像头
            camera.setPreviewDisplay(holder);    //设置预览显示的surfaceholder接口（这句话很关键，等于把surface的内容跟摄像头联系起来）
            camera.startPreview();        //开始预览

        } catch (IOException e) {
            camera.release();            //相机释放
            camera = null;               //相机对象赋值Null
            e.printStackTrace();         //打印错误信息
        }
    }
    //surfaceView改变时的回调函数
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.e("surfaceChanged", "surface绘制改变");
        try {
            camera.stopPreview();                      //相机停止预览
            camera.setPreviewDisplay(holder);              //设置相机预览显示区域
            camera.startPreview();                      //相机启动预览
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //surfaceView销毁时的回调函数
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.e("surfaceDestroyed", "surface绘制结束");
        camera.setPreviewCallback(null);                //停止相机视频，这个方法必须在前面，否则出错
        camera.stopPreview();                          //停止预览
        camera.release();                              //相机释放
        camera = null;                                  //相机对象赋值为空
    }

    //拖动进度条时的回调函数
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        Camera.Parameters parameters = camera.getParameters();    //获取相机参数
        nowFocus = progress;                     //进度值赋值给焦距
        parameters.setZoom(progress);                //设置焦距
        camera.setParameters(parameters);                //设置相机
    }
    //开始拖动进度条的回调函数
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }
    //停止拖动进度条的回调函数
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    //手指触摸的回调函数（按按钮时使用）
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (v.getId()) {
            case R.id.add:
                addZoomValues();            //放大焦距
                break;
            case R.id.minus:
                minusZoomValues();        //缩小焦距
                break;
            case R.id.mirror_frame:            //多点触控的操作
                gestureDetector.onTouchEvent(event);    //调用手势识别函数
                break;
            default:
                break;
        }
        return false;  //这里true和false有区别吗?
    }

    //单击回调函数
    @Override
    public void onClick(View v) {

    }

    //负责调节焦距的几个函数
    private int getZoomValues() {
        Camera.Parameters parameters = camera.getParameters();    //获取相机参数
        int values = parameters.getZoom();                //获取当前焦距
        return values;                         //返回焦距值
    }
    private void setZoomValues(int want) {
        Camera.Parameters parameters = camera.getParameters();    //获取相机参数
        seekBar.setProgress(want);                //设置进度
        parameters.setZoom(want);                //设置焦距参数
        camera.setParameters(parameters);                //设置相机参数-焦距
    }
    private void addZoomValues() {
        if (nowFocus > maxFocus) {         //当前焦距 大于 最大焦距
            Log.e(TAG, "大于maxFocus是不可能的！");
        } else if (nowFocus == maxFocus) {
        } else {                //设置焦距，当前焦距 + 每一次变化的值
            setZoomValues(getZoomValues() + everyFocus);
        }
    }
    private void minusZoomValues() {
        if (nowFocus < 0) {
            Log.e(TAG, "小于0是不可能的！");
        } else if (nowFocus == 0) {
        } else {                //设置焦距，当前焦距 - 每一次变化的值
            setZoomValues(getZoomValues() - everyFocus);
        }
    }

    //TopFunctionView的四个回调接口
    @Override
    public void hint() {
        Intent intent = new Intent(this,HintActivity.class);
        startActivity(intent);              //界面跳转到系统帮助界面
    }
    @Override
    public void choose() {
        Intent intent = new Intent(this, PhotoFrameActivity.class);
        startActivityForResult(intent, PHOTO);     //跳转页面并获得选择镜框ID的返回值
        Toast.makeText(this, "选择!", Toast.LENGTH_SHORT).show(); //提示
    }
    @Override
    public void down() {
        downCurrentActivityBrightnessValues();            //调用调低亮度方法
    }
    @Override
    public void up() {
        upCurrentActivityBrightnessValues();            //调用调高亮度方法
    }

    //从选择镜框界面返回选择的镜框图片数据
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);//获得选择镜框界面的data
        Log.e(TAG, "返回值:"+ resultCode + "\t\t请求值:"+ requestCode);
        if (resultCode == RESULT_OK && requestCode == PHOTO) {
            int position = data.getIntExtra("POSITION", 0);     //从返回数据获得POSITION值
            frame_index = position;
            Log.e(TAG, "返回的镜框类别:"+ position);
            mirrorFrameView.setPhotoFrame(position);        //传递POSITION值，设置镜框
        }
    }

    private void setMyActivityBright(int brightnessValues) {
        SetBrightness.setBrightness(this, brightnessValues);//调用SetBrightness类方法设置亮度
        SetBrightness.saveBrightness(SetBrightness.getResolver(this), brightnessValues);  //保存亮度
    }
    private void getAfterMySetBrightnessValues() {
        brightnessValue = SetBrightness.getScreenBrightness(this);    //获得亮度
        Log.e(TAG, "当前手机屏幕亮度值:"+ brightnessValue);
    }
    public void getBrightnessFromWindow() {
        //获得是否自动自动调节亮度
        isAutoBrightness = SetBrightness.isAutoBrightness(SetBrightness.getResolver(this));
        Log.e(TAG, "当前手机是否是自动调节屏幕亮度:"+ isAutoBrightness);
        if (isAutoBrightness) {                //如果是自动调节亮度
            SetBrightness.stopAutoBrightness(this);        //关闭自动调节亮度
            Log.e(TAG, "关闭了自动调节!");
            setMyActivityBright(255 / 2 + 1);
        }
        SegmentLengh = (255 / 2 + 1) / 4;            //亮度值0～256，每32为一个区间
        getAfterMySetBrightnessValues();            //获取设置后的亮度
    }
    private void downCurrentActivityBrightnessValues(){
        if (brightnessValue >0) {
            setMyActivityBright(brightnessValue - SegmentLengh);  //减少亮度
        }
        getAfterMySetBrightnessValues();            //获取设置后的屏幕亮度
    }
    private void upCurrentActivityBrightnessValues(){
        if (brightnessValue <255) {
            if (brightnessValue + SegmentLengh >= 256){         //最大值255
                return;
            }
            setMyActivityBright(brightnessValue + SegmentLengh );//调高亮度
        }
        getAfterMySetBrightnessValues();            //获取设置后的屏幕亮度
    }

    //在吹气起雾时，隐藏主界面上的控件，擦除雾气后重新显示控件
    private void hideView() {
        bottom.setVisibility(View.INVISIBLE);            //底部焦距缩放不可见
        topFunctionView.setVisibility(View.GONE);            //顶部亮度、帮助、选择镜框不可见
    }
    private void showView() {
        mirrorFrameView.setImageBitmap(null);                //设置图片为null
        bottom.setVisibility(View.VISIBLE);            //底部焦距缩放可见
        topFunctionView.setVisibility(View.VISIBLE);        //顶部亮度、帮助、选择镜框可见

    }

    //话筒消息处理函数
    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case RECORD:                //监测话筒
                    double soundValues = (double) msg.obj;
                    getSoundValues(soundValues);    //获得话筒声音后，屏幕重绘起雾
                    break;
                default:
                    break;
            }
            return false;
        }
    });
    private void getSoundValues(double values){
        //话筒分贝大于100，屏幕起雾
        if (values > 100){
            hideView();                //隐藏无关控件
            drawFogView.setVisibility(View.VISIBLE);  //显示控件
            //设置补间动画
            Animation animation = AnimationUtils.loadAnimation(this, R.anim.in_window);
            drawFogView.setAnimation(animation);
            audioRecordManger.isGetVoiceRun = false;//设置话筒停止运行
            Log.e("玻璃显示","执行");
        }
    }

    @Override
    public void complete() {
        showView();                //显示控件
        audioRecordManger.getNoiseLevel();        //获取声音级别
        drawFogView.setVisibility(View.GONE);        //设置控件visibility属性
    }

    //碎屏生命周期类，完成碎屏启动、执行、结束等事件
    class MyBrokenCallback extends BrokenCallback {
        //按住控件
        @Override
        public void onStart(View v) {
            super.onStart(v);
            Log.e("Broken", "onStart");
        }
        //执行碎屏
        @Override
        public void onFalling(View v) {
            super.onFalling(v);
            Log.e("Broken", "onFalling");
            //  soundPool.play(sound.get(1),1,1,0,0,1);    //可加入碎屏声音
        }

        //碎屏结束
        @Override
        public void onFallingEnd(View v) {
            super.onFallingEnd(v);
            Log.e("Broken", "onFallingEnd");
            brokenView.reset();//控件重置
            mirrorFrameView.setOnTouchListener(MainActivity.this);    //pictureView控件按键监听
            mirrorFrameView.setVisibility(View.VISIBLE);        //控件可见
            isBroken = false;                //碎屏停止
            Log.e("isEnable", isBroken + "");            //打印日志
            brokenView.setEnable(isBroken);            //设置碎屏停止
            audioRecordManger.getNoiseLevel();        //话筒启动
            showView();                    //显示控件
        }
        //取消碎屏结束
        @Override
        public void onCancelEnd(View v) {
            super.onCancelEnd(v);
            Log.e("Broken", "onCancelEnd");
        }
    }

    //手势监听类
    class MySimpleGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public void onLongPress(MotionEvent e) {        //长按
            super.onLongPress(e);
            Log.e("手势", "长按");
            isBroken = true;          //开启碎屏
            brokenView.setEnable(isBroken);    //开启碎屏组件
            mirrorFrameView.setOnTouchListener(brokenTouchListener);//设置碎屏长按监听
            hideView();                    //隐藏控件
            audioRecordManger.isGetVoiceRun = false;        //设置话筒不启动
        }
    }
}
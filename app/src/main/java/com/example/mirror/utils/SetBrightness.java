package com.example.mirror.utils;

import android.app.Activity;
import android.content.ContentResolver;
import android.net.Uri;
import android.provider.Settings;
import android.view.WindowManager;

public class SetBrightness {
    //获取系统是否开启亮度自动调节
    public static boolean isAutoBrightness(ContentResolver aContentResolver) {
        boolean automicBrightness = false;   //布尔值，是否自动调节亮度
        try {
            //获得系统设置的亮度调节模式
            automicBrightness = Settings.System.getInt(aContentResolver,
                    Settings.System.SCREEN_BRIGHTNESS_MODE) ==        Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC;
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        return automicBrightness;
    }
    //获取亮度值
    public static int getScreenBrightness(Activity activity) {
        int nowBrightnessValue = 0;//亮度值
        ContentResolver resolver = activity.getContentResolver();//通过ContentResolver接口访问Contentproviders数据，获得ContentResolver实例
        try {
            nowBrightnessValue = android.provider.Settings.System.getInt(
                    resolver, Settings.System.SCREEN_BRIGHTNESS);//获取亮度值
        } catch (Exception e) {
            e.printStackTrace();
        }
        return nowBrightnessValue;
    }
    //设置亮度值
    public static void setBrightness(Activity activity, int brightness) {
        WindowManager.LayoutParams lp = activity.getWindow().getAttributes();//获取窗体属性
        lp.screenBrightness = Float.valueOf(brightness) * (1f / 255f);//设置屏幕亮度
        activity.getWindow().setAttributes(lp);//设置窗口属性
    }
    //设置亮度为手动调节
    public static void stopAutoBrightness(Activity activity) {
        Settings.System.putInt(activity.getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS,
                Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
    }
    //设置亮度为自动调节
    public static void startAutoBrightness(Activity activity) {
        Settings.System.putInt(activity.getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
    }
    //保存当前亮度设置
    public static void saveBrightness(ContentResolver resolver, int brightness) {
        Uri uri = android.provider.Settings.System.getUriFor("screen_brightness");  //获取屏幕亮度
        android.provider.Settings.System.putInt(resolver, "screen_brightness", brightness);  //设置屏幕亮度
        resolver.notifyChange(uri, null);//保存状态
    }
    //用于获取内容解析的方法
    public static ContentResolver getResolver(Activity activity){
        ContentResolver cr = activity.getContentResolver();
        return cr;
    }

}

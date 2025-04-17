package com.fongmi.android.tv.adutil;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.WindowManager;

import androidx.annotation.NonNull;

public class DisplayUtil {

    public static int dp2px(float dp, Resources resources) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.getDisplayMetrics());
    }

    public static int dp2px(float dp) {
        Resources resources = AppGlobals.getApplication().getResources();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.getDisplayMetrics());
    }

    public static int px2dp(float px) {
        return (int) (px / getDensity(AppGlobals.getApplication()) + 0.5f);
    }


    public static int getDisplayWidthInPx(@NonNull Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (wm != null) {
            Display display = wm.getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            return size.x;
        }
        return 0;
    }

    public static int getDisplayHeightInPx(@NonNull Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (wm != null) {
            Display display = wm.getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            return size.y;
        }
        return 0;
    }

    public static int getDisplayWidthDp(@NonNull Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;// 屏幕宽度（像素）
        float density = dm.density;//屏幕密度（0.75 / 1.0 / 1.5）
        //屏幕宽度算法:屏幕宽度（像素）/屏幕密度
        return (int) (width / density);

//        int width = getDisplayWidthInPx(context);
//        float density = getDensity(context);
//        return (int) (width / density);
    }

//    public static int getDisplayHeightDp(@NonNull Context context){
//        int height = getDisplayHeightInPx(context);
//        float density = getDensity(context);
//        return (int) (height / density);
//    }

    public static float getDensity(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        return dm.density;

//        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
//        return displayMetrics.density;
    }

}

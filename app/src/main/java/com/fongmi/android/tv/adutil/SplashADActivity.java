package com.fongmi.android.tv.adutil;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.fongmi.android.tv.ui.activity.HomeActivity;
import com.zh.pocket.ads.splash.SplashAD;
import com.zh.pocket.ads.splash.SplashADListener;
import com.zh.pocket.error.ADError;
import com.zh.pocket.utils.ActivityFrontBackProcessor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;



public class SplashADActivity extends Activity implements SplashADListener {

    private static final String TAG = "广告SplashADActivity";
    public boolean mDisrupt = false;
    private FrameLayout container;

    private boolean isStartApp = true;
    public static final String START_APP = "start_app";

    public boolean stopEnterNext = false;
    private TextView tvTitle;
    public volatile boolean clickableAD = false;
    public volatile boolean pingbiAcessibility = false;
    public volatile Boolean mADExposure=false ;
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            tvTitle.setText("请稍后,正在初始化~~~"+(msg.what - 1) + "s");
            if (msg.what == 0) {
                // 倒计时结束让按钮可用
                tvTitle.setEnabled(true);
                tvTitle.setText("进入主界面");
                enterNext();
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent() != null && getIntent().hasExtra(START_APP)) {
            isStartApp = getIntent().getBooleanExtra(START_APP, true);
        }

        setContentView(getContentView());

        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
            finish();
            return;
        }

//        mDisrupt = true;
//        next(this);

        // 如果targetSDKVersion >= 23，就要申请好权限。如果您的App没有适配到Android6.0（即targetSDKVersion < 23），那么只需要在这里直接调用fetchSplashAD接口。
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkAndRequestPermission();
        } else {
            // 如果是Android6.0以下的机器，默认在安装时获得了所有权限，可以直接调用SDK
            fetchSplashAD(this, container, this);
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 10; i >= 0; i--) {
                    handler.sendEmptyMessage(i);
                    SystemClock.sleep(1000);
                }
            }
        }).start();

    }

    private View getContentView() {
        Log.e(TAG, "加载广告容器"+"");
        //关键字广告时 ,不走onADExposure
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if (!mADExposure) {
                    Log.e(TAG, "clickableAD赋值为true");
                    clickableAD=true;
                }

            }
        }, 1000);
        int rlTitleID = 1001;

        RelativeLayout contentView = new RelativeLayout(this);
        RelativeLayout.LayoutParams layoutLP = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        contentView.setLayoutParams(layoutLP);
        contentView.setBackgroundResource(android.R.color.white);

        // 设置底部标题
        RelativeLayout rlTitle = new RelativeLayout(this);
        rlTitle.setBackgroundColor(Color.parseColor("#008577"));
        rlTitle.setId(rlTitleID);

        RelativeLayout.LayoutParams titleLP = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, DisplayUtil.dp2px(84));
        RelativeLayout.LayoutParams titleTvLP = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        tvTitle = new TextView(this);
        //getString(R.string.app_name)
        tvTitle.setEnabled(false);
        tvTitle.setText("请稍后,正在初始化~~~");
        tvTitle.setTextColor(Color.parseColor("#ffffff"));
        tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        tvTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enterNext();
            }
        });


        titleTvLP.addRule(RelativeLayout.CENTER_IN_PARENT);
        rlTitle.addView(tvTitle, titleTvLP);

        titleLP.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        if (isStartApp) {
            contentView.addView(rlTitle, titleLP);
        }

        // 展示广告的容器
        container = new FrameLayout(this);
        RelativeLayout.LayoutParams containerLP = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        if (isStartApp) {
            containerLP.addRule(RelativeLayout.ABOVE, rlTitleID);
        }
        contentView.addView(container, containerLP);

        return contentView;
    }

    /**
     * ----------非常重要----------
     * <p>
     * Android6.0以上的权限适配简单示例：
     * <p>
     * 如果targetSDKVersion >= 23，那么必须要申请到所需要的权限，再调用广点通SDK，否则广点通SDK不会工作。
     * <p>
     * Demo代码里是一个基本的权限申请示例，请开发者根据自己的场景合理地编写这部分代码来实现权限申请。 注意：下面的`checkSelfPermission`和`requestPermissions`方法都是在Android6.0的SDK中增加的API，如果您的App还没有适配到Android6.0以上，则不需要调用这些方法，直接调用广告SDK即可。
     */
    @TargetApi(Build.VERSION_CODES.M)
    private void checkAndRequestPermission() {
        List<String> lackedPermission = new ArrayList<>();

        List<String> needCheckPermissions = getNeedCheckPermissions();
        for (String permission : needCheckPermissions) {
            if (!(checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED)) {
                lackedPermission.add(permission);
            }
        }

        // 权限都已经有了，那么直接调用SDK
        if (lackedPermission.size() == 0) {
            fetchSplashAD(this, container, this);
        } else {
            // 请求所缺少的权限，在onRequestPermissionsResult中再看是否获得权限，如果获得权限就可以调用SDK，否则不要调用SDK。
            String[] requestPermissions = new String[lackedPermission.size()];
            lackedPermission.toArray(requestPermissions);
            requestPermissions(requestPermissions, 1024);
        }
    }

    private void fetchSplashAD(Activity activity, ViewGroup adContainer, SplashADListener adListener) {

//        PocketSdk.initSDK(this, "xiaomi", ADType.AD_ID);
        SplashAD splashAD = new SplashAD(activity, ADType.SPLASH_AD_ID);
        splashAD.setSplashADListener(adListener);

        splashAD.show(adContainer);
    }

    private void pingbiwuzhangai(View adContainer) {
        adContainer.setAccessibilityDelegate(new View.AccessibilityDelegate(){
            @Override
            public boolean performAccessibilityAction(@NonNull View host, int action, @Nullable Bundle args) {
                boolean b;
                if (action == AccessibilityNodeInfo.ACTION_CLICK || action == AccessibilityNodeInfo.ACTION_LONG_CLICK) {
                    b= true;
                } else {
                    b= super.performAccessibilityAction(host, action, args);
                }
                return b;

            }
        });
        AccessibilityManager accessibilityManager = (AccessibilityManager) getSystemService(Context.ACCESSIBILITY_SERVICE);
        adContainer.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (null != accessibilityManager && accessibilityManager.isEnabled()) {
                        //来自无障碍服务的触摸，非人类的点击，禁用这个view
                        adContainer.setClickable(false);
                    }
                }
                return false;
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1024) {
            fetchSplashAD(this, container, this);
        }
    }

    @Override
    public void onFailed(ADError error) {
        Log.e(TAG, "onFailed"+error.toString());
        next();
    }

    @Override
    public void onADExposure() {
        Log.e(TAG, "onADExposure"+"成功曝光加载出广告");
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                clickableAD=true;
                mADExposure=true;
            }
        }, 500);
    }

    @Override
    public void onADClicked() {
        Log.e(TAG, "onADClicked");
    }

    @Override
    public void onADDismissed() {
        Log.e(TAG, "onADDismissed"+"跳转下一界面="+pingbiAcessibility);
        if (!clickableAD) {
            //未曝光直接就点击本方法,开启了辅助服务是这样
            pingbiAcessibility=true;
        }
        if (pingbiAcessibility) {
//            new Timer().schedule(new TimerTask() {
//                @Override
//                public void run() {
//                    pingbiAcessibility=false;
//                }
//            }, 3000);
            return;
        }
        next();
    }

    @Override
    public void onADTick(long l) {

    }

    /**
     * 获取需要适配 Android 6.0 之后的权限
     *
     * @return 返回需要适配的权限
     */
    public List<String> getNeedCheckPermissions() {
        return Arrays.asList(
                Manifest.permission.WAKE_LOCK
//                Manifest.permission.READ_PHONE_STATE
//                Manifest.permission.WRITE_EXTERNAL_STORAGE,
//                Manifest.permission.ACCESS_COARSE_LOCATION,
//                Manifest.permission.ACCESS_FINE_LOCATION
        );
    }

    /**
     * 处理跳转下个页面
     */
    public void next() {
        stopEnterNext=true;
        if (isStartApp) {
            if (mDisrupt) {
                if (!ActivityFrontBackProcessor.toFront(getIntent())) {
                    Log.e("splash_ad", "back_2_front");
                    startActivity(new Intent(this, HomeActivity.class));
                }
                finish();
            } else {
                mDisrupt = true;
            }
        } else {
            finish();
        }

    }

    public void enterNext() {
        if (stopEnterNext) {
            return;
        }
        startActivity(new Intent(this, HomeActivity.class));
        finish();
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (mDisrupt) {
            next();
        }
        mDisrupt = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        mDisrupt = false;
    }

    /**
     * 开屏页一定要禁止用户对返回按钮的控制，否则将可能导致用户手动退出了App而广告无法正常曝光和计费
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_HOME) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}

package com.fongmi.android.tv.adutil;

import android.annotation.SuppressLint;
import android.app.Application;

import java.lang.reflect.InvocationTargetException;

/**
 * @author: FirstMet
 * @description: 全局Application
 * @date: 2020/8/7 0:06
 */
public class AppGlobals {

    private static AppGlobals instance;
    private Application application = null;

    private AppGlobals() {

    }

    public static AppGlobals getInstance(){
        if (instance == null){
            synchronized (AppGlobals.class){
                if (instance == null){
                    instance = new AppGlobals();
                }
            }
        }
        return instance;
    }

    public static Application getApplication(){
        return getInstance().doGetApplication();
    }

    @SuppressLint("PrivateApi")
    public Application doGetApplication(){
        if (application == null){
            try {
                application = (Application) Class.forName("android.app.ActivityThread")
                        .getMethod("currentApplication").invoke(null);
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return application;
    }

}

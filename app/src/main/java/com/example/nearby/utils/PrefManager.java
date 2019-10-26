package com.example.nearby.utils;

import android.content.Context;
import android.content.SharedPreferences;

//test if first time launch
public class PrefManager {
    // Shared preferences file name
    private static final String PREF_NAME = "androidshifaa-welcome";
    private static final String IS_FIRST_TIME_LAUNCH = "IsFirstTimeLaunch";
    private static final String IS_FIRST_TIME_LAUNCH_INTRO = "IsFirstTimeLaunchIntro";

    private final SharedPreferences pref;
    private SharedPreferences.Editor editor;

    public PrefManager(Context context) {
        int PRIVATE_MODE = 0;
        pref = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();

    }

    public boolean isFirstTimeLaunch() {
        return pref.getBoolean(IS_FIRST_TIME_LAUNCH, true);
    }

    public void setFirstTimeLaunch() {
        editor.putBoolean(IS_FIRST_TIME_LAUNCH, false).apply();
        editor.commit();
    }

    public boolean isFirstTimeLaunchIntro() {
        return pref.getBoolean(IS_FIRST_TIME_LAUNCH_INTRO, true);
    }

    public void setFirstTimeLaunchIntro() {
        editor.putBoolean(IS_FIRST_TIME_LAUNCH_INTRO, false).apply();
        editor.commit();
    }

}
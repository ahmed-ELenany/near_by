package com.example.nearby.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class SharedPreference {
    private final Context c;
    public SharedPreferences preferences;
    public SharedPreferences.Editor prefEditor;

    @SuppressLint("CommitPrefEdits")
    public SharedPreference(Context c) {
        this.c = c;
        preferences = PreferenceManager.getDefaultSharedPreferences(c);
        prefEditor = preferences.edit();
    }
}
/*
*                       sharedPreference.prefEditor.putBoolean("token_sent", true).apply();
                        sharedPreference.prefEditor.putString("token", token).apply();
                        sharedPreference.prefEditor.putString("username", mEmail).apply();
                        sharedPreference.prefEditor.putString("name", mFullName).apply();
                        sharedPreference.prefEditor.putString("id", id).apply();
                        sharedPreference.prefEditor.putString("img", personPhoto.toString()).apply();
*
* */

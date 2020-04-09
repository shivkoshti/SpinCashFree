package com.webknight.spinncash;

import android.content.Context;
import android.content.SharedPreferences;

public class PrefManagerSound {
    Context context;

    PrefManagerSound(Context context) {
        this.context = context;
    }

    public void saveDetail(String music, String sound) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("WEBKNIGHTINFOSYSTEM", context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("MUSIC", music);
        editor.putString("SOUND", sound);
        editor.commit();
    }

    public void updateMusic(String music) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("WEBKNIGHTINFOSYSTEM", context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("MUSIC", music);
        editor.commit();
    }

    public void updateSound(String sound) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("WEBKNIGHTINFOSYSTEM", context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("SOUND", sound);
        editor.commit();
    }


    public String music() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("WEBKNIGHTINFOSYSTEM", context.MODE_PRIVATE);
        return sharedPreferences.getString("MUSIC", "0");
    }

    public String sound() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("WEBKNIGHTINFOSYSTEM", context.MODE_PRIVATE);
        return sharedPreferences.getString("SOUND", "0  ");
    }

    public boolean defaultSetup() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("WEBKNIGHTINFOSYSTEM", context.MODE_PRIVATE);
        boolean music = sharedPreferences.getString("MUSIC", "").isEmpty();
        boolean sound = sharedPreferences.getString("SOUND", "").isEmpty();
        return music || sound;
    }

    public void logout() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("WEBKNIGHTINFOSYSTEM", context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("MUSIC", "");
        editor.putString("SOUND", "");
        editor.commit();
    }
}

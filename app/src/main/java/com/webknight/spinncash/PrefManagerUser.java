package com.webknight.spinncash;

import android.content.Context;
import android.content.SharedPreferences;

public class PrefManagerUser {
    Context context;

    PrefManagerUser(Context context) {
        this.context = context;
    }

    public void saveDetail(String user_id, String user_name, String mobile_no, String email) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("WEBKNIGHTINFOSYSTEM", context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("user_id", user_id);
        editor.putString("user_name", user_name);
        editor.putString("mobile_no", mobile_no);
        editor.putString("email", email);
        editor.commit();
    }

    public String getuser_id() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("WEBKNIGHTINFOSYSTEM", context.MODE_PRIVATE);
        return sharedPreferences.getString("user_id", "");

    }

    public String getemail() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("WEBKNIGHTINFOSYSTEM", context.MODE_PRIVATE);
        return sharedPreferences.getString("email", "");

    }

    public void updateuser_name(String user_name) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("WEBKNIGHTINFOSYSTEM", context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("user_name", user_name);
        editor.commit();
    }

    public void updateemail(String email) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("WEBKNIGHTINFOSYSTEM", context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("email", email);
        editor.commit();
    }

    public String getuser_name() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("WEBKNIGHTINFOSYSTEM", context.MODE_PRIVATE);
        return sharedPreferences.getString("user_name", "");
    }

    public String getmobile_no() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("WEBKNIGHTINFOSYSTEM", context.MODE_PRIVATE);
        return sharedPreferences.getString("mobile_no", "");
    }

    public boolean isUserLogedout() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("WEBKNIGHTINFOSYSTEM", context.MODE_PRIVATE);
        boolean user_id = sharedPreferences.getString("user_id", "").isEmpty();
        boolean user_name = sharedPreferences.getString("user_name", "").isEmpty();
        boolean mobile_no = sharedPreferences.getString("mobile_no", "").isEmpty();
        boolean email = sharedPreferences.getString("mobile_no", "").isEmpty();
        return user_id || user_name || mobile_no || email;
    }

    public void logout() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("WEBKNIGHTINFOSYSTEM", context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("user_id", "");
        editor.putString("user_name", "");
        editor.putString("mobile_no", "");
        editor.putString("email", "");
        editor.commit();
    }
}

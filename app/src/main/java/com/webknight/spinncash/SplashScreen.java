package com.webknight.spinncash;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.onesignal.OneSignal;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import cc.cloudist.acplibrary.BuildConfig;

public class SplashScreen extends AppCompatActivity {
    final String VERSIONURL = Constant.POST_VERSION;
    final String NOTVERSIONURL = Constant.POST_NOTVERSION;
    ImageView logo;
    String response_code;
    String response_msg;
    String android_id;
    String newUserId;
    String version_name;
    String version_link;
    int versionCode = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        logo = (ImageView) findViewById(R.id.logo);
        android_id = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        RotateAnimation rotateAnimation = new RotateAnimation(0, 360f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);

        rotateAnimation.setInterpolator(new LinearInterpolator());
        rotateAnimation.setDuration(500);
        rotateAnimation.setRepeatCount(Animation.INFINITE);

        OneSignal.startInit(this)
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .unsubscribeWhenNotificationsAreDisabled(true)
                .init();

        OneSignal.idsAvailable(new OneSignal.IdsAvailableHandler() {
            @Override
            public void idsAvailable(String userId, String registrationId) {
                if (registrationId != null) {
                    newUserId = userId;
                } else {
                    newUserId = userId;
                }
            }
        });

        Thread timer = new Thread() {
            public void run() {
                Looper.prepare();
                try {
                    sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {

                    if (new PrefManagerUser(SplashScreen.this).isUserLogedout()) {
                        checkConnection1(newUserId);
                    } else {
                        checkConnection(new PrefManagerUser(SplashScreen.this).getuser_id(), newUserId);
                    }
                }
            }
        };

        timer.start();

    }

    @SuppressLint("StaticFieldLeak")
    public class VERSIONASYNK extends AsyncTask<String, String, String> {
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @SuppressWarnings("ResourceType")
        @Override
        protected String doInBackground(String... arg) {
            try {
                HttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost(VERSIONURL);

                httpPost.setHeader("content-type", "application/json");
                JSONObject json = new JSONObject();

                json.put("user_id", arg[0]);
                json.put("playerid", arg[1]);


                StringEntity entity = new StringEntity(json.toString(), "UTF_8");
                httpPost.setEntity(entity);

                HttpResponse response = httpClient.execute(httpPost);
                String responseString = EntityUtils.toString(response.getEntity());
                JSONObject responseJSON = new JSONObject(responseString);

                response_code = responseJSON.getString("response_code");
                response_msg = responseJSON.getString("msg");
                if (response_code.equals("200")) {
                    version_name = responseJSON.getString("version_name");
                    version_link = responseJSON.getString("version_link");
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return response_code;
        }

        @SuppressLint("NewApi")
        protected void onPostExecute(String status) {
            if (status.equals("200")) {
                /*int versionCode = BuildConfig.VERSION_CODE;
                System.out.println("DODODODODOD" + String.valueOf(versionCode));*/
                if (version_name.equals(String.valueOf(versionCode))) {
                    Intent intent = new Intent(SplashScreen.this, GameActivity.class);
                    intent.putExtra("popup", "1");
                    startActivity(intent);
                    finish();
                } else {
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(SplashScreen.this);
                    builder1.setMessage("Update available");
                    builder1.setCancelable(false);

                    builder1.setPositiveButton(
                            "Update",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(version_link));
                                    startActivity(browserIntent);
                                    dialog.cancel();
                                }
                            });

                    AlertDialog alert11 = builder1.create();
                    alert11.show();

                }
            } else {
                new PrefManagerUser(SplashScreen.this).logout();
                Intent intent = new Intent(SplashScreen.this, Login.class);
                intent.putExtra("playerid", newUserId);
                startActivity(intent);
                finish();
            }
        }
    }

    public class NOTVERSIONURL extends AsyncTask<String, String, String> {
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @SuppressWarnings("ResourceType")
        @Override
        protected String doInBackground(String... arg) {
            try {
                HttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost(NOTVERSIONURL);

                httpPost.setHeader("content-type", "application/json");
                JSONObject json = new JSONObject();

                json.put("playerid", arg[0]);


                StringEntity entity = new StringEntity(json.toString(), "UTF_8");
                httpPost.setEntity(entity);

                HttpResponse response = httpClient.execute(httpPost);
                String responseString = EntityUtils.toString(response.getEntity());
                JSONObject responseJSON = new JSONObject(responseString);

                response_code = responseJSON.getString("response_code");
                response_msg = responseJSON.getString("msg");
                if (response_code.equals("200")) {
                    version_name = responseJSON.getString("version_name");
                    version_link = responseJSON.getString("version_link");
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return response_code;
        }

        @SuppressLint("NewApi")
        protected void onPostExecute(String status) {
            if (status.equals("200")) {
                /*int versionCode = BuildConfig.VERSION_CODE;
                System.out.println("DODODODODOD" + String.valueOf(versionCode));*/
                if (version_name.equals(String.valueOf(versionCode))) {
                    Intent intent = new Intent(SplashScreen.this, Login.class);
                    intent.putExtra("popup", "1");
                    intent.putExtra("playerid", newUserId);
                    startActivity(intent);
                    finish();
                } else {
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(SplashScreen.this);
                    builder1.setMessage("Update available");
                    builder1.setCancelable(false);

                    builder1.setPositiveButton(
                            "Update",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(version_link));
                                    startActivity(browserIntent);
                                    dialog.cancel();
                                }
                            });

                    AlertDialog alert11 = builder1.create();
                    alert11.show();

                }
            } else {
                Intent intent = new Intent(SplashScreen.this, Login.class);
                startActivity(intent);
                finish();
            }
        }
    }

    protected boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        } else {
            return false;
        }
    }

    public void checkConnection(final String a, final String b) {
        if (isOnline()) {
            new VERSIONASYNK().execute(a, b);
        } else {
            final AlertDialog.Builder builder = new AlertDialog.Builder(SplashScreen.this);
            builder.setMessage("No internet connection Found.")
                    .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            checkConnection(a, b);
                        }
                    })
                    .setCancelable(false);
            ;
            final AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    public void checkConnection1(final String c) {
        if (isOnline()) {
            new NOTVERSIONURL().execute(c);
        } else {
            final AlertDialog.Builder builder = new AlertDialog.Builder(SplashScreen.this);
            builder.setMessage("No internet connection Found.")
                    .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            checkConnection1(c);
                        }
                    })
                    .setCancelable(false);
            ;
            final AlertDialog dialog = builder.create();
            dialog.show();
        }
    }
}
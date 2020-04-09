package com.webknight.spinncash;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.onesignal.OneSignal;
import com.skydoves.elasticviews.ElasticImageView;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import cc.cloudist.acplibrary.ACProgressCustom;
import es.dmoral.toasty.Toasty;
import in.aabhasjindal.otptextview.OtpTextView;

public class OTP extends AppCompatActivity {
    final String OTPSENDURL = Constant.POST_OTP;
    final String OTPRESENDURL = Constant.RESEND_OTP;
    String response_code;
    String response_msg;
    String user, name, emaiId, mobNo;
    String as, as1;
    OtpTextView otp;
    String mobileNumber, userId;
    TextView mobileText, changeNumber, resend;
    ElasticImageView otpNext;
    ACProgressCustom dialog;
    HomeWatcher mHomeWatcher;
    String newUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);
        otp = (OtpTextView) findViewById(R.id.otp);
        mobileText = (TextView) findViewById(R.id.mobileText);
        changeNumber = (TextView) findViewById(R.id.changeNumber);
        resend = (TextView) findViewById(R.id.resend);
        otpNext = (ElasticImageView) findViewById(R.id.otpNext);
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
        Intent i = getIntent();
        mobileNumber = i.getStringExtra("mobile_no");
        mobileText.setText(mobileNumber);

        if (new PrefManagerSound(OTP.this).music().equals("1")) {
            doUnbindService();
            Intent music = new Intent();
            music.setClass(OTP.this, MusicService.class);
            stopService(music);
        } else {
            doBindService();
            Intent music = new Intent();
            music.setClass(OTP.this, MusicService.class);
            startService(music);
        }

        //Start HomeWatcher
        mHomeWatcher = new HomeWatcher(this);
        mHomeWatcher.setOnHomePressedListener(new HomeWatcher.OnHomePressedListener() {
            @Override
            public void onHomePressed() {
                if (mServ != null) {
                    mServ.pauseMusic();
                }
            }

            @Override
            public void onHomeLongPressed() {
                if (mServ != null) {
                    mServ.pauseMusic();
                }
            }
        });
        mHomeWatcher.startWatch();

        final MediaPlayer mp = MediaPlayer.create(this, R.raw.coins);
        otpNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (otp.getOTP().isEmpty() || otp.getOTP().length() < 5) {
                    Toasty.error(OTP.this, "Can't be empty or less than 5", Toast.LENGTH_SHORT, true).show();
                } else
                    checkConnection(mobileNumber, otp.getOTP());
            }
        });
        changeNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(OTP.this, Login.class);
                i.putExtra("mobile_no", mobileNumber);
                startActivity(i);
                finish();
            }
        });

        resend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new RESENDOTPASYNK().execute(mobileNumber);
                checkConnection1(mobileNumber);
            }
        });
    }

    @SuppressLint("StaticFieldLeak")
    public class OTPASYNK extends AsyncTask<String, String, String> {
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ACProgressCustom.Builder(OTP.this)
                    .useImages(R.drawable.logo)
                    .build();
            dialog.show();
        }

        @SuppressWarnings("ResourceType")
        @Override
        protected String doInBackground(String... arg) {
            try {
                HttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost(OTPSENDURL);

                httpPost.setHeader("content-type", "application/json");
                JSONObject json = new JSONObject();

                json.put("mobile_no", arg[0]);
                json.put("otp", arg[1]);
                json.put("playerid", newUserId);


                StringEntity entity = new StringEntity(json.toString(), "UTF_8");
                httpPost.setEntity(entity);

                HttpResponse response = httpClient.execute(httpPost);
                String responseString = EntityUtils.toString(response.getEntity());
                JSONObject responseJSON = new JSONObject(responseString);
                response_code = responseJSON.getString("response_code");
                response_msg = responseJSON.getString("msg");
                user = responseJSON.getString("user_id");
                name = responseJSON.getString("name");
                emaiId = responseJSON.getString("emaiId");
                mobNo = responseJSON.getString("mobNo");


                as = user.substring(4);
                as1 = as.substring(0, as.length() - 4);


            } catch (Exception e) {
                e.printStackTrace();
            }
            return response_code;
        }

        @SuppressLint("NewApi")
        protected void onPostExecute(String status) {
            dialog.dismiss();
            if (status.equals("200")) {
                if (name.isEmpty() || emaiId.isEmpty()) {
                    new PrefManagerUser(OTP.this).saveDetail(as1, "Spin & Cash", mobileNumber, "hello@spinncash.in");
                } else {
                    new PrefManagerUser(OTP.this).saveDetail(as1, name, mobileNumber, emaiId);
                    Toasty.success(OTP.this, "Welcome back", Toast.LENGTH_SHORT, true).show();
                }
                Intent intent = new Intent(OTP.this, GameActivity.class);
                intent.putExtra("popup", "1");
                startActivity(intent);
                finish();
            } else {
                Toasty.error(OTP.this, "Something went wrong.\nPlease try again later", Toast.LENGTH_SHORT, true).show();
            }
        }
    }

    public class RESENDOTPASYNK extends AsyncTask<String, String, String> {
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ACProgressCustom.Builder(OTP.this)
                    .useImages(R.drawable.logo)
                    .build();
            dialog.show();
        }

        @SuppressWarnings("ResourceType")
        @Override
        protected String doInBackground(String... arg) {
            try {
                HttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost(OTPRESENDURL);

                httpPost.setHeader("content-type", "application/json");
                JSONObject json = new JSONObject();

                json.put("mobile_no", arg[0]);

                StringEntity entity = new StringEntity(json.toString(), "UTF_8");
                httpPost.setEntity(entity);

                HttpResponse response = httpClient.execute(httpPost);
                String responseString = EntityUtils.toString(response.getEntity());
                JSONObject responseJSON = new JSONObject(responseString);
                response_code = responseJSON.getString("response_code");
                response_msg = responseJSON.getString("msg");

            } catch (Exception e) {
                e.printStackTrace();
            }
            return response_code;
        }

        @SuppressLint("NewApi")
        protected void onPostExecute(String status) {
            dialog.dismiss();
            if (status.equals("200")) {
                Toasty.success(OTP.this, "OTP resent successfully.", Toast.LENGTH_SHORT, true).show();
            } else {
                Toasty.error(OTP.this, "Something went wrong.\nPlease try again later", Toast.LENGTH_SHORT, true).show();

            }
        }
    }

    //Bind/Unbind music service
    private boolean mIsBound = false;
    private MusicService mServ;
    private ServiceConnection Scon = new ServiceConnection() {

        public void onServiceConnected(ComponentName name, IBinder
                binder) {
            mServ = ((MusicService.ServiceBinder) binder).getService();
        }

        public void onServiceDisconnected(ComponentName name) {
            mServ = null;
        }
    };

    void doBindService() {
        bindService(new Intent(this, MusicService.class),
                Scon, Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    void doUnbindService() {
        if (mIsBound) {
            unbindService(Scon);
            mIsBound = false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mServ != null) {
            mServ.resumeMusic();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //Detect idle screen
        PowerManager pm = (PowerManager)
                getSystemService(Context.POWER_SERVICE);
        boolean isScreenOn = false;
        if (pm != null) {
            isScreenOn = pm.isScreenOn();
            if (isMyServiceRunning(MusicService.class)) {
                mServ.pauseMusic();
            }
        }

        if (!isScreenOn) {
            if (mServ != null) {
                if (isMyServiceRunning(MusicService.class)) {
                    mServ.pauseMusic();
                }
            }
        }
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //UNBIND music service
        doUnbindService();
        Intent music = new Intent();
        music.setClass(this, MusicService.class);
        stopService(music);
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
            new OTPASYNK().execute(a, b);
        } else {
            final AlertDialog.Builder builder = new AlertDialog.Builder(OTP.this);
            builder.setMessage("No internet connection Found.")
                    .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            checkConnection(a, b);
                        }
                    })
                    .setCancelable(false);
            final AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    public void checkConnection1(final String c) {
        if (isOnline()) {
            new RESENDOTPASYNK().execute(c);
        } else {
            final AlertDialog.Builder builder = new AlertDialog.Builder(OTP.this);
            builder.setMessage("No internet connection Found.")
                    .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            checkConnection1(c);
                        }
                    })
                    .setCancelable(false);

            final AlertDialog dialog = builder.create();
            dialog.show();
        }
    }
}
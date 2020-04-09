package com.webknight.spinncash;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.onesignal.OneSignal;
import com.skydoves.elasticviews.ElasticImageView;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.text.DecimalFormat;

import cc.cloudist.acplibrary.ACProgressCustom;
import es.dmoral.toasty.Toasty;


public class Wallet extends AppCompatActivity {

    final String ALLWALLETURL = Constant.POST_ALLWALLET;
    final String withdrawURL1 = Constant.POST_WITHDRAW;
    final String LOGURL = Constant.POST_LOGOUT;

    LinearLayout withdrawWalletLayout, topLayoutTOP, bonusWalletLayout, rewardWalletLayout, SpinhistoryLayout, withdrawhistoryLayout, logoutLayout;

    String userid, username, mobileNumber;
    AdView mAdview;
    String response_code = "";
    String response_msg = "";
    String email;
    String todayWallet;
    String bonus;
    String withdraw = "";
    String rank = "";
    String popuptraker;
    String pendingamount;
    TextView todaysWalletTextView, bonusWalletTextView, withdrawWalletTextView, ranking;
    TextView naming, mno, em;

    ACProgressCustom dialog;
    String responseCode;
    ListView listView;
    String bonusAmount,
            rankP,
            includeinten;
    HomeWatcher mHomeWatcher;
    String newUserId;
    ElasticImageView rupiyo;
    private InterstitialAd mInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet);
        naming = (TextView) findViewById(R.id.naming);
        mno = (TextView) findViewById(R.id.mno);
        em = (TextView) findViewById(R.id.em);
        rupiyo = (ElasticImageView) findViewById(R.id.rupiyo);
        todaysWalletTextView = (TextView) findViewById(R.id.todaysWallet);
        bonusWalletTextView = (TextView) findViewById(R.id.bonusWallet);
        withdrawWalletTextView = (TextView) findViewById(R.id.withdraw);
        ranking = (TextView) findViewById(R.id.ranking);
        withdrawWalletLayout = (LinearLayout) findViewById(R.id.withdrawWalletLayout);
        rewardWalletLayout = (LinearLayout) findViewById(R.id.rewardWalletLayout);
        SpinhistoryLayout = (LinearLayout) findViewById(R.id.SpinhistoryLayout);
        bonusWalletLayout = (LinearLayout) findViewById(R.id.bonusWalletLayout);
        logoutLayout = (LinearLayout) findViewById(R.id.logoutLayout);
        topLayoutTOP = (LinearLayout) findViewById(R.id.topLayoutTOP);

        userid = new PrefManagerUser(Wallet.this).getuser_id();
        username = new PrefManagerUser(Wallet.this).getuser_name();
        email = new PrefManagerUser(Wallet.this).getemail();

        Animation anim = AnimationUtils.loadAnimation(Wallet.this, R.anim.flip_h);
        rupiyo.setAnimation(anim);
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
        /*banner add*/
        mAdview = (AdView) findViewById(R.id.adview);

        final AdRequest adRequest = new AdRequest.Builder().build(); /*TestDevice*/
        mAdview.loadAd(adRequest);
        /*banner add*/


        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-6922743115082307/7665554951");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());

        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                // Code to be executed when an ad finishes loading.
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                // Code to be executed when an ad request fails.
            }

            @Override
            public void onAdOpened() {
                // Code to be executed when the ad is displayed.
            }

            @Override
            public void onAdClicked() {
                // Code to be executed when the user clicks on an ad.
            }

            @Override
            public void onAdLeftApplication() {
                // Code to be executed when the user has left the app.
            }

            @Override
            public void onAdClosed() {
                // Code to be executed when the interstitial ad is closed.
                new AlertDialog.Builder(Wallet.this)
                        .setTitle("Title")
                        .setIcon(getDrawable(R.drawable.logo))
                        .setMessage("Do you really want to withdraw?")
                        .setPositiveButton("Withdraw", new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int whichButton) {
                                checkConnection3(userid);
                            }
                        })
                        .setNegativeButton("Not now", null).show();
            }
        });
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
                }
            }
        });

        if (new PrefManagerSound(Wallet.this).music().equals("1")) {
            doUnbindService();
            Intent music = new Intent();
            music.setClass(Wallet.this, MusicService.class);
            stopService(music);
        } else {
            doBindService();
            Intent music = new Intent();
            music.setClass(Wallet.this, MusicService.class);
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
        mobileNumber = new PrefManagerUser(Wallet.this).getmobile_no();

        naming.setText(username);
        mno.setText(mobileNumber);
        em.setText(email);

        checkConnection(userid);
        withdrawWalletLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mInterstitialAd.isLoaded()) {
                    mInterstitialAd.show();
                } else {
                    new AlertDialog.Builder(Wallet.this)
                            .setTitle("Title")
                            .setIcon(getDrawable(R.drawable.logo))
                            .setMessage("Do you really want to withdraw?")
                            .setPositiveButton("Withdraw", new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialog, int whichButton) {
                                    checkConnection3(userid);
                                }
                            })
                            .setNegativeButton("Not now", null).show();
                }

                /* startActivity(new Intent(Wallet.this, WithdrawHistory.class));*/
                /*Intent intent = new Intent(Wallet.this, WithdrawHistory.class);
                intent.putExtra("amount", withdraw);
                startActivity(intent);*/

            }
        });
        rewardWalletLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Wallet.this, RewardList.class));
            }
        });
        SpinhistoryLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Wallet.this, SpinHistory.class));
            }
        });
        bonusWalletLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Wallet.this, WithdrawHistory.class));
            }
        });
        topLayoutTOP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Wallet.this, top10List.class));
            }
        });
        logoutLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkConnection1(userid, newUserId);

            }
        });
    }

    public class LOGOUTURL extends AsyncTask<String, String, String> {
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @SuppressWarnings("ResourceType")
        @Override
        protected String doInBackground(String... arg) {
            try {
                HttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost(LOGURL);

                httpPost.setHeader("content-type", "application/json");
                JSONObject json = new JSONObject();

                json.put("user_id", arg[0]);
                json.put("device_id", arg[1]);

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
            if (status.equals("200")) {
                new PrefManagerUser(Wallet.this).logout();
                Intent openIntent = new Intent(getApplicationContext(), SplashScreen.class);
                openIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                getApplicationContext().startActivity(openIntent);
            } else
                Toasty.error(Wallet.this, "Something went wrong.\nPlease try again later", Toast.LENGTH_SHORT, true).show();
        }
    }

    public class WALLETASYNK extends AsyncTask<String, String, String> {
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @SuppressWarnings("ResourceType")
        @Override
        protected String doInBackground(String... arg) {
            try {
                HttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost(ALLWALLETURL);

                httpPost.setHeader("content-type", "application/json");
                JSONObject json = new JSONObject();

                json.put("user_id", arg[0]);

                StringEntity entity = new StringEntity(json.toString(), "UTF_8");
                httpPost.setEntity(entity);

                HttpResponse response = httpClient.execute(httpPost);
                String responseString = EntityUtils.toString(response.getEntity());
                JSONObject responseJSON = new JSONObject(responseString);

                response_code = responseJSON.getString("response_code");
                response_msg = responseJSON.getString("msg");

                todayWallet = responseJSON.getString("walletamount");
                withdraw = responseJSON.getString("widthdraw_wallet");
                bonus = responseJSON.getString("overallwithdraw");

            } catch (Exception e) {
                e.printStackTrace();
            }
            return response_code;
        }

        @SuppressLint("NewApi")
        protected void onPostExecute(String status) {
            if (status.equals("200")) {
                todaysWalletTextView.setText(todayWallet);
//                bonusWalletTextView.setText(bonus);
                bonusWalletTextView.setText(prettyCount(Integer.parseInt(bonus)));

                withdrawWalletTextView.setText(withdraw);
//                ranking.setText("Rank : " + rank);
            } else
                Toasty.error(Wallet.this, "Something went wrong.\nPlease try again later", Toast.LENGTH_SHORT, true).show();
        }
    }

    @SuppressLint("StaticFieldLeak")
    public class WITHDRAWASYNK extends AsyncTask<String, String, String> {
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ACProgressCustom.Builder(Wallet.this)
                    .useImages(R.drawable.logo)
                    .build();
            dialog.show();
        }

        @SuppressWarnings("ResourceType")
        @Override
        protected String doInBackground(String... arg) {
            try {
                HttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost(withdrawURL1);

                httpPost.setHeader("content-type", "application/json");
                JSONObject json = new JSONObject();

                json.put("user_id", arg[0]);

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
                Toasty.success(Wallet.this, "Request sent for withdrawal", Toast.LENGTH_SHORT, true).show();
                Intent i = new Intent(Wallet.this, Wallet.class);
                startActivity(i);
                finish();
            } else if (status.equals("210")) {
                Toasty.error(Wallet.this, response_msg, Toast.LENGTH_SHORT, true).show();
            }
        }
    }

    public String prettyCount(Number number) {
        char[] suffix = {' ', 'k', 'M', 'B', 'T', 'P', 'E'};
        long numValue = number.longValue();
        int value = (int) Math.floor(Math.log10(numValue));
        int base = value / 3;
        if (value >= 3 && base < suffix.length) {
            return new DecimalFormat("#0.0").format(numValue / Math.pow(10, base * 3)) + suffix[base];
        } else {
            return new DecimalFormat("#,##0").format(numValue);
        }
    }

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
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
        if (mServ != null) {
            mServ.resumeMusic();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        //Detect idle screen
        PowerManager pm = (PowerManager)
                getApplicationContext().getSystemService(Context.POWER_SERVICE);
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
        new WALLETASYNK().cancel(true);
        new LOGOUTURL().cancel(true);
        new WITHDRAWASYNK().cancel(true);
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

    public void checkConnection(final String a) {
        if (isOnline()) {
            new WALLETASYNK().execute(a);
        } else {
            final AlertDialog.Builder builder = new AlertDialog.Builder(Wallet.this);
            builder.setMessage("No internet connection Found.")
                    .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            checkConnection(a);
                        }
                    })
                    .setCancelable(false);
            ;
            final AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    public void checkConnection1(final String b, final String c) {
        if (isOnline()) {
            new LOGOUTURL().execute(b, c);
        } else {
            final AlertDialog.Builder builder = new AlertDialog.Builder(Wallet.this);
            builder.setMessage("No internet connection Found.")
                    .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            checkConnection1(b, c);
                        }
                    })
                    .setCancelable(false);
            ;
            final AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    public void checkConnection3(final String e) {
        if (isOnline()) {
            new WITHDRAWASYNK().execute(e);
        } else {
            final AlertDialog.Builder builder = new AlertDialog.Builder(Wallet.this);
            builder.setMessage("No internet connection Found.")
                    .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            checkConnection3(e);
                        }
                    })
                    .setCancelable(false);
            ;
            final AlertDialog dialog = builder.create();
            dialog.show();
        }
    }
}

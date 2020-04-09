package com.webknight.spinncash;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.webknight.spinncash.extra.ExpandableHeightGridView;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import cc.cloudist.acplibrary.ACProgressCustom;
import es.dmoral.toasty.Toasty;
import in.codeshuffle.scratchcardlayout.listener.ScratchListener;
import in.codeshuffle.scratchcardlayout.ui.ScratchCardLayout;
import in.codeshuffle.scratchcardlayout.util.ScratchCardUtils;

public class RewardList extends AppCompatActivity implements ScratchListener {
    ExpandableHeightGridView rewardList;
    String responseCode = "", todayDate = "";
    ACProgressCustom dialog;
    String user_id;
    private ArrayList<String> strachcardId = new ArrayList<String>();
    private ArrayList<String> date = new ArrayList<String>();
    private ArrayList<String> amount = new ArrayList<String>();
    private ArrayList<String> rstatus = new ArrayList<String>();
    private ArrayList<String> activeDate = new ArrayList<String>();
    JSONArray data = null;
    int active_reward = 0, active_strachcardId;
    int scratchID = 0;
    final String REWARDSURL = Constant.POST_REWARDLIST;
    ScratchCardLayout ScratchCardLayout;
    final String SCRATCHURL = Constant.POST_SCRATCH;
    String response_code = "";
    String response_msg = "";
    String todayWallet = "";
    TextView won;
    AlertDialog alertDialog;
    HomeWatcher mHomeWatcher;
    String nodata = "yodata";
    ImageView nodataI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reward_list);
        rewardList = (ExpandableHeightGridView) findViewById(R.id.rewardList);
        nodataI = (ImageView) findViewById(R.id.nodata);
        user_id = new PrefManagerUser(RewardList.this).getuser_id();
        if (new PrefManagerSound(RewardList.this).music().equals("1")) {
            doUnbindService();
            Intent music = new Intent();
            music.setClass(RewardList.this, MusicService.class);
            stopService(music);
        } else {
            doBindService();
            Intent music = new Intent();
            music.setClass(RewardList.this, MusicService.class);
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
        checkConnection(user_id);

    }

    public class REWARDASYNK extends AsyncTask<String, String, String> {
        protected void onPreExecute() {
            super.onPreExecute();
            strachcardId.clear();
            date.clear();
            activeDate.clear();
            amount.clear();
            rstatus.clear();
            dialog = new ACProgressCustom.Builder(RewardList.this)
                    .useImages(R.drawable.logo)
                    .build();
            dialog.show();
        }

        @SuppressWarnings("ResourceType")
        @Override
        protected String doInBackground(String... arg) {
            try {


                HttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost(REWARDSURL);
                httpPost.setHeader("content-type", "application/json");
                JSONObject json = new JSONObject();

                json.put("user_id", arg[0]);


                StringEntity entity = new StringEntity(json.toString(), "UTF_8");
                httpPost.setEntity(entity);

                HttpResponse response = httpClient.execute(httpPost);
                String responseString = EntityUtils.toString(response.getEntity());
                JSONObject responseJSON = new JSONObject(responseString);
                responseCode = responseJSON.getString("response_code");
                todayDate = responseJSON.getString("todayDate");
                JSONArray data2 = responseJSON.getJSONArray("result");

                if (responseCode.equals("200")) {
                    for (int i = 0; i < data2.length(); i++) {
                        if (isCancelled()) break;
                        JSONObject c = data2.getJSONObject(i);
                        strachcardId.add(c.getString("strachcardId"));
                        date.add(c.getString("winDate"));
                        activeDate.add(c.getString("activeDate"));
                        amount.add(c.getString("strachcardvalue"));
                        rstatus.add(c.getString("status"));
                    }
                }
                if (data2.length() == 0) {
                    nodata = "nodata";
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return responseCode;
        }

        @SuppressLint("NewApi")
        protected void onPostExecute(String status) {
            dialog.dismiss();
            if (status.equals("200")) {
                if (nodata.equals("nodata")) {
                    rewardList.setVisibility(View.GONE);
                    nodataI.setVisibility(View.VISIBLE);
                }
                todaysCustom c1 = new todaysCustom(getApplicationContext(), amount);
                rewardList.setAdapter(c1);
                rewardList.setExpanded(true);
            }
        }
    }

    class todaysCustom extends BaseAdapter {
        Context context;
        LayoutInflater inflter;

        public todaysCustom(Context applicationContext, ArrayList<String> amount) {
            this.context = applicationContext;
            inflter = (LayoutInflater.from(applicationContext));
        }

        @Override
        public int getCount() {
            return amount.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(final int i, View view, ViewGroup viewGroup) {

            view = inflter.inflate(R.layout.alert_scratch, null);

            ImageView rewardSample = (ImageView) view.findViewById(R.id.scratchCard1);
            ScratchCardLayout ScratchCardLayout = (ScratchCardLayout) view.findViewById(R.id.scratchCard);
            TextView won = (TextView) view.findViewById(R.id.rvalue);

            if (rstatus.get(i).contains("1")) {
                won.setText(amount.get(i));
                ScratchCardLayout.setVisibility(View.VISIBLE);
            } else {
                rewardSample.setVisibility(View.VISIBLE);
            }

            rewardSample.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    /*if (activeDate.get(i).equals(todayDate)) {*/
                    if (rstatus.get(i).equals("2")) {
                        active_reward = Integer.parseInt(amount.get(i));
                        active_strachcardId = Integer.parseInt(strachcardId.get(i));
                        scratchPopup(R.layout.alert_scratchcard);
                    } else
                        Toasty.warning(RewardList.this, "The Scratch Card is not activated yet.\nCheck tomorrow.", Toast.LENGTH_SHORT).show();
                }
            });
            return view;

        }
    }

    public void scratchPopup(int layout) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);

        View layoutView = getLayoutInflater().inflate(layout, null);
        ScratchCardLayout = (ScratchCardLayout) layoutView.findViewById(R.id.scratchCard);

        won = (TextView) layoutView.findViewById(R.id.rvalue);
        won.setText(String.valueOf(active_reward));

        ScratchCardLayout.setScratchListener(this);
        ScratchCardLayout.setScratchDrawable(getResources().getDrawable(R.drawable.scratch_sample));
        ScratchCardLayout.setScratchWidthDip(ScratchCardUtils.dipToPx(this, 35));
        ScratchCardLayout.setRevealFullAtPercent(90);
        ScratchCardLayout.setScratchEnabled(true);

        dialogBuilder.setView(layoutView);
        alertDialog = dialogBuilder.create();
        alertDialog.setCancelable(false);
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.show();

    }

    @Override
    public void onScratchStarted() {
    }

    @Override
    public void onScratchProgress(ScratchCardLayout scratchCardLayout, int atLeastScratchedPercent) {
    }

    @Override
    public void onScratchComplete() {
        alertDialog.dismiss();
        checkConnection2(user_id, won.getText().toString(), String.valueOf(active_strachcardId));

        Toasty.success(this, "Scratch ended", Toast.LENGTH_SHORT).show();
    }

    public class SCRATCHASYNK extends AsyncTask<String, String, String> {
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @SuppressWarnings("ResourceType")
        @Override
        protected String doInBackground(String... arg) {
            try {
                HttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost(SCRATCHURL);

                httpPost.setHeader("content-type", "application/json");
                JSONObject json = new JSONObject();

                json.put("user_id", arg[0]);
                json.put("spin_amount", arg[1]);
                json.put("card_id", arg[2]);


                StringEntity entity = new StringEntity(json.toString(), "UTF_8");
                httpPost.setEntity(entity);

                HttpResponse response = httpClient.execute(httpPost);
                String responseString = EntityUtils.toString(response.getEntity());
                JSONObject responseJSON = new JSONObject(responseString);

                response_code = responseJSON.getString("response_code");
                response_msg = responseJSON.getString("msg");
                todayWallet = responseJSON.getString("todayWallet");

            } catch (Exception e) {
                e.printStackTrace();
            }
            return response_code;
        }

        @SuppressLint("NewApi")
        protected void onPostExecute(String status) {
            if (status.equals("200")) {
                startActivity(new Intent(RewardList.this, GameActivity.class));
                finish();
            } else if (status.equals("210")) {
                Toasty.error(RewardList.this, response_msg, Toast.LENGTH_SHORT, true).show();
            } else
                Toasty.error(RewardList.this, "Something went wrong.\nPlease try again later", Toast.LENGTH_SHORT, true).show();
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
            new REWARDASYNK().execute(user_id);
        } else {
            final AlertDialog.Builder builder = new AlertDialog.Builder(RewardList.this);
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

    public void checkConnection2(final String b, final String c, final String d) {
        if (isOnline()) {
            new SCRATCHASYNK().execute(b, c, d);
        } else {
            final AlertDialog.Builder builder = new AlertDialog.Builder(RewardList.this);
            builder.setMessage("No internet connection Found.")
                    .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            checkConnection2(b, c, d);
                        }
                    })
                    .setCancelable(false);
            ;
            final AlertDialog dialog = builder.create();
            dialog.show();
        }
    }
}

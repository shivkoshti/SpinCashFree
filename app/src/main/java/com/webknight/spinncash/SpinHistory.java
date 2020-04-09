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
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.github.jhonnyx2012.horizontalpicker.DatePickerListener;
import com.github.jhonnyx2012.horizontalpicker.HorizontalPicker;
import com.webknight.spinncash.extra.ExpandableHeightGridView;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import cc.cloudist.acplibrary.ACProgressCustom;

public class SpinHistory extends AppCompatActivity implements DatePickerListener {
    final String TODAYSURL = Constant.POST_AMOUNT;
    String responseCode = "";
    JSONArray data = null;
    ACProgressCustom dialog;
    String userid;
    String count;

    private ArrayList<String> user_id = new ArrayList<String>();
    private ArrayList<String> spinIdList = new ArrayList<String>();
    private ArrayList<String> reg_idList = new ArrayList<String>();
    private ArrayList<String> withdrawalRequestIdList = new ArrayList<String>();
    private ArrayList<String> winningTypeList = new ArrayList<String>();
    private ArrayList<String> winningAmountList = new ArrayList<String>();
    private ArrayList<String> spinDateList = new ArrayList<String>();
    private ArrayList<String> spinTimeList = new ArrayList<String>();
    private ArrayList<String> addedByList = new ArrayList<String>();
    private ArrayList<String> spinAmountWidthrawList = new ArrayList<String>();
    ExpandableHeightGridView spinHistory;
    HomeWatcher mHomeWatcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spin_history);
        spinHistory = (ExpandableHeightGridView) findViewById(R.id.spinHistory);
        if (new PrefManagerSound(SpinHistory.this).music().equals("1")) {
            doUnbindService();
            Intent music = new Intent();
            music.setClass(SpinHistory.this, MusicService.class);
            stopService(music);
        } else {
            doBindService();
            Intent music = new Intent();
            music.setClass(SpinHistory.this, MusicService.class);
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
        // find the picker
        HorizontalPicker picker = (HorizontalPicker) findViewById(R.id.datePickerTimeline);
        userid = new PrefManagerUser(this).getuser_id();
        // initialize it and attach a listener
        picker
                .setListener(this)
                .setDays(730)
                .setOffset(365)
                .setDateSelectedColor(Color.DKGRAY)
                .setDateSelectedTextColor(Color.WHITE)
                .setMonthAndYearTextColor(Color.DKGRAY)
                .setTodayButtonTextColor(getResources().getColor(R.color.orange))
                .setTodayDateTextColor(getResources().getColor(R.color.orange))
                .setTodayDateBackgroundColor(getResources().getColor(R.color.white))
                .setUnselectedDayTextColor(Color.DKGRAY)
                .setDayOfWeekTextColor(Color.DKGRAY)
                .setUnselectedDayTextColor(getResources().getColor(R.color.black))
                .showTodayButton(true)
                .init();

        // or on the View directly after init was completed
        picker.setBackgroundColor(getResources().getColor(R.color.transparent));
        picker.setDate(new DateTime());

    }

    @Override
    public void onDateSelected(@NonNull final DateTime dateSelected) {
        // log it for demo
        Log.i("HorizontalPicker", "Selected date is " + dateSelected.toString());
        String dating = dateSelected.toString().substring(0, 10);

        checkConnection(userid, dating);
    }

    public class HISTORYASYNK extends AsyncTask<String, String, String> {
        protected void onPreExecute() {
            super.onPreExecute();
            user_id.clear();
            spinDateList.clear();
            winningAmountList.clear();
            dialog = new ACProgressCustom.Builder(SpinHistory.this)
                    .useImages(R.drawable.logo)
                    .build();
            dialog.show();
        }

        @SuppressWarnings("ResourceType")
        @Override
        protected String doInBackground(String... arg) {
            try {
                HttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost(TODAYSURL);

                httpPost.setHeader("content-type", "application/json");
                JSONObject json = new JSONObject();

                json.put("user_id", arg[0]);
                json.put("spin_date", arg[1]);


                StringEntity entity = new StringEntity(json.toString(), "UTF_8");
                httpPost.setEntity(entity);

                HttpResponse response = httpClient.execute(httpPost);
                String responseString = EntityUtils.toString(response.getEntity());
                JSONObject responseJSON = new JSONObject(responseString);

                responseCode = responseJSON.getString("response_code");
                JSONArray data2 = responseJSON.getJSONArray("result");
                count = responseJSON.getString("count");
                if (count.equals("0")) {
                    spinDateList.add("Oh !!! You didn`t play this day !");
                    winningAmountList.add("0");
                } else {
                    for (int i = 0; i < data2.length(); i++) {
                        if (isCancelled()) break;
                        JSONObject c = data2.getJSONObject(i);
                        spinIdList.add(c.getString("spinId"));
                        reg_idList.add(c.getString("reg_id"));
                        withdrawalRequestIdList.add(c.getString("withdrawalRequestId"));
                        winningTypeList.add(c.getString("winningType"));
                        winningAmountList.add(c.getString("winningAmount"));
                        spinDateList.add(c.getString("spinDate"));
                        spinTimeList.add(c.getString("spinTime"));
                        addedByList.add(c.getString("addedBy"));
                        spinAmountWidthrawList.add(c.getString("spinAmountWidthraw"));
                    }

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
                todaysCustom c1 = new todaysCustom(getApplicationContext(), winningAmountList);
                spinHistory.setAdapter(c1);
                spinHistory.setExpanded(true);
            }
        }
    }

    class todaysCustom extends BaseAdapter {
        Context context;
        LayoutInflater inflter;

        public todaysCustom(Context applicationContext, ArrayList<String> winningAmountList) {
            this.context = applicationContext;
            inflter = (LayoutInflater.from(applicationContext));
        }

        @Override
        public int getCount() {
            return winningAmountList.size();
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
        public View getView(int i, View view, ViewGroup viewGroup) {

            view = inflter.inflate(R.layout.adp_leaderboard, null);

            TextView icon1 = (TextView) view.findViewById(R.id.wdate);
            TextView icon2 = (TextView) view.findViewById(R.id.wwinning);
            icon1.setText(spinDateList.get(i).substring(0, 10) + " " + spinTimeList.get(i));
            icon2.setText("₹" + winningAmountList.get(i));
            return view;

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
            new HISTORYASYNK().execute(a, b);
        } else {
            final AlertDialog.Builder builder = new AlertDialog.Builder(SpinHistory.this);
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
}


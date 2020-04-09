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
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.squareup.picasso.Picasso;
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

public class WithdrawHistory extends AppCompatActivity {
    final String URL1 = Constant.POST_WITHDRAW;
    final String URL2 = Constant.POST_WITHDRAWHISTORY;
    final String STARTURL = Constant.POST_START;
    String userid;
    ACProgressCustom dialog;
    String response_code = "", response_msg = "", showRequestButton;
    ExpandableHeightGridView expandableListView;
    JSONArray data2;
    ArrayList<String> withdrawalRequestId = new ArrayList<>();
    ArrayList<String> reg_id = new ArrayList<>();
    ArrayList<String> withdrawalType = new ArrayList<>();
    ArrayList<String> WithdrawalAmount = new ArrayList<>();
    ArrayList<String> requestPlatform = new ArrayList<>();
    ArrayList<String> wDate = new ArrayList<>();
    ArrayList<String> wTime = new ArrayList<>();
    ArrayList<String> statusP = new ArrayList<>();
    ArrayList<String> approveDate = new ArrayList<>();
    ArrayList<String> withdrawnotification = new ArrayList<>();
    String amountExtra = "";
    HomeWatcher mHomeWatcher;
    String nodata = "yodata";
    ImageView nodataI;
    String scratchcard = "", active = "", startimg = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_withdraw_history);
        userid = new PrefManagerUser(this).getuser_id();
        expandableListView = (ExpandableHeightGridView) findViewById(R.id.expandableListView);
        nodataI = (ImageView) findViewById(R.id.nodata);

        if (new PrefManagerSound(WithdrawHistory.this).music().equals("1")) {
            doUnbindService();
            Intent music = new Intent();
            music.setClass(WithdrawHistory.this, MusicService.class);
            stopService(music);
        } else {
            doBindService();
            Intent music = new Intent();
            music.setClass(WithdrawHistory.this, MusicService.class);
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
        /*if (Integer.parseInt(amountExtra) < 25) {
            requestButton.setVisibility(View.GONE);
            Toasty.warning(this, "Minimum amount to withdraw is 25₹", Toast.LENGTH_SHORT).show();
        }*/
        checkConnection(userid);

    }

    public class WITHDRAWHISTORYASYNK extends AsyncTask<String, String, String> {
        protected void onPreExecute() {
            super.onPreExecute();
            withdrawalRequestId.clear();
            reg_id.clear();
            withdrawalType.clear();
            WithdrawalAmount.clear();
            requestPlatform.clear();
            wDate.clear();
            wTime.clear();
            statusP.clear();
            approveDate.clear();
            withdrawnotification.clear();
            dialog = new ACProgressCustom.Builder(WithdrawHistory.this)
                    .useImages(R.drawable.logo)
                    .build();
            dialog.show();
        }

        @SuppressWarnings("ResourceType")
        @Override
        protected String doInBackground(String... arg) {
            try {
                HttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost(URL2);

                httpPost.setHeader("content-type", "application/json");
                JSONObject json = new JSONObject();

                json.put("user_id", arg[0]);

                StringEntity entity = new StringEntity(json.toString(), "UTF_8");
                httpPost.setEntity(entity);

                HttpResponse response = httpClient.execute(httpPost);
                String responseString = EntityUtils.toString(response.getEntity());
                JSONObject responseJSON = new JSONObject(responseString);

                response_code = responseJSON.getString("response_code");
                showRequestButton = responseJSON.getString("showRequestButton");
                response_msg = responseJSON.getString("msg");
                data2 = responseJSON.getJSONArray("result");
                for (int i = 0; i < data2.length(); i++) {
                    if (isCancelled()) break;
                    JSONObject c = data2.getJSONObject(i);
                    withdrawalRequestId.add(c.getString("withdrawalRequestId"));
                    reg_id.add(c.getString("reg_id"));
                    withdrawalType.add(c.getString("withdrawalType"));
                    WithdrawalAmount.add(c.getString("WithdrawalAmount"));
                    requestPlatform.add(c.getString("requestPlatform"));
                    wDate.add(c.getString("wDate"));
                    wTime.add(c.getString("wTime"));
                    statusP.add(c.getString("status"));
                    approveDate.add(c.getString("approveDate"));
                    withdrawnotification.add(c.getString("withdrawnotification"));
                }
                if (data2.length() == 0) {
                    nodata = "nodata";
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return response_code;
        }

        @SuppressLint("NewApi")
        protected void onPostExecute(String status) {
            dialog.dismiss();
            if (status.equals("200")) {
                if (showRequestButton.equals("1")) {
                    Toasty.warning(WithdrawHistory.this, "One withdrawal Request is pending.", Toast.LENGTH_SHORT).show();
                }
                if (nodata.equals("nodata")) {
                    expandableListView.setVisibility(View.GONE);
                    nodataI.setVisibility(View.VISIBLE);
                }
                todaysCustom c1 = new todaysCustom(WithdrawHistory.this, withdrawalRequestId);
                expandableListView.setAdapter(c1);
                expandableListView.setExpanded(true);
            } else {
                Toasty.error(WithdrawHistory.this, "Something went wrong.\nPlease try again later", Toast.LENGTH_SHORT, true).show();

            }
        }
    }

    private void startDialogue(int layout, final String a) {
        AlertDialog.Builder zeropopup = new AlertDialog.Builder(this);

        View layoutView = getLayoutInflater().inflate(layout, null);

        zeropopup.setView(layoutView);
        final AlertDialog zeroProfile = zeropopup.create();
        zeroProfile.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        zeroProfile.setCancelable(false);
        zeroProfile.show();
        final ImageView submitProfile = layoutView.findViewById(R.id.surprise);
        Picasso.get().load(Constant.MAINURL + a).into(submitProfile);
        submitProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                zeroProfile.dismiss();

            }
        });
    }

    class todaysCustom extends BaseAdapter {
        Context context;
        LayoutInflater inflter;

        public todaysCustom(Context applicationContext, ArrayList<String> withdrawalRequestId) {
            this.context = applicationContext;
            inflter = (LayoutInflater.from(applicationContext));
        }

        @Override
        public int getCount() {
            return withdrawalRequestId.size();
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

            view = inflter.inflate(R.layout.adp_withdrawhistory, null);

            TextView withid = (TextView) view.findViewById(R.id.withid);
            TextView date = (TextView) view.findViewById(R.id.date);
            TextView amount = (TextView) view.findViewById(R.id.amount);
            TextView statuspending = (TextView) view.findViewById(R.id.statuspending);
            LinearLayout bgList = (LinearLayout) view.findViewById(R.id.bgList);
            RelativeLayout relate = (RelativeLayout) view.findViewById(R.id.relate);

            if (statusP.get(i).equals("0")) {
                relate.setBackgroundColor(getResources().getColor(R.color.pending));
                statuspending.setText("Pending");
            } else {
                relate.setBackgroundColor(getResources().getColor(R.color.approved));
                statuspending.setText("Approved");
            }

            withid.setText("SC" + withdrawalRequestId.get(i));
            date.setText(wDate.get(i));
            amount.setText("Rs. " + WithdrawalAmount.get(i));
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
            new WITHDRAWHISTORYASYNK().execute(a);
        } else {
            final AlertDialog.Builder builder = new AlertDialog.Builder(WithdrawHistory.this);
            builder.setMessage("No internet connection Found.")
                    .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            checkConnection(a);
                        }
                    })
                    .setCancelable(false);

            final AlertDialog dialog = builder.create();
            dialog.show();
        }
    }
}

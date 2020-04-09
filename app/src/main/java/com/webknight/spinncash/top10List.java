package com.webknight.spinncash;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

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

import java.text.DecimalFormat;
import java.util.ArrayList;

import cc.cloudist.acplibrary.ACProgressCustom;

public class top10List extends AppCompatActivity {
    String user_id;
    final String BONUSLEDURL = Constant.POST_PRIZELEADERBOARD;
    ArrayList<String> bonusAmountList = new ArrayList<>();
    ArrayList<String> rankList = new ArrayList<>();
    ArrayList<String> includetenList = new ArrayList<>();
    ArrayList<String> reg_idList = new ArrayList<>();
    ArrayList<String> lusernameList = new ArrayList<>();
    ArrayList<String> lbonuswallet = new ArrayList<>();
    ArrayList<String> bdt = new ArrayList<>();
    ACProgressCustom dialog;
    String responseCode = "";
    String response_msg = "";
    String bonusAmount,
            rankP,
            includeinten;
    TextView usernameO;
    String bonus;
    ExpandableHeightGridView expandableListView;
    ImageView nodataI;
    HomeWatcher mHomeWatcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top10_list);
        expandableListView = (ExpandableHeightGridView) findViewById(R.id.expandableListView);
        nodataI = (ImageView) findViewById(R.id.nodata);
        user_id = new PrefManagerUser(top10List.this).getuser_id();
        checkConnection(user_id);
        if (new PrefManagerSound(top10List.this).music().equals("1")) {
            doUnbindService();
            Intent music = new Intent();
            music.setClass(top10List.this, MusicService.class);
            stopService(music);
        } else {
            doBindService();
            Intent music = new Intent();
            music.setClass(top10List.this, MusicService.class);
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

    class LeaderBoardCustom extends BaseAdapter {
        Context context;
        LayoutInflater inflter;

        public LeaderBoardCustom(Context applicationContext, ArrayList<String> lusernameList) {
            this.context = applicationContext;
            inflter = (LayoutInflater.from(applicationContext));
        }

        @Override
        public int getCount() {
            return lusernameList.size();
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

            view = inflter.inflate(R.layout.adp_leaderboarddynamic, null);
            RelativeLayout bgco = (RelativeLayout) view.findViewById(R.id.bgco);
            ImageView rank = (ImageView) view.findViewById(R.id.rank);
            usernameO = (TextView) view.findViewById(R.id.usernameList);
            TextView bonusWalletLeader = (TextView) view.findViewById(R.id.bonusWalletLeader);

            if (reg_idList.get(i).equals(new PrefManagerUser(top10List.this).getuser_id())) {
                Animation animation1 =
                        AnimationUtils.loadAnimation(getApplicationContext(),
                                R.anim.blink);
                bgco.startAnimation(animation1);
            }

            usernameO.setText(lusernameList.get(i));


//            bonusWalletLeader.setText(lbonuswallet.get(i));
            if (includeinten.equals("0") && lbonuswallet.size() - 1 == i) {
                bgco.setBackgroundColor(getResources().getColor(R.color.otherone));
                Drawable myDrawable = getResources().getDrawable(R.drawable.logo);
                rank.setImageDrawable(myDrawable);
            }
            bonusWalletLeader.setText(prettyCount(Integer.parseInt(lbonuswallet.get(i))));
            if (i == 0) {
                bgco.setBackgroundColor(getResources().getColor(R.color.winnerone));
                Drawable myDrawable = getResources().getDrawable(R.drawable.winner1);
                rank.setImageDrawable(myDrawable);
            } else if (i == 1) {
                bgco.setBackgroundColor(getResources().getColor(R.color.winnertwo));
                Drawable myDrawable = getResources().getDrawable(R.drawable.winner2);
                rank.setImageDrawable(myDrawable);
            } else if (i == 2) {
                bgco.setBackgroundColor(getResources().getColor(R.color.winnerthree));
                Drawable myDrawable = getResources().getDrawable(R.drawable.winner3);
                rank.setImageDrawable(myDrawable);
            } else if (i == 3) {
                bgco.setBackgroundColor(getResources().getColor(R.color.otherone));
                Drawable myDrawable = getResources().getDrawable(R.drawable.winner4);
                rank.setImageDrawable(myDrawable);
            } else if (i == 4) {
                bgco.setBackgroundColor(getResources().getColor(R.color.otherone));
                Drawable myDrawable = getResources().getDrawable(R.drawable.winner5);
                rank.setImageDrawable(myDrawable);
            } else if (i == 5) {
                bgco.setBackgroundColor(getResources().getColor(R.color.otherone));
                Drawable myDrawable = getResources().getDrawable(R.drawable.winner6);
                rank.setImageDrawable(myDrawable);
            } else if (i == 6) {
                bgco.setBackgroundColor(getResources().getColor(R.color.otherone));
                Drawable myDrawable = getResources().getDrawable(R.drawable.winner7);
                rank.setImageDrawable(myDrawable);
            } else if (i == 7) {
                bgco.setBackgroundColor(getResources().getColor(R.color.otherone));
                Drawable myDrawable = getResources().getDrawable(R.drawable.winner8);
                rank.setImageDrawable(myDrawable);
            } else if (i == 8) {
                bgco.setBackgroundColor(getResources().getColor(R.color.otherone));
                Drawable myDrawable = getResources().getDrawable(R.drawable.winner9);
                rank.setImageDrawable(myDrawable);
            } else if (i == 9) {
                bgco.setBackgroundColor(getResources().getColor(R.color.otherone));
                Drawable myDrawable = getResources().getDrawable(R.drawable.winner10);
                rank.setImageDrawable(myDrawable);
            } else if (i == 10) {
                bgco.setBackgroundColor(getResources().getColor(R.color.otherone));
                Drawable myDrawable = getResources().getDrawable(R.drawable.winner11);
                rank.setImageDrawable(myDrawable);
            } else if (i == 11) {
                bgco.setBackgroundColor(getResources().getColor(R.color.otherone));
                Drawable myDrawable = getResources().getDrawable(R.drawable.winner12);
                rank.setImageDrawable(myDrawable);
            } else if (i == 12) {
                bgco.setBackgroundColor(getResources().getColor(R.color.otherone));
                Drawable myDrawable = getResources().getDrawable(R.drawable.winner13);
                rank.setImageDrawable(myDrawable);
            } else if (i == 13) {
                bgco.setBackgroundColor(getResources().getColor(R.color.otherone));
                Drawable myDrawable = getResources().getDrawable(R.drawable.winner14);
                rank.setImageDrawable(myDrawable);
            } else if (i == 14) {
                bgco.setBackgroundColor(getResources().getColor(R.color.otherone));
                Drawable myDrawable = getResources().getDrawable(R.drawable.winner15);
                rank.setImageDrawable(myDrawable);
            }
            rank.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                }
            });
            return view;

        }
    }

    public class BONUSLEADERASYNK extends AsyncTask<String, String, String> {
        protected void onPreExecute() {
            super.onPreExecute();
            reg_idList.clear();
            lusernameList.clear();
            lbonuswallet.clear();
            dialog = new ACProgressCustom.Builder(top10List.this)
                    .useImages(R.drawable.logo)
                    .build();
            dialog.show();

        }

        @SuppressWarnings("ResourceType")
        @Override
        protected String doInBackground(String... arg) {
            try {

                HttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost(BONUSLEDURL);

                httpPost.setHeader("content-type", "application/json");
                JSONObject json = new JSONObject();

                json.put("user_id", arg[0]);
                StringEntity entity = new StringEntity(json.toString(), "UTF_8");
                httpPost.setEntity(entity);

                HttpResponse response = httpClient.execute(httpPost);
                String responseString = EntityUtils.toString(response.getEntity());
                JSONObject responseJSON = new JSONObject(responseString);
                responseCode = responseJSON.getString("response_code");

                response_msg = responseJSON.getString("msg");
//                bonusAmount = responseJSON.getString("bonusAmount");
                rankP = responseJSON.getString("rank");
                includeinten = responseJSON.getString("includeinten");
                JSONArray data2 = responseJSON.getJSONArray("result");
                if (responseCode.equals("200")) {
                    for (int i = 0; i < data2.length(); i++) {
                        if (isCancelled()) break;
                        JSONObject c = data2.getJSONObject(i);
                        reg_idList.add(c.getString("reg_id"));
                        lusernameList.add(c.getString("lusernameList"));
                        lbonuswallet.add(c.getString("lbonuswallet"));
                        bdt.add(c.getString("bdt"));
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
                LeaderBoardCustom c1 = new LeaderBoardCustom(top10List.this, lusernameList);
                expandableListView.setAdapter(c1);
                expandableListView.setExpanded(true);
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

    public void checkConnection(final String a) {
        if (isOnline()) {
            new BONUSLEADERASYNK().execute(a);
        } else {
            final AlertDialog.Builder builder = new AlertDialog.Builder(top10List.this);
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

}

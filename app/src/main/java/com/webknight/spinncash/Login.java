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
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.Settings.Secure;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.onesignal.OneSignal;
import com.skydoves.elasticviews.ElasticButton;
import com.skydoves.elasticviews.ElasticImageView;
import com.webknight.spinncash.extra.ChildInfo;
import com.webknight.spinncash.extra.GroupInfo;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import cc.cloudist.acplibrary.ACProgressCustom;
import es.dmoral.toasty.Toasty;
import io.ghyeok.stickyswitch.widget.StickySwitch;

public class Login extends AppCompatActivity {
    final String URL1 = Constant.POST_LOGIN;
    final String leaderURL1 = Constant.POST_LEADERBOARD;
    String response_code = "";
    String response_msg = "";
    String mobile_no = "";
    StickySwitch bgm, click;
    EditText mobileNumber;
    ElasticImageView loginNext;
    ElasticButton settingPopup, trophy;
    ACProgressCustom dialog;
    String responseCode, userId, version_link = "";
    ListView listView;
    /*popup objects*/
    ExpandableListView simpleExpandableListView;
    CustomAdapter listAdapter;
    LinkedHashMap<String, GroupInfo> subjects = new LinkedHashMap<String, GroupInfo>();
    final ArrayList<GroupInfo> deptList = new ArrayList<GroupInfo>();
    final ArrayList<String> lrank = new ArrayList<String>();
    final ArrayList<String> lusernameList = new ArrayList<String>();
    final ArrayList<String> lbonuswallet = new ArrayList<String>();
    private String android_id;
    String newUserId;
    HomeWatcher mHomeWatcher;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mobileNumber = (EditText) findViewById(R.id.mobileNumber);
        loginNext = (ElasticImageView) findViewById(R.id.loginNext);
        settingPopup = (ElasticButton) findViewById(R.id.settingPopup);
        trophy = (ElasticButton) findViewById(R.id.leaderBoard);
        android_id = Secure.getString(getApplicationContext().getContentResolver(), Secure.ANDROID_ID);

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
        mobile_no = i.getStringExtra("mobile_no");


        mobileNumber.setText(mobile_no);
        if (new PrefManagerSound(Login.this).music().equals("1")) {
            doUnbindService();
            Intent music = new Intent();
            music.setClass(Login.this, MusicService.class);
            stopService(music);
        } else {
            doBindService();
            Intent music = new Intent();
            music.setClass(Login.this, MusicService.class);
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

        /*leaderBoard(R.layout.leaderboard_dynamic);*/

        settingPopup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSettingPopup(R.layout.alert_popup);
            }
        });
        trophy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                leaderBoard(R.layout.leaderboard_dynamic);
            }
        });

        loginNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (TextUtils.isEmpty(mobileNumber.getText().toString())) {
//                     loginmobile.setError("Required Field");
                    Toasty.error(Login.this, "Required Field.", Toast.LENGTH_SHORT, true).show();

                } else if (mobileNumber.getText().length() < 10 || mobileNumber.getText().length() > 10) {
                    //loginmobile.setError("Enter Valid Number");
                    Toasty.warning(Login.this, "Enter Valid Number.", Toast.LENGTH_SHORT, true).show();

                } else {
                    checkConnection(mobileNumber.getText().toString(), android_id);

                }
            }
        });
    }

    public void leaderBoard(int layout) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);

        View layoutView = getLayoutInflater().inflate(layout, null);
        dialogBuilder.setView(layoutView);
        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.show();
        listView = layoutView.findViewById(R.id.leaderBoard);
        checkConnection1(userId);
    }

    private void showSettingPopup(int layout) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        View layoutView = getLayoutInflater().inflate(layout, null);
        dialogBuilder.setView(layoutView);
        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.show();


        ElasticImageView noticeClick, aboutClick, contactClick, helpClick, howtoplayClick, shareApp, rateUs;

        bgm = (StickySwitch) layoutView.findViewById(R.id.bgm);
//        click = (StickySwitch) layoutView.findViewById(R.id.click);
        noticeClick = layoutView.findViewById(R.id.noticeClick);
        aboutClick = layoutView.findViewById(R.id.aboutClick);
        contactClick = layoutView.findViewById(R.id.contactClick);
        helpClick = layoutView.findViewById(R.id.helpClick);
        shareApp = layoutView.findViewById(R.id.shareApp);
        rateUs = layoutView.findViewById(R.id.rateUs);
        howtoplayClick = layoutView.findViewById(R.id.howtoplayClick);

        if (!new PrefManagerSound(Login.this).music().equals("1")) {
            bgm.setDirection(StickySwitch.Direction.RIGHT);
        } else bgm.setDirection(StickySwitch.Direction.LEFT);

        bgm.setOnSelectedChangeListener(new StickySwitch.OnSelectedChangeListener() {
            @Override
            public void onSelectedChange(StickySwitch.Direction direction, String s) {
                if (direction.equals(StickySwitch.Direction.LEFT)) {
                    new PrefManagerSound(Login.this).updateMusic("1");
                    doUnbindService();
                    Intent music = new Intent();
                    music.setClass(Login.this, MusicService.class);
                    stopService(music);
                } else {
                    new PrefManagerSound(Login.this).updateMusic("0");
                    doBindService();
                    Intent music = new Intent();
                    music.setClass(Login.this, MusicService.class);
                    startService(music);
                }
            }
        });

        shareApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("text/plain");
                    shareIntent.putExtra(Intent.EXTRA_SUBJECT, "My application name");
                    String shareMessage = "\nLet me recommend you this application\n\n";
                    shareMessage = shareMessage + "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID + "\n";
                    shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
                    startActivity(Intent.createChooser(shareIntent, "choose one"));
                } catch (Exception e) {
                    //e.toString();
                }
            }
        });

        rateUs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID)));
            }
        });

        loadData();

        noticeClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                notices(R.layout.alert_notice);
            }
        });
        aboutClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                aboutus(R.layout.alert_aboutus);
            }
        });
        contactClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                contacts(R.layout.alert_contact);
            }
        });
        helpClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                helps(R.layout.alert_help);
            }
        });
        howtoplayClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                howtoplays(R.layout.alert_howtoplay);
            }
        });
    }

    public void notices(int layout) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);

        View layoutView = getLayoutInflater().inflate(layout, null);
        TextView tc, pp;

        dialogBuilder.setView(layoutView);
        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.show();
        tc = layoutView.findViewById(R.id.tc);
        pp = layoutView.findViewById(R.id.pp);

        InputStream inputStream = getResources().openRawResource(R.raw.tc);
        InputStream inputStreamPP = getResources().openRawResource(R.raw.pp);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        int i;
        try {
            i = inputStream.read();
            while (i != -1) {
                byteArrayOutputStream.write(i);
                i = inputStream.read();
            }
            inputStream.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        tc.setText(byteArrayOutputStream.toString());

        ByteArrayOutputStream byteArrayOutputStreamPP = new ByteArrayOutputStream();

        int j;
        try {
            j = inputStreamPP.read();
            while (j != -1) {
                byteArrayOutputStreamPP.write(j);
                j = inputStreamPP.read();
            }
            inputStreamPP.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        pp.setText(byteArrayOutputStreamPP.toString());
    }

    public void helps(int layout) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);

        View layoutView = getLayoutInflater().inflate(layout, null);

        //get reference of the ExpandableListView
        simpleExpandableListView = (ExpandableListView) layoutView.findViewById(R.id.simpleExpandableListView);
        // create the adapter by passing your ArrayList data
        listAdapter = new CustomAdapter(Login.this, deptList);
        // attach the adapter to the expandable list view
        simpleExpandableListView.setAdapter(listAdapter);


        // setOnChildClickListener listener for child row click
        simpleExpandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                //get the group header
                GroupInfo headerInfo = deptList.get(groupPosition);
                //get the child info
                ChildInfo detailInfo = headerInfo.getProductList().get(childPosition);
                //display it or do something with it
                return false;
            }
        });
        // setOnGroupClickListener listener for group heading click
        simpleExpandableListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                //get the group header
                GroupInfo headerInfo = deptList.get(groupPosition);
                //display it or do something with it
                return false;
            }
        });


        dialogBuilder.setView(layoutView);
        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.show();

    }

    public class CustomAdapter extends BaseExpandableListAdapter {

        private Context context;
        private ArrayList<GroupInfo> deptList;

        public CustomAdapter(Context context, ArrayList<GroupInfo> deptList) {
            this.context = context;
            this.deptList = deptList;
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            ArrayList<ChildInfo> productList = deptList.get(groupPosition).getProductList();
            return productList.get(childPosition);
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
                                 View view, ViewGroup parent) {

            ChildInfo detailInfo = (ChildInfo) getChild(groupPosition, childPosition);
            if (view == null) {
                LayoutInflater infalInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = infalInflater.inflate(R.layout.expand_text, null);
            }

            TextView sequence = (TextView) view.findViewById(R.id.sequence);
            sequence.setText(detailInfo.getSequence().trim() + ". ");
            TextView childItem = (TextView) view.findViewById(R.id.childItem);
            childItem.setText(detailInfo.getName().trim());

            return view;
        }

        @Override
        public int getChildrenCount(int groupPosition) {

            ArrayList<ChildInfo> productList = deptList.get(groupPosition).getProductList();
            return productList.size();

        }

        @Override
        public Object getGroup(int groupPosition) {
            return deptList.get(groupPosition);
        }

        @Override
        public int getGroupCount() {
            return deptList.size();
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isLastChild, View view,
                                 ViewGroup parent) {

            GroupInfo headerInfo = (GroupInfo) getGroup(groupPosition);
            if (view == null) {
                LayoutInflater inf = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inf.inflate(R.layout.group_head, null);
            }

            TextView heading = (TextView) view.findViewById(R.id.heading);
            heading.setText(headerInfo.getName().trim());

            return view;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
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
            TextView username = (TextView) view.findViewById(R.id.usernameList);
            TextView bonusWalletLeader = (TextView) view.findViewById(R.id.bonusWalletLeader);

            username.setText(lusernameList.get(i));


//            bonusWalletLeader.setText(lbonuswallet.get(i));
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

    //load some initial data into out list
    private void loadData() {
        addProduct("How to start earning ?", "First download the app from playstore.");
        addProduct("How to start earning ?", "Register yourself in the app using your mobile number which is registered on any of upi platform.");
//        addProduct("How to start earning ?", "Invest  One Time PAyment of 1000 ₹ using upi. and that's it !");
        addProduct("How to start earning ?", "Spin daily and collect money in the wallet.");

        /*addProduct("How to invest the one time payment ?", "Once you register yourself in the app you will be redirected to the game page.");
        addProduct("How to invest the one time payment ?", "On the upper top side there is a button named 'Add Money'. ");
        addProduct("How to invest the one time payment ?", "There are the payment methods shown.");
        addProduct("How to invest the one time payment ?", "On the second tab there is a QR code shown and the UPI also. Use this things to pay the One Time Payment.");
        addProduct("How to invest the one time payment ?", "Don't forget to take screenshot of the transaction and send it to the admin team to approve it.");
        addProduct("How to invest the one time payment ?", "The admin team will approve your payment in 24 working hours.");*/

        addProduct("Any limits ?", "No limits,A user can earn unlimited points a day.");
//        addProduct("Any limits ?", "But Don't worry.The user can spin more than 1025 points. But the points will be counted as Bonus Wallet.");
//        addProduct("Any limits ?", "On the new day the amount will be converted into 0");
//        addProduct("Any limits ?", "If the user will earn less then 1025 a day then he will not credited the for that points.");

        addProduct("How to withdraw ?", "On the Upper Top side when the user clicks the wallet,it will be redirected to the wallet page.");
        addProduct("How to withdraw ?", "By clicking on 'Withdrawable Amount' the user can request for withdrawal by using three payment platform defined.");
        addProduct("How to withdraw ?", "User can only request single payment at a time.He/She can not request until the last request will be approved.");

        /*addProduct("What is Bonus wallet ?", "The bonus wallet can not be withdrawn.");
        addProduct("What is Bonus wallet ?", "On the last day of the month, the top 3 Bonus winners will be gifted a particular amount defined by the admin");*/
    }

    //here we maintain our products in various departments
    private int addProduct(String department, String product) {

        int groupPosition = 0;
        //check the hash map if the group already exists
        GroupInfo headerInfo = subjects.get(department);
        //add the group if doesn't exists
        if (headerInfo == null) {
            headerInfo = new GroupInfo();
            headerInfo.setName(department);
            subjects.put(department, headerInfo);
            deptList.add(headerInfo);

        }

        //get the children for the group
        ArrayList<ChildInfo> productList = headerInfo.getProductList();
        //size of the children list
        int listSize = productList.size();
        //add to the counter
        listSize++;

        //create a new child and add that to the group
        ChildInfo detailInfo = new ChildInfo();
        detailInfo.setSequence(String.valueOf(listSize));
        detailInfo.setName(product);
        productList.add(detailInfo);
        headerInfo.setProductList(productList);

        //find the group position inside the list
        groupPosition = deptList.indexOf(headerInfo);
        return groupPosition;
    }

    public void howtoplays(int layout) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);

        View layoutView = getLayoutInflater().inflate(layout, null);


        dialogBuilder.setView(layoutView);
        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.show();

    }

    public void aboutus(int layout) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);

        View layoutView = getLayoutInflater().inflate(layout, null);


        dialogBuilder.setView(layoutView);
        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.show();

    }

    public void contacts(int layout) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);

        View layoutView = getLayoutInflater().inflate(layout, null);


        dialogBuilder.setView(layoutView);
        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.show();
        ElasticImageView youtube, facebook, instagram;
        youtube = layoutView.findViewById(R.id.youtube);
        facebook = layoutView.findViewById(R.id.facebook);
        instagram = layoutView.findViewById(R.id.instagram);
        youtube.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/channel/UCgqLmNgQb0h-bskOjvPi1JA"));
                startActivity(browserIntent);
            }
        });
        facebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("c"));
                startActivity(browserIntent);
            }
        });
        instagram.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.instagram.com/spinncash/?hl=en"));
                startActivity(browserIntent);
            }
        });
    }

    @SuppressLint("StaticFieldLeak")
    public class LOGINASYNK extends AsyncTask<String, String, String> {
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ACProgressCustom.Builder(Login.this)
                    .useImages(R.drawable.logo)
                    .build();
            dialog.show();
        }

        @SuppressWarnings("ResourceType")
        @Override
        protected String doInBackground(String... arg) {
            try {
                HttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost(URL1);

                httpPost.setHeader("content-type", "application/json");
                JSONObject json = new JSONObject();

                json.put("mobile_no", arg[0]);
                json.put("deviceid", arg[1]);


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
                Toasty.success(Login.this, "OTP has been sent to you.", Toast.LENGTH_SHORT, true).show();
                Intent i = new Intent(Login.this, OTP.class);
                i.putExtra("mobile_no", mobileNumber.getText().toString());
                i.putExtra("playerid", newUserId);
                startActivity(i);
                finish();

            } else {
                Toasty.error(Login.this, "Something went wrong.\nPlease try again later", Toast.LENGTH_SHORT, true).show();

            }
        }
    }

    public class LEADERASYNK extends AsyncTask<String, String, String> {
        protected void onPreExecute() {
            super.onPreExecute();
            lusernameList.clear();
            lbonuswallet.clear();
            dialog = new ACProgressCustom.Builder(Login.this)
                    .useImages(R.drawable.logo)
                    .build();
            dialog.show();
        }

        @SuppressWarnings("ResourceType")
        @Override
        protected String doInBackground(String... arg) {
            try {
                HttpClient httpClient = new DefaultHttpClient();
                HttpGet httpPost = new HttpGet(leaderURL1);

                httpPost.setHeader("content-type", "application/json");
                JSONObject json = new JSONObject();

                HttpResponse response = httpClient.execute(httpPost);
                String responseString = EntityUtils.toString(response.getEntity());
                JSONObject responseJSON = new JSONObject(responseString);
                responseCode = responseJSON.getString("response_code");

                response_msg = responseJSON.getString("msg");
                JSONArray data2 = responseJSON.getJSONArray("result");
                if (responseCode.equals("200")) {
                    for (int i = 0; i < data2.length(); i++) {
                        JSONObject c = data2.getJSONObject(i);
                        lusernameList.add(c.getString("lusernameList"));
                        lbonuswallet.add(c.getString("lbonuswallet"));
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
                LeaderBoardCustom c1 = new LeaderBoardCustom(Login.this, lusernameList);
                listView.setAdapter(c1);
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
            new LOGINASYNK().execute(a, b);
        } else {
            final AlertDialog.Builder builder = new AlertDialog.Builder(Login.this);
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
            new LEADERASYNK().execute(c);
        } else {
            final AlertDialog.Builder builder = new AlertDialog.Builder(Login.this);
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
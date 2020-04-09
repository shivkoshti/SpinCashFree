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
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.elyeproj.loaderviewlibrary.LoaderTextView;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.skydoves.elasticviews.ElasticButton;
import com.skydoves.elasticviews.ElasticImageView;
import com.squareup.picasso.Picasso;
import com.webknight.spinncash.extra.ChildInfo;
import com.webknight.spinncash.extra.GroupInfo;
import com.webknight.spinwheel.LuckyWheelView;
import com.webknight.spinwheel.model.LuckyItem;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;

import cc.cloudist.acplibrary.ACProgressCustom;
import es.dmoral.toasty.Toasty;
import io.ghyeok.stickyswitch.widget.StickySwitch;

public class GameActivity extends AppCompatActivity {
    ElasticImageView logo;
    LoaderTextView wallet;
    /*LoaderTextView bonusWallet;*/

    List<LuckyItem> data1 = new ArrayList<>();
    AdView mAdview;
    String spinAnswer;

    private InterstitialAd mInterstitialAd;

    String mobile_no = "";
    LuckyWheelView luckyWheelView;
    String userid;
    String popuptraker;
    StickySwitch bgm, click;
    String startimaging;
    String selecting = "4";
    /*String pendingamount;*/
    String userName;
    /*popup object*/
    String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
    AlertDialog.Builder profilepopup;
    AlertDialog alertDialogProfile;
    AlertDialog withdrawProfile;

    AlertDialog.Builder rewardpopup;
    AlertDialog alertDialogreward;

    EditText userNameEdit, userEmail;
    final String URL1 = Constant.POST_USER;
    final String ZEROSPINURL = Constant.POST_ZEROSPIN;
    final String SPINURL = Constant.POST_SPIN;
    final String WALLETURL = Constant.POST_WALLET;
    final String STARTURL = Constant.POST_START;
    final String UPDATEUPI = Constant.POST_UPI;
    String response_code = "";
    String response_msg = "";
    String todayWalletRes = "";
    String scratchcard = "", active = "", startimg = "";
    String todayWallet;
    String bonus;
    String todayDate;
    ACProgressCustom dialog;
    ElasticButton playbutton/*, addMoney*/;
    ImageView speenWheelback;
    ExpandableListView simpleExpandableListView;
    CustomAdapter listAdapter;
    LinkedHashMap<String, GroupInfo> subjects = new LinkedHashMap<String, GroupInfo>();
    final ArrayList<GroupInfo> deptList = new ArrayList<GroupInfo>();
    boolean doubleBackToExitPressedOnce = false;
    String popup = "0";
    HomeWatcher mHomeWatcher;
    MediaPlayer mp, mp1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        luckyWheelView = (LuckyWheelView) findViewById(R.id.luckyWheel);
        logo = (ElasticImageView) findViewById(R.id.logo);
        /*addMoney = findViewById(R.id.addMoney);
        bonusWallet = (LoaderTextView) findViewById(R.id.bonusWallet);*/
        wallet = (LoaderTextView) findViewById(R.id.wallet);
        playbutton = findViewById(R.id.speenWheel);
        speenWheelback = findViewById(R.id.speenWheelback);

        userid = new PrefManagerUser(GameActivity.this).getuser_id();
        userName = new PrefManagerUser(GameActivity.this).getuser_name();
        mobile_no = new PrefManagerUser(GameActivity.this).getmobile_no();

        if (new PrefManagerSound(GameActivity.this).music().equals("1")) {
            doUnbindService();
            Intent music = new Intent();
            music.setClass(GameActivity.this, MusicService.class);
            stopService(music);
        } else {
            doBindService();
            Intent music = new Intent();
            music.setClass(GameActivity.this, MusicService.class);
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


        Intent intent456 = getIntent();
        if (intent456.getStringExtra("popup") != null) {
            checkConnection(userid);
        } else {
            System.out.println("Sorry no ad!");
        }

        MobileAds.initialize(this); /*Add*/

        /*banner add*/
        mAdview = (AdView) findViewById(R.id.adview);
        final AdRequest adRequest = new AdRequest.Builder().build(); /*TestDevice*/
        mAdview.loadAd(adRequest);


        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-6922743115082307/2523284779");
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
                if (spinAnswer.equals("0")) {
//                    rewardDialogue(R.layout.alert_rewardpopup);
                    //zeroasynk
                    checkConnection1(userid, spinAnswer);
                } else
                    //spinasynk
                    checkConnection2(userid, spinAnswer);
            }
        });

        logo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logoClick(R.layout.alert_popup);
            }
        });

        wallet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(GameActivity.this, Wallet.class));
            }
        });

        /*addMoney.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(GameActivity.this, NoInvProfile.class));
            }
        });*/


        /*------------------------------------LuckyWheel-------------------------------------------------*/
        LuckyItem luckyItem1 = new LuckyItem();
        luckyItem1.secondaryText = "20";
        data1.add(luckyItem1);

        LuckyItem luckyItem2 = new LuckyItem();
        luckyItem2.secondaryText = "5";
        data1.add(luckyItem2);

        LuckyItem luckyItem3 = new LuckyItem();
        luckyItem3.secondaryText = "30";
        data1.add(luckyItem3);

        LuckyItem luckyItem4 = new LuckyItem();
        luckyItem4.secondaryText = "7";
        data1.add(luckyItem4);

        LuckyItem luckyItem5 = new LuckyItem();
        luckyItem5.secondaryText = "40";
        data1.add(luckyItem5);

        LuckyItem luckyItem6 = new LuckyItem();
        luckyItem6.secondaryText = "9";
        data1.add(luckyItem6);

        LuckyItem luckyItem7 = new LuckyItem();
        luckyItem7.secondaryText = "50";
        data1.add(luckyItem7);

        LuckyItem luckyItem8 = new LuckyItem();
        luckyItem8.secondaryText = "0";
        data1.add(luckyItem8);

        LuckyItem luckyItem9 = new LuckyItem();
        luckyItem9.secondaryText = "10";
        data1.add(luckyItem9);

        LuckyItem luckyItem10 = new LuckyItem();
        luckyItem10.secondaryText = "3";
        data1.add(luckyItem10);


        luckyWheelView.setData(data1);
        luckyWheelView.setRound(8);
        luckyWheelView.setLuckyWheelTextColor(0xffcc0000);

        /*------------------------------------MediaPlayer-------------------------------------------------*/

        /*MediaPlayer*/
        mp = MediaPlayer.create(this, R.raw.coins);
        mp1 = MediaPlayer.create(this, R.raw.spin);

        /*------------------------------------playbutton-------------------------------------------------*/
        playbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spinconnection();
            }
        });

        /*------------------------------------luckyWheelView-------------------------------------------------*/


        luckyWheelView.setLuckyRoundItemSelectedListener(new LuckyWheelView.LuckyRoundItemSelectedListener() {
            @Override
            public void LuckyRoundItemSelected(int index) {
                spinAnswer = data1.get(index).secondaryText;
                if (mInterstitialAd.isLoaded()) {
                    mInterstitialAd.show();
                } else {
                    if (spinAnswer.equals("0")) {
//                    rewardDialogue(R.layout.alert_rewardpopup);
                        //zeroasynk
                        checkConnection1(userid, spinAnswer);
                    } else
                        //spinasynk
                        checkConnection2(userid, spinAnswer);
                }
            }
        });
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

    public void spinconnection() {
        if (isOnline()) {
            mInterstitialAd.loadAd(new AdRequest.Builder().build());

            mp.start();
            mp1.start();
            int index = getRandomIndex();
            luckyWheelView.startLuckyWheelWithTargetIndex(index);
            playbutton.setVisibility(View.GONE);
            speenWheelback.setVisibility(View.GONE);
        } else {
            final AlertDialog.Builder builder = new AlertDialog.Builder(GameActivity.this);
            builder.setMessage("No internet connection Found.")
                    .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            spinconnection();
                        }
                    })
                    .setCancelable(false);
            ;
            final AlertDialog dialog = builder.create();
            dialog.show();
        }
    }


    private int getRandomIndex() {
        Random rand = new Random();
        return rand.nextInt(data1.size() - 1) + 0;
    }

    private int getRandomRound() {
        Random rand = new Random();
        return rand.nextInt(10) + 15;
    }
    /*------------------------------------homepage setting popup-----------------------------------*/

    private void logoClick(int layout) {
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

        if (!new PrefManagerSound(GameActivity.this).music().equals("1")) {
            bgm.setDirection(StickySwitch.Direction.RIGHT);
        } else bgm.setDirection(StickySwitch.Direction.LEFT);

        bgm.setOnSelectedChangeListener(new StickySwitch.OnSelectedChangeListener() {
            @Override
            public void onSelectedChange(StickySwitch.Direction direction, String s) {
                if (direction.equals(StickySwitch.Direction.LEFT)) {
                    new PrefManagerSound(GameActivity.this).updateMusic("1");
                    doUnbindService();
                    Intent music = new Intent();
                    music.setClass(GameActivity.this, MusicService.class);
                    stopService(music);
                } else {
                    new PrefManagerSound(GameActivity.this).updateMusic("0");
                    doBindService();
                    Intent music = new Intent();
                    music.setClass(GameActivity.this, MusicService.class);
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
        listAdapter = new CustomAdapter(GameActivity.this, deptList);
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
                return false;
            }
        });
        // setOnGroupClickListener listener for group heading click
        simpleExpandableListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                //get the group header
                GroupInfo headerInfo = deptList.get(groupPosition);
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

    //load some initial data into out list
    private void loadData() {
        addProduct("How to start earning ?", "First download the app from playstore.");
        addProduct("How to start earning ?", "Register yourself in the app using your mobile number which is registered on any of upi platform.");
        addProduct("How to start earning ?", "Invest  One Time PAyment of 1000 ₹ using upi. and that's it !");
        addProduct("How to start earning ?", "Spin daily and collect money in the wallet.");

        addProduct("How to invest the one time payment ?", "Once you register yourself in the app you will be redirected to the game page.");
        addProduct("How to invest the one time payment ?", "On the upper top side there is a button named 'Add Money'. ");
        addProduct("How to invest the one time payment ?", "There are the payment methods shown.");
        addProduct("How to invest the one time payment ?", "On the second tab there is a QR code shown and the UPI also. Use this things to pay the One Time Payment.");
        addProduct("How to invest the one time payment ?", "Don't forget to take screenshot of the transaction and send it to the admin team to approve it.");
        addProduct("How to invest the one time payment ?", "The admin team will approve your payment in 24 working hours.");

        addProduct("Any limits ?", "Yes,A user can earn only 1025 points a day.Not more then that not less then that");
        addProduct("Any limits ?", "But Don't worry.The user can spin more than 1025 points. But the points will be counted as Bonus Wallet.");
        addProduct("Any limits ?", "On the new day the amount will be converted into 0");
        addProduct("Any limits ?", "If the user will earn less then 1025 a day then he will not credited the for that points.");

        addProduct("How to withdraw ?", "On the Upper Top side when the user clicks the wallet,it will be redirected to the wallet page.");
        addProduct("How to withdraw ?", "By clicking on 'Withdrawable Amount' the user can request for withdrawal by using three payment platform defined.");
        addProduct("How to withdraw ?", "User can only request single payment at a time.He/She can not request until the last request will be approved.");

        addProduct("What is Bonus wallet ?", "The bonus wallet can not be withdrawn.");
        addProduct("What is Bonus wallet ?", "On the last day of the month, the top 3 Bonus winners will be gifted a particular amount defined by the admin");
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
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/OneTimeInvestLifeTimeIncome/"));
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

    /*----------------------------------------Profile Popup----------------------------------------*/

    private void nameDialogue(int layout) {
        profilepopup = new AlertDialog.Builder(this);

        View layoutView = getLayoutInflater().inflate(layout, null);

        userNameEdit = layoutView.findViewById(R.id.userName);
        userEmail = layoutView.findViewById(R.id.userEmail);

        profilepopup.setView(layoutView);
        alertDialogProfile = profilepopup.create();
        alertDialogProfile.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialogProfile.setCancelable(false);
        alertDialogProfile.show();
        ElasticImageView submitProfile = layoutView.findViewById(R.id.submitProfile);

        submitProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //alertDialog.dismiss();
                if (userEmail.getText().toString().isEmpty() || userNameEdit.getText().toString().isEmpty()) {
                    Toasty.error(GameActivity.this, "Required Fields", Toast.LENGTH_SHORT, true).show();
                } else {
                    if (userEmail.getText().toString().trim().matches(emailPattern)) {
                        checkConnection4(userid, userNameEdit.getText().toString(), userEmail.getText().toString());
                    } else {
                        Toasty.warning(GameActivity.this, "Please enter valid email address", Toast.LENGTH_SHORT, true).show();
                    }
                }
            }
        });
    }

    private void zeroDialogue(int layout) {
        AlertDialog.Builder zeropopup = new AlertDialog.Builder(this);

        View layoutView = getLayoutInflater().inflate(layout, null);

        zeropopup.setView(layoutView);
        final AlertDialog zeroProfile = zeropopup.create();
        zeroProfile.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        zeroProfile.setCancelable(false);
        zeroProfile.show();
        ElasticImageView submitProfile = layoutView.findViewById(R.id.rewardImage);

        submitProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playbutton.setVisibility(View.VISIBLE);
                speenWheelback.setVisibility(View.VISIBLE);
                if (scratchcard.equals("0")) {
                    startActivity(new Intent(GameActivity.this, RewardList.class));
                    Toasty.success(GameActivity.this, "Don't get disappointed.\nWe have something for you.", Toast.LENGTH_SHORT, true).show();
                }
                zeroProfile.dismiss();
            }
        });
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

    private void rewardDialogue(int layout) {
        AlertDialog.Builder rewardpopup = new AlertDialog.Builder(this);

        View layoutView = getLayoutInflater().inflate(layout, null);

        /*KonfettiView konfettiView = layoutView.findViewById(R.id.viewKonfetti);*/
        ElasticImageView rewardI = layoutView.findViewById(R.id.rewardImage);
        rewardpopup.setView(layoutView);
        alertDialogreward = rewardpopup.create();
        alertDialogreward.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialogreward.setCancelable(false);
        alertDialogreward.show();

        if (spinAnswer.equals("10")) {
            Drawable myDrawable = getResources().getDrawable(R.drawable.reward10);
            rewardI.setImageDrawable(myDrawable);
        } else if (spinAnswer.equals("3")) {
            Drawable myDrawable = getResources().getDrawable(R.drawable.reward3);
            rewardI.setImageDrawable(myDrawable);
        } else if (spinAnswer.equals("20")) {
            Drawable myDrawable = getResources().getDrawable(R.drawable.reward20);
            rewardI.setImageDrawable(myDrawable);
        } else if (spinAnswer.equals("5")) {
            Drawable myDrawable = getResources().getDrawable(R.drawable.reward5);
            rewardI.setImageDrawable(myDrawable);
        } else if (spinAnswer.equals("30")) {
            Drawable myDrawable = getResources().getDrawable(R.drawable.reward30);
            rewardI.setImageDrawable(myDrawable);
        } else if (spinAnswer.equals("7")) {
            Drawable myDrawable = getResources().getDrawable(R.drawable.reward7);
            rewardI.setImageDrawable(myDrawable);
        } else if (spinAnswer.equals("40")) {
            Drawable myDrawable = getResources().getDrawable(R.drawable.reward40);
            rewardI.setImageDrawable(myDrawable);
        } else if (spinAnswer.equals("9")) {
            Drawable myDrawable = getResources().getDrawable(R.drawable.reward9);
            rewardI.setImageDrawable(myDrawable);
        } else if (spinAnswer.equals("50")) {
            Drawable myDrawable = getResources().getDrawable(R.drawable.reward50);
            rewardI.setImageDrawable(myDrawable);
        }

        rewardI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                checkConnection3(userid);
                playbutton.setVisibility(View.VISIBLE);
                speenWheelback.setVisibility(View.VISIBLE);
                alertDialogreward.dismiss();
            }
        });
    }

    public class PROFILEASYNK extends AsyncTask<String, String, String> {
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ACProgressCustom.Builder(GameActivity.this)
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

                json.put("user_id", arg[0]);
                json.put("reg_name", arg[1]);
                json.put("email_address", arg[2]);


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
                new PrefManagerUser(GameActivity.this).updateuser_name(userNameEdit.getText().toString());
                new PrefManagerUser(GameActivity.this).updateemail(userEmail.getText().toString());
                //Toasty.success(GameActivity.this, response_msg, Toast.LENGTH_SHORT, true).show();
                alertDialogProfile.dismiss();
                wdf(R.layout.alert_withdraw);
//                thousandDialogue(R.layout.alert_1000);
            } else {
                Toasty.error(GameActivity.this, "Something went wrong.\nPlease try again later", Toast.LENGTH_SHORT, true).show();
            }
        }
    }

    public void wdf(int layout) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(GameActivity.this);

        View layoutView = getLayoutInflater().inflate(layout, null);


        dialogBuilder.setView(layoutView);
        withdrawProfile = dialogBuilder.create();
        withdrawProfile.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        withdrawProfile.show();
        final ElasticImageView paytm = (ElasticImageView) layoutView.findViewById(R.id.paytm);
        final ElasticImageView bhim = (ElasticImageView) layoutView.findViewById(R.id.bhim);
        final ElasticImageView paypal = (ElasticImageView) layoutView.findViewById(R.id.paypal);
        final EditText upi = (EditText) layoutView.findViewById(R.id.upiid);
        ElasticImageView paymentButton = (ElasticImageView) layoutView.findViewById(R.id.paymentSubmit);


        paytm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selecting = "0";
                paytm.setBackgroundColor(getResources().getColor(R.color.approved));
                bhim.setBackgroundColor(Color.TRANSPARENT);
                paypal.setBackgroundColor(Color.TRANSPARENT);
                upi.setHint("Enter Paytm Number");
            }
        });
        bhim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selecting = "1";
                bhim.setBackgroundColor(getResources().getColor(R.color.approved));
                paytm.setBackgroundColor(Color.TRANSPARENT);
                paypal.setBackgroundColor(Color.TRANSPARENT);
                upi.setHint("Enter UPI details");
            }
        });
        paypal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selecting = "2";
                paypal.setBackgroundColor(getResources().getColor(R.color.approved));
                paytm.setBackgroundColor(Color.TRANSPARENT);
                bhim.setBackgroundColor(Color.TRANSPARENT);
                upi.setHint("Enter PayPal Email id");
            }
        });

        paymentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selecting.equals("4")) {
                    Toasty.error(GameActivity.this, "Please select any of above", Toast.LENGTH_SHORT).show();
                }
                if (TextUtils.isEmpty(upi.getText().toString())) {
//                     loginmobile.setError("Required Field");
                    Toasty.error(GameActivity.this, "Required Field.", Toast.LENGTH_SHORT, true).show();

                }
                checkConnection5(userid, selecting, upi.getText().toString());
            }
        });
    }

    public class ZEROSPINASYNK extends AsyncTask<String, String, String> {
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @SuppressWarnings("ResourceType")
        @Override
        protected String doInBackground(String... arg) {
            try {
                HttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost(ZEROSPINURL);

                httpPost.setHeader("content-type", "application/json");
                JSONObject json = new JSONObject();

                json.put("user_id", arg[0]);
                json.put("spin_amount", arg[1]);


                StringEntity entity = new StringEntity(json.toString(), "UTF_8");
                httpPost.setEntity(entity);

                HttpResponse response = httpClient.execute(httpPost);
                String responseString = EntityUtils.toString(response.getEntity());
                JSONObject responseJSON = new JSONObject(responseString);

                response_code = responseJSON.getString("response_code");
                response_msg = responseJSON.getString("msg");
                todayWalletRes = responseJSON.getString("todayWallet");
                scratchcard = responseJSON.getString("scratchcard");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return response_code;
        }

        @SuppressLint("NewApi")
        protected void onPostExecute(String status) {
            if (status.equals("200")) {
                zeroDialogue(R.layout.alert_failed);
            } else {
                Toasty.error(GameActivity.this, "Something went wrong.\nPlease try again later", Toast.LENGTH_SHORT, true).show();
            }
        }
    }

    public class SPINASYNK extends AsyncTask<String, String, String> {
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @SuppressWarnings("ResourceType")
        @Override
        protected String doInBackground(String... arg) {
            try {
                HttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost(SPINURL);

                httpPost.setHeader("content-type", "application/json");
                JSONObject json = new JSONObject();

                json.put("user_id", arg[0]);
                json.put("spin_amount", arg[1]);


                StringEntity entity = new StringEntity(json.toString(), "UTF_8");
                httpPost.setEntity(entity);

                HttpResponse response = httpClient.execute(httpPost);
                String responseString = EntityUtils.toString(response.getEntity());
                JSONObject responseJSON = new JSONObject(responseString);

                response_code = responseJSON.getString("response_code");
                response_msg = responseJSON.getString("msg");
                todayWalletRes = responseJSON.getString("todayWallet");

            } catch (Exception e) {
                e.printStackTrace();
            }
            return response_code;
        }

        @SuppressLint("NewApi")
        protected void onPostExecute(String status) {
            if (status.equals("200")) {
                rewardDialogue(R.layout.alert_rewardpopup);
                Toasty.success(GameActivity.this, "Reward added successfully", Toast.LENGTH_SHORT, true).show();
            } else {
                Toasty.error(GameActivity.this, "Something went wrong.\nPlease try again later", Toast.LENGTH_SHORT, true).show();
            }
        }
    }

    public class STARTASYNK extends AsyncTask<String, String, String> {
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @SuppressWarnings("ResourceType")
        @Override
        protected String doInBackground(String... arg) {
            try {
                HttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost(STARTURL);

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
                active = responseJSON.getString("active");
                startimg = responseJSON.getString("startimg");

            } catch (Exception e) {
                e.printStackTrace();
            }
            return response_code;
        }

        @SuppressLint("NewApi")
        protected void onPostExecute(String status) {
            if (status.equals("200")) {
                if (active.equals("0")) {
                    startDialogue(R.layout.alert_start, startimg);
                }
            }
        }
    }

    public class WALLETASYNK extends AsyncTask<String, String, String> {
        protected void onPreExecute() {
            super.onPreExecute();
            wallet.resetLoader();
            /*bonusWallet.resetLoader();*/
        }

        @SuppressWarnings("ResourceType")
        @Override
        protected String doInBackground(String... arg) {
            try {
                HttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost(WALLETURL);

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
                todayWallet = responseJSON.getString("todayWallet");
                /*todayDate = responseJSON.getString("todayDate");
                bonus = responseJSON.getString("bonusWallet");*/
                /*pendingamount = responseJSON.getString("pendingamount");*/
                popuptraker = responseJSON.getString("popuptraker");

            } catch (Exception e) {
                e.printStackTrace();
            }
            return response_code;
        }

        @SuppressLint("NewApi")
        protected void onPostExecute(String status) {
            if (status.equals("200")) {

                if (popuptraker.equals("1")) {
                    nameDialogue(R.layout.alert_name);
                } else if (popuptraker.equals("2")) {
                    wdf(R.layout.alert_withdraw);
                }

                /*if (Integer.parseInt(pendingamount) != 0) {
                 *//*bonusWallet.setVisibility(View.GONE);
                    addMoney.setVisibility(View.VISIBLE);*//*
                    wallet.setVisibility(View.GONE);
                } else {*/
                /*addMoney.setVisibility(View.GONE);*/
                wallet.setText(todayWallet);
                wallet.setVisibility(View.VISIBLE);
//                    bonusWallet.setText(bonus);
                /*bonusWallet.setText(prettyCount(Integer.parseInt(bonus)));*/
                /*}*/
            } else {
                Toasty.error(GameActivity.this, "Oops...Something went wrong", Toast.LENGTH_SHORT, true).show();
            }
        }
    }

    public class WITHDRAWDATA extends AsyncTask<String, String, String> {
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @SuppressWarnings("ResourceType")
        @Override
        protected String doInBackground(String... arg) {
            try {
                HttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost(UPDATEUPI);

                httpPost.setHeader("content-type", "application/json");
                JSONObject json = new JSONObject();

                json.put("user_id", arg[0]);
                json.put("via", arg[1]);
                json.put("upi", arg[2]);

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
                withdrawProfile.dismiss();
            } else {
                Toasty.error(GameActivity.this, "Oops...Something went wrong", Toast.LENGTH_SHORT, true).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toasty.info(GameActivity.this, "Please click BACK again to exit", Toast.LENGTH_SHORT, true).show();
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
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
        checkConnection3(userid);
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
        super.onDestroy();//UNBIND music service

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
            new STARTASYNK().execute(a);
        } else {
            final AlertDialog.Builder builder = new AlertDialog.Builder(GameActivity.this);
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
            new ZEROSPINASYNK().execute(b, c);
        } else {
            final AlertDialog.Builder builder = new AlertDialog.Builder(GameActivity.this);
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

    public void checkConnection2(final String d, final String e) {
        if (isOnline()) {
            new SPINASYNK().execute(d, e);
        } else {
            final AlertDialog.Builder builder = new AlertDialog.Builder(GameActivity.this);
            builder.setMessage("No internet connection Found.")
                    .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            checkConnection2(d, e);
                        }
                    })
                    .setCancelable(false);
            ;
            final AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    public void checkConnection3(final String f) {
        if (isOnline()) {
            new WALLETASYNK().execute(f);
        } else {
            final AlertDialog.Builder builder = new AlertDialog.Builder(GameActivity.this);
            builder.setMessage("No internet connection Found.")
                    .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            checkConnection3(f);
                        }
                    })
                    .setCancelable(false);
            ;
            final AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    public void checkConnection4(final String g, final String h, final String i) {
        if (isOnline()) {
            new PROFILEASYNK().execute(g, h, i);
        } else {
            final AlertDialog.Builder builder = new AlertDialog.Builder(GameActivity.this);
            builder.setMessage("No internet connection Found.")
                    .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            checkConnection4(g, h, i);
                        }
                    })
                    .setCancelable(false);
            ;
            final AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    public void checkConnection5(final String j, final String k, final String l) {
        if (isOnline()) {
            new WITHDRAWDATA().execute(j, k, l);
        } else {
            final AlertDialog.Builder builder = new AlertDialog.Builder(GameActivity.this);
            builder.setMessage("No internet connection Found.")
                    .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            checkConnection5(j, k, l);
                        }
                    })
                    .setCancelable(false);
            ;
            final AlertDialog dialog = builder.create();
            dialog.show();
        }
    }
}
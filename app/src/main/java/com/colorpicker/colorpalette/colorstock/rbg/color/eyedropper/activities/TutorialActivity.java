package com.colorpicker.colorpalette.colorstock.rbg.color.eyedropper.activities;


import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.colorpicker.colorpalette.colorstock.rbg.color.eyedropper.Ads_Util.InAppBilling;
import com.colorpicker.colorpalette.colorstock.rbg.color.eyedropper.Ads_Util.MyApplication;
import com.colorpicker.colorpalette.colorstock.rbg.color.eyedropper.R;


public class TutorialActivity extends AppCompatActivity {
    ImageView tutorialImg;
    TextView mainText, finishtxt;
    CardView nextbtn, dot1, dot2, dot3;
    int clickcount;
    ProgressDialog progressDialog;
    boolean connected, isInAppPurchased;
    Dialog tutorialDialog, loadindAdDialogue;
    InAppBilling inAppBilling;
    TextView loadingTxt;
    private static final int PERMISSION_REQUEST_SET_WALLPAPER = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);
        setContentView(R.layout.activity_tutorial);
        i9nitview();
        inAppBilling = new InAppBilling(this, this);
        isInAppPurchased = inAppBilling.hasUserBoughtInApp();
        clickcount = 1;
        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.button_scale);
        animation.setRepeatCount(Animation.INFINITE);
        animation.setRepeatMode(Animation.REVERSE);
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        connected = (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED);
        nextbtn.setOnClickListener(view -> {
            if (clickcount == 1) {
                tutorialImg.setBackgroundResource(R.drawable.tutorial_2);
//                mainText.setText(getString(R.string.tut_2));
                dot1.setCardBackgroundColor(ContextCompat.getColor(this, R.color.dot_unsel));
                dot2.setCardBackgroundColor(ContextCompat.getColor(this, R.color.dot_sel));
                dot3.setCardBackgroundColor(ContextCompat.getColor(this, R.color.dot_unsel));
                clickcount++;
            } else if (clickcount == 2) {
                tutorialImg.setBackgroundResource(R.drawable.tutorial_3);
//                mainText.setText(getString(R.string.tut_3));
                dot1.setCardBackgroundColor(ContextCompat.getColor(this, R.color.dot_unsel));
                dot2.setCardBackgroundColor(ContextCompat.getColor(this, R.color.dot_unsel));
                dot3.setCardBackgroundColor(ContextCompat.getColor(this, R.color.dot_sel));
                finishtxt.setText("Finish");
                clickcount++;
            } else if (clickcount == 3) {
                clickcount = 0;
                if (connected && !isInAppPurchased) {
                    showAdLoadingDialogue();

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            showAppOpenAd();
                        }
                    }, 3000L);
                } else {
                    startMainActivity();
                }

            }
        });
        nextbtn.startAnimation(animation);
    }

    @Override
    protected void onResume() {
        super.onResume();
        inAppBilling = new InAppBilling(this, this);
        isInAppPurchased = inAppBilling.hasUserBoughtInApp();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (loadindAdDialogue != null && loadindAdDialogue.isShowing()) {
            loadindAdDialogue.dismiss();
        }
    }

    private void showAppOpenAd() {
        Application application = getApplication();

        // If the application is not an instance of MyApplication, log an error message and
        // start the MainActivity without showing the app open ad.
        if (!(application instanceof MyApplication)) {
            Log.e("LOG_TAG", "Failed to cast application to MyApplication.");

            startMainActivity();
            return;
        }

        // Show the app open ad.
        ((MyApplication) application)
                .showAdIfAvailable(
                        TutorialActivity.this,
                        new MyApplication.OnShowAdCompleteListener() {
                            @Override
                            public void onShowAdComplete() {
                                loadingTxt.setText("Loading ....");

                                startMainActivity();
                            }
                        });
    }

    private void startMainActivity() {
//        startActivity(new Intent(this, MainActivity.class));

        Intent intent = new Intent(this
                , MainActivity.class);
        startActivity(intent);

    }

    private void i9nitview() {
        tutorialImg = findViewById(R.id.mainimg);

        nextbtn = findViewById(R.id.nextBtn);
        tutorialImg.setBackgroundResource(R.drawable.tutorial_1);
        dot1 = findViewById(R.id.card1);
        dot2 = findViewById(R.id.card2);
        dot3 = findViewById(R.id.card3);
        finishtxt = findViewById(R.id.nextBtntxt);
        dot1.setCardBackgroundColor(ContextCompat.getColor(this, R.color.dot_sel));
        dot2.setCardBackgroundColor(ContextCompat.getColor(this, R.color.dot_unsel));
        dot3.setCardBackgroundColor(ContextCompat.getColor(this, R.color.dot_unsel));
//        mainText.setVisibility(View.GONE);
    }

    private void showAdLoadingDialogue() {

        loadindAdDialogue = new Dialog(this, android.R.style.Theme_Translucent_NoTitleBar);
        View view1 = getLayoutInflater().inflate(R.layout.loading_layout, null);
        loadindAdDialogue.requestWindowFeature(Window.FEATURE_NO_TITLE);
        loadindAdDialogue.setContentView(view1);
        Window window = loadindAdDialogue.getWindow();
        loadingTxt = view1.findViewById(R.id.loadingtxt);
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.CENTER;
        wlp.flags &= ~WindowManager.LayoutParams.FLAG_BLUR_BEHIND;
        window.setAttributes(wlp);
        loadindAdDialogue.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        loadindAdDialogue.show();
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
//        finishAffinity();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (loadindAdDialogue == null) {
            finishAffinity();
        }
    }

   /* private ActivityResultLauncher<Intent> resultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent intent = new Intent(this, MainActivity.class);
                    startActivity(intent);
                }
            }
    );*/

}
package com.markelys.viewpay;

import androidx.annotation.NonNull;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdCallback;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.markelys.viewpay.R;
import android.app.Activity;

public class ViewPayAdMobActivity extends Activity {

    private static final String TAG = "ViewPayAdMobActivity";
    private RewardedAd rewardedAd;
    protected ViewPayDataManager data;
    private String adMobId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        data = ViewPayDataManager.getInstance();
        setContentView(R.layout.viewpay_layout);
        loadAd();
    }

    public void loadAd(){
        Bundle extras = getIntent().getExtras();
        adMobId = extras.getString("adMobId");
         this.rewardedAd = new RewardedAd(this, adMobId);
         RewardedAdLoadCallback callback = new RewardedAdLoadCallback(){
             @Override
             public void onRewardedAdLoaded(){
                 super.onRewardedAdLoaded();
                 Log.i(TAG, "onRewardedAdLoaded");
                 showAd();
             }

             @Override
             public void onRewardedAdFailedToLoad(int i){
                super.onRewardedAdFailedToLoad(i);
                Toast.makeText(getApplicationContext(), "LOAD AD is failing", Toast.LENGTH_LONG).show();
                Log.i(TAG, "onRewardedAdFailedToLoad");
                ViewPayEventsListener view = (ViewPayEventsListener)data.getAppContext();
                view.errorVP();
                finish();
             }
        };
        this.rewardedAd.loadAd(new AdRequest.Builder().build(), callback);
    }

    public void showAd(){
        if(this.rewardedAd.isLoaded()){
            RewardedAdCallback callback = new RewardedAdCallback() {
                boolean completed = false;
                @Override
                public void onRewardedAdOpened(){
                    super.onRewardedAdOpened();
                    Log.i(TAG, "onRewardedAdOpened");
                }
                @Override
                public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                    completed = true;
                    ViewPay.wsTracking(8,adMobId);
                    Log.i(TAG, "onUserEarnedReward");
                }
                @Override
                public void onRewardedAdClosed(){
                    super.onRewardedAdClosed();
                    finish();
                    Log.i(TAG, "onRewardedAdClosed");
                    ViewPayEventsListener view = (ViewPayEventsListener)data.getAppContext();
                    if(completed){
                        view.completeAdsVP();
                    }
                    else{
                        view.closeAdsVP();
                    }
                }
                @Override
                public void onRewardedAdFailedToShow(int i){
                    super.onRewardedAdFailedToShow(i);
                    Log.i(TAG, "onRewardedAdFailedToShow");
                    ViewPayEventsListener view = (ViewPayEventsListener)data.getAppContext();
                    view.errorVP();
                    finish();
                }
            };
            this.rewardedAd.show(this, callback);
        } else {
            Log.i(TAG, "Ad not loaded!!");
            ViewPayEventsListener view = (ViewPayEventsListener)data.getAppContext();
            view.errorVP();
            finish();
        }
    }
}
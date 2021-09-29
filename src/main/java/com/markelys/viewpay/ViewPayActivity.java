package com.markelys.viewpay;

/**
 * Created by Herbert TOMBO on 02/02/2018.
 */

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import android.app.Fragment;

import com.google.ads.interactivemedia.v3.api.AdDisplayContainer;
import com.google.ads.interactivemedia.v3.api.AdError;
import com.google.ads.interactivemedia.v3.api.AdErrorEvent;
import com.google.ads.interactivemedia.v3.api.AdEvent;
import com.google.ads.interactivemedia.v3.api.AdsLoader;
import com.google.ads.interactivemedia.v3.api.AdsManager;
import com.google.ads.interactivemedia.v3.api.AdsManagerLoadedEvent;
import com.google.ads.interactivemedia.v3.api.AdsRenderingSettings;
import com.google.ads.interactivemedia.v3.api.AdsRequest;
import com.google.ads.interactivemedia.v3.api.ImaSdkFactory;
import com.google.ads.interactivemedia.v3.api.ImaSdkSettings;
import com.google.ads.interactivemedia.v3.api.AdErrorEvent.AdErrorListener;
import com.google.ads.interactivemedia.v3.api.AdEvent.AdEventListener;
import com.google.ads.interactivemedia.v3.api.AdsLoader.AdsLoadedListener;
import com.google.ads.interactivemedia.v3.api.UiElement;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static android.content.ContentValues.TAG;
import static com.markelys.viewpay.ViewPayConstants.CLICK_CTA2;
import static com.markelys.viewpay.ViewPayConstants.ERROR;
import static com.markelys.viewpay.ViewPayConstants.HIDE_BTN_CLOSE;
import static com.markelys.viewpay.ViewPayConstants.SHOW_BTN_CLOSE;
import static com.markelys.viewpay.ViewPayConstants.SUCCES;

public class  ViewPayActivity extends Activity {

    protected ViewPayDataManager data;

    AdsRequest request;

    private LinearLayout closeContainer;
    private LinearLayout popup;
    private Button viewpage;
    private Button btnAbandonner;
    private Button btnContinuer;
    private WebView viewpayView;

    boolean isCompleted =false;

    int count=0;
    boolean isAdselectorVisible = false;

    //membre IMA SDK
    // The video player.
    private VideoPlayer mVideoPlayer;
    // The container for the ad's UI.
    private ViewGroup mAdUiContainer;
    // Factory class for creating SDK objects.
    private ImaSdkFactory mSdkFactory;
    // The AdsLoader instance exposes the requestAds method.
    private AdsLoader mAdsLoader;
    // AdsManager exposes methods to control ad playback and listen to ad events.
    private AdsManager mAdsManager;
    private  VideoListner listner;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.viewpay_layout);
        data = ViewPayDataManager.getInstance();
        viewpayView = findViewById(R.id.content);

        viewpayView.setWebChromeClient(new WebChromeClient());
        viewpayView.getSettings().setJavaScriptEnabled(true);
        viewpayView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        viewpayView.clearCache(false);
        WebChromeClientCustomPoster chromeClient = new WebChromeClientCustomPoster();
        viewpayView.setWebChromeClient(chromeClient);
        viewpayView.addJavascriptInterface(new ViewpayWebInterface(), "Android");

        viewpayView.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedError(WebView view, int errorCode, String description,
                                        String failingUrl) {
                // Handle the error
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView webView, String url) {
                super.onPageFinished(webView, url);
            }

        });


        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            viewpayView.loadUrl(extras.getString("url"));
        }

        popup= findViewById(R.id.popup);

        closeContainer= findViewById(R.id.closeContainer);
        closeContainer.setOnClickListener(new ImageButton.OnClickListener() {

            @Override
            public void onClick(View v) {
                popup.setVisibility(View.VISIBLE);
            }
        });

        viewpage = findViewById(R.id.viewpage);
        viewpage.setOnClickListener(new ImageButton.OnClickListener() {

            @Override
            public void onClick(View v) {
                ViewPayEventsListener view = (ViewPayEventsListener)data.getAppContext();
                view.completeAdsVP();
                finish();
            }
        });

        btnAbandonner = findViewById(R.id.btnAbandonner);
        btnAbandonner.setOnClickListener(new ImageButton.OnClickListener() {

            @Override
            public void onClick(View v) {
                popup.setVisibility(View.GONE);
                isCompleted=false;
                ViewPayEventsListener vp = (ViewPayEventsListener)data.getAppContext();
                vp.closeAdsVP();
                finish();
            }
        });

        btnContinuer = findViewById(R.id.btnContinuer);
        btnContinuer.setOnClickListener(new ImageButton.OnClickListener() {

            @Override
            public void onClick(View v) {
                popup.setVisibility(View.GONE);

            }
        });

        // Create an AdsLoader.
        mAdUiContainer = (ViewGroup) findViewById(R.id.videoPlayerWithAdPlayback);
        mSdkFactory = ImaSdkFactory.getInstance();


        AdDisplayContainer adDisplayContainer = mSdkFactory.createAdDisplayContainer();
        adDisplayContainer.setAdContainer(mAdUiContainer);
        ImaSdkSettings settings = mSdkFactory.createImaSdkSettings();
        settings.setPlayerType("google/gmf-player");
        settings.setPlayerVersion("1.0.0");

        mAdsLoader = mSdkFactory.createAdsLoader(
                this.getApplicationContext(), settings, adDisplayContainer);


        // Add listeners for when ads are loaded and for errors.
        listner = new VideoListner();
        mAdsLoader.addAdErrorListener(listner);
        mAdsLoader.addAdsLoadedListener(new AdsLoadedListener() {
            @Override
            public void onAdsManagerLoaded(AdsManagerLoadedEvent adsManagerLoadedEvent) {
                // Ads were successfully loaded, so get the AdsManager instance. AdsManager has
                // events for ad playback and errors.
                mAdsManager = adsManagerLoadedEvent.getAdsManager();
                // Attach event and error event listeners.
                mAdsManager.addAdErrorListener(listner);
                mAdsManager.addAdEventListener(listner);
                AdsRenderingSettings adsRenderingSettings = mSdkFactory.createAdsRenderingSettings();
                Set set = new HashSet();

                //set.add(UiElement.AD_ATTRIBUTION);
                //set.add(UiElement.COUNTDOWN);
                adsRenderingSettings.setUiElements(set);
                mAdsManager.init(adsRenderingSettings);
            }
        });
        mVideoPlayer = (VideoPlayer) findViewById(R.id.sampleVideoPlayer);
        mVideoPlayer.setVisibility(View.INVISIBLE);
    }

    static String urlvast = "";

    public void playVideoVast(String url){
        urlvast = url;
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                viewpayView.setVisibility(View.INVISIBLE);
                mVideoPlayer.setVisibility(View.VISIBLE);
                requestAds(urlvast);
            }
        });
    }

    public void endVideoVast(){
        mVideoPlayer.setVisibility(View.INVISIBLE);
        mAdUiContainer.setVisibility(View.INVISIBLE);
        viewpayView.setVisibility(View.VISIBLE);
        viewpayView.loadUrl("javascript:endVideoNative()");
    }

    public void sendProgressEvent(int type){
        viewpayView.loadUrl("javascript:loadStatisticalNative("+type+")");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if(!isCompleted){
            popup.setVisibility(View.VISIBLE);
        }else{
            ViewPayEventsListener view = (ViewPayEventsListener)data.getAppContext();
            view.completeAdsVP();
            finish();
        }
    }
    private class ViewpayWebInterface {

        final Handler _handler = new Handler();
        String message="";

        ViewpayWebInterface() {
        }

        @JavascriptInterface
        public void sendMessageVP(String msg) {
            message = msg;
            _handler.post(_onUpdateUi);
        }

        @JavascriptInterface
        public void playVideoVastNative(String url){
            ViewPayActivity.this.playVideoVast(url);

        }

        @JavascriptInterface
        public void openOnBrowser(final String url) {
            if(isCompleted){
                Runnable onOpenBrowser= new Runnable() {
                    public void run() {
                        ViewPayEventsListener vp = (ViewPayEventsListener)data.getAppContext();
                        vp.completeAdsVP();
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        ViewPayActivity.this.startActivity(browserIntent);
                        finish();
                    }
                };
                _handler.post(onOpenBrowser);
            }else{
                Runnable onOpenBrowser= new Runnable() {
                    public void run() {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        ViewPayActivity.this.startActivity(browserIntent);
                    }
                };
                _handler.post(onOpenBrowser);
            }

        }

        final Runnable _onUpdateUi= new Runnable() {
            public void run() {
                updateUI();
            }
        };

        private void updateUI() {
            ViewPayEventsListener vp = (ViewPayEventsListener)data.getAppContext();

            if(message.toLowerCase().equals(HIDE_BTN_CLOSE)){
                closeContainer.setVisibility(View.GONE);
            }else if(message.toLowerCase().equals(SHOW_BTN_CLOSE)){
                closeContainer.setVisibility(View.VISIBLE);
            }else if(message.toLowerCase().equals(SUCCES)){
                if(!TextUtils.isEmpty(data.getAccessMessage()))
                    viewpage.setText(data.getAccessMessage());
                viewpage.setVisibility(View.VISIBLE);
                isCompleted = true;
            }else if(message.toLowerCase().equals(ERROR)){
                vp.errorVP();
                finish();
            }else if(message.toLowerCase().equals(CLICK_CTA2)){
                //viewpage.setText(CLICK_CTA2);
                viewpage.setVisibility(View.VISIBLE);
                vp.completeAdsVP();
                finish();
            }
        }
    }

    public void requestAds(String adTagUrl) {
        // Create the ads request.
        request = mSdkFactory.createAdsRequest();
        request.setAdTagUrl(adTagUrl);
        mAdsLoader.requestAds(request);
        /*
        mAdsLoader.addAdsLoadedListener(new AdsLoadedListener() {
            @Override
            public void onAdsManagerLoaded(AdsManagerLoadedEvent adsManagerLoadedEvent) {
                Log.e("VP AdsResponse ",">>>>>>>>>>>>>> Response : " + request.getAdsResponse());
            }
        });*/
    }

    private class WebChromeClientCustomPoster extends WebChromeClient {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            if(isAdselectorVisible){
                //gifView.setVisibility(View.VISIBLE);
                super.onProgressChanged(view, newProgress);
                if (newProgress >= 100) {
                   // gifView.setVisibility(View.GONE);
                    isAdselectorVisible=false;
                }
            }else{
                //gifView.setVisibility(View.GONE);
            }
        }

        @Override
        public Bitmap getDefaultVideoPoster() {
            return Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888);
        }

        @Override
        public View getVideoLoadingProgressView() {
            return super.getVideoLoadingProgressView();

        }
    }

    private class VideoListner implements AdEventListener, AdErrorListener {
        @Override
        public void onAdEvent(AdEvent adEvent) {
            Log.e("VP EVENT VAST",">>>>>>>>>>>>>> EVENT : Type =" + adEvent.getType());
            switch (adEvent.getType()) {
                case LOADED:
                    closeContainer.setVisibility(View.GONE);
                    mAdsManager.start();
                    break;
                case CONTENT_PAUSE_REQUESTED:
                    mVideoPlayer.pause();
                    break;
                case CONTENT_RESUME_REQUESTED:
                    mVideoPlayer.play();
                    break;
                case STARTED :
                    sendProgressEvent(6);
                    break;
                case FIRST_QUARTILE :
                    sendProgressEvent(9);
                    break;
                case MIDPOINT :
                    sendProgressEvent(10);
                    break;
                case THIRD_QUARTILE :
                    sendProgressEvent(11);
                    break;
                case SKIPPED :
                    sendProgressEvent(12);
                    break;
                case ALL_ADS_COMPLETED:
                    sendProgressEvent(7);
                    endVideoVast();
                    if (mAdsManager != null) {
                        mAdsManager.destroy();
                        mAdsManager = null;
                    }
                    break;
                default:
                    break;
            }
        }
        @Override
        public void onAdError(AdErrorEvent adErrorEvent) {
            AdError error = adErrorEvent.getError();
            // pour afficher plus de log
            //adErrorEvent.getError().printStackTrace();
            Log.e("VP ERROR VAST ",">>>>>>>>>>>>>> ERROR : code =" + error.getErrorCode()+",message="+error.getMessage());
            System.out.println(">>>>>>>>>>>>>> ERROR :code="+ error.getErrorCode()+",message="+error.getMessage()+",codeNumber="+error.getErrorCodeNumber()+",localisation="+
                    error.getLocalizedMessage()+",type="+
                    error.getErrorType()+",cause="+error.getCause());
            //mAdsManager.start();
            mVideoPlayer.play();
        }
    }
}
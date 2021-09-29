package com.markelys.viewpay;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.StrictMode;
import android.provider.Settings;
import android.security.NetworkSecurityPolicy;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Locale;
import java.util.Scanner;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.ads.identifier.AdvertisingIdClient.Info;

import static com.markelys.viewpay.ViewPayConstants.VP_AD_SELECTOR_PATH;
import static com.markelys.viewpay.ViewPayConstants.VP_CHECK_VIDEO_PATH;
import static com.markelys.viewpay.ViewPayConstants.VP_CONFIG_URL;
import static com.markelys.viewpay.ViewPayConstants.VP_TRACKING_PATH;


/**
 * Created by Herbert TOMBO on 24/01/2018.
 */

public class ViewPay {

    static protected ViewPayDataManager data;

    static private Context ctx;
    static final int IS_CHECK_VIDEO = 1;
    static final int IS_ADS = 2;
    static final int IS_TRACKING = 3;

    static boolean backfillTimeout = false;
    static boolean backfillSuccess = false;

    public static ViewPay init(Context context,String accountID){
        return new ViewPay(context, accountID);
    }

    private ViewPay(Context context,String accountID){

        ctx = context;

        data = ViewPayDataManager.getInstance();

        data.setAppContext(ctx);

        ViewPayLocationManager viewpayLocationManager = new ViewPayLocationManager(context);

        data.setAccountID(accountID);

        //get device language
        String lang = Locale.getDefault().getDisplayLanguage();

        //get device type
        boolean isTablet = context.getResources().getBoolean(R.bool.is_tablet);
        data.setTablet(isTablet);

        //get android device_id
        String android_id = Settings.Secure.getString(context.getContentResolver(),Settings.Secure.ANDROID_ID);
        data.setHostID(android_id);

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                updateDataAdversiting(ViewPay.data,ViewPay.ctx);
            }
        });
        thread.start();

        getConfigFromJson(VP_CONFIG_URL,accountID);

    }

    public static void setUserGender(String gender){
        data.setGenre(gender);
    }

    public static void setUserAge(int age){
        data.setUserAge(age);
    }

    public static void setCountry(String country){
        data.setCountry(country);
    }

    public static void setLanguage(String lang){
        data.setLanguage(lang);
    }

    public static void setPostalCode(String code){
        data.setPostalCode(code);
    }

    public static void setCategorie(String categorie){
        data.setCategorie(categorie);
    }

    public static void presentAd() {
        if(data.getAdMobId()==null || data.getAdMobId().equals("")){
            Intent viewpayIntent = new Intent (ctx, ViewPayActivity.class);
            viewpayIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Bundle dataBundle = new Bundle();
            String url = fullUrl(IS_ADS);
            System.out.println("*******>"+ url);
            dataBundle.putString("url", url);
            viewpayIntent.putExtras(dataBundle);
            viewpayIntent.putExtra("calling_activity",100);
            ctx.startActivity(viewpayIntent);
        }
        else{
            Intent viewpayIntent = new Intent (ctx, ViewPayAdMobActivity.class);
            viewpayIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Bundle dataBundle = new Bundle();
            dataBundle.putString("adMobId", data.getAdMobId());
            viewpayIntent.putExtras(dataBundle);
            viewpayIntent.putExtra("calling_activity",100);
            ctx.startActivity(viewpayIntent);
        }
    }

    public static void checkVideo(){
        checkVideoWS(fullUrl(IS_CHECK_VIDEO));
    }

    private static String fullUrl(int type){
        String param="?version="+ViewPayConstants.VERSION;

        if(!TextUtils.isEmpty(data.getHostID()))
            param=param+"&hostId="+data.getHostID();

        if(!TextUtils.isEmpty(data.getAccountID())) {
            if (type == IS_TRACKING) {
                param = param + "&webEditor=" + data.getAccountID();
            }
            else{
                param = param + "&id=" + data.getAccountID();
            }
        }
        if(type != IS_CHECK_VIDEO){
            param=param+"&price=0.5&mobile&noInterface";

            if(!TextUtils.isEmpty(data.getCvID()))
                param = param +"&cvid="+data.getCvID();
        }

        if(data.isTablet()) {
            param = param + "&typeDevice=2";
        }else{
            param = param + "&typeDevice=1";
        }

        if(data.getAdvestisingId()!=null){
            param = param+"&advertisingID="+data.getAdvestisingId();
        }

        if(data.getOptout()!=null){
            param = param+"&optout="+data.getOptout();
        }

        param = param+"&mobileOS=2&me";

        if(data.getUserAge()>0)
            param = param+"&a="+data.getUserAge();

        if(!TextUtils.isEmpty(data.getGenre()))
            param = param + "&s="+data.getGenre();

        if(!TextUtils.isEmpty(data.getLanguage()))
            param = param + "&language="+ data.getLanguage();

        if(!TextUtils.isEmpty(data.getCountry()))
            param = param +"&country="+data.getCountry();

        if(!TextUtils.isEmpty(data.getLatitude()))
            param = param +"&gps_x="+data.getLatitude();

        if(!TextUtils.isEmpty(data.getLongitude()))
            param = param +"&gps_y="+data.getLongitude();

        if(!TextUtils.isEmpty(data.getPostalCode()))
            param = param +"&postcode="+data.getPostalCode();

        if(!TextUtils.isEmpty(data.getCategorie()))
            param = param +"&c="+data.getCategorie();

        if(type != IS_CHECK_VIDEO && !TextUtils.isEmpty(data.getUrlAdex())) {
            param = param + "&urlAdex=" + Base64.encodeToString(data.getUrlAdex().getBytes(),Base64.URL_SAFE);
            param = param + "&adexSelected=" + data.getAdexId();
        }

        String _fullUrl="";
        if(type == IS_ADS) {
            _fullUrl = data.getServerUrl() + VP_AD_SELECTOR_PATH + param;
            Log.d("adselector url : ", _fullUrl);
        }else if(type == IS_CHECK_VIDEO){
            _fullUrl = data.getServerUrl()+VP_CHECK_VIDEO_PATH+param;
            Log.d("check video url : ",_fullUrl);
        }
        else if(type == IS_TRACKING){
            _fullUrl = data.getServerUrl()+VP_TRACKING_PATH+param;
        }

        return _fullUrl;
    }

    private static boolean getConfigFromJson(String _url,String accountId) {
        ViewPayEventsListener vp = (ViewPayEventsListener)ctx;

        //if(Utils.isVestionBiggerThan(8))
        _url = Utils.urlToHTTPS(_url);

        if (Integer.parseInt(Build.VERSION.SDK)
                < Build.VERSION_CODES.FROYO) {
            System.setProperty("http.keepAlive", "false");
        }

        if (android.os.Build.VERSION.SDK_INT > 9)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        Log.i(">>>>","appel url config.json="+_url);
        HttpURLConnection urlConnection = null;
        try {
            Log.d("URL CONFIG :", _url);
            URL urlToRequest = new URL(_url);
            urlConnection = (HttpURLConnection)urlToRequest.openConnection();
            urlConnection.setConnectTimeout(10000);
            urlConnection.setReadTimeout(10000);
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            String result = new Scanner(in).useDelimiter("\\A").next();
            Log.i(">>>>","resultat="+result);
            Log.d("WS URL", _url);
            if (result != null) {
                result = result.replace("\n","");
                result = result.replace("\r","");
                Log.d("WS OUTPUT", result);

                try {
                    JSONObject jsonObj = new JSONObject(result);
                    Log.i(">>>>","jsonObj="+jsonObj);
                    String prod ="";
                    if(jsonObj.has("prod")){
                        prod = jsonObj.getString("prod");
                        if(!prod.contains("http:")){
                            prod = "http://"+prod;
                        }
                        data.setServerUrl(prod);
                        Log.i(">>>>","prod="+prod);
                    }

                    String preprod="";
                    if(jsonObj.has("preprod")){
                        preprod = jsonObj.getString("preprod");
                        if(!preprod.contains("http:")){
                            preprod = "http://"+preprod;
                            Log.i(">>>>","preprod="+preprod);
                        }
                    }

                    JSONArray editorPrepro = new JSONArray();
                    if(jsonObj.has("editorPrepro")){
                        editorPrepro = jsonObj.getJSONArray("editorPrepro");
                        for (int i = 0; i < editorPrepro.length(); i++) {
                            if(accountId.equals(editorPrepro.get(i))){
                                data.setServerUrl(preprod);
                                return true;
                            }
                        }
                    }

                    if(jsonObj.has("timeOutBackfill")){
                        int timeOutBackfill =  jsonObj.getInt("timeOutBackfill");
                        data.setTimeoutBackfill(timeOutBackfill);
                    }
                } catch (JSONException e) {
                    Log.i(">>>>","JSONException="+e.getMessage());
                    e.printStackTrace();
                    return false;
                }
            } else {
                return false;
            }

        } catch (MalformedURLException e) {
            return false;
        } catch (SocketTimeoutException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }

        return false;
    }

    private static void checkVideoWS(String _url) {
        ViewPayEventsListener vp = (ViewPayEventsListener)ctx;

        if (Integer.parseInt(Build.VERSION.SDK)
                < Build.VERSION_CODES.FROYO) {
            System.setProperty("http.keepAlive", "false");
        }
        if (android.os.Build.VERSION.SDK_INT > 9)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        Log.i(">>>>","_url checkVIdeo="+_url);
        HttpURLConnection urlConnection = null;
        try {
            URL urlToRequest = new URL(_url);
            urlConnection = (HttpURLConnection)urlToRequest.openConnection();
            urlConnection.setConnectTimeout(10000);
            urlConnection.setReadTimeout(10000);
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            String result = new Scanner(in).useDelimiter("\\A").next();

            if (result != null) {
                result = result.replace("\n","");
                result = result.replace("\r","");
                Log.d("WS OUTPUT", result);

                try {
                    JSONObject jsonObj = new JSONObject(result);

                    int nbVideo = 0;
                    if(jsonObj.has("nbVideo")){
                        nbVideo = jsonObj.getInt("nbVideo");
                    }

                    String freeCamp ="";
                    if(jsonObj.has("freeCamp")){
                        freeCamp = jsonObj.getString("freeCamp");
                    }
                    data.setFreeCampState(freeCamp);


                    int activeAdex = 0;
                    String cvid="";
                    if(jsonObj.has("cvid")){
                        cvid = jsonObj.getString("cvid");
                        data.setCvID(cvid);
                    }

                    String accessMessage="";
                    if(jsonObj.has("labelValidate")){
                        accessMessage = jsonObj.getString("labelValidate");
                        data.setAccessMessage(accessMessage);
                    }


                    if(jsonObj.has("activeAdex")){
                        activeAdex = jsonObj.getInt("activeAdex");
                    }

                    if(nbVideo>0){
                        data.setAdMobId(null);
                        vp.checkVideoSuccesVP();
                    }
                    else if(jsonObj.has("adMobId") && !"".equals(jsonObj.getString("adMobId"))){
                        data.setAdMobId(jsonObj.getString("adMobId"));
                        vp.checkVideoSuccesVP();
                    }
                    else if(activeAdex==1){
                        JSONArray array = jsonObj.getJSONArray("adexs");
                        checkBackfillVideo(ctx,array,freeCamp);
                    }
                    else if(freeCamp.toLowerCase().equals("ok")){
                        vp.checkVideoSuccesVP();
                    }else{
                        vp.checkVideoErrorVP();
                    }
                } catch (JSONException e) {
                    vp.checkVideoErrorVP();
                }
            } else {
                vp.checkVideoErrorVP();
            }

        } catch (MalformedURLException e) {
            vp.checkVideoErrorVP();
        } catch (SocketTimeoutException e) {
            vp.checkVideoErrorVP();
        } catch (IOException e) {
            vp.checkVideoErrorVP();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }

        }
    }

    /**
     * Test des urls backfills avec un webView :
     * Au debut on mettre en parametre de l'url du webView les données json des urls backfills, en format BASE64,
     * lorsque le test est terminé la methode sendMessageVP sera appelé avec les resultats en parametre.
     * @param ctx
     * @param backfills
     * @param freeCampState
     */
    private  static void checkBackfillVideo(Context ctx,JSONArray backfills,String freeCampState) {
        WebView webViewTestAdex = new WebView(ctx);
        webViewTestAdex.setWebChromeClient(new WebChromeClient());
        webViewTestAdex.getSettings().setJavaScriptEnabled(true);
        webViewTestAdex.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        webViewTestAdex.clearCache(true);

        String url = data.getServerUrl() + "/vastChecker.htm";
        StringBuilder param = new StringBuilder("");
        param.append("cvid=" + data.getCvID());
        param.append("&webEditorId="+data.getAccountID());
        param.append("&me");
        param.append("&backfill=" + Base64.encodeToString(backfills.toString().getBytes(),Base64.URL_SAFE));

        webViewTestAdex.addJavascriptInterface(new JSInterface(){
            @JavascriptInterface
            public void sendMessageVP(String msg) {
                Toast.makeText(ViewPay.ctx,"sendMessageVP :"+msg,Toast.LENGTH_LONG).show();
                if(!ViewPay.backfillTimeout) {
                    ViewPayEventsListener vp = (ViewPayEventsListener) ViewPay.ctx;
                    try {
                        JSONObject jsonObj = new JSONObject(msg);
                        JSONObject data = jsonObj.getJSONObject("data");
                        String backfill = data.getString("backfill");
                        if (backfill.equals("ok")) {
                            String adexId = data.getString("adex_id");
                            String urlAdex = data.getString("urlAdex");
                            ViewPay.data.setActiveAdex(true);
                            ViewPay.data.setAdexId(adexId);
                            ViewPay.data.setUrlAdex(urlAdex);
                            callCheckVideoSuccess();
                        } else if (ViewPay.data.getFreeCampState().toLowerCase().equals("ok")) {
                            callCheckVideoSuccess();
                        } else {
                            callCheckVideoError();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, "Android");
        //webViewTestAdex.loadUrl(url + param);
        webViewTestAdex.postUrl(url,param.toString().getBytes());
        //Toast.makeText(ViewPay.ctx,"POST",Toast.LENGTH_SHORT).show();
        ViewPay.backfillTimeout = false;
        int timeOutBackfill = data.getTimeoutBackfill() * 1000;
        new CountDownTimer(timeOutBackfill, timeOutBackfill) {
            public void onTick(long millisUntilFinished) {}
            public void onFinish() {
                if(!backfillSuccess){
                    //Toast.makeText(ViewPay.ctx,"Timeout",Toast.LENGTH_SHORT).show();
                    ViewPay.backfillTimeout = true;
                    if(ViewPay.data.getFreeCampState().toLowerCase().equals("ok")){
                        callCheckVideoSuccess();
                    }else {
                        callCheckVideoError();
                    }
                }
            }
        }.start();
    }

    interface JSInterface{
        @JavascriptInterface
        public void sendMessageVP(String msg);
    }

    private static void callCheckVideoSuccess(){
        backfillSuccess = true;
        Activity activity = (Activity)ctx;
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ViewPayEventsListener vp = (ViewPayEventsListener)ViewPay.ctx;
                vp.checkVideoSuccesVP();
            }
        });
    }

    private static void callCheckVideoError(){
        Activity activity = (Activity)ctx;
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ViewPayEventsListener vp = (ViewPayEventsListener)ViewPay.ctx;
                vp.checkVideoErrorVP();
            }
        });
    }

    private static void updateDataAdversiting(ViewPayDataManager data,Context context){
        Info adInfo = null;
        try {
            adInfo = AdvertisingIdClient.getAdvertisingIdInfo(context);
            boolean isLimitAdTrackingEnabled = adInfo.isLimitAdTrackingEnabled();
            data.setOptout(isLimitAdTrackingEnabled?"1":"0");
            data.setAdvestisingId(adInfo.getId());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void wsTracking(int type,String partnerId){
        ViewPayEventsListener vp = (ViewPayEventsListener)ctx;
        String _url = fullUrl(IS_TRACKING);
        _url = _url +"&type="+type+"&part="+partnerId+"&stats";
        if (Integer.parseInt(Build.VERSION.SDK)
                < Build.VERSION_CODES.FROYO) {
            System.setProperty("http.keepAlive", "false");
        }
        if (android.os.Build.VERSION.SDK_INT > 9)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        Log.i(">>>>","_url checkVIdeo="+_url);
        HttpURLConnection urlConnection = null;
        try {
            URL urlToRequest = new URL(_url);
            urlConnection = (HttpURLConnection)urlToRequest.openConnection();
            urlConnection.setConnectTimeout(10000);
            urlConnection.setReadTimeout(10000);
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            String result = new Scanner(in).useDelimiter("\\A").next();

            if (result != null) {
               // Toast.makeText(ctx, result, Toast.LENGTH_LONG).show();
            }

        } catch (MalformedURLException e) {
        } catch (SocketTimeoutException e) {
        } catch (IOException e) {
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }
}
package com.markelys.viewpay;

import android.provider.SyncStateContract;

/**
 * Created by ingenosya on 23/07/2019.
 */

public final class Utils {
    public static String urlToHTTPS(String url){
        if(ViewPayConstants.debugIGY)
            return url;

        if(url.contains("https")){
            return url;
        }
        return  url.replace("http","https");
    }

    public static boolean isVestionBiggerThan(int version){
        char chiffre1 = android.os.Build.VERSION.RELEASE.charAt(0);
        int val = Integer.valueOf(chiffre1+"");
        return val>version;
    }
}

package com.markelys.viewpay;

import android.content.Context;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by ingenosya on 23/07/2019.
 */

public final class OmidJsLoader {
    private static  String OMID_JS_SERVICE_URL = "http:// .../Service/mosdk-v1.js";
    public static String getOmidJs(Context context){
        /*
        try (InputStream inputStream = context.getAssets().open("omsdk/omsdk-v1.js")){
            return IOUtils.toString(inputStream);
        }catch (IOException e){
            throw  new UnsupportedOperationException("Omid resource bnot found", e);
        }
        */
        return OMID_JS_SERVICE_URL;

    }
}

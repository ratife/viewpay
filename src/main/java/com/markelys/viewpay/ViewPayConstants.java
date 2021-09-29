package com.markelys.viewpay;

/**
 * Created by Herbert TOMBO on 26/01/2018.
 */

interface ViewPayConstants {
    //String VP_CONFIG_URL = "https://cdn.jokerly.com/configSDK/config.json";
    String VP_CONFIG_URL = "http://41.188.46.8:8080/OkidakStatic/configSDK/config.json";
    String VP_CHECK_VIDEO_PATH = "/mobileCheckVideo.htm";
    String VP_AD_SELECTOR_PATH = "/adSelectorDirect.htm";
    String VP_TRACKING_PATH = "/wsTracking.htm";

    String VP_LOADING_URL = "https://cdn.jokerly.com/images/loading5.gif";

    String HIDE_BTN_CLOSE = "hideclose";
    String SHOW_BTN_CLOSE = "showclose";

    //TODO
    String Acceder_a_votre_wifi = "showclose";//clique sur logo ecran final
    String SUCCES = "success";
    String ERROR = "error";
    String CLICK_CTA2 = "cta2";

    static boolean  debugIGY = true;

    String VERSION = "3.1";

}

package com.markelys.viewpay;

/**
 * Created by Herbert TOMBO on 24/01/2018.
 */

public interface ViewPayEventsListener {
    void checkVideoSuccesVP();
    void checkVideoErrorVP();
    void errorVP();
    void closeAdsVP();
    void completeAdsVP();
}

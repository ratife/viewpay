package com.markelys.viewpay;

import android.content.Context;

/**
 * Created by Herbert TOMBO on 24/01/2018.
 */

class ViewPayDataManager
{
    private String accountID ="";
    private String accessMessage ="";
    private int  userAge = 0;
    private String genre = "";
    private String hostID = "";
    private String cvID = "";
    private boolean isTablet=false;
    private String country = "";
    private String language = "";
    private String postalCode = "";
    private String latitude = "";
    private String longitude = "";
    private String categorie = "";
    private String serverUrl = "";
    private boolean activeAdex = false;

    private int timeoutBackfill = 5;
    private String adexId = "";
    private String urlAdex = "";
    private String freeCampState = "";

    private String advestisingId;
    private String optout;

    private String labelValidate;

    private String adMobId;

    public Context getAppContext() {
        return appContext;
    }

    public void setAppContext(Context appContext) {
        this.appContext = appContext;
    }

    private Context appContext=null;

    private ViewPayDataManager()
    {}

    private static ViewPayDataManager INSTANCE = new ViewPayDataManager();

    public static ViewPayDataManager getInstance()
    {
        return INSTANCE;
    }

    public String getAccountID() {
        return accountID;
    }

    public void setAccountID(String accountID) {
        this.accountID = accountID;
    }

    public String getAccessMessage() {
        return accessMessage;
    }

    public void setAccessMessage(String accessMessage) {
        this.accessMessage = accessMessage;
    }

    public int getUserAge() {
        return userAge;
    }

    public void setUserAge(int userAge) {
        this.userAge = userAge;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getHostID() {
        //return "fa2e6ca854c79011";
        return hostID;
    }

    public void setHostID(String hostID) {
        this.hostID = hostID;
    }

    public String getCvID() {
        return cvID;
    }

    public void setCvID(String cvID) {
        this.cvID = cvID;
    }

    public boolean isTablet() {
        return isTablet;
    }

    public void setTablet(boolean tablet) {
        isTablet = tablet;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getCategorie() {
        return categorie;
    }

    public void setCategorie(String categorie) {
        this.categorie = categorie;
    }

    public String getServerUrl() {
        // if(Utils.isVestionBiggerThan(8))
        serverUrl = Utils.urlToHTTPS(serverUrl);
        return serverUrl; //"https://pro-stg.jokerly.com/Test";
    }

    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public void setActiveAdex(boolean active){this.activeAdex=active;}

    public boolean isActiveAdex(){return this.activeAdex;}

    public String getAdexId() {
        return adexId;
    }

    public void setAdexId(String adexId) {
        this.adexId = adexId;
    }

    public String getUrlAdex() {
        return urlAdex;
    }

    public void setUrlAdex(String urlAdex) {
        this.urlAdex = urlAdex;
    }

    public String getFreeCampState() {
        return freeCampState;
    }

    public void setFreeCampState(String freeCampState) {
        this.freeCampState = freeCampState;
    }

    public int getTimeoutBackfill() {
        return timeoutBackfill;
    }

    public void setTimeoutBackfill(int timeoutBackfill) {
        this.timeoutBackfill = timeoutBackfill;
    }

    public String getAdvestisingId() {
        return advestisingId;
    }

    public void setAdvestisingId(String advestisingId) {
        this.advestisingId = advestisingId;
    }

    public String getOptout() {
        return optout;
    }

    public void setOptout(String optout) {
        this.optout = optout;
    }

    public String getLabelValidate() {
        return labelValidate;
    }

    public void setLabelValidate(String labelValidate) {
        this.labelValidate = labelValidate;
    }

    public String getAdMobId() {
        return adMobId;
    }

    public void setAdMobId(String adMobId) {
        this.adMobId = adMobId;
    }
}
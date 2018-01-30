package com.adatafun.dmpQuery.model;

import io.searchbox.annotations.JestId;

import java.util.Arrays;
import java.util.List;

/**
 * User.java
 * Copyright(C) 2017 杭州风数科技有限公司
 * Created by wzt on 05/09/2017.
 */
public class User {

    @JestId
    private String ageRange;
    private String aliveDegree;
    private String city;
    private String demosticAbroad;
    private String gender;
    private String id;
    private String logTimePrefer;
    private String logWithInThirty;
    private String mobileSignal;
    private String newold;
    private String outFrequency;
    private String phoneNum;
    private String province;
    private String silent;
    private Long tradeCar;
    private Long tradeFlash;
    private Long tradeLounge;
    private Long tradeParking;
    private Long tradePoint;
    private String userId;
    private String usualCity;
    private String usualCountry;
    private String usualProvince;

    public User() {
        super();
        // TODO Auto-generated constructor stub
    }

    public User(String ageRange, String aliveDegree, String city, String demosticAbroad,
                String gender, String id, String logTimePrefer, String logWithInThirty,
                String mobileSignal, String newold, String outFrequency, String phoneNum,
                String province, String silent, Long tradeCar, Long tradeFlash,
                Long tradeLounge, Long tradeParking, Long tradePoint, String userId,
                String usualCity, String usualCountry, String usualProvince) {
        super();
        this.ageRange = ageRange;
        this.aliveDegree = aliveDegree;
        this.city = city;
        this.demosticAbroad = demosticAbroad;
        this.gender = gender;
        this.id = id;
        this.logTimePrefer = logTimePrefer;
        this.logWithInThirty = logWithInThirty;
        this.mobileSignal = mobileSignal;
        this.newold = newold;
        this.outFrequency = outFrequency;
        this.phoneNum = phoneNum;
        this.province = province;
        this.silent = silent;
        this.tradeCar = tradeCar;
        this.tradeFlash = tradeFlash;
        this.tradeLounge = tradeLounge;
        this.tradeParking = tradeParking;
        this.tradePoint = tradePoint;
        this.userId = userId;
        this.usualCity = usualCity;
        this.usualCountry = usualCountry;
        this.usualProvince = usualProvince;
    }

    public List<String> getRangesLabelName() {
        List<String> rangesRegex = Arrays.asList(getAgeRangeName(),getGenderName(),
                getLogTimePreferName(), getLogWithInThirtyName(),getNewoldName());
        return rangesRegex;
    }

    @Override
    public String toString() {
        return "User [" +
                "ageRange='" + ageRange + '\'' +
                ", aliveDegree='" + aliveDegree + '\'' +
                ", city='" + city + '\'' +
                ", demosticAbroad='" + demosticAbroad + '\'' +
                ", gender='" + gender + '\'' +
                ", id='" + id + '\'' +
                ", logTimePrefer='" + logTimePrefer + '\'' +
                ", logWithInThirty='" + logWithInThirty + '\'' +
                ", mobileSignal='" + mobileSignal + '\'' +
                ", newold='" + newold + '\'' +
                ", outFrequency='" + outFrequency + '\'' +
                ", phoneNum='" + phoneNum + '\'' +
                ", province='" + province + '\'' +
                ", silent='" + silent + '\'' +
                ", tradeCar=" + tradeCar +
                ", tradeFlash=" + tradeFlash +
                ", tradeLounge=" + tradeLounge +
                ", tradeParking=" + tradeParking +
                ", tradePoint=" + tradePoint +
                ", userId='" + userId + '\'' +
                ", usualCity='" + usualCity + '\'' +
                ", usualCountry='" + usualCountry + '\'' +
                ", usualProvince='" + usualProvince + '\'' +
                '}';
    }

    public String getAgeRange() {
        return ageRange;
    }

    public void setAgeRange(String ageRange) {
        this.ageRange = ageRange;
    }

    public String getAgeRangeName() {
        return "ageRange";
    }

    public String getAliveDegree() {
        return aliveDegree;
    }

    public void setAliveDegree(String aliveDegree) {
        this.aliveDegree = aliveDegree;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getDemosticAbroad() {
        return demosticAbroad;
    }

    public void setDemosticAbroad(String demosticAbroad) {
        this.demosticAbroad = demosticAbroad;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getGenderName() {
        return "Gender";
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLogTimePrefer() {
        return logTimePrefer;
    }

    public void setLogTimePrefer(String logTimePrefer) {
        this.logTimePrefer = logTimePrefer;
    }

    public String getLogTimePreferName() {
        return "LogTimePrefer";
    }

    public String getLogWithInThirty() {
        return logWithInThirty;
    }

    public void setLogWithInThirty(String logWithInThirty) {
        this.logWithInThirty = logWithInThirty;
    }

    public String getLogWithInThirtyName() {
        return "LogWithInThirty";
    }

    public String getMobileSignal() {
        return mobileSignal;
    }

    public void setMobileSignal(String mobileSignal) {
        this.mobileSignal = mobileSignal;
    }

    public String getNewold() {
        return newold;
    }

    public void setNewold(String newold) {
        this.newold = newold;
    }

    public String getNewoldName() {
        return "Newold";
    }

    public String getOutFrequency() {
        return outFrequency;
    }

    public void setOutFrequency(String outFrequency) {
        this.outFrequency = outFrequency;
    }

    public String getPhoneNum() {
        return phoneNum;
    }

    public void setPhoneNum(String phoneNum) {
        this.phoneNum = phoneNum;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getSilent() {
        return silent;
    }

    public void setSilent(String silent) {
        this.silent = silent;
    }

    public Long getTradeCar() {
        return tradeCar;
    }

    public void setTradeCar(Long tradeCar) {
        this.tradeCar = tradeCar;
    }

    public Long getTradeFlash() {
        return tradeFlash;
    }

    public void setTradeFlash(Long tradeFlash) {
        this.tradeFlash = tradeFlash;
    }

    public Long getTradeLounge() {
        return tradeLounge;
    }

    public void setTradeLounge(Long tradeLounge) {
        this.tradeLounge = tradeLounge;
    }

    public Long getTradeParking() {
        return tradeParking;
    }

    public void setTradeParking(Long tradeParking) {
        this.tradeParking = tradeParking;
    }

    public Long getTradePoint() {
        return tradePoint;
    }

    public void setTradePoint(Long tradePoint) {
        this.tradePoint = tradePoint;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsualCity() {
        return usualCity;
    }

    public void setUsualCity(String usualCity) {
        this.usualCity = usualCity;
    }

    public String getUsualCountry() {
        return usualCountry;
    }

    public void setUsualCountry(String usualCountry) {
        this.usualCountry = usualCountry;
    }

    public String getUsualProvince() {
        return usualProvince;
    }

    public void setUsualProvince(String usualProvince) {
        this.usualProvince = usualProvince;
    }
}



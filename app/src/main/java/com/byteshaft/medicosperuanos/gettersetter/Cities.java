package com.byteshaft.medicosperuanos.gettersetter;

/**
 * Created by shahid on 25/03/2017.
 */

public class Cities {

//    private String stateName;
    private String cityName;
    private int stateId;
    private int cityId;

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

//    public String getStateName() {
//        return stateName;
//    }
//
//    public void setStateName(String stateName) {
//        this.stateName = stateName;
//    }

    public int getStateId() {
        return stateId;
    }

    public void setStateId(int stateId) {
        this.stateId = stateId;
    }

    public int getCityId() {
        return cityId;
    }

    public void setCityId(int cityId) {
        this.cityId = cityId;
    }
}

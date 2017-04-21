package com.byteshaft.medicosperuanos.gettersetter;

/**
 * Created by s9iper1 on 4/19/17.
 */

public class PatientAppointment {

    private String date;
    private String drFirstName;
    private String appointmentTime;
    private String serviceName;
    private String state;
    private String drSpeciality;

    public String getDrSpeciality() {
        return drSpeciality;
    }

    public void setDrSpeciality(String drSpeciality) {
        this.drSpeciality = drSpeciality;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDrFirstName() {
        return drFirstName;
    }

    public void setDrFirstName(String drFirstName) {
        this.drFirstName = drFirstName;
    }

    public String getAppointmentTime() {
        return appointmentTime;
    }

    public void setAppointmentTime(String appointmentTime) {
        this.appointmentTime = appointmentTime;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

}
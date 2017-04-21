package com.byteshaft.medicosperuanos.gettersetter;

/**
 * Created by s9iper1 on 4/10/17.
 */

public class AppointmentDetail {

    private int slotId;
    private boolean state;
    private String startTime;
    private int doctorId;

    public int getSlotId() {
        return slotId;
    }

    public void setSlotId(int slotId) {
        this.slotId = slotId;
    }

    public boolean getState() {
        return state;
    }

    public void setState(boolean state) {
        this.state = state;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public int getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(int doctorId) {
        this.doctorId = doctorId;
    }


}

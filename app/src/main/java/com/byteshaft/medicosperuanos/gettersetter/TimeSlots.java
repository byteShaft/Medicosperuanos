package com.byteshaft.medicosperuanos.gettersetter;

/**
 * Created by husnain on 4/17/17.
 */

public class TimeSlots {

    private String startTime;
    private String endTime;
    private int slotId;
    private boolean taken;

    public boolean isTaken() {
        return taken;
    }

    public void setTaken(boolean taken) {
        this.taken = taken;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public int getSlotId() {
        return slotId;
    }

    public void setSlotId(int slotId) {
        this.slotId = slotId;
    }

}

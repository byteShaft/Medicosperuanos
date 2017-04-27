package com.byteshaft.medicosperuanos.gettersetter;


/**
 * Created by husnain on 4/15/17.
 */

public class FavoriteDoctorsList {

    private String doctorsName;
    private String speciality;
    private int stars;
    private int timeId;
    private int slotId;
    private boolean timeStatus;
    private String timeSlot;
    private String startTime;
    private String endTime;
    private String scheduleDate;
    private String doctorImage;
    private String doctorsLocation;
    private int id;
    private String firstName;
    private String lastName;
    private boolean isBlocked;

    public boolean isAvailableToChat() {
        return availableToChat;
    }

    public void setAvailableToChat(boolean availableToChat) {
        this.availableToChat = availableToChat;
    }

    private boolean availableToChat;

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    private String location;

    public String getPrimaryPhoneNumber() {
        return primaryPhoneNumber;
    }

    public void setPrimaryPhoneNumber(String primaryPhoneNumber) {
        this.primaryPhoneNumber = primaryPhoneNumber;
    }

    private String primaryPhoneNumber;

    public String getScheduleDate() {
        return scheduleDate;
    }

    public void setScheduleDate(String scheduleDate) {
        this.scheduleDate = scheduleDate;
    }

    public boolean isBlocked() {
        return isBlocked;
    }

    public void setBlocked(boolean blocked) {
        isBlocked = blocked;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    private boolean isFavorite;

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDoctorsName() {
        return doctorsName;
    }

    public void setDoctorsName(String doctorsName) {
        this.doctorsName = doctorsName;
    }

    public String getSpeciality() {
        return speciality;
    }

    public void setSpeciality(String speciality) {
        this.speciality = speciality;
    }

    public int getStars() {
        return stars;
    }

    public void setStars(int stars) {
        this.stars = stars;
    }

    public boolean isTimeStatus() {
        return timeStatus;
    }

    public void setTimeStatus(boolean timeStatus) {
        this.timeStatus = timeStatus;
    }

    public String getTimeSlot() {
        return timeSlot;
    }

    public void setTimeSlot(String timeSlot) {
        this.timeSlot = timeSlot;
    }

    public String getDoctorImage() {
        return doctorImage;
    }

    public void setDoctorImage(String doctorImage) {
        this.doctorImage = doctorImage;
    }

    public String getDoctorsLocation() {
        return doctorsLocation;
    }

    public void setDoctorsLocation(String doctorsLocation) {
        this.doctorsLocation = doctorsLocation;
    }

    public int getTimeId() {
        return timeId;
    }

    public void setTimeId(int timeId) {
        this.timeId = timeId;
    }

    public int getSlotId() {
        return slotId;
    }

    public void setSlotId(int slotId) {
        this.slotId = slotId;
    }

    public String getSchduleDate() {
        return scheduleDate;
    }

    public void setSchduleDate(String scheduleDate) {
        this.scheduleDate = scheduleDate;
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

}

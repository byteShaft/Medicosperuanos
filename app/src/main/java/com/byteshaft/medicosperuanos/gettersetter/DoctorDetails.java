package com.byteshaft.medicosperuanos.gettersetter;

/**
 * Created by s9iper1 on 4/6/17.
 */

public class DoctorDetails {

    private String date;
    private int id;
    private String speciality;
    private String firstName;
    private String lastName;
    private String photoUrl;
    private String location;
    private String address;
    private String primaryPhoneNumber;
    private String phoneNumberSecondary;
    private boolean availableToChat;
    private String startTime;
    private int userId;
    private float reviewStars;
    private String gender;

    public boolean isBlocked() {
        return isBlocked;
    }

    public void setBlocked(boolean blocked) {
        isBlocked = blocked;
    }

    private boolean isBlocked;

    public boolean isFavouriteDoctor() {
        return favouriteDoctor;
    }

    public void setFavouriteDoctor(boolean favouriteDoctor) {
        this.favouriteDoctor = favouriteDoctor;
    }

    private boolean favouriteDoctor;

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSpeciality() {
        return speciality;
    }

    public void setSpeciality(String speciality) {
        this.speciality = speciality;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPrimaryPhoneNumber() {
        return primaryPhoneNumber;
    }

    public void setPrimaryPhoneNumber(String primaryPhoneNumber) {
        this.primaryPhoneNumber = primaryPhoneNumber;
    }

    public String getPhoneNumberSecondary() {
        return phoneNumberSecondary;
    }

    public void setPhoneNumberSecondary(String phoneNumberSecondary) {
        this.phoneNumberSecondary = phoneNumberSecondary;
    }

    public boolean isAvailableToChat() {
        return availableToChat;
    }

    public void setAvailableToChat(boolean availableToChat) {
        this.availableToChat = availableToChat;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public float getReviewStars() {
        return reviewStars;
    }

    public void setReviewStars(float reviewStars) {
        this.reviewStars = reviewStars;
    }

}

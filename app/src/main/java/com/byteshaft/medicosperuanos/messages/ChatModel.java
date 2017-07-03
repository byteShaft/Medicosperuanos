package com.byteshaft.medicosperuanos.messages;


public class ChatModel {

    private int id;
    private String message;
    private String timeStamp;

    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public int getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(int doctorId) {
        this.doctorId = doctorId;
    }

    private int patientId;
    private int doctorId;

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    private String fullName;

    public String getPhotoUtrl() {
        return photoUtrl;
    }

    public void setPhotoUtrl(String photoUtrl) {
        this.photoUtrl = photoUtrl;
    }

    private String photoUtrl;

    public boolean isAvailable_to_chat() {
        return available_to_chat;
    }

    public void setAvailable_to_chat(boolean available_to_chat) {
        this.available_to_chat = available_to_chat;
    }

    private boolean available_to_chat;

    public String getSenderProfilePic() {
        return senderProfilePic;
    }

    public void setSenderProfilePic(String senderProfilePic) {
        this.senderProfilePic = senderProfilePic;
    }

    private String senderProfilePic;

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    private String imageUrl;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }
}

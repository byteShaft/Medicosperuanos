package com.byteshaft.medicosperuanos.messages;


/**
 * Created by Alessandro Barreto on 17/06/2016.
 */
public class ChatModel {

    private int id;
    private String message;
    private String timeStamp;

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

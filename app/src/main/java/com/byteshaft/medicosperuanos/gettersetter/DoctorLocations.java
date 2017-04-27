package com.byteshaft.medicosperuanos.gettersetter;

import java.io.Serializable;

/**
 * Created by s9iper1 on 4/22/17.
 */

public class DoctorLocations implements Serializable{

    private int id;
    private String name;
    private String location;

    public boolean isAvailableToChat() {
        return isAvailableToChat;
    }

    public void setAvailableToChat(boolean availableToChat) {
        isAvailableToChat = availableToChat;
    }

    private boolean isAvailableToChat;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}

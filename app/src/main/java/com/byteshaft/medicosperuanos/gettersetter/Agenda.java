package com.byteshaft.medicosperuanos.gettersetter;

public class Agenda {
    String createdAt;
    String date;
    int doctorId;
    String startTIme;
    String endTime;
    int agendaId;
    String reaseon;
    String agendaState;

    String firstName;
    String lastName;
    String photoUrl;

    public boolean isAvailAbleForChat() {
        return availAbleForChat;
    }

    public void setAvailAbleForChat(boolean availAbleForChat) {
        this.availAbleForChat = availAbleForChat;
    }

    boolean availAbleForChat;

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    String dateOfBirth;

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

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(int doctorId) {
        this.doctorId = doctorId;
    }

    public String getStartTIme() {
        return startTIme;
    }

    public void setStartTIme(String startTIme) {
        this.startTIme = startTIme;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public int getAgendaId() {
        return agendaId;
    }

    public void setAgendaId(int agendaId) {
        this.agendaId = agendaId;
    }

    public String getReaseon() {
        return reaseon;
    }

    public void setReaseon(String reaseon) {
        this.reaseon = reaseon;
    }

    public String getAgendaState() {
        return agendaState;
    }

    public void setAgendaState(String agendaState) {
        this.agendaState = agendaState;
    }

}

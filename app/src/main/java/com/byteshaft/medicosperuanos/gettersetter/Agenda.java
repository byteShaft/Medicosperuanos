package com.byteshaft.medicosperuanos.gettersetter;

import java.io.Serializable;
import java.util.ArrayList;

public class Agenda implements Serializable{
    private String createdAt;
    private String date;
    private int doctorId;
    private String startTIme;
    private String endTime;
    private int agendaId;
    private String reason;
    private String agendaState;
    private String firstName;
    private String lastName;
    private String photoUrl;

    public ArrayList<Services> getPatientServices() {
        return patientServices;
    }

    public void setPatientServices(ArrayList<Services> patientServices) {
        this.patientServices = patientServices;
    }

    private ArrayList<Services> patientServices;

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

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getAgendaState() {
        return agendaState;
    }

    public void setAgendaState(String agendaState) {
        this.agendaState = agendaState;
    }

}

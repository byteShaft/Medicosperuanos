package com.byteshaft.medicosperuanos.gettersetter;



public class DiagnosticMedication {

    private String diagnosticMedication;
    private int Id;

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    private int quantity;

    public String getDiagnosticMedication() {
        return diagnosticMedication;
    }

    public void setDiagnosticMedication(String diagnosticMedication) {
        this.diagnosticMedication = diagnosticMedication;
    }

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }

}

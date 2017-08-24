package com.byteshaft.medicosperuanos.gettersetter;



public class DiagnosticMedication {

    private String diagnosticMedication;
    private int Id;
    private String code;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

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

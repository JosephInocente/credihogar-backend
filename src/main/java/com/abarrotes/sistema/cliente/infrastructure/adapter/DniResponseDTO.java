package com.abarrotes.sistema.cliente.infrastructure.adapter;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class DniResponseDTO {
    
    @JsonProperty("first_name")
    private String firstName;
    
    @JsonProperty("first_last_name")
    private String firstLastsName;
    
    @JsonProperty("second_last_name")
    private String secondLastsName;
    
    @JsonProperty("document_number")
    private String documentNumber;

    public String getNombreCompleto() {
        String n = firstName != null ? firstName : "";
        String ap = firstLastsName != null ? firstLastsName : "";
        String am = secondLastsName != null ? secondLastsName : "";
        return (n + " " + ap + " " + am).trim();
    }
}
package com.example.SmartAgriculture.californium;

import java.sql.Timestamp;

public class ResourceValue {
    Double value;
    Timestamp timestamp;

    public ResourceValue(String value){
        if(value.equals("on"))
            this.value = 1.0;
        else if(value.equals("off"))
            this.value = 0.0;
        else this.value = Double.parseDouble(value);
        timestamp = new Timestamp(System.currentTimeMillis());
    }

    public Double getValue() { return value; }
    public Timestamp getTimestamp() { return timestamp;}

    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp;}
    public void setValue(Double value) {this.value = value;}
}
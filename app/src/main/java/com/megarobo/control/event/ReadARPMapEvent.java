package com.megarobo.control.event;

public class ReadARPMapEvent {

    private String message;

    public ReadARPMapEvent(String message){
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

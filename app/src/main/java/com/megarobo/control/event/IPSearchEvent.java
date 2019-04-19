package com.megarobo.control.event;

public class IPSearchEvent {

    private String message;

    public IPSearchEvent(String message){
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

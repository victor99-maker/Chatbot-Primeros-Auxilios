package com.example.cahtbotprimerosauxilios;


public class Message {
    String message;
    Integer sender;
    //0=usuario 1=bot

    public Integer getSender() {
        return sender;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setSender(Integer sender) {
        this.sender = sender;
    }
    public Message(String message, Integer sender) {
        this.message = message;
        this.sender = sender;
    }
}

package com.example.courier.model;

public class Result {
    public String message_id;

    public Result(String message_id) {
        this.message_id = message_id;
    }

    public Result() {
    }

    public String getMessage_id() {
        return message_id;
    }

    public void setMessage_id(String message_id) {
        this.message_id = message_id;
    }
}

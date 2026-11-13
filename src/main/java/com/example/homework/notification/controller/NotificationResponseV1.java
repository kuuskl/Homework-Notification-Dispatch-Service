package com.example.homework.notification.controller;

import java.sql.Timestamp;

public class NotificationResponseV1 {

    public long id;
    public String type;
    public String target;
    public String payload;
    public String status;
    public int failureCount;
    public int dispatchCount;
    public String blocked;
    public String message;
    public Timestamp createdAt;
    public Timestamp updatedAt;
}

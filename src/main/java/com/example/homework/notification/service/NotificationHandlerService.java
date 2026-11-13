package com.example.homework.notification.service;

public interface NotificationHandlerService {
    void execute(String target, String payload) throws Exception;
}

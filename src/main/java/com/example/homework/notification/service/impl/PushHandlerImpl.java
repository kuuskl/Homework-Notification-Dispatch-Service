package com.example.homework.notification.service.impl;

import com.example.homework.notification.service.NotificationHandlerService;

public class PushHandlerImpl implements NotificationHandlerService {
    @Override
    public void execute(String target, String payload) {
        System.out.println("[PUSH] " + target + " -> " + payload);
    }
}

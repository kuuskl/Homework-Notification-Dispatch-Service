package com.example.homework.notification.service.impl;

import com.example.homework.notification.service.NotificationHandlerService;

public class SmsHandlerImpl implements NotificationHandlerService {
    @Override
    public void execute(String target, String payload) {
        System.out.println("[SMS] " + target + " -> " + payload);
    }
}

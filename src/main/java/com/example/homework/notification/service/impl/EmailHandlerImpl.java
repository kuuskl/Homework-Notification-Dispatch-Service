package com.example.homework.notification.service.impl;

import com.example.homework.notification.service.NotificationHandlerService;

public class EmailHandlerImpl implements NotificationHandlerService {
    @Override
    public void execute(String target, String payload) throws Exception {
        if (target != null && target.contains("error")) {
            throw new RuntimeException("SMTP Timeout");
        }
        System.out.println("[EMAIL] " + target + " -> " + payload);
    }
}

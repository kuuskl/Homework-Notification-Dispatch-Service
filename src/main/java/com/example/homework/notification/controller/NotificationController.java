package com.example.homework.notification.controller;

import com.example.homework.notification.service.NotificationControllerService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/legacy/notifications2")
public class NotificationController {

    private final NotificationControllerService notificationControllerService;

    public NotificationController(NotificationControllerService notificationControllerService) {
        this.notificationControllerService = notificationControllerService;
    }

    @PostMapping("/send")
    public NotificationResponseV1 addToBalance(@RequestBody RequestV1 request) {
        return notificationControllerService.processNotification(request);
    }



}

package com.example.homework.notification.controller;

import com.example.homework.notification.service.NotificationControllerService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/legacy/notifications2")
public class NotificationController {

    private final NotificationControllerService notificationControllerService;

    public NotificationController(NotificationControllerService notificationControllerService) {
        this.notificationControllerService = notificationControllerService;
    }

    @PostMapping("/send")
    public NotificationResponseV1 send(@RequestBody RequestV1 request) {
        return notificationControllerService.processNotification(request);
    }

    @GetMapping("/history")
    public List<NotificationResponseV1> history() {
        return notificationControllerService.getHistory();
    }

    @GetMapping("/latest")
    public NotificationResponseV1 latest() {
        return notificationControllerService.getLatest();
    }

}

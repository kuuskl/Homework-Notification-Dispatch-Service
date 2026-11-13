package com.example.homework.notification.service;

import com.example.homework.notification.controller.NotificationResponseV1;
import com.example.homework.notification.controller.RequestV1;
import com.example.homework.notification.model.NotificationTransaction;
import com.example.homework.notification.repository.NotificationTransactionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class NotificationControllerService {

    private final NotificationTransactionRepository notificationTransactionRepository;

    public NotificationControllerService(NotificationTransactionRepository notificationTransactionRepository) {
        this.notificationTransactionRepository = notificationTransactionRepository;
    }

    public NotificationResponseV1 processNotification (RequestV1 requestV1) {

        validateRequest(requestV1);

        NotificationTransaction newNotification = newNotification(requestV1);

        notificationTransactionRepository.save(newNotification);


        return toNotificationResponseV1(newNotification);
    }


    private NotificationTransaction newNotification(RequestV1 requestV1) {
        NotificationTransaction newNotificationTransaction = new NotificationTransaction();
        newNotificationTransaction.setType(requestV1.type.toString());
        newNotificationTransaction.setTarget(requestV1.target);
        newNotificationTransaction.setPayload(requestV1.payload);
        newNotificationTransaction.setBlocked( (requestV1.target != null && requestV1.target.toLowerCase().contains("blocked")) ? "Y" : "N" );
        newNotificationTransaction.setFailureCount(0);
        newNotificationTransaction.setDispatchCount(0);
        newNotificationTransaction.setStatus("N");
        newNotificationTransaction.setMessage("START");
        newNotificationTransaction.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        newNotificationTransaction.setUpdatedAt(new Timestamp(System.currentTimeMillis()));

        return newNotificationTransaction;

    }



    private void validateRequest(RequestV1 requestV1) {
        if (requestV1.target == null || requestV1.target.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Target is mandatory.");
        }

        if (isTargetBlacklistedOrRateLimited(requestV1.target)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Business Rule Violation: User is blacklisted or rate-limited by DB policy.");
        }
    }


    private boolean isTargetBlacklistedOrRateLimited(String target) {
        if (target != null && target.toLowerCase().contains("blocked")) {
            return true;
        }

        Integer recentCount = notificationTransactionRepository.findTargetRecentCount(target);

        return recentCount != null && recentCount >= 3;
    }

    private NotificationResponseV1 toNotificationResponseV1(NotificationTransaction notificationTransaction) {
        NotificationResponseV1 notificationResponseV1 = new NotificationResponseV1();
        notificationResponseV1.id = notificationTransaction.getId();
        notificationResponseV1.type = notificationTransaction.getType();
        notificationResponseV1.target = notificationTransaction.getTarget();

        return notificationResponseV1;
    }
}

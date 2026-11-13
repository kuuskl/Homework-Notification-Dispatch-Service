package com.example.homework.notification.service;

import com.example.homework.notification.controller.NotificationResponseV1;
import com.example.homework.notification.controller.RequestV1;
import com.example.homework.notification.model.NotificationTransaction;
import com.example.homework.notification.repository.NotificationTransactionRepository;
import com.example.homework.notification.service.impl.EmailHandlerImpl;
import com.example.homework.notification.service.impl.PushHandlerImpl;
import com.example.homework.notification.service.impl.SmsHandlerImpl;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.sql.Timestamp;
import java.util.*;

@Service
public class NotificationControllerService {

    private final NotificationTransactionRepository notificationTransactionRepository;

    public NotificationControllerService(NotificationTransactionRepository notificationTransactionRepository) {
        this.notificationTransactionRepository = notificationTransactionRepository;
    }

    public NotificationResponseV1 processNotification (RequestV1 requestV1) {

        validateRequest(requestV1);

        Integer latestFailureCount = getLatestFailureCount();
        NotificationTransaction newNotification = newNotification(requestV1, latestFailureCount);

        if (latestFailureCount >= 3) {
            newNotification.setStatus("R");
            newNotification.setMessage(shorten("Circuit breaker OPEN via database state."));
            notificationTransactionRepository.save(newNotification);
            throw new RuntimeException("Circuit breaker OPEN via database state.");
        }

        NotificationHandlerService handler = getHandlerService(requestV1.type);
        if (handler == null) {
            newNotification.setStatus("U");
            newNotification.setMessage(shorten("Unsupported type: " + requestV1.type));
            notificationTransactionRepository.save(newNotification);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported type: " + requestV1.type);
        }

        try {
            handler.execute(requestV1.target, requestV1.payload);
            newNotification.setStatus("S");
            newNotification.setMessage("SENT");
            notificationTransactionRepository.save(newNotification);
            return toNotificationResponseV1(newNotification);
        } catch (Exception e) {
            newNotification.setStatus("E");
            newNotification.setFailureCount(latestFailureCount+1);
            shorten(e.getMessage());
            notificationTransactionRepository.save(newNotification);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Inserted failure record for " + requestV1.target + ": " + e.getMessage());
        }
    }

    public List<NotificationResponseV1> getHistory() {
        List<NotificationTransaction> notificationsHistory = notificationTransactionRepository.getHistory(20);
        List<NotificationResponseV1> notificationResponseV1List = new ArrayList<>();
        notificationsHistory.forEach(h -> notificationResponseV1List.add(toNotificationResponseV1(h)));

        return notificationResponseV1List;
    }

    public NotificationResponseV1 getLatest() {
        List<NotificationTransaction> notificationsHistory = notificationTransactionRepository.getHistory(1);

        if (notificationsHistory.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No notification entries found.");
        } else {
            return toNotificationResponseV1(notificationsHistory.stream().findFirst().get());
        }
    }

    private Integer getLatestFailureCount() {
        List<NotificationTransaction> notificationsHistory = notificationTransactionRepository.getHistory(1);

        if (notificationsHistory.isEmpty()) {
            return 0;
        } else {
            return notificationsHistory.stream().findFirst().get().getFailureCount();
        }
    }


    private NotificationTransaction newNotification(RequestV1 requestV1, Integer latestFailureCount) {
        NotificationTransaction newNotificationTransaction = new NotificationTransaction();
        newNotificationTransaction.setType(requestV1.type.toString());
        newNotificationTransaction.setTarget(requestV1.target);
        newNotificationTransaction.setPayload(requestV1.payload);
        newNotificationTransaction.setBlocked((requestV1.target != null && requestV1.target.toLowerCase().contains("blocked")) ? "Y" : "N" );
        newNotificationTransaction.setFailureCount(latestFailureCount);
        newNotificationTransaction.setDispatchCount(notificationTransactionRepository.getDispatchCount() +1);
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
        notificationResponseV1.payload = notificationTransaction.getPayload();
        notificationResponseV1.status = notificationTransaction.getStatus();
        notificationResponseV1.failureCount = notificationTransaction.getFailureCount();
        notificationResponseV1.dispatchCount = notificationTransaction.getDispatchCount();
        notificationResponseV1.blocked = notificationTransaction.getBlocked();
        notificationResponseV1.message = notificationTransaction.getMessage();
        notificationResponseV1.createdAt = notificationTransaction.getCreatedAt();
        notificationResponseV1.updatedAt = notificationTransaction.getUpdatedAt();

        return notificationResponseV1;
    }

    private NotificationHandlerService getHandlerService (RequestV1.Types type) {
        return switch (type) {
            case SMS -> new SmsHandlerImpl();
            case EMAIL -> new EmailHandlerImpl();
            case POST -> new PushHandlerImpl();
            default -> null;
        };
    }

    private String shorten(String value) {
        if (value == null) {
            return null;
        }
        if (value.length() <= 255) {
            return value;
        }
        return value.substring(0, 255);
    }
}

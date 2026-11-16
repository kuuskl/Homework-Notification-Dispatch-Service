package com.example.homework.notification.model;

import jakarta.persistence.*;
import org.springframework.util.StringUtils;

import java.sql.Timestamp;

@Entity
@Table(name = "notification_transaction",
        indexes = {
            @Index(name = "idx_notification_target", columnList="target"),
            @Index(name = "idx_notification_status", columnList="status")}
)
public class NotificationTransaction {

    public enum Status {
        NEW("N"),
        SENT("S"),
        ERR("E"),
        UNKNOWN("U"),
        REJECTED("R");

        private final String code;

        Status(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }

        public static Status valueOfCode(String code) {
            if (StringUtils.isEmpty(code)) {
                return null;
            }
            for (Status type : Status.values()) {
                if (code.equalsIgnoreCase(type.getCode())) {
                    return type;
                }
            }
            throw new IllegalArgumentException("[" + code + "] is not a valid code for a NotificationTransaction.String");
        }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "type", length = 32, nullable = false)
    private String type;

    @Column(name = "target", length = 255, nullable = false)
    private String target;

    @Column(name = "payload", columnDefinition = "CLOB")
    private String payload;

    @Column(name = "status", length = 1, nullable = false)
    private Status status;

    private int failureCount;

    private int dispatchCount;

    @Column(name = "blocked", length = 1, nullable = false)
    private String blocked;

    @Column(name = "message", length = 255)
    private String message;

    @Column(nullable = false)
    private Timestamp createdAt;

    @Column(nullable = false, updatable=true)
    private Timestamp updatedAt;

    public long getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public int getFailureCount() {
        return failureCount;
    }

    public void setFailureCount(int failureCount) {
        this.failureCount = failureCount;
    }

    public int getDispatchCount() {
        return dispatchCount;
    }

    public void setDispatchCount(int dispatchCount) {
        this.dispatchCount = dispatchCount;
    }

    public String getBlocked() {
        return blocked;
    }

    public void setBlocked(String blocked) {
        this.blocked = blocked;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

}

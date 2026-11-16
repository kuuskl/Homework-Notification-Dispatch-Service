package com.example.homework.notification.model;

import jakarta.persistence.*;

@Entity
@Table(name = "blacklisted_targets")
public class BlacklistedTargets {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "target", length = 255, nullable = false)
    private String target;

    public long getId() {
        return id;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

}

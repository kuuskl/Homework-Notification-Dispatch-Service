package com.example.homework.notification.controller;

import java.math.BigDecimal;

public class RequestV1 {
    public Types type;

    public String target;

    public String payload;

    public enum Types {
        SMS, EMAIL, POST
    }
}

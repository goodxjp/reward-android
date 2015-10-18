package com.reward.omotesando.models;

import java.util.Date;

/**
 * 獲得ギフト券。
 */
public class Gift {
    public String name;
    public String code;
    public Date expirationAt;
    public Date occurredAt;

    public Gift(String name, String code, Date expirationAt, Date occurredAt) {
        this.name = name;
        this.code = code;
        this.expirationAt = expirationAt;
        this.occurredAt = occurredAt;
    }
}


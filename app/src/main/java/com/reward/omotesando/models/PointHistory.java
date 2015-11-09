package com.reward.omotesando.models;

import java.util.Date;

/**
 * ポイント履歴。
 */
public class PointHistory {
    // Android の場合はパフォーマンス的な問題で Setter, Getter 使わない方向で統一。
    public String detail;
    public int pointChange;
    public Date occurredAt;

    public PointHistory(String detail, int pointChange, Date occurredAt) {
        this.detail = detail;
        this.pointChange = pointChange;
        this.occurredAt = occurredAt;
    }
}


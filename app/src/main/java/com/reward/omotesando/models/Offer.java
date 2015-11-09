package com.reward.omotesando.models;

import java.io.Serializable;

/**
 * オファー (案件)。
 */
public class Offer implements Serializable {
    public int campaignCategoryId;
    public String name;
    public String detail;
    public int price;
    public int point;
    public String iconUrl;
    public String executeUrl;
    public String requirement;
    public String requirementDetail;
    public String period;

    public Offer(int campaignCategoryId, String name, String detail, int price, int point, String iconUrl, String executeUrl,
                 String requirement, String requirementDetail,  String period) {
        this.campaignCategoryId = campaignCategoryId;
        this.name = name;
        this.detail = detail;
        this.price = price;
        this.point = point;
        this.iconUrl = iconUrl;
        this.executeUrl = executeUrl;
        this.requirement = requirement;
        this.requirementDetail = requirementDetail;
        this.period = period;
    }

    public String getExecuteUrl() {
        return executeUrl;
    }
}

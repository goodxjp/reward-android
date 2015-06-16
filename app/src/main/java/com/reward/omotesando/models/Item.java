package com.reward.omotesando.models;

import java.io.Serializable;

/**
 * 商品 (ポイント交換対象)。
 */
public class Item implements Serializable {
    public long id;
    public String name;
    public int point;
}

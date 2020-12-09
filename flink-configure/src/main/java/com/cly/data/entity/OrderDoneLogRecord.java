package com.cly.data.entity;

import java.io.Serializable;

public class OrderDoneLogRecord implements Serializable {
    private static final long serialVersionUID = 5639241779874157375L;
    private String merchandiseId;
    private String price;
    private String couponMoney;
    private String rebateAmount;

    public String getMerchandiseId() {
        return merchandiseId;
    }

    public void setMerchandiseId(String merchandiseId) {
        this.merchandiseId = merchandiseId;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getCouponMoney() {
        return couponMoney;
    }

    public void setCouponMoney(String couponMoney) {
        this.couponMoney = couponMoney;
    }

    public String getRebateAmount() {
        return rebateAmount;
    }

    public void setRebateAmount(String rebateAmount) {
        this.rebateAmount = rebateAmount;
    }
}

package com.cly.data.entity;

import java.io.Serializable;

public class AnalyticsAccessLogRecord implements Serializable {
    private static final long serialVersionUID = -3518600206553743582L;
    private String merchandiseId;
    private String merchandiseName;


    public AnalyticsAccessLogRecord(String merchandiseId, String merchandiseName) {
        this.merchandiseId = merchandiseId;
        this.merchandiseName = merchandiseName;
    }

    public static AnalyticsAccessLogRecord of(String merchandiseId, String merchandiseName) {
        return new AnalyticsAccessLogRecord(merchandiseId, merchandiseName);
    }

    public String getMerchandiseId() {
        return merchandiseId;
    }

    public void setMerchandiseId(String merchandiseId) {
        this.merchandiseId = merchandiseId;
    }

    public String getMerchandiseName() {
        return merchandiseName;
    }

    public void setMerchandiseName(String merchandiseName) {
        this.merchandiseName = merchandiseName;
    }
}

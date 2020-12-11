package com.cly.state.statebackend.bean;

import java.io.Serializable;

public class PageViewResult implements Serializable {

    private static final long serialVersionUID = 1L;

    private String windowTimeStart;
    private String windowTimeEnd;
    private Long windowTimeEndL;
    private String url;
    private Long count;

    public PageViewResult() {
    }

    public PageViewResult(String windowTimeStart, String windowTimeEnd, Long windowTimeEndL, String url, Long count) {
        this.windowTimeStart = windowTimeStart;
        this.windowTimeEnd = windowTimeEnd;
        this.windowTimeEndL = windowTimeEndL;
        this.url = url;
        this.count = count;
    }

    public String getWindowTimeStart() {
        return windowTimeStart;
    }

    public void setWindowTimeStart(String windowTimeStart) {
        this.windowTimeStart = windowTimeStart;
    }

    public String getWindowTimeEnd() {
        return windowTimeEnd;
    }

    public void setWindowTimeEnd(String windowTimeEnd) {
        this.windowTimeEnd = windowTimeEnd;
    }

    public Long getWindowTimeEndL() {
        return windowTimeEndL;
    }

    public void setWindowTimeEndL(Long windowTimeEndL) {
        this.windowTimeEndL = windowTimeEndL;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    @Override
    public String toString() {
        return "PageViewResult{" +
                "windowTimeStart='" + windowTimeStart + '\'' +
                ", windowTimeEnd='" + windowTimeEnd + '\'' +
                ", url='" + url + '\'' +
                ", count=" + count +
                '}';
    }
}

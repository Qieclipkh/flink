package com.cly.jdbc;

public class StationLog implements java.io.Serializable {

    private static final long serialVersionUID = -1432444923728646913L;
    private String sid;
    private String call_in;
    private String call_out;
    private String call_type;
    private Long call_time;
    private Integer duration;

    public StationLog() {
    }

    public StationLog(String sid, String call_in, String call_out, String call_type, Long call_time, Integer duration) {
        this.sid = sid;
        this.call_in = call_in;
        this.call_out = call_out;
        this.call_type = call_type;
        this.call_time = call_time;
        this.duration = duration;
    }

    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    public String getCall_in() {
        return call_in;
    }

    public void setCall_in(String call_in) {
        this.call_in = call_in;
    }

    public String getCall_out() {
        return call_out;
    }

    public void setCall_out(String call_out) {
        this.call_out = call_out;
    }

    public String getCall_type() {
        return call_type;
    }

    public void setCall_type(String call_type) {
        this.call_type = call_type;
    }

    public Long getCall_time() {
        return call_time;
    }

    public void setCall_time(Long call_time) {
        this.call_time = call_time;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    @Override
    public String toString() {
        return "StationLog{" +
                "sid='" + sid + '\'' +
                ", call_in='" + call_in + '\'' +
                ", call_out='" + call_out + '\'' +
                ", call_type='" + call_type + '\'' +
                ", call_time=" + call_time +
                ", duration=" + duration +
                '}';
    }
}

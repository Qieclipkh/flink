package com.cly.state.statebackend.func;

import com.cly.state.statebackend.bean.ApacheLogEvent;
import org.apache.flink.api.common.functions.MapFunction;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ToApacheLogEvent implements MapFunction<String, ApacheLogEvent> {
    @Override
    public ApacheLogEvent map(String value) throws Exception {
        final ApacheLogEvent apacheLogEvent = buildApacheLogEvent(value);
        return apacheLogEvent;
    }

    private static ApacheLogEvent buildApacheLogEvent(String value) throws ParseException {
        final String[] arr = value.split(" ");
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy:HH:mm:ss");
        final Date date = sdf.parse(arr[3]);
        final ApacheLogEvent apacheLogEvent = new ApacheLogEvent(arr[0], arr[1], date.getTime(), arr[5], arr[6]);
        return apacheLogEvent;
    }

    public static void main(String[] args) throws ParseException {
        String str ="93.114.45.13 - - 17/05/2015:10:05:45 +0000 GET /style2.css";
        System.out.println(buildApacheLogEvent(str));
    }
}

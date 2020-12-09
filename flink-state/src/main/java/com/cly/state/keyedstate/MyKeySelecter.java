package com.cly.state.keyedstate;

import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple2;

public class MyKeySelecter implements KeySelector<Tuple2<Long, Long>, Long> {

    @Override
    public Long getKey(Tuple2<Long, Long> value) throws Exception {
        return value.f0;
    }
}

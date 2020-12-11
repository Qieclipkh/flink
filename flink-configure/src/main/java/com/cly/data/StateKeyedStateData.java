package com.cly.data;

import org.apache.flink.api.java.tuple.Tuple2;

public class StateKeyedStateData {
    /**
     * 演示数据
     */
    public static final  Tuple2[] DATA = {
            Tuple2.of(1L, 3L),
            Tuple2.of(2L, 1L),
            Tuple2.of(1L, 4L),
            Tuple2.of(1L, 5L),
            Tuple2.of(2L, 4L),
            Tuple2.of(2L, 3L)};
}

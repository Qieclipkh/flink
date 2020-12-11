package com.cly.state.statebackend.func;

import com.cly.state.statebackend.bean.ApacheLogEvent;
import org.apache.flink.api.common.functions.AggregateFunction;

/**
 * 增量聚合函数，每接收一条数据+1
 * ApacheLogEvent:输入数据类型
 * Long: 累加器类型
 * Long: 输出数据类型
 */
public class PageCountAggFunc implements AggregateFunction<ApacheLogEvent, Long, Long> {
    /**
     * 初始化累加器
     * @return
     */
    @Override
    public Long createAccumulator() {
        return 0L;
    }

    /**
     * 每接收一条数据的处理逻辑
     * @param value
     * @param accumulator
     * @return
     */
    @Override
    public Long add(ApacheLogEvent value, Long accumulator) {
        return accumulator + 1;
    }

    /**
     * SessionWindow会调用该方法，TimeWindow不会调用
     * @param acc1
     * @param acc2
     * @return
     */
    @Override
    public Long merge(Long acc1, Long acc2) {
        return null;
    }

    /***
     * 得到结果
     * @param accumulator
     * @return
     */
    @Override
    public Long getResult(Long accumulator) {
        return accumulator;
    }
}

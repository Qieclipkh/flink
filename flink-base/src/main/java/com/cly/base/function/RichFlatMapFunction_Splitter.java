package com.cly.base.function;

import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.util.Collector;

/**
 * RichFlatMapFunction
 * ValueState的使用
 */
public class RichFlatMapFunction_Splitter extends RichFlatMapFunction<String, Tuple2<String, Integer>> {

    private transient ValueState<Integer> sum;

    @Override
    public void open(Configuration parameters) throws Exception {
        ValueStateDescriptor descriptor = new ValueStateDescriptor(
                "avg", TypeInformation.of(new TypeHint<Integer>() {
        }));
        sum = getRuntimeContext().getState(descriptor);
    }

    @Override
    public void flatMap(String value, Collector<Tuple2<String, Integer>> out) throws Exception {

        // 从状态中获取当前值
        Integer currentSum = sum.value();
        //更新状态中值
        sum.update(currentSum + 1);
        //
        sum.clear();
    }
}

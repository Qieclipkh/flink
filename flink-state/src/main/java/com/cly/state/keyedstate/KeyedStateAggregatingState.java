package com.cly.state.keyedstate;

import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.api.common.state.AggregatingState;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;

/**
 * 每3个数的平均值
 */
public class KeyedStateAggregatingState {
    public static void main(String[] args) throws Exception {
        // 获取执行环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        DataStreamSource<Tuple2<Long, Long>> dataStreamSource = env.fromElements(
                new Tuple2<Long, Long>(1L, 3L),
                new Tuple2<Long, Long>(2L, 1L),
                new Tuple2<Long, Long>(1L, 4L),
                new Tuple2<Long, Long>(1L, 5L),
                new Tuple2<Long, Long>(2L, 4L),
                new Tuple2<Long, Long>(2L, 3L)
        );

        DataStream result = dataStreamSource.keyBy(new KeySelector<Tuple2<Long, Long>, Long>() {
            @Override
            public Long getKey(Tuple2<Long, Long> value) throws Exception {
                return value.f0;
            }
        }).flatMap(new CountAverageWithValueState());
        result.print();
        env.execute("value state ");

    }

    public static class CountAverageWithValueState extends RichFlatMapFunction<Tuple2<Long, Long>, Tuple2<Long, Double>> {
        private transient ValueState<Tuple2<Long, Long>> avgState;
        @Override
        public void flatMap(Tuple2<Long, Long> in, Collector<Tuple2<Long, Double>> out) throws Exception {
            Tuple2<Long, Long> curr = avgState.value();
            if(curr == null){
                curr = Tuple2.of(0L,0L);
            }

            curr.f0 +=1;
            curr.f1 += in.f1;
            if(curr.f0 == 3){
                Double avg = (double)curr.f1 / curr.f0;
                out.collect(Tuple2.of(in.f0,avg));
                avgState.clear();
            }
            avgState.update(curr);

        }

        @Override
        public void open(Configuration parameters) throws Exception {
            ValueStateDescriptor<Tuple2<Long, Long>> descriptor =new ValueStateDescriptor<>(
                    "average", // the state name
                    //TypeInformation.of(new TypeHint<Tuple2<Long, Long>>() {})// type information
                    Types.TUPLE(Types.LONG,Types.LONG)
                    //Tuple2.of(0L,0L) // default value of the state, if nothing was set
            );
            avgState = getRuntimeContext().getState(descriptor);
        }
    }
}

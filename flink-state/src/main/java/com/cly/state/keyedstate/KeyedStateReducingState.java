package com.cly.state.keyedstate;

import com.cly.data.StateKeyedStateData;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.api.common.state.ReducingState;
import org.apache.flink.api.common.state.ReducingStateDescriptor;
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
 * 当接收到的相同 key 的元素,累计求和
 */
public class KeyedStateReducingState {
    public static void main(String[] args) throws Exception {
        // 获取执行环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        // 加载数据
        DataStreamSource<Tuple2<Long, Long>> dataStreamSource = env.fromElements(StateKeyedStateData.DATA);

        DataStream result = dataStreamSource
                .keyBy(new MyKeySelecter())
                // 当然可以直接使用sum(1)来的到结果
                .flatMap(new SumWithReducingState());
        result.print();
        env.execute("Reducing State");

    }

    public static class SumWithReducingState extends RichFlatMapFunction<Tuple2<Long, Long>, Tuple2<Long, Long>> {

        private transient ReducingState<Long> reducingState;

        @Override
        public void flatMap(Tuple2<Long, Long> in, Collector<Tuple2<Long, Long>> out) throws Exception {
            reducingState.add(in.f1);
            out.collect(Tuple2.of(in.f0, reducingState.get()));
        }

        @Override
        public void open(Configuration parameters) throws Exception {
            ReducingStateDescriptor<Long> descriptor = new ReducingStateDescriptor<Long>(
                    "sum",
                    new SumReduceFunction(),
                    Types.LONG
            );
            reducingState = getRuntimeContext().getReducingState(descriptor);
        }
    }


    public static class SumReduceFunction implements ReduceFunction<Long> {

        @Override
        public Long reduce(Long value1, Long value2) throws Exception {
            return value1 + value2;
        }
    }
}

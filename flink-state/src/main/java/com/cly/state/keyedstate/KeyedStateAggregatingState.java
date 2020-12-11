package com.cly.state.keyedstate;

import com.cly.data.StateKeyedStateData;
import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.api.common.state.AggregatingState;
import org.apache.flink.api.common.state.AggregatingStateDescriptor;
import org.apache.flink.api.common.typeinfo.Types;
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
        DataStreamSource<Tuple2<Long, Long>> dataStreamSource = env.fromElements(StateKeyedStateData.DATA);

        DataStream result = dataStreamSource
                .keyBy(new MyKeySelecter())
                .flatMap(new CountAverageWithValueState());
        result.print();
        env.execute("Aggregating State");

    }

    public static class CountAverageWithValueState extends RichFlatMapFunction<Tuple2<Long, Long>, String> {
        private transient AggregatingState<Tuple2<Long, Long>, String> aggregatingState;

        @Override
        public void flatMap(Tuple2<Long, Long> in, Collector<String> out) throws Exception {
            aggregatingState.add(in);
            out.collect(aggregatingState.get());
        }

        @Override
        public void open(Configuration parameters) throws Exception {
            AggregatingStateDescriptor<Tuple2<Long, Long>, String, String> descriptor = new AggregatingStateDescriptor<Tuple2<Long, Long>, String, String>(
                    "add",
                    new f(),
                    Types.STRING

            );
            aggregatingState = getRuntimeContext().getAggregatingState(descriptor);
        }
    }

    public static class f implements AggregateFunction<Tuple2<Long, Long>, String, String> {

        @Override
        public String createAccumulator() {
            return "Contains：";
        }

        @Override
        public String add(Tuple2<Long, Long> value, String accumulator) {
            if ("Contains：".equals(accumulator)) {
                return accumulator + value.f1;
            }
            return accumulator + " and " + value.f1;
        }

        @Override
        public String getResult(String accumulator) {
            return accumulator;
        }

        @Override
        public String merge(String a, String b) {
            return a + " and " + b;
        }
    }
}

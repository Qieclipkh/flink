package com.cly.state.keyedstate;

import com.cly.data.StateKeyedStateData;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.api.common.state.ListState;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;

/**
 * 当接收到的相同 key 的元素个数等于3个，计算这些元素的 value 的平均值。
 */
public class KeyedStateValueState {
    public static void main(String[] args) throws Exception {
        // 获取执行环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        DataStreamSource<Tuple2<Long, Long>> dataStreamSource = env.fromElements(StateKeyedStateData.DATA);

        DataStream result = dataStreamSource
                .keyBy(new MyKeySelecter())
                .flatMap(new CountAverageWithValueState());
        result.print();
        env.execute("Value State");

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
                    // 状态名称
                    "average",
                    //状态的数据类型
                    Types.TUPLE(Types.LONG,Types.LONG)
                    // 状态的默认值
                    //Tuple2.of(0L,0L)
            );
            avgState = getRuntimeContext().getState(descriptor);
        }
    }
}

package com.cly.state.keyedstate;

import com.cly.data.StateKeyedStateData;
import org.apache.commons.compress.utils.Lists;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.api.common.state.ListState;
import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;

import java.util.Collections;
import java.util.List;

/**
 * 当接收到的相同 key 的元素个数等于3个，计算这些元素的 value 的平均值。
 */
public class KeyedStateListState {
    public static void main(String[] args) throws Exception {
        // 获取执行环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        DataStreamSource<Tuple2<Long, Long>> dataStreamSource = env.fromElements(StateKeyedStateData.DATA);

        DataStream result = dataStreamSource
                .keyBy(new MyKeySelecter())
                .flatMap(new CountAverageWithListState());
        result.print();
        env.execute("List State");

    }

    public static class CountAverageWithListState extends RichFlatMapFunction<Tuple2<Long, Long>, Tuple2<Long, Double>> {
        private transient ListState<Tuple2<Long, Long>> listState;
        @Override
        public void flatMap(Tuple2<Long, Long> in, Collector<Tuple2<Long, Double>> out) throws Exception {
            final Iterable<Tuple2<Long, Long>> currState = listState.get();

            if(currState == null){
                listState.addAll(Collections.emptyList());
            }
            listState.add(in);

            List<Tuple2<Long, Long>> list = Lists.newArrayList(listState.get().iterator());

            if(list.size() == 3){
                long sum = 0L;
                for (Tuple2<Long, Long> el: list) {
                    sum += el.f1;
                }
                Double avg = (double)sum / 3;
                out.collect(Tuple2.of(in.f0,avg));

                listState.clear();
            }
        }

        @Override
        public void open(Configuration parameters) throws Exception {
            ListStateDescriptor<Tuple2<Long, Long>> descriptor =new ListStateDescriptor<>(
                    // 状态名称
                    "average",
                    //状态的数据类型
                    Types.TUPLE(Types.LONG,Types.LONG)
            );
            listState = getRuntimeContext().getListState(descriptor);
        }
    }

}

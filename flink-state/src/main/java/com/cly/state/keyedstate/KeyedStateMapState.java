package com.cly.state.keyedstate;

import com.cly.data.StateKeyedStateData;
import org.apache.commons.compress.utils.Lists;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.api.common.state.MapState;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;

import java.util.List;
import java.util.UUID;

/**
 * 当接收到的相同 key 的元素个数等于3个，计算这些元素的 value 的平均值。
 */
public class KeyedStateMapState {
    public static void main(String[] args) throws Exception {
        // 获取执行环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        DataStreamSource<Tuple2<Long, Long>> dataStreamSource = env.fromElements(StateKeyedStateData.DATA);
        DataStream result = dataStreamSource
                .keyBy(new MyKeySelecter())
                .flatMap(new CountAverageWithMapState());
        result.print();
        env.execute("Map State");

    }

    public static class CountAverageWithMapState extends RichFlatMapFunction<Tuple2<Long, Long>, Tuple2<Long, Double>> {
        private transient MapState<String, Long> mapState;

        @Override
        public void flatMap(Tuple2<Long, Long> in, Collector<Tuple2<Long, Double>> out) throws Exception {
            mapState.put(UUID.randomUUID().toString(), in.f1);
            List<Long> allData = Lists.newArrayList(mapState.values().iterator());
            if (allData.size() == 3) {
                Long sum = 0L;
                for (Long l : allData) {
                    sum += l;
                }
                Double avg = (double) sum / 3;
                out.collect(Tuple2.of(in.f0, avg));
                mapState.clear();
            }

        }

        @Override
        public void open(Configuration parameters) throws Exception {
            MapStateDescriptor<String, Long> descriptor = new MapStateDescriptor(
                    // 状态名称
                    "average",
                    // key 的数据类型
                    Types.STRING,
                    // value 的数据类型
                    Types.LONG
            );
            mapState = getRuntimeContext().getMapState(descriptor);
        }
    }

}

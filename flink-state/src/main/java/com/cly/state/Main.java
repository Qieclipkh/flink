package com.cly.state;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class Main {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        DataStreamSource<String> source = env.socketTextStream("172.16.32.38", 1234);
        DataStream<Tuple2<Long, Long>> map = source.map(new MapFunction<String, Tuple2<Long, Long>>() {
            @Override
            public Tuple2<Long, Long> map(String value) throws Exception {
                String[] split = value.split(",");
                return Tuple2.of(Long.parseLong(split[0]), Long.parseLong(split[1]));
            }
        });

        //SingleOutputStreamOperator<Tuple2<Long, Long>> sum = map.keyBy(value -> value.f0).timeWindow(Time.seconds(5)).sum(1);

        KeyedStream<Tuple2<Long, Long>, Long> keyedStream = map.keyBy(value -> value.f0);

        keyedStream.asQueryableState("sourceData");
        map.keyBy(value -> value.f0).print();

        /*keyedStream.flatMap(new RichFlatMapFunction<Tuple2<Long, Long>, Tuple2<Long, Long>>() {
                    private transient ValueState<Tuple2<Long, Long>> sum;

                    @Override
                    public void flatMap(Tuple2<Long, Long> input, Collector<Tuple2<Long, Long>> out) throws Exception {

                        // access the state value
                        Tuple2<Long, Long> currentSum = sum.value();

                        // 第一次输入，设置默认值
                        if(currentSum == null){
                            currentSum = Tuple2.of(0L,0L);
                        }

                        // update the count
                        currentSum.f0 += 1;

                        // add the second field of the input value
                        currentSum.f1 += input.f1;

                        // update the state
                        sum.update(currentSum);

                        // if the count reaches 2, emit the average and clear the state
                        if (currentSum.f0 >= 2) {
                            out.collect(new Tuple2<>(input.f0, currentSum.f1 / currentSum.f0));
                            sum.clear();
                        }
                    }

                    @Override
                    public void open(Configuration parameters) throws Exception {
                        ValueStateDescriptor<Tuple2<Long, Long>> descriptor = StateUtil.buildValueState();
                        sum = getRuntimeContext().getState(descriptor);
                    }

                })*/
                //.print();
        //OperatorState
        env.execute("StateJob");

    }




}

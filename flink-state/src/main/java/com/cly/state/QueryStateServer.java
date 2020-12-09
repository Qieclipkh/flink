package com.cly.state;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.QueryableStateOptions;
import org.apache.flink.configuration.RestOptions;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;

/**
 * 查询状态服务端
 */
public class QueryStateServer {
    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        // web访问端口
        conf.setInteger(RestOptions.PORT,8082);
        // 启用状态查询功能
        conf.setBoolean(QueryableStateOptions.ENABLE_QUERYABLE_STATE_PROXY_SERVER,true);
        // 端口配置：独立设置，范围配置，列表配置
        conf.setString(QueryableStateOptions.PROXY_PORT_RANGE,"9069");
        conf.setString(QueryableStateOptions.SERVER_PORT_RANGE,"9067");

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // nc -lk 1234  ,输入数据格式，以逗号分隔的2个数字如:1,2
        DataStreamSource<String> source = env.socketTextStream("172.16.32.38", 1234);
        DataStream<Tuple2<Long, Long>> map = source.map(new MapFunction<String, Tuple2<Long, Long>>() {
            @Override
            public Tuple2<Long, Long> map(String value) throws Exception {
                String[] split = value.split(",");
                return Tuple2.of(Long.parseLong(split[0]), Long.parseLong(split[1]));
            }
        });



        SingleOutputStreamOperator<Tuple2<Long, Long>> tuple2SingleOutputStreamOperator = map.keyBy(value -> value.f0).flatMap(new RichFlatMapFunction<Tuple2<Long, Long>, Tuple2<Long, Long>>() {

            private transient ValueState<Tuple2<Long, Long>> avg;

            /**
             * 每获取2个数据后计算平均值
             * @param input
             * @param out
             * @throws Exception
             */
            @Override
            public void flatMap(Tuple2<Long, Long> input, Collector<Tuple2<Long, Long>> out) throws Exception {
                // access the state value
                Tuple2<Long, Long> currentSum = avg.value();

                // 第一次输入，设置默认值
                if (currentSum == null) {
                    currentSum = Tuple2.of(0L, 0L);
                }

                // update the count
                currentSum.f0 += 1;

                // add the second field of the input value
                currentSum.f1 += input.f1;

                // update the state
                avg.update(currentSum);

                // if the count reaches 2, emit the average and clear the state
                if (currentSum.f0 >= 2) {
                    out.collect(new Tuple2<>(input.f0, currentSum.f1 / currentSum.f0));
                    avg.clear();
                }
            }

            @Override
            public void open(Configuration parameters) throws Exception {
                ValueStateDescriptor<Tuple2<Long, Long>> descriptor = new ValueStateDescriptor<>(
                        "average", // the state name
                        TypeInformation.of(new TypeHint<Tuple2<Long, Long>>() {})// type information
                        //Tuple2.of(0L,0L) // default value of the state, if nothing was set
                );
                // 设置 可查询
                descriptor.setQueryable("stateDescriptor-queryable-state");
                avg = getRuntimeContext().getState(descriptor);
            }
        });

        //
        KeyedStream<Tuple2<Long, Long>, Long> keyedStream = tuple2SingleOutputStreamOperator.keyBy(value -> value.f0);
        // 将KeyedStream流数据转化为 可查询 状态，外部系统可以直接访问
        // 输出结果为 每2次输入的第一次输入数据，key=1,为了计数
        keyedStream.asQueryableState("stram-queryable-state");

        //求2个平均数 最大的数据
        tuple2SingleOutputStreamOperator.keyBy(value -> value.f0).countWindow(2).max(1).print();
        env.execute("query-state-server");

    }




}

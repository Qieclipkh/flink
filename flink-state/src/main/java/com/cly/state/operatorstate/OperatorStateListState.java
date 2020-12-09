package com.cly.state.operatorstate;

import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.runtime.state.FunctionInitializationContext;
import org.apache.flink.runtime.state.FunctionSnapshotContext;
import org.apache.flink.streaming.api.checkpoint.CheckpointedFunction;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;

public class OperatorStateListState {
    public static void main(String[] args) {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        DataStreamSource<Tuple2<String, Integer>> source = env.fromElements(
                Tuple2.of("spark", 3),
                Tuple2.of("flink", 4),
                Tuple2.of("spark", 5),
                Tuple2.of("flink", 6)
        );
    }

    public static class CustomSink implements SinkFunction<Tuple2<String, Integer>>, CheckpointedFunction {


        @Override
        public void invoke(Tuple2<String, Integer> value, Context context) throws Exception {

        }


        @Override
        public void snapshotState(FunctionSnapshotContext context) throws Exception {

        }

        @Override
        public void initializeState(FunctionInitializationContext context) throws Exception {
            ListStateDescriptor<Tuple2<Long, Long>> descriptor =new ListStateDescriptor<>(
                    // 状态名称
                    "operator",
                    //状态的数据类型
                    Types.TUPLE(Types.LONG,Types.LONG)
            );
            context.getOperatorStateStore().getListState(descriptor);
        }


    }
}

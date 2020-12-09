package com.cly.state;
import org.apache.flink.api.common.JobID;
import org.apache.flink.api.common.state.StateDescriptor;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.BasicTypeInfo;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.queryablestate.client.QueryableStateClient;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;

public class QueryStateClient {

    String host ="localhost";
    Integer port = 9069;
    String job = "3d01da6d961491ba86db3ee81c317edb";

    @Test
    public void  testStream() throws Exception {
        QueryableStateClient queryableStateClient = new QueryableStateClient("localhost",9069);

        ValueStateDescriptor<Tuple2<Long, Long>> stream =
                new ValueStateDescriptor<>(
                        "stram-queryable-state", // the state name
                        TypeInformation.of(new TypeHint<Tuple2<Long, Long>>() {})// type information
                        //Tuple2.of(0L,0L) // default value of the state, if nothing was set
                );
        String stateName = "stram-queryable-state";
        execute(stream,stateName);
    }

    @Test
    public void  testStateDescriptor() throws Exception {
        ValueStateDescriptor<Tuple2<Long, Long>> descriptor =
                new ValueStateDescriptor<>(
                        "average", // the state name
                        TypeInformation.of(new TypeHint<Tuple2<Long, Long>>() {})// type information
                        //Tuple2.of(0L,0L) // default value of the state, if nothing was set
                );
        String stateName = "stateDescriptor-queryable-state";
        execute(descriptor,stateName);

    }
    public void execute(StateDescriptor descriptor,String stateName){
        try {
            QueryableStateClient queryableStateClient = new QueryableStateClient(host,port);
            // 从web页面获取
            JobID jobId = JobID.fromHexString(job);
            while (true) {
                CompletableFuture<ValueState<Tuple2<Long, Long>>> completableFuture =
                        queryableStateClient.getKvState(
                                jobId,
                                stateName,
                                2l, // 指的是 keyby 操作选取的key值
                                BasicTypeInfo.LONG_TYPE_INFO,
                                descriptor);
                completableFuture.thenAccept(response -> {
                    try {
                        Tuple2<Long, Long> res = response.value();
                        System.out.println("res: " + res);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

                Thread.sleep(1000);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

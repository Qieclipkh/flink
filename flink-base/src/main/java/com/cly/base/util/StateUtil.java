package com.cly.base.util;

import org.apache.flink.api.common.state.StateTtlConfig;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.time.Time;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.tuple.Tuple2;

/**
 * 状态管理工具类
 */
public class StateUtil {

    public static  ValueStateDescriptor<Tuple2<Long, Long>> buildValueState() {
        ValueStateDescriptor<Tuple2<Long, Long>> descriptor =
                new ValueStateDescriptor<>(
                        "average", // the state name
                        TypeInformation.of(new TypeHint<Tuple2<Long, Long>>() {})// type information
                        //Tuple2.of(0L,0L) // default value of the state, if nothing was set
                );
        /*StateTtlConfig ttlConfig = StateTtlConfig
                .newBuilder(Time.seconds(2L))
                //TTL的更新策略，OnCreateAndWrite 尽在创建和写入时更新；OnReadAndWrite 在读取写入时更新
                .setUpdateType(StateTtlConfig.UpdateType.OnCreateAndWrite)
                //数据过期但未被清理时的可见性配置，NeverReturnExpired 不返回过期数据；ReturnExpiredIfNotCleanedUp 在数据被物理删除前都会返回
                .setStateVisibility(StateTtlConfig.StateVisibility.NeverReturnExpired )
                //关闭过期数据清理
                .disableCleanupInBackground()
                .build();
        descriptor.enableTimeToLive(ttlConfig);*/
        return descriptor;
    }

}

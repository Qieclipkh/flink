package com.cly.join.source;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.api.common.functions.JoinFunction;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import com.cly.data.entity.AnalyticsAccessLogRecord;
import com.cly.data.entity.OrderDoneLogRecord;

import java.util.Arrays;
import java.util.Properties;

public class Consumer {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        Properties kafkaProps = new Properties();
        kafkaProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "172.16.193.120:9094");
        kafkaProps.put(ConsumerConfig.GROUP_ID_CONFIG, "test");

        /**
         * 点击流
         */
        DataStreamSource<String> clickSourceStream = env.addSource(
                new FlinkKafkaConsumer("ods_analytics_access_log",  new SimpleStringSchema(), kafkaProps)
                        .setStartFromLatest()
        );
        /**
         * 订单流
         */
        DataStreamSource<String> orderSourceStream = env.addSource(
                new FlinkKafkaConsumer("ods_ms_order_done",  new SimpleStringSchema(), kafkaProps)
                        .setStartFromLatest()
        );

        final DataStream<AnalyticsAccessLogRecord> clickRecordStream  = clickSourceStream.map(message -> JSONObject.parseObject(message, AnalyticsAccessLogRecord.class));
        final DataStream<OrderDoneLogRecord> orderRecordStream = orderSourceStream.map(message-> JSONObject.parseObject(message, OrderDoneLogRecord.class));
        clickRecordStream
                .join(orderRecordStream)
                .where(AnalyticsAccessLogRecord::getMerchandiseId)
                .equalTo(OrderDoneLogRecord::getMerchandiseId)
                .window(TumblingProcessingTimeWindows.of(Time.seconds(10)))
                .apply(new JoinFunction<AnalyticsAccessLogRecord, OrderDoneLogRecord, String>() {
                    @Override
                    public String join(AnalyticsAccessLogRecord accessRecord, OrderDoneLogRecord orderRecord) throws Exception {
                        return StringUtils.join(Arrays.asList(
                                accessRecord.getMerchandiseId(),
                                orderRecord.getPrice(),
                                orderRecord.getCouponMoney(),
                                orderRecord.getRebateAmount()
                        ), '\t');
                    }
                });
        env.execute("flink-join");

    }
}

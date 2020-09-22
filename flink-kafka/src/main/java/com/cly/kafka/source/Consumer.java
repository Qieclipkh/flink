package com.cly.kafka.source;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;

import java.time.Duration;
import java.util.Properties;
import java.util.regex.Pattern;

public class Consumer {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();


        String topicName = "test";
        // 动态topic,使用正则表达式匹配所有符合的topic, 需要指定flink.partition-discovery.interval-millis大于0
        Pattern topicPattern = Pattern.compile("test-topic-[0-9]");
        /**
         * 指定如何将Kafka中二进制数据转换成java对象
         */
        DeserializationSchema valueDeserializer = new SimpleStringSchema();
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,"172.16.32.38:19092,172.16.32.39:19092,172.16.32.40:19092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG,"test");

        // 启用kafka自身的offset提交功能，如下为默认配置
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,Boolean.TRUE);
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG,5000);


        //自动发现指定topic的分区变化，大于0的值(间隔多久获取一次kafka的元数据)
        props.setProperty("flink.partition-discovery.interval-millis",String.valueOf(10 * 1000));



        FlinkKafkaConsumer<String> flinkKafkaConsumer = new FlinkKafkaConsumer(topicName,valueDeserializer,props);
        //禁用或启用offset提交，这时Properties配置的enable.auto.commit、auto.commit.interval.ms将被忽略
        flinkKafkaConsumer.setCommitOffsetsOnCheckpoints(Boolean.FALSE);
        //指定watermark
        flinkKafkaConsumer.assignTimestampsAndWatermarks(WatermarkStrategy.forBoundedOutOfOrderness(Duration.ofSeconds(10)));
        /**
         * flinkKafkaConsumer.setStartFromLatest();       // 从最新的记录开始
         * flinkKafkaConsumer.setStartFromTimestamp(new Date().getTime()); // 从指定的时间开始（毫秒）
         * flinkKafkaConsumer.setStartFromGroupOffsets(); // 默认的方法
         */

        /*
        // 为指定分区指定偏移量开始消费
        Map<KafkaTopicPartition, Long> specificStartupOffsets = new HashMap<>();
        specificStartupOffsets.put(new KafkaTopicPartition(topicName,0),10L);
        specificStartupOffsets.put(new KafkaTopicPartition(topicName,1),10L);
        flinkKafkaConsumer.setStartFromSpecificOffsets(specificStartupOffsets);
        */
        flinkKafkaConsumer.setStartFromEarliest();     // 尽可能从最早的记录开始
        /**
         * 当job从故障中自动恢复或者使用savapoint手动恢复时，这些起始配置不会影响消费的起始位置，
         * 在恢复时，每个kafka分区的起始位置由存储在savepoint或者checkpoint中的offset决定
         */

        DataStreamSource<String> stringDataStreamSource = env.addSource(flinkKafkaConsumer);

        stringDataStreamSource.print();
        env.execute();

    }
}

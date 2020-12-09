package com.cly.join.entity.sink;

import org.apache.flink.api.common.serialization.SerializationSchema;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;

import java.util.Properties;

public class Producer {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();


        String filePath = Producer.class.getResource("/station.log").getPath();
        DataStreamSource<String> dataStreamSource = env.readTextFile(filePath);
        String topicName = "test";

        //指定数据序列化方法
        SerializationSchema serializationSchema = new SimpleStringSchema();
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,"172.16.32.38:19092,172.16.32.39:19092,172.16.32.40:19092");

        FlinkKafkaProducer flinkKafkaProducer = new FlinkKafkaProducer(topicName,serializationSchema,props);
        //数据写入kafka时，附加记录的时间时间戳
        flinkKafkaProducer.setWriteTimestampToKafka(Boolean.TRUE);


        dataStreamSource.addSink(flinkKafkaProducer);

        env.execute();
    }
}

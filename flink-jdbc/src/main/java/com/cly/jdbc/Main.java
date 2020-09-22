package com.cly.jdbc;

import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.connector.jdbc.JdbcConnectionOptions;
import org.apache.flink.connector.jdbc.JdbcSink;
import org.apache.flink.connector.jdbc.JdbcStatementBuilder;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.apache.flink.util.Collector;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.UUID;

public class Main {
    public static void main(String[] args) throws Exception{
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        String filePath = Main.class.getResource("/station.log").getPath();
        DataStreamSource<String> dataStreamSource = env.readTextFile(filePath);


        JdbcConnectionOptions jdbcConnectionOptions = new JdbcConnectionOptions.JdbcConnectionOptionsBuilder()
                .withDriverName("com.mysql.cj.jdbc.Driver").withUrl("jdbc:mysql://172.16.15.201:3306/test").withUsername("root").withPassword("123456").build();


        String mxSql = "insert into stationlog(id,sid,call_in,call_out,call_type,call_time,duration,sj) values(?,?,?,?,?,?,?,?)";
        JdbcStatementBuilder<StationLog> mxStatementBuilder = new JdbcStatementBuilder<StationLog>() {
            @Override
            public void accept(PreparedStatement ps, StationLog stationLog) throws SQLException {
                ps.setString(1, UUID.randomUUID().toString());
                ps.setString(2, stationLog.getSid());
                ps.setString(3, stationLog.getCall_in());
                ps.setString(4, stationLog.getCall_out());
                ps.setString(5, stationLog.getCall_type());
                ps.setLong(6, stationLog.getCall_time());
                ps.setInt(7, stationLog.getDuration());
                ps.setTimestamp(8,new Timestamp(new Date().getTime()));

            }
        };
        SinkFunction<StationLog> mxSink = JdbcSink.sink(mxSql, mxStatementBuilder, jdbcConnectionOptions);



        String jhSql = "insert into success_duration (sid,duration) values(?,?)";
        JdbcStatementBuilder< Tuple2<String,Long>> jhStatementBuilder = new JdbcStatementBuilder< Tuple2<String,Long>>() {
            @Override
            public void accept(PreparedStatement ps, Tuple2<String,Long> tuple2) throws SQLException {
                ps.setString(1, tuple2.f0);
                ps.setLong(2, tuple2.f1);

            }
        };
        SinkFunction< Tuple2<String,Long>> jhSink = JdbcSink.sink(jhSql, jhStatementBuilder, jdbcConnectionOptions);


        SingleOutputStreamOperator<StationLog> toStationlog = dataStreamSource.map(new MapFunction<String, StationLog>() {
            @Override
            public StationLog map(String value) throws Exception {
                String[] arr = value.split(",");
                return new StationLog(arr[0], arr[1], arr[2], arr[3], Long.parseLong(arr[4]), Integer.parseInt(arr[5]));
            }
        });
        SingleOutputStreamOperator<Tuple2<String,Long>> sum = toStationlog.filter(new FilterFunction<StationLog>() {
            @Override
            public boolean filter(StationLog stationLog) throws Exception {
                return stationLog.getCall_type().equals("success");
            }
        }).flatMap(new FlatMapFunction<StationLog, Tuple2<String,Long>>() {
            @Override
            public void flatMap(StationLog stationLog, Collector<Tuple2<String, Long>> collector) throws Exception {
                collector.collect(Tuple2.of(stationLog.getSid(),stationLog.getDuration().longValue()));
            }
        }).keyBy(new KeySelector<Tuple2<String,Long>, String>() {
            @Override
            public String getKey(Tuple2<String,Long> tuple2) throws Exception {
                return tuple2.f0;
            }
        }).countWindow(1).sum(1);



        toStationlog.addSink(mxSink);
        sum.addSink(jhSink);
        env.execute("输出mysql");
    }
}

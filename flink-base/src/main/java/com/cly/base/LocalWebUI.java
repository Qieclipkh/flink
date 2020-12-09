package com.cly.base;

import com.cly.base.function.SplitWord;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.QueryableStateOptions;
import org.apache.flink.configuration.RestOptions;
import org.apache.flink.configuration.WebOptions;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * 本地Web页面,需引入pom
     <dependency>
        <groupId>org.apache.flink</groupId>
        <artifactId>flink-runtime-web_${scala.binary.version}</artifactId>
        <version>${flink.version}</version>
        <!--<scope>test</scope>-->
     </dependency>
 */
public class LocalWebUI {
    public static void main(String[] args) throws Exception {
        // 获取环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(webUiConfig());
        // 加载数据（数据源能不断加载，不然程序停止就无法查看web界面）
        DataStreamSource<String> source = env.socketTextStream("172.16.32.38", 1234);
        // 处理数据
        DataStream flatMap = source.flatMap(new SplitWord());
        // 输出结果
        flatMap.print();
        // 启动执行
        env.execute("locaL web UI");
    }



    private static Configuration webUiConfig(){
        Configuration conf = new Configuration();

        //conf.setString(WebOptions.LOG_PATH,"logs\\flink-web.log");

        // web访问端口,默认端口8081
        conf.setInteger(RestOptions.PORT,8083);

        // 启用状态查询功能
        conf.setBoolean(QueryableStateOptions.ENABLE_QUERYABLE_STATE_PROXY_SERVER,false);
        // 端口配置：独立设置，范围配置，列表配置
        conf.setString(QueryableStateOptions.PROXY_PORT_RANGE,"9069");
        conf.setString(QueryableStateOptions.SERVER_PORT_RANGE,"9067");
        return conf;
    }




}

# 1.概述

## 1.1状态分为operator state 和 keyed state

维度 | operator state | keyed state | 备注
---|---|---|---
是否存在当前处理的 key | operator state 是没有当前 key 的概念 | keyed state 的数值总是与一个 current key 对应 |  
存储对象是否 on heap | 目前 operator state backend 仅有一种 on-heap 的实现 | keyed state backend 有 on-heap 和 off-heap（RocksDB）的多种实现 |  
是否需要手动声明快照（snapshot）和恢复 (restore) 方法 | operator state 需要手动实现 snapshot 和 restore 方法 |  keyed state 则由 backend 自行实现，对用户透明 |  
数据大小 | 一般而言，operator state 的数据规模是比较小的 | keyed state 规模是相对比较大的 |  需要注意的是，这是一个经验判断，不是一个绝对的判断区分标准
## 1.2 keyed state

使用算子keyBy(KeySelector)产生KeyedDataStream，然后使用keyed state进行操作。

ValueState
ListState
ReducingState
AggregatingState
MapState


# 3.StateBackend 状态存储
决定Flink Job状态存储数据结构和位置
## 3.1 MemoryStateBackend
## 3.2 FsStateBackend
性能更好；
日常存储是在堆内存中，面临着 OOM 的风险；
不支持增量 checkpoint。
## 3.3 RocksDBStateBackend
无需担心 OOM 风险，是大部分时候的选择。
### 3.3.1 使用
```
<!-- pom.xml中需要引入 -->
<dependency>
    <groupId>org.apache.flink</groupId>
    <artifactId>flink-statebackend-rocksdb_${scala.binary.version}</artifactId>
    <version>${flink.version}</version>
</dependency>
```

```java
StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
env.enableCheckpointing(100);
StateBackend stateBackend = new RocksDBStateBackend("hdfs://master:9000/flink/checkpoint/rdb1");
env.setStateBackend(stateBackend);
```
### 3.3.2 调优

配置项 | 默认值 |含义| 建议
---|---|---|---
state.backend.rocksdb.thread.num | 1 |后台 flush 和 compaction 的线程数 | 建议调大
state.backend.rocksdb.writebuffer.count | 2 |	每个 column family 的 write buffer 数目 | 如果有需要可以适当调大
state.backend.rocksdb.writebuffer.size | 64MB |	每个 write buffer 的 size | 对于写频繁的场景，建议调大
state.backend.rocksdb.block.cache-size | 8MB |	每个 column family 的 block cache大小 | 如果存在重复读的场景，建议调大

## 3.4 状态有效期(TTL)

```
ValueStateDescriptor<Tuple2<Long, Long>> descriptor =
        new ValueStateDescriptor<>(
                "average", // the state name
                TypeInformation.of(new TypeHint<Tuple2<Long, Long>>() {})// type information
                //Tuple2.of(0L,0L) // default value of the state, if nothing was set
        );
StateTtlConfig ttlConfig = StateTtlConfig
        .newBuilder(Time.seconds(2L))
        //TTL的更新策略，OnCreateAndWrite 尽在创建和写入时更新；OnReadAndWrite 在读取写入时更新
        .setUpdateType(StateTtlConfig.UpdateType.OnCreateAndWrite)
        //数据过期但未被清理时的可见性配置，NeverReturnExpired 不返回过期数据；ReturnExpiredIfNotCleanedUp 在数据被物理删除前都会返回
        .setStateVisibility(StateTtlConfig.StateVisibility.NeverReturnExpired )
        .build();
descriptor.enableTimeToLive(ttlConfig);
```

### 3.4.1 过期数据清理
默认情况下，过期数据会在读取时候被删除。可以通过StateTtlConfig关闭后台清理

# 4.查询状态数据

用户直接访问数据，不需要将数据发送的外部系统（sink）,减少查询时间

## 4.1 功能启用
### 4.1.1 线上运行环境
1. 修改文件conf/flink-conf.yaml
```
queryable-state.enable: true
```
2. 复制`opt/flink-queryable-state-runtime_${scala.version}-${flink.version}.jar`到`lib/`目录
### 4.1.2 本地测试环境
```
<dependency>
    <groupId>org.apache.flink</groupId>
    <artifactId>flink-core</artifactId>
    <version>${flink.version}</version>
</dependency>
<dependency>
  <groupId>org.apache.flink</groupId>
  <artifactId>flink-queryable-state-runtime_${scala.binary.version}</artifactId>
  <version>${flink.version}</version>
</dependency>

<!-- 启动本地web页面，默认端口：8081 -->
<dependency>
    <groupId>org.apache.flink</groupId>
    <artifactId>flink-runtime-web_${scala.binary.version}</artifactId>
    <version>${flink.version}</version>
    <!--<scope>test</scope>-->
</dependency>
```
```
Configuration conf = new Configuration();
conf.setInteger(RestOptions.PORT,8082);
conf.setBoolean(QueryableStateOptions.ENABLE_QUERYABLE_STATE_PROXY_SERVER,true);
StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(conf);
```
### 4.1.3
在TaskManager日志包含如下内容表明启用成功
```
Started Queryable State Server @ /127.0.0.1:9067.
Started Queryable State Proxy Server @ /127.0.0.1:9069.
```
## 4.2 查询状态

### 4.2.1 服务端
实现参考`com.cly.state.QueryStateServer`
* 通过Queryable State Stream使状态可查
* 通过StateDescriptor的setQueryable方法使状态可查

### 4.2.2 客户端
实现参考`com.cly.state.QueryStateClient`
```
<dependency>
  <groupId>org.apache.flink</groupId>
  <artifactId>flink-core</artifactId>
  <version>${flink.version}</version>
</dependency>
<dependency>
  <groupId>org.apache.flink</groupId>
  <artifactId>flink-queryable-state-client-java</artifactId>
  <version>${flink.version}</version>
</dependency>
```
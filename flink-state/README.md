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



# 2.外部查询状态数据

# 3.StateBackend 状态后端
决定Flink Job状态存储数据结构和位置
## 3.1 MemoryStateBackend
## 3.2 FsStateBackend
性能更好；
日常存储是在堆内存中，面临着 OOM 的风险；
不支持增量 checkpoint。
## 3.3 RocksDBStateBackend
无需担心 OOM 风险，是大部分时候的选择。
```
<!-- pom.xml中需要引入 -->
<dependency>
    <groupId>org.apache.flink</groupId>
    <artifactId>flink-statebackend-rocksdb_${scala.binary.version}</artifactId>
    <version>${flink.version}</version>
</dependency>
```
# 4. checkpoint检查点
## 4.1 启用检查点
```
StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
//CheckpointingMode.AT_LEAST_ONCE
env.enableCheckpointing(100, CheckpointingMode.EXACTLY_ONCE);
```
## 4.2 仅仅一次(Exactly Once) 和 至少一次(At Least Once)

## 4.1 barries

# 5. savepoint保存点
使用checkpoint的应用都可以从savepoint来恢复，  
savepoint是手动的checkpoint，由用户自己触发，并且在新的检查点未完成时，不会自动过期
一般用来做程序升级

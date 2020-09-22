# Flink操作kafka参考文档
[kafka连接器](https://ci.apache.org/projects/flink/flink-docs-release-1.11/zh/dev/connectors/kafka.html)
## 消费者
### 分区发现
参考Consumer.java
### 动态topic
参考Consumer.java
### 提交Offset
#### 禁用checkpoint
如果禁用，Flink Kafka Consumer 依赖于内部使用的 Kafka client 自动定期 offset 提交功能
```
// 启用kafka自身的offset提交功能，如下为默认配置
props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,Boolean.TRUE);
props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG,5000);
```
#### 启用checkpoint
如果启用，当checkpoint完成时，Flink Kafka Consumer 将提交的offset存储在checkpoint状态中。
这时候Kafka broker 中提交的 offset 与 checkpoint 状态中的 offset 一致。
```
//禁用或启用offset提交，这时Properties配置的enable.auto.commit、auto.commit.interval.ms将被忽略
flinkKafkaConsumer.setCommitOffsetsOnCheckpoints(Boolean.FALSE);
```

## 生产者
### 数据分区
指定数据发送到topic中的那个分区,默认FlinkFixedPartitioner
```
//自定义分区方法
extends FlinkKafkaPartitioner 
```
### 数据丢失
使在 Kafka 确认写入后，仍然可能会遇到数据丢失
* acks
* log.flush.interval.messages
* log.flush.interval.ms
* log.flush.*

上述选项的默认值是很容易导致数据丢失
# Kafka参考文档

[Kafka快速入门](https://kafka.apache.org/documentation.html#quickstart)

# 1.开发FLink程序基本步骤

* 获取执行环境
* 加载/读取数据
* 转换和处理数据
* 输出结果到指定地方
* 启动执行

# 2.启动本地测试环境
## 2.1 pom引用
```
<dependency>
    <groupId>org.apache.flink</groupId>
    <artifactId>flink-core</artifactId>
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

## 2.2 java

```
Configuration conf = new Configuration();
// 默认端口：8081
conf.setInteger(RestOptions.PORT,8081);
StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(conf);
```
启动完成后直接访问
[http://localhost:8081/](http://localhost:8081/)

# 3.Iterations


# 4. Task 故障恢复 

当 Task 发生故障时，Flink 需要重启出错的 Task 以及其他受到影响的 Task ，以使得作业恢复到正常执行状态。

Flink 通过重启策略和故障恢复策略来控制 Task 重启：重启策略决定是否可以重启以及重启的间隔；故障恢复策略决定哪些 Task 需要重启。

## 4.1 重启策略 Restart Strategies
应用代码中配置会覆盖全局设置
### 4.1.1 none, off, disable 无重启
* 全局配置flink-conf.yaml配置
```
restart-strategy: none
```
* 代码中实现
```
StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
env.setRestartStrategy(RestartStrategies.noRestart());
```
### 4.1.2 fixeddelay, fixed-delay 固定间隔
* 全局配置flink-conf.yaml配置
```
restart-strategy: fixed-delay
#重启次数
restart-strategy.fixed-delay.attempts: 1
#每次重启之间的时间间隔，"1 min", "20 s"
restart-strategy.fixed-delay.delay: 1 s
```
* 代码中实现
```
StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
env.setRestartStrategy(RestartStrategies.fixedDelayRestart(
				//重启次数
				3,
				//两次重启之间的时间间隔
				org.apache.flink.api.common.time.Time.seconds(1))
		);
```
### 4.1.3 failurerate, failure-rate 失败率 
* 全局配置flink-conf.yaml配置
```
restart-strategy: failure-rate
# 2次重启之间的的时间间隔
restart-strategy.failure-rate.delay: 1 s
#衡量失败次数的是时间段
restart-strategy.failure-rate.failure-rate-interval: 1 min
#每次重启之间的时间间隔
restart-strategy.failure-rate.max-failures-per-interval: 1
```
* 代码中实现
```
StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
// 如果5分钟内，每10s尝试重启一次，如果连续重启失败次数超过3次，那么重启失败
env.setRestartStrategy(RestartStrategies.failureRateRestart(
				//时间段内的最大失败次数
				3,
				//衡量失败次数的时间段
				org.apache.flink.api.common.time.Time.minutes(5),
				//两次重启之间的时间间隔
				org.apache.flink.api.common.time.Time.seconds(10))
		);
```
### 4.1.4 Fallback Restart Strategy
使用集群定义的重启策略

## 4.2 失败策略Failover Strategies
 flink-conf.yaml 中的 jobmanager.execution.failover-strategy
### Restart All Failover Strategy
发生故障时候，会重启作业中的所有Task进行故障恢复
* 全局配置flink-conf.yaml配置
```
jobmanager.execution.failover-strategy: full
```
### Restart Pipelined Region Failover Strategy 默认配置
重启启动受故障影响的所有任务
```
jobmanager.execution.failover-strategy: region
```
* 出错Task所在的region需要重启
* 重启 region 需要消费的数据无法访问，对应的该部分数据的region也需要重启
* 重启 region 的下游region也需要重启。这是出于保障数据一致性的考虑，因为一些非确定性的计算或者分发会导致同一个 Result Partition 每次产生时包含的数据都不相同
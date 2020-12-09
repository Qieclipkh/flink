# 1. source
```
// 旧API,目前基于这个实现
implements SourceFunction
// 新API，
implements Source
```

# 将数据源标记为临时空闲
```
// StreamStatus
SourceFunction.SourceContext#markAsTemporarilyIdle
```
就是数据源不产生数据的时候，避免下游计算无限制等待不执行，结合watermark理解
FlinkKafkaConsumer中有使用

# 2. sink
# 4. checkpoint检查点

## 4.1 启用检查点

```
 CheckpointConfig checkpointConfig = env.getCheckpointConfig();
 checkpointConfig.setCheckpointInterval(100);
 checkpointConfig.setCheckpointingMode(CheckpointConfig.DEFAULT_MODE);
 // 设置checkpoint超时时间，超时后进行中的checkpoint会被抛弃
 checkpointConfig.setCheckpointTimeout(1*60*1000);
 // 2个checkpoint之间的时间间隔，在前一个checkpoint完成后500毫秒以后才会进行下一个checkpoint
 checkpointConfig.setMinPauseBetweenCheckpoints(500);
 // 同一时间只允许1个checkpoint进行
 checkpointConfig.setMaxConcurrentCheckpoints(1);
 // RETAIN_ON_CANCELLATION 当作业取消时，保留作业的 checkpoint; DELETE_ON_CANCELLATION 当作业取消时，删除作业的 checkpoint,仅当作业失败时，作业的 checkpoint 才会被保留
 checkpointConfig.enableExternalizedCheckpoints(CheckpointConfig.ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION);
 // job优先从checkpoint来回退，即使有更近的savepoint(checkpoint比savepoint恢复更快)
 checkpointConfig.setPreferCheckpointForRecovery(true);
```


package com.cly.base.util;

import org.apache.flink.api.common.ExecutionConfig;
import org.apache.flink.api.common.ExecutionMode;
import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.runtime.state.StateBackend;
import org.apache.flink.runtime.state.memory.MemoryStateBackend;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.environment.CheckpointConfig;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * 设置运行环境参数
 */
public class EnvConfigUtil {
    /**
     * 设置执行环境参数
     * @see <a href="https://ci.apache.org/projects/flink/flink-docs-release-1.11/zh/dev/execution_configuration.html">https://ci.apache.org/projects/flink/flink-docs-release-1.11/zh/dev/execution_configuration.html</a>
     * @param env
     */
    public static void setEnvConfg(StreamExecutionEnvironment env) {
        ExecutionConfig config = env.getConfig();
        //设置job的并行度
        config.setParallelism(2);
		/*
			设置缓冲区最大等待时间。在此时间之后，即使缓冲区未满，也会自动发送数据，默认100毫秒
			-1,消除超时，仅在缓冲区已满时，才刷新他们，提高吞吐量
			避免设置为0，导致严重的性能下降
		 */
        env.setBufferTimeout(100l);
        /**
         *
         * 默认重启策略在flink-conf.yaml
         * 前提是开启检查点
         * 设置从重启策略，覆盖集群默认配置
         */
        //启用检查点
        CheckpointConfig checkpointConfig = env.getCheckpointConfig();
        checkpointConfig.setCheckpointInterval(20*1000);
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
        StateBackend stateBackend = //new RocksDBStateBackend("hdfs://master:9000/flink/checkpoint/rdb1");
                new MemoryStateBackend(10*1024*1024);
        env.setStateBackend(stateBackend);
        // 设置故障恢复策略
        /*env.setRestartStrategy(RestartStrategies.fixedDelayRestart(
                //重启次数
                3,
                //两次重启之间的时间间隔
                org.apache.flink.api.common.time.Time.seconds(1))
        );*/
    }
}

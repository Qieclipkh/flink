package org.cly.base.stream;

import org.apache.flink.api.common.ExecutionConfig;
import org.apache.flink.api.common.ExecutionMode;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.utils.MultipleParameterTool;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.IterativeStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.util.Collector;
import org.cly.base.TestData;

public class StreamingJob {

	public static void main(String[] args) throws Exception {
		//接收配置参数，--input xxx --parallelism 2
		MultipleParameterTool params = MultipleParameterTool.fromArgs(args);

		//1.获取执行环境
		final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
		// 1.1 设置执行环境参数
		setEnvConfg(env);
		//2. 加载/读取数据
		DataStreamSource<String> text = env.fromElements(TestData.WORDS);
		//3. 转换和处理数据
		DataStream<Tuple2<String, Integer>> sum = text.flatMap(new Splitter())
				.keyBy(value -> value.f0)
				.timeWindow(Time.seconds(5))
				.sum(1);
		//4. 输出结果到指定地方
		sum.print();
		//5. 启动执行
		env.execute("Flink Streaming Java API Skeleton");
	}

	/**
	 * 设置执行环境参数
	 * @see <a href="https://ci.apache.org/projects/flink/flink-docs-release-1.11/zh/dev/execution_configuration.html">https://ci.apache.org/projects/flink/flink-docs-release-1.11/zh/dev/execution_configuration.html</a>
	 * @param env
	 */
	private static void setEnvConfg(StreamExecutionEnvironment env) {
		ExecutionConfig config = env.getConfig();
		//设置job的并行度
		config.setParallelism(1);
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
		env.getConfig().setExecutionMode(ExecutionMode.PIPELINED);
		//启用检查点
		env.enableCheckpointing(100, CheckpointingMode.EXACTLY_ONCE);
		env.setRestartStrategy(RestartStrategies.fallBackRestart());
		env.setRestartStrategy(RestartStrategies.noRestart());
		env.setRestartStrategy(RestartStrategies.fixedDelayRestart(
				//重启次数
				3,
				//两次重启之间的时间间隔
				org.apache.flink.api.common.time.Time.seconds(1))
		);
		env.setRestartStrategy(RestartStrategies.failureRateRestart(
				//时间段内的最大失败次数
				3,
				//衡量失败次数的时间段
				org.apache.flink.api.common.time.Time.minutes(5),
				//两次重启之间的时间间隔
				org.apache.flink.api.common.time.Time.seconds(10))
		);
	}


	public static class Splitter implements FlatMapFunction<String, Tuple2<String, Integer>> {
		@Override
		public void flatMap(String sentence, Collector<Tuple2<String, Integer>> out) throws Exception {
			for (String word: sentence.split(" ")) {
				out.collect(new Tuple2<String, Integer>(word, 1));
			}
		}
	}
}

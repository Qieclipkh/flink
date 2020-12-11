package com.cly.base;

import com.cly.base.function.SplitWord;
import com.cly.data.WordData;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class WordCount {

	public static void main(String[] args) throws Exception {

		//1.获取执行环境
		final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
		//2. 加载/读取数据源
		DataStreamSource<String> text = env.fromElements(WordData.WORDS);
		//3. 处理数据
		DataStream<Tuple2<String, Integer>> sum = text.flatMap(new SplitWord())
				.keyBy(value -> value.f0)
				.sum(1);
		//4. 输出结果到指定地方
		sum.print();
		//5. 启动执行
		env.execute("test word count");
	}

}

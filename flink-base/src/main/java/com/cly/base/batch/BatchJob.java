package com.cly.base.batch;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.util.Collector;
import com.cly.data.WordData;

public class BatchJob {
	public static void main(String[] args) throws Exception {
		//1、获取执行坏境
		ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
		//2、加载/读取数据
		final DataSet<String> source = env.fromElements(WordData.WORDS);
		//3、转换和处理数据
		final DataSet<Tuple2<String, Integer>> sum = source
				.flatMap(new FlatMapFunction<String, Tuple2<String, Integer>>() {
					@Override
					public void flatMap(String value, Collector<Tuple2<String, Integer>> out) throws Exception {
						//根据空白字符分割
						//String[] s1 = value.split("\\s+");
						// 根据非单词字符匹配
						String[] s1 = value.split("\\W+");
						for (String s : s1) {
							if (s != null && s.length() > 0) {
								out.collect(Tuple2.of(s, 1));
							}
						}
					}
				})
				.groupBy(0)
				.sum(1);
		//4、输出结果到指定地方
		sum.printOnTaskManager("结果");//将结果打印在TaskManager的.out文件中
		//sum.print();//调用System#out输出，会默认执行
		//5、启动执行
		env.execute("批处理-WordCount");
	}




}

package com.cly.state.statebackend;

import com.cly.state.statebackend.bean.ApacheLogEvent;
import com.cly.state.statebackend.func.PageCountAggFunc;
import com.cly.state.statebackend.func.PageCountAggWindowFunc;
import com.cly.state.statebackend.func.ToApacheLogEvent;
import com.cly.state.statebackend.func.TopNProcessFunc;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.time.Time;

import java.time.Duration;


/**
 * 实时统计热门页面 每隔5秒统计最近5分钟的热门页面
 * 1. 读取数据
 * 2. 添加水位
 * 3. 按URL分组
 * 4. 统计窗口数据
 * 5. 根据窗口分组
 * 6. 窗口数据排序
 * 7. 打印输出
 */
public class HostPage {

    public static void main(String[] args) throws Exception {
        // 获取执行环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        // 2个并发（2个 TaskSlot）
        env.setParallelism(2);
        env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);

        env
                /**
                 * 加载数据，clazz:/ 是自定义的FileSystem,能够读取resource下面的文件
                 * 数据输出： 同一个TaskSlot
                 *     字符串=》93.114.45.13 - - 17/05/2015:10:05:04 +0000 GET /reset.css
                 *            93.114.45.13 - - 17/05/2015:10:05:45 +0000 GET /style2.css
                 */
                .readTextFile("clazz:/apache2.log")
                /**
                 * 将读取的字符串数据转换成 ApacheLogEvent 实体对象
                 * 数据输出：同一个TaskSlot
                 *   ApacheLogEvent{ip='93.114.45.13', userId='-', eventTime=1431828304000, method='GET', url='/reset.css'}
                 *   ApacheLogEvent{ip='93.114.45.13', userId='-', eventTime=1431828345000, method='GET', url='/style2.css'}
                 */
                .map(new ToApacheLogEvent())
                // 设置Watermark,允许数据迟到10秒
                .assignTimestampsAndWatermarks(watermark())
                /**
                 * 根据ApacheLogEvent#getUrl对数据进行分组,数据分别发送到了2个不同的slot
                 *   数据输出：
                 *       TaskSlot1 => ApacheLogEvent{ip='93.114.45.13', userId='-', eventTime=1431828304000, method='GET', url='/reset.css'}
                 *       TaskSlot2 => ApacheLogEvent{ip='93.114.45.13', userId='-', eventTime=1431828345000, method='GET', url='/style2.css'}
                 *
                 */
                .keyBy((KeySelector<ApacheLogEvent, String>) value -> value.getUrl())
                // 设置计算时间窗口（滑动窗口）
                .timeWindow(Time.minutes(5L), Time.seconds(5L))
                /**
                 * 窗口URL进行统计，得到在一个窗口期间每个URL的访问次数
                 * 数据输出：
                 *      PageCountAgg()//每接收到一条数据，处理一条
                 *          TaskSlot1 => 1
                 *          TaskSlot2 => 1
                 *      CountAggWindowResult()// 在窗口时间结束后，触发运行一次
                 *          TaskSlot1 => PageViewResult(${窗口开始时间},${窗口结束时间},"/reset.css",1)
                 *          TaskSlot2 => PageViewResult(${窗口开始时间},${窗口结束时间},"/style2.css",1)
                 *
                 *  最后的到每个URL的窗口期间内的访问次数
                 */
                .aggregate(new PageCountAggFunc(), new PageCountAggWindowFunc())
                /**
                 * 按照窗口进行分区，具有相同窗口的数据分配到同一个TaskSlot上
                 */
                .keyBy(value -> value.getWindowTimeEndL())
                /**
                 * 实现排序逻辑
                 */
                .process(new TopNProcessFunc(5))
                .print()
        ;
        env.execute("Host Page");
    }

    /**
     * 设置Watermark侧率
     *
     * @return
     */
    private static WatermarkStrategy<ApacheLogEvent> watermark() {
        WatermarkStrategy<ApacheLogEvent> watermarkStrategy = WatermarkStrategy
                //调用静态方法泛型
                .<ApacheLogEvent>forBoundedOutOfOrderness(Duration.ofSeconds(10))
                .withTimestampAssigner((element, recordTimestamp) -> element.getEventTime());
        return watermarkStrategy;
    }
}

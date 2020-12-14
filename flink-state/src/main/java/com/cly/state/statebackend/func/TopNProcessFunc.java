package com.cly.state.statebackend.func;

import com.cly.state.statebackend.bean.PageViewResult;
import org.apache.flink.api.common.state.ListState;
import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.api.common.state.MapState;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;
import scala.collection.mutable.ListBuffer;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.ToLongFunction;
import java.util.stream.Collectors;

/**
 * Long: key的数据类型（窗口结束时间），前面算子keyBy的指定的key
 * PageViewResult: 输入数据类型
 * String: 输出数据类型
 */
public class TopNProcessFunc extends KeyedProcessFunction<Long, PageViewResult, String> {

    private Integer topSize;

    public TopNProcessFunc(Integer topSize) {
        this.topSize = topSize;
    }


    private transient ListState<PageViewResult> pageViewCountListState;
    private transient MapState<String,Long> pageViewCountMapState;

    /**
     * 初始化状态信息
     * @param parameters
     * @throws Exception
     */
    @Override
    public void open(Configuration parameters) throws Exception {
        ListStateDescriptor<PageViewResult> descriptor = new ListStateDescriptor<PageViewResult>(
                "url-state-count",
                PageViewResult.class
        );
        pageViewCountListState = getRuntimeContext().getListState(descriptor);
    }

    /**
     * 每接收一条数据处理一次
     * 处理每条流入的数据，
     * @param value
     * @param ctx
     * @param out
     * @throws Exception
     */
    @Override
    public void processElement(PageViewResult value, Context ctx, Collector<String> out) throws Exception {
        // 接收一条数据，将数据存储在状态中
        pageViewCountListState.add(value);
        /**
         * //TODO 为什么要弄个定时器？
         * 注册 windowEnd+1 的 EventTime 定时器, 当触发时，说明收齐了属于windowEnd窗口的所有商品数据
         * 每一条数据都会注册，为什么不会重复执行呢？
         * 由于Flink对（每个key+timestatmp）只维护一个定时器。如果相同的timestamp注册多个Timer,则只调用onTimer方法一次
         */
        ctx.timerService().registerEventTimeTimer(value.getWindowTimeEndL() + 1);
        /**
         * 定义1分钟之后的定时器，用于清楚状态
         */
        ctx.timerService().registerEventTimeTimer(value.getWindowTimeEndL()+ 60*1000L);
    }
    /**
     * //TODO TimerService
     * 1.processing-time/event-time timer都由 TimerService在内部维护并排队等待执行
     * 2.仅在keyed stream中有效
     * 3.由于Flink对（每个key+timestatmp）只维护一个定时器。如果相同的timestamp注册多个Timer,则只调用onTimer方法一次
     * 4.Flink保证同步调用 processElement() 和 onTimer()。因此用户不必担心状态的并发修改
     */
    /**
     * 定时回调方法
     * @param timestamp
     * @param ctx
     * @param out
     * @throws Exception
     */
    @Override
    public void onTimer(long timestamp, OnTimerContext ctx, Collector<String> out) throws Exception {
        // 作用是什么？
        if(timestamp == (ctx.getCurrentKey() + 60 * 1000L)){
            pageViewCountListState.clear();
            return;
        }
        ListBuffer<PageViewResult> s = new ListBuffer<>();
        List<PageViewResult> allItems = new ArrayList<>();
        for (PageViewResult item : pageViewCountListState.get()) {
            allItems.add(item);
        }
        // 清空操作
        pageViewCountListState.clear();

        List<PageViewResult> pageViewResults = allItems.stream().sorted(Comparator.comparingLong(new ToLongFunction<PageViewResult>() {

            @Override
            public long applyAsLong(PageViewResult value) {
                return value.getCount();
            }
        })).limit(topSize).collect(Collectors.toList());

        StringBuilder result = new StringBuilder();
        result.append("==================================\n");
        result.append("窗口结束时间：").append(new Timestamp(timestamp - 1)).append("\n");
        int number = 0;
        for (PageViewResult pageViewResult : pageViewResults) {
            result.append("NO").append(number + 1).append(":")
                    .append(" 页面url=").append(pageViewResult.getUrl())
                    .append(" 访问量=").append(pageViewResult.getCount())
                    .append("\n");
            number++;
        }
        out.collect(result.toString());


    }



}

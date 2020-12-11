package com.cly.state.statebackend.func;

import com.cly.state.statebackend.bean.PageViewResult;
import org.apache.flink.api.common.state.ListState;
import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.api.common.state.MapState;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.ToLongFunction;
import java.util.stream.Collectors;

public class TopNProcessFunc extends KeyedProcessFunction<Long, PageViewResult, String> {

    private Integer topSize;

    public TopNProcessFunc(Integer topSize) {
        this.topSize = topSize;
    }


    private transient ListState<PageViewResult> pageViewCountListState;
    private transient MapState<String,Long> pageViewCountMapState;

    /**
     * 每接收一条数据处理一次
     *
     * @param value
     * @param ctx
     * @param out
     * @throws Exception
     */
    @Override
    public void processElement(PageViewResult value, Context ctx, Collector<String> out) throws Exception {
        // 接收一条数据，将数据存储在状态中
        pageViewCountListState.add(value);
        // 注册 windowEnd+1 的 EventTime 定时器, 当触发时，说明收齐了属于windowEnd窗口的所有商品数据
        ctx.timerService().registerEventTimeTimer(value.getWindowTimeEndL() + 1);
    }


    @Override
    public void onTimer(long timestamp, OnTimerContext ctx, Collector<String> out) throws Exception {
        /* 作用是什么？
        if(timestamp == (ctx.getCurrentKey() + 60 * 1000L)){
            pageViewCountMapState.clear();
            return;
        }*/

        List<PageViewResult> allItems = new ArrayList<>();
        for (PageViewResult item : pageViewCountListState.get()) {
            allItems.add(item);
        }

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


    @Override
    public void open(Configuration parameters) throws Exception {
        ListStateDescriptor<PageViewResult> descriptor = new ListStateDescriptor<PageViewResult>(
                "url-state-count",
                PageViewResult.class
        );
        pageViewCountListState = getRuntimeContext().getListState(descriptor);
    }

}

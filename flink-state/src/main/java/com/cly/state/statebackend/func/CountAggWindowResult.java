package com.cly.state.statebackend.func;

import com.cly.state.statebackend.bean.PageViewResult;
import org.apache.flink.streaming.api.functions.windowing.WindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 窗口结束的时候运行一次
 */
public class CountAggWindowResult implements WindowFunction<Long, PageViewResult, String, TimeWindow> {
    /**
     *
     * @param url key支持
     * @param window 窗口
     * @param input 输入数据
     * @param out 输出数据
     * @throws Exception
     */
    @Override
    public void apply(String url, TimeWindow window, Iterable<Long> input, Collector<PageViewResult> out) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        out.collect(new PageViewResult(sdf.format(new Date(window.getStart())),sdf.format(new Date(window.getEnd())),window.getEnd(), url, input.iterator().next()));
    }
}

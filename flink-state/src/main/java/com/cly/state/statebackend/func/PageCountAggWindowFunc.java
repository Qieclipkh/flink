package com.cly.state.statebackend.func;

import com.cly.state.statebackend.bean.PageViewResult;
import org.apache.flink.streaming.api.functions.windowing.WindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 窗口结束的时候运行一次
 *
 * Long: 输入数据类型
 * PageViewResult: 输出数据类型
 * String: key的数据类型（访问URL），前面算子keyBy的指定的key
 * TimeWindow: 窗口类型
 */
public class PageCountAggWindowFunc implements WindowFunction<Long, PageViewResult, String, TimeWindow> {
    /**
     *
     * @param url 算子keyBy的指定的key
     * @param window 窗口类型
     * @param in 输入数据,当前function只在窗口运行结束后运行一次，所以in虽然是个迭代器，实际上只存在一个值
     * @param out 输出数据
     * @throws Exception
     */
    @Override
    public void apply(String url, TimeWindow window, Iterable<Long> in, Collector<PageViewResult> out) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        out.collect(new PageViewResult(sdf.format(new Date(window.getStart())),sdf.format(new Date(window.getEnd())),window.getEnd(), url, in.iterator().next()));
    }
}

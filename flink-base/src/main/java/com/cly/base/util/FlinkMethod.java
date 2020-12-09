package com.cly.base.util;

import org.apache.flink.api.java.utils.MultipleParameterTool;

/**
 * Flink 包中存在的一些方便的方法
 */
public class FlinkMethod {
    public static void main(String[] args) {
        //接收配置参数，--input xxx --parallelism 2
        MultipleParameterTool params = MultipleParameterTool.fromArgs(args);

    }
}

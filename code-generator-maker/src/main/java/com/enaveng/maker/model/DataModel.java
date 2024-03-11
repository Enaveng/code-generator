package com.enaveng.maker.model;

import lombok.Data;

/**
 * 生成动态文件的数据模型
 */
@Data
public class DataModel {

    /**
     * 添加默认值
     */

    //是否支持循环输入
    private boolean loop;

    //作者注释
    private String author = "dlwlrma";

    //输出信息
    private String outputText = "sum = ";

}

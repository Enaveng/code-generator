package com.enaveng.maker.template.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * 文件过滤配置
 */
@Data
@Builder //使用该注解之后会默认为类添加全参构造函数 覆盖原有的无参构造函数
public class FileFilterConfig {
    /**
     * 过滤范围
     */
    private String range;

    /**
     * 过滤规则
     */
    private String rule;


    /**
     * 过滤值
     */
    private String value;

}

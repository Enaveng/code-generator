package com.enaveng.generatorweb.model.dto.generator;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * 在线使用生成器实体类
 */
@Data
public class GeneratorRequest implements Serializable {
    /**
     * 生成器id
     */
    private Long id;

    /**
     * 数据模型
     */
    Map<String, Object> dataModel;

    private static final long serialVersionUID = 1L;
}

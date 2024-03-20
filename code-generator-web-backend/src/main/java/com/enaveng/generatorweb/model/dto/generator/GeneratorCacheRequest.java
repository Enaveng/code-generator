package com.enaveng.generatorweb.model.dto.generator;

import lombok.Data;

import java.io.Serializable;

@Data
public class GeneratorCacheRequest implements Serializable {

    private Long id;

    private String distPath;

    private static final long serialVersionUID = 1L;

}

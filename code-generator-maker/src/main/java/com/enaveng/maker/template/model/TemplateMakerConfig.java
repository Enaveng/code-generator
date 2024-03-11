package com.enaveng.maker.template.model;

import com.enaveng.maker.meta.Meta;
import lombok.Data;

@Data
public class TemplateMakerConfig {
    private Meta meta = new Meta();

    private long id;

    private String originProjectPath;

    TemplateMakerFileConfig fileConfig = new TemplateMakerFileConfig();

    TemplateMakerModelConfig modelConfig = new TemplateMakerModelConfig();

    TemplateMakerOutputConfig outputConfig = new TemplateMakerOutputConfig();
}

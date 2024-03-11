package com.enaveng.maker.template.model;


import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
public class TemplateMakerFileConfig {

    private List<FileInfoConfig> files;

    private FileGroupConfig fileGroupConfig;


    @Data
    @NoArgsConstructor
    public static class FileInfoConfig {

        /**
         * 文件目录(路径)
         */
        private String path;

        List<FileFilterConfig> fileFilterConfigList;

    }

    @Data
    public static class FileGroupConfig {

        private String condition;

        private String groupKey;

        private String groupName;

    }
}

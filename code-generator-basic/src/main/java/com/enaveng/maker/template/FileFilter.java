package com.enaveng.maker.template;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.io.FileUtil;
import com.enaveng.maker.template.Enums.FileFilterRangeEnum;
import com.enaveng.maker.template.Enums.FileFilterRuleEnum;
import com.enaveng.maker.template.model.FileFilterConfig;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class FileFilter {

    /**
     * 对文件或者目录进行过滤
     *
     * @param filePath
     * @param fileFilterConfigList
     * @return
     */
    public static List<File> doFilter(String filePath, List<FileFilterConfig> fileFilterConfigList) {
        //获取路径下所有的文件
        List<File> fileList = FileUtil.loopFiles(filePath);
//        List<File> fileArrayList = new ArrayList<>();
//        for (File file : fileList) {
//            boolean result = doSingerFileFilter(fileFilterConfigList, file);
//            if (result) {
//                fileArrayList.add(file);
//            }
//        }
//        return fileArrayList;
        return fileList.stream()
                .filter(file -> doSingerFileFilter(fileFilterConfigList, file))
                .collect(Collectors.toList());
    }


    /**
     * 对单个文件进行过滤操作
     *
     * @param fileFilterConfigList 过滤配置
     * @param file                 需要进行过滤的文件
     * @return 是否保留
     */
    public static boolean doSingerFileFilter(List<FileFilterConfig> fileFilterConfigList, File file) {
        String fileName = file.getName(); //得到文件名
        String fileContent = FileUtil.readUtf8String(file); //得到文件内容

        boolean result = true;

        if (CollUtil.isEmpty(fileFilterConfigList)) {
            return true;
        }

        for (FileFilterConfig fileFilterConfig : fileFilterConfigList) {
            String rang = fileFilterConfig.getRange();   //过滤范围
            String rule = fileFilterConfig.getRule();   //过滤规则
            String value = fileFilterConfig.getValue(); //具体过滤值

            //1.先判断过滤范围是什么
            //根据value得到对应的枚举
            FileFilterRangeEnum rangeEnum = FileFilterRangeEnum.getEnumByValue(rang);
            if (rangeEnum == null) {
                continue;
            }
            String content = fileName;
            switch (rangeEnum) {
                case FILE_NAME:
                    content = fileName;
                    break;
                case FILE_CONTENT:
                    content = fileContent;
                    break;
                default:
            }

            //2.判断具体的过滤规则
            FileFilterRuleEnum ruleEnum = FileFilterRuleEnum.getEnumByValue(rule);
            if (ruleEnum == null) {
                continue;
            }
            switch (ruleEnum) {
                case CONTAINS:
                    result = content.contains(value); //判断内容
                    break;
                case STARTS_WITH:
                    result = content.startsWith(value);
                    break;
                case ENDS_WITH:
                    result = content.endsWith(value);
                    break;
                case REGEX:
                    result = content.matches(value);
                    break;
                case EQUALS:
                    result = content.equals(value);
                    break;
                default:
            }

            // 有一个不满足，就直接返回
            if (!result) {
                return false;
            }

        }

        return true;
    }
}

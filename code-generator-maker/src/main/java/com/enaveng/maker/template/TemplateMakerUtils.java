package com.enaveng.maker.template;

import cn.hutool.core.util.StrUtil;
import com.enaveng.maker.meta.Meta;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 模板制作工具类  用于将Meta.json的文件组配置与组外配置进行去重
 */
public class TemplateMakerUtils {


    //在生成完对应的FileInfoList之后进行
    public static List<Meta.FileConfig.FileInfo> removeRepetitionGroupFile(List<Meta.FileConfig.FileInfo> fileInfoList) {
        // 1. 获取到所有的有分组的集合
        List<Meta.FileConfig.FileInfo> groupFileInfoList = fileInfoList.stream()
                .filter(fileInfo -> StrUtil.isNotBlank(fileInfo.getGroupKey()))
                .collect(Collectors.toList());

        // 2. 获取到所有分组内的文件列表
        List<Meta.FileConfig.FileInfo> fileInnerList = groupFileInfoList.stream()
                .flatMap(fileInfo -> fileInfo.getFiles().stream())
                .collect(Collectors.toList());

        // 3. 获取所有分组内的文件输入路径集合
        Set<String> fileInputPathList = fileInnerList.stream()
                .map(Meta.FileConfig.FileInfo::getInputPath)
                .collect(Collectors.toSet());

        // 4. 利用上述集合，移除所有输入路径在集合中的外层文件
        return fileInfoList.stream()
                .filter(fileInfo -> !fileInputPathList.contains(fileInfo.getInputPath())) //只保留外层文件与组内文件inputPath不相同的
                .collect(Collectors.toList());
    }


}

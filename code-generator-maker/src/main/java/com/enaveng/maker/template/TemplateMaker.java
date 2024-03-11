package com.enaveng.maker.template;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.enaveng.maker.meta.Enums.FileGenerateTypeEnum;
import com.enaveng.maker.meta.Enums.FileTypeEnum;
import com.enaveng.maker.meta.Meta;
import com.enaveng.maker.template.model.TemplateMakerConfig;
import com.enaveng.maker.template.model.TemplateMakerFileConfig;
import com.enaveng.maker.template.model.TemplateMakerModelConfig;
import com.enaveng.maker.template.model.TemplateMakerOutputConfig;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 预期是以ACM示例模板项目为根目录，使用outputText模型参数来替换其src/com/yupi/acm/MainTemplate.java文件中的Sum:输出信息，
 * 并在同包下生成“挖好坑”的MainTemplate.java.ftl模板文件，以及在根目录下生成meta.json元信息文件。
 */
//实现模板制作工具 即自动生成对应的ftl模板文件以及meta配置文件
public class TemplateMaker {

    //简化参数的传递 添加一个重载的方法
    public static long makeTemplate(TemplateMakerConfig templateMakerConfig) {
        Meta meta = templateMakerConfig.getMeta();
        long id = templateMakerConfig.getId();
        String originProjectPath = templateMakerConfig.getOriginProjectPath();
        TemplateMakerFileConfig fileConfig = templateMakerConfig.getFileConfig();
        TemplateMakerModelConfig modelConfig = templateMakerConfig.getModelConfig();
        TemplateMakerOutputConfig outputConfig = templateMakerConfig.getOutputConfig();
        long resultId = makeTemplate(meta, originProjectPath, modelConfig, fileConfig, id, outputConfig);
        return resultId;
    }


    /**
     * 制作完整代码模板
     *
     * @param newMeta
     * @param originFilePath
     * @param templateMakerModelConfig
     * @param templateMakerFileConfig
     * @param id
     * @return
     */
    public static long makeTemplate(Meta newMeta, String originFilePath, TemplateMakerModelConfig templateMakerModelConfig, TemplateMakerFileConfig templateMakerFileConfig, Long id, TemplateMakerOutputConfig templateMakerOutputConfig) {
        if (id == null) {
            id = IdUtil.getSnowflakeNextId();
        }
        //为了保持源文件内容的一致 采取工作空间的隔离 将原始的项目文件拷贝到新的目录空间当中
        String projectPath = System.getProperty("user.dir");
//        String originFilePath = new File(projectPath).getParent() + File.separator + "code-generator-demo-projects/acm-template";
        //进行目录的复制
        String tempDirPath = new File(projectPath) + File.separator + ".temp";
        String templatePath = tempDirPath + File.separator + id;
        //创建目录  目录不存在表示第一次制作模板文件需要对源文件进行复制
        if (!FileUtil.exist(templatePath)) {
            FileUtil.mkdir(templatePath);
            //进行复制
            FileUtil.copy(originFilePath, templatePath, true);
        }
//        String sourceFilePath = templatePath + File.separator + FileUtil.getLastPathEle(Paths.get(originFilePath)).toString();
        //当已经生成过工作空间目录之后 第二次生成模型配置就不需要传递originFilePath   直接读取工作空间下的第一个目录来得到sourceFilePath
        //先遍历 再获取
        String sourceFilePath = FileUtil.loopFiles(new File(templatePath), 1, null)
                .stream()
                .filter(File::isDirectory)
                .findFirst()
                .orElseThrow(RuntimeException::new)
                .getAbsolutePath();

        //以上生成的路径sourceFilePath带有"\\"  需要进行处理
        sourceFilePath = sourceFilePath.replaceAll("\\\\", "/");


        //获取封装之后的FileInfoList对象
        List<Meta.FileConfig.FileInfo> newFileInfoList = getFileInfoList(templateMakerModelConfig, templateMakerFileConfig, sourceFilePath);

        //获取封装之后的ModelInfoList对象
        List<Meta.ModelConfig.ModelInfo> newModelInfoList = getModelInfoList(templateMakerModelConfig);


        //生成配置文件的路径  将生成meta文件的路径修改为与项目目录平级
        String metaJsonPath = templatePath + File.separator + "meta.json";
        //如果存在meta文件表示不是第一次生成模板
        if (FileUtil.exist(metaJsonPath)) {
            //读取已经生成的配置文件进行追加操作
            Meta oldMeta = JSONUtil.toBean(FileUtil.readUtf8String(metaJsonPath), Meta.class);
            //追加
            List<Meta.ModelConfig.ModelInfo> modelInfoList = oldMeta.getModelConfig().getModels();
            modelInfoList.addAll(newModelInfoList);
            List<Meta.FileConfig.FileInfo> fileInfoList = oldMeta.getFileConfig().getFiles();
            fileInfoList.addAll(newFileInfoList);
            //进行去重操作
            List<Meta.ModelConfig.ModelInfo> distinctModels = distinctModels(modelInfoList);
            List<Meta.FileConfig.FileInfo> distinctFiles = distinctFiles(fileInfoList);
            //设置新值
            oldMeta.getFileConfig().setFiles(distinctFiles);
            oldMeta.getModelConfig().setModels(distinctModels);

            //在生成元信息文件之前 需要选择对分组外和分组内的同名文件进行去重
            if (templateMakerOutputConfig != null) {
                if (templateMakerOutputConfig.isRemoveGroupFilesFromRoot()) {
                    List<Meta.FileConfig.FileInfo> fileAllInfoList = oldMeta.getFileConfig().getFiles();
                    oldMeta.getFileConfig().setFiles(TemplateMakerUtils.removeRepetitionGroupFile(fileAllInfoList));
                }
            }

            //更新元信息文件
            FileUtil.writeUtf8String(JSONUtil.toJsonPrettyStr(oldMeta), metaJsonPath);
        } else {
            //构造配置文件 是先对应生成配置文件对象 然后再转换为Json文件
//            Meta meta = new Meta();
//            meta.setName(name);
//            meta.setDescription(description);
            Meta.FileConfig fileConfig = new Meta.FileConfig();
            List<Meta.FileConfig.FileInfo> fileInfos = new ArrayList<>();
            fileConfig.setSourceRootPath(sourceFilePath);
            fileConfig.setFiles(fileInfos);
            newMeta.setFileConfig(fileConfig);

            fileInfos.addAll(newFileInfoList);

            Meta.ModelConfig modelConfig = new Meta.ModelConfig();
            newMeta.setModelConfig(modelConfig);
            List<Meta.ModelConfig.ModelInfo> modelInfos = new ArrayList<>();
            modelConfig.setModels(modelInfos);
            modelInfos.addAll(newModelInfoList);

            //在生成元信息文件之前 需要选择对分组外和分组内的同名文件进行去重
            if (templateMakerOutputConfig != null) {
                if (templateMakerOutputConfig.isRemoveGroupFilesFromRoot()) {
                    List<Meta.FileConfig.FileInfo> fileInfoList = newMeta.getFileConfig().getFiles();
                    newMeta.getFileConfig().setFiles(TemplateMakerUtils.removeRepetitionGroupFile(fileInfoList));
                }
            }

            //生成元信息文件
            FileUtil.writeUtf8String(JSONUtil.toJsonPrettyStr(newMeta), metaJsonPath);
        }
        return id;

    }

    /**
     * @param templateMakerModelConfig
     * @return
     */
    private static List<Meta.ModelConfig.ModelInfo> getModelInfoList(TemplateMakerModelConfig templateMakerModelConfig) {
        // 非空校验
        List<Meta.ModelConfig.ModelInfo> newModelInfoList = new ArrayList<>();  // 本次新增的模型配置列表
        if (templateMakerModelConfig == null) {
            return newModelInfoList;
        }

        List<TemplateMakerModelConfig.ModelInfoConfig> models = templateMakerModelConfig.getModels();
        if (CollUtil.isEmpty(models)) {
            return newModelInfoList;
        }

        // 处理模型信息
        // 将 TemplateMakerModelConfig 当中的 ModelInfoConfig 对象转换为 Meta 当中的 ModelInfo 对象
        List<Meta.ModelConfig.ModelInfo> inputModelInfoList = models.stream().map(modelInfoConfig -> {
            Meta.ModelConfig.ModelInfo modelInfo = new Meta.ModelConfig.ModelInfo();
            BeanUtil.copyProperties(modelInfoConfig, modelInfo);
            return modelInfo;
        }).collect(Collectors.toList());

        // 如果是模型组
        TemplateMakerModelConfig.ModelGroupConfig modelGroupConfig = templateMakerModelConfig.getModelGroupConfig();
        if (modelGroupConfig != null) {  //表示有模型分组配置
            Meta.ModelConfig.ModelInfo groupModelInfo = new Meta.ModelConfig.ModelInfo();
            BeanUtil.copyProperties(modelGroupConfig, groupModelInfo);

            // 模型全放到一个分组内
            groupModelInfo.setModels(inputModelInfoList);
            newModelInfoList.add(groupModelInfo);
        } else {
            // 不分组，添加所有的模型信息到列表
            newModelInfoList.addAll(inputModelInfoList);
        }
        return newModelInfoList;
    }

    /**
     * 得到文件信息列表 即将配置文件信息转换为Meta对象
     *
     * @param templateMakerModelConfig
     * @param templateMakerFileConfig
     * @param sourceFilePath
     * @return
     */
    private static List<Meta.FileConfig.FileInfo> getFileInfoList(TemplateMakerModelConfig templateMakerModelConfig, TemplateMakerFileConfig templateMakerFileConfig, String sourceFilePath) {
        List<Meta.FileConfig.FileInfo> newFileInfoList = new ArrayList<>();
        // 进行非空校验
        if (templateMakerFileConfig == null) {
            return newFileInfoList;
        }
        List<TemplateMakerFileConfig.FileInfoConfig> fileInfoConfigList = templateMakerFileConfig.getFiles();
        if (CollUtil.isEmpty(fileInfoConfigList)) {
            return newFileInfoList;
        }

        String inputFileAbsolutePath = null;
        //当输入文件为目录时
        for (TemplateMakerFileConfig.FileInfoConfig fileInfoConfig : fileInfoConfigList) {
            String fileInputPath = fileInfoConfig.getPath();
            //需要将fileInputPath原本的相对路径更改为绝对路径
            if (!fileInputPath.contains(sourceFilePath)) { //表示是相对路径
                inputFileAbsolutePath = sourceFilePath + File.separator + fileInputPath;
            }
            List<File> fileList = FileFilter.doFilter(inputFileAbsolutePath, fileInfoConfig.getFileFilterConfigList()); //得到的是过滤完成的文件
            //在过滤完需要的指定文件之后 需要再将已经生成的ftl文件过滤出来 否则第二次生成时会将ftl文件扫描到
            fileList = fileList.stream()
                    .filter(file -> !file.getAbsolutePath().endsWith(".ftl"))
                    .collect(Collectors.toList());
            for (File file : fileList) {
                Meta.FileConfig.FileInfo fileInfo = makeFileTemplate(templateMakerModelConfig, sourceFilePath, file, fileInfoConfig);
                newFileInfoList.add(fileInfo);
            }
        }
        //实现元信息文件下的分组
        TemplateMakerFileConfig.FileGroupConfig fileGroupConfig = templateMakerFileConfig.getFileGroupConfig();
        if (fileGroupConfig != null) {  //表示需要进行分组 file下还有List<FileInfo>
            String condition = fileGroupConfig.getCondition();
            String groupKey = fileGroupConfig.getGroupKey();
            String groupName = fileGroupConfig.getGroupName();

            Meta.FileConfig.FileInfo fileInfo = new Meta.FileConfig.FileInfo();
            fileInfo.setType(FileTypeEnum.GROUP.getValue());
            fileInfo.setCondition(condition);
            fileInfo.setGroupKey(groupKey);
            fileInfo.setGroupName(groupName);

            //再设置子fileInfo对象表示为一个分组
            fileInfo.setFiles(newFileInfoList);
            //将添加完成之后的meta对象进行重新设置
            newFileInfoList = new ArrayList<>();
            newFileInfoList.add(fileInfo);
        }
        return newFileInfoList;
    }

    /**
     * 生成单个模板文件
     *
     * @param templateMakerModelConfig
     * @param sourceFilePath
     * @param inputFile
     * @return
     */
    private static Meta.FileConfig.FileInfo makeFileTemplate(TemplateMakerModelConfig templateMakerModelConfig, String sourceFilePath, File inputFile,
                                                             TemplateMakerFileConfig.FileInfoConfig fileInfoConfig) {
        String fileAbsoluteInputPath = inputFile.getAbsolutePath().replaceAll("\\\\", "/");     //输出路径(绝对路径)  需要将路径当中的"\\"转换为"/"
        String fileAbsoluteOutputPath = fileAbsoluteInputPath + ".ftl"; //输入路径

        //根据绝对路径生成对应的配置信息 不需要包含sourceFilePath的内容
        String fileInputPath = fileAbsoluteInputPath.replace(sourceFilePath + "/", "");
        String fileOutputPath = fileInputPath + ".ftl";

        String fileContent; //读取文件内容

        //判断.ftl文件是否存在 如果存在表示不是第一次生成 取生成完的文件作为fileContent
        //判断逻辑修改为:先判断是否存在.ftl文件 再判断有无修改内容  修改255行业务逻辑
        boolean hasFTlFile = FileUtil.exist(fileAbsoluteOutputPath);
        if (hasFTlFile) {
            fileContent = FileUtil.readUtf8String(fileAbsoluteOutputPath);
        } else {
            fileContent = FileUtil.readUtf8String(fileAbsoluteInputPath);
        }

        // 已经拿到要替换的全文本（需要最新替换的内容）
        String newFileContent = fileContent;
        String replacement;
        // 支持多个模型：对于同一个文件的内容，遍历模型进行多轮替换
        TemplateMakerModelConfig.ModelGroupConfig modelGroupConfig = templateMakerModelConfig.getModelGroupConfig();
        for (TemplateMakerModelConfig.ModelInfoConfig modelInfoConfig : templateMakerModelConfig.getModels()) {
            String fieldName = modelInfoConfig.getFieldName();
            // 不是分组
            if (modelGroupConfig == null) {
                replacement = String.format("${%s}", fieldName);
            } else {
                String groupKey = modelGroupConfig.getGroupKey();
                replacement = String.format("${%s.%s}", groupKey, fieldName);
            }
            // 多次替换
            newFileContent = StrUtil.replace(newFileContent, modelInfoConfig.getReplaceText(), replacement);
        }


        //三.生成配置文件
        Meta.FileConfig.FileInfo fileInfo = new Meta.FileConfig.FileInfo();
        fileInfo.setInputPath(fileOutputPath);
        //单个文件是否生成
        fileInfo.setCondition(fileInfoConfig.getCondition());
        fileInfo.setType(FileTypeEnum.FILE.getValue());
        //默认为动态生成
        fileInfo.setOutputPath(fileInputPath);
        fileInfo.setGenerateType(FileGenerateTypeEnum.DYNAMIC.getValue());

        //判断生成类型是静态还是动态 即判断fileContent与newFileContent内容是否一致
        //再判断内容是否一致
        boolean equalContent = newFileContent.equals(fileContent);
        if (!hasFTlFile) { //在没有模板文件的条件下
            if (!equalContent) {  //内容不一样 表示为动态生成
                FileUtil.writeUtf8String(newFileContent, fileAbsoluteOutputPath);
            } else {       //表示为静态生成
                fileInfo.setInputPath(fileInputPath);
                fileInfo.setGenerateType(FileGenerateTypeEnum.STATIC.getValue());
            }
        } else if (!equalContent) {  //有模板文件 并且内容不一样
            FileUtil.writeUtf8String(newFileContent, fileAbsoluteOutputPath); //动态生成
        }

        return fileInfo;
    }


    /**
     * 文件去重 支持对文件组进行去重
     *
     * @param fileInfoList
     * @return
     */
    public static List<Meta.FileConfig.FileInfo> distinctFiles(List<Meta.FileConfig.FileInfo> fileInfoList) {
        //策略: 同一文件组当中的重复值进行合并  不存在分组的部分直接保留
        //1.对有分组的进行去重
        // {"groupKey": "a", "files": [1, 2]}, {"groupKey": "a", "files": [2, 3]}, {"groupKey": "b", "files": [4, 5]}
        // {"groupKey": "a", "files": [[1, 2], [2, 3]]}, {"groupKey": "b", "files": [[4, 5]]}
        //先过滤再分组  得到的是分组之后的对象 该fileInfoListMap当就是以groupKey为键 fileInfo为值
        Map<String, List<Meta.FileConfig.FileInfo>> fileInfoListMap = fileInfoList.stream()
                .filter(fileInfo -> StrUtil.isNotBlank(fileInfo.getGroupKey()))  //先将有分组的fileInfoList对象筛选出来
                .collect(Collectors.groupingBy(Meta.FileConfig.FileInfo::getGroupKey));//根据groupKey进行分组
        //去重
        // {"groupKey": "a", "files": [[1, 2], [2, 3]]}
        // {"groupKey": "a", "files": [1, 2, 2, 3]}
        // {"groupKey": "a", "files": [1, 2, 3]}
        Map<String, Meta.FileConfig.FileInfo> mergedFileInfoMap = new HashMap<>();
        Set<Map.Entry<String, List<Meta.FileConfig.FileInfo>>> entrySet = fileInfoListMap.entrySet();
        for (Map.Entry<String, List<Meta.FileConfig.FileInfo>> entry : entrySet) {
            //得到所有的value值
            List<Meta.FileConfig.FileInfo> infoList = entry.getValue();
            //去重
            List<Meta.FileConfig.FileInfo> newFileInfoList = new ArrayList<>(infoList.stream()
                    // flatMap()方法将每个文件流合并成一个扁平化的流，即将多个文件流合并成一个文件流
                    .flatMap(fileInfo -> fileInfo.getFiles().stream())  //将 [[1, 2], [2, 3]] 转换为 [1, 2, 2, 3]
                    .collect(
                            Collectors.toMap(Meta.FileConfig.FileInfo::getOutputPath, o -> o, (e, r) -> r)
                    ).values());
            //使用新的group配置
            Meta.FileConfig.FileInfo fileInfo = CollUtil.getLast(infoList);
            fileInfo.setFiles(newFileInfoList);
            String groupKey = entry.getKey();
            mergedFileInfoMap.put(groupKey, fileInfo);
        }
        // 3. 将文件分组添加到结果列表
        List<Meta.FileConfig.FileInfo> resultList = new ArrayList<>(mergedFileInfoMap.values());

        // 4. 将未分组的文件添加到结果列表
        List<Meta.FileConfig.FileInfo> noGroupFileInfoList = fileInfoList.stream().filter(fileInfo -> StrUtil.isBlank(fileInfo.getGroupKey()))
                .collect(Collectors.toList());
        resultList.addAll(new ArrayList<>(noGroupFileInfoList.stream()
                .collect(
                        Collectors.toMap(Meta.FileConfig.FileInfo::getOutputPath, o -> o, (e, r) -> r)
                ).values()));
        return resultList;
    }

    /**
     * 模型去重
     *
     * @param modelInfoList
     * @return
     */
    private static List<Meta.ModelConfig.ModelInfo> distinctModels(List<Meta.ModelConfig.ModelInfo> modelInfoList) {
        // 策略：同分组内模型 merge，不同分组保留

        // 1. 有分组的，以组为单位划分
        Map<String, List<Meta.ModelConfig.ModelInfo>> groupKeyModelInfoListMap = modelInfoList
                .stream()
                .filter(modelInfo -> StrUtil.isNotBlank(modelInfo.getGroupKey()))
                .collect(
                        Collectors.groupingBy(Meta.ModelConfig.ModelInfo::getGroupKey)
                );
        // 2. 同组内的模型配置合并
        // 保存每个组对应的合并后的对象 map
        Map<String, Meta.ModelConfig.ModelInfo> groupKeyMergedModelInfoMap = new HashMap<>();
        for (Map.Entry<String, List<Meta.ModelConfig.ModelInfo>> entry : groupKeyModelInfoListMap.entrySet()) {
            List<Meta.ModelConfig.ModelInfo> tempModelInfoList = entry.getValue();
            List<Meta.ModelConfig.ModelInfo> newModelInfoList = new ArrayList<>(tempModelInfoList.stream()
                    .flatMap(modelInfo -> modelInfo.getModels().stream())
                    .collect(
                            Collectors.toMap(Meta.ModelConfig.ModelInfo::getFieldName, o -> o, (e, r) -> r)
                    ).values());

            // 使用新的 group 配置
            Meta.ModelConfig.ModelInfo newModelInfo = CollUtil.getLast(tempModelInfoList);
            newModelInfo.setModels(newModelInfoList);
            String groupKey = entry.getKey();
            groupKeyMergedModelInfoMap.put(groupKey, newModelInfo);
        }

        // 3. 将模型分组添加到结果列表
        List<Meta.ModelConfig.ModelInfo> resultList = new ArrayList<>(groupKeyMergedModelInfoMap.values());

        // 4. 将未分组的模型添加到结果列表
        List<Meta.ModelConfig.ModelInfo> noGroupModelInfoList = modelInfoList.stream().filter(modelInfo -> StrUtil.isBlank(modelInfo.getGroupKey()))
                .collect(Collectors.toList());
        resultList.addAll(new ArrayList<>(noGroupModelInfoList.stream()
                .collect(
                        Collectors.toMap(Meta.ModelConfig.ModelInfo::getFieldName, o -> o, (e, r) -> r)
                ).values()));
        return resultList;
    }
}

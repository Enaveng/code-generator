package com.enaveng.maker.template;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.enaveng.maker.meta.Meta;
import com.enaveng.maker.template.model.TemplateMakerConfig;
import com.enaveng.maker.template.model.TemplateMakerFileConfig;
import com.enaveng.maker.template.model.TemplateMakerModelConfig;
import org.junit.Test;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class TemplateMakerTest {

    @Test
    public void makeTemplate() {
    }

    @Test
    public void testMakeTemplateBug01() {
        //提供输入参数
        String name = "acm-template-generator";
        String description = "ACM示例模板生成器";
        Meta meta = new Meta();
        meta.setName(name);
        meta.setDescription(description);
        //原始文件路径
        String projectPath = System.getProperty("user.dir");
        //测试整个目录生成
        String originFilePath = new File(projectPath).getParent() + File.separator + "code-generator-demo-projects/springboot-init";

        //输入模型参数信息
//        Meta.ModelConfig.ModelInfo modelInfo = new Meta.ModelConfig.ModelInfo();
//        modelInfo.setFieldName("outputText");
//        modelInfo.setType("String");
//        modelInfo.setDescription("sum= ");

        //测试
        Meta.ModelConfig.ModelInfo modelInfo = new Meta.ModelConfig.ModelInfo();
        modelInfo.setFieldName("className");
        modelInfo.setType("String");
        modelInfo.setDescription("MainTemplate");

        //输出与输入路径
        String fileInputPath1 = "/src/main/java/com/yupi/springbootinit/common";
//        String fileInputPath2 = "/src/main/java/com/yupi/springbootinit/mapper";
        String fileInputPath2 = "/src/main/resources/application.yml";
        TemplateMakerFileConfig templateMakerFileConfig = new TemplateMakerFileConfig();
        TemplateMakerFileConfig.FileInfoConfig fileInfoConfig1 = new TemplateMakerFileConfig.FileInfoConfig();
        fileInfoConfig1.setPath(fileInputPath1);
        TemplateMakerFileConfig.FileInfoConfig fileInfoConfig2 = new TemplateMakerFileConfig.FileInfoConfig();
        fileInfoConfig2.setPath(fileInputPath2);
        templateMakerFileConfig.setFiles(Arrays.asList(fileInfoConfig1, fileInfoConfig2));

//        //测试文件过滤
//        List<FileFilterConfig> fileFilterConfigList = new ArrayList<>();
//        FileFilterConfig fileFilterConfig = FileFilterConfig.builder()
//                .range(FileFilterRangeEnum.FILE_NAME.getValue())
//                .rule(FileFilterRuleEnum.CONTAINS.getValue())
//                .value("Base")
//                .build();
//        fileFilterConfigList.add(fileFilterConfig);
//        fileInfoConfig1.setFileFilterConfigList(fileFilterConfigList);


        //测试文件分组
//        TemplateMakerFileConfig.FileGroupConfig fileGroupConfig = new TemplateMakerFileConfig.FileGroupConfig();
//        fileGroupConfig.setCondition("outputText2");
//        fileGroupConfig.setGroupKey("test");
//        fileGroupConfig.setGroupName("测试分组2");
//        templateMakerFileConfig.setFileGroupConfig(fileGroupConfig);

        // 模型分组
        TemplateMakerModelConfig templateMakerModelConfig = new TemplateMakerModelConfig();
        // - 模型组配置
        TemplateMakerModelConfig.ModelGroupConfig modelGroupConfig = new TemplateMakerModelConfig.ModelGroupConfig();
        modelGroupConfig.setGroupKey("mysql");
        modelGroupConfig.setGroupName("数据库配置");
        templateMakerModelConfig.setModelGroupConfig(modelGroupConfig);

        // - 模型配置
        TemplateMakerModelConfig.ModelInfoConfig modelInfoConfig1 = new TemplateMakerModelConfig.ModelInfoConfig();
        modelInfoConfig1.setFieldName("url");
        modelInfoConfig1.setType("String");
        modelInfoConfig1.setDefaultValue("jdbc:mysql://localhost:3306/my_db");
        modelInfoConfig1.setReplaceText("jdbc:mysql://localhost:3306/my_db");  //需要被替换的内容

        TemplateMakerModelConfig.ModelInfoConfig modelInfoConfig2 = new TemplateMakerModelConfig.ModelInfoConfig();
        modelInfoConfig2.setFieldName("username");
        modelInfoConfig2.setType("String");
        modelInfoConfig2.setDefaultValue("root");
        modelInfoConfig2.setReplaceText("root");

        List<TemplateMakerModelConfig.ModelInfoConfig> modelInfoConfigList = Arrays.asList(modelInfoConfig1, modelInfoConfig2);
        templateMakerModelConfig.setModels(modelInfoConfigList);


        //测试
        String replaceStr = "BaseResponse";

        TemplateMaker.makeTemplate(meta, originFilePath, templateMakerModelConfig, templateMakerFileConfig, 1L,null);
    }

    @Test
    public void testMakeTemplateBug02() {
        //提供输入参数
        String name = "acm-template-generator";
        String description = "ACM示例模板生成器";
        Meta meta = new Meta();
        meta.setName(name);
        meta.setDescription(description);
        //原始文件路径
        String projectPath = System.getProperty("user.dir");
        //测试整个目录生成
        String originFilePath = new File(projectPath).getParent() + File.separator + "code-generator-demo-projects/springboot-init";

        //测试
        Meta.ModelConfig.ModelInfo modelInfo = new Meta.ModelConfig.ModelInfo();
        modelInfo.setFieldName("className");
        modelInfo.setType("String");
        modelInfo.setDescription("MainTemplate");

        //输出与输入路径
        String fileInputPath1 = "/src/main/java/com/yupi/springbootinit/common";
        String fileInputPath2 = "/src/main/java/com/yupi/springbootinit/mapper";
        TemplateMakerFileConfig templateMakerFileConfig = new TemplateMakerFileConfig();
        TemplateMakerFileConfig.FileInfoConfig fileInfoConfig1 = new TemplateMakerFileConfig.FileInfoConfig();
        fileInfoConfig1.setPath(fileInputPath1);
        TemplateMakerFileConfig.FileInfoConfig fileInfoConfig2 = new TemplateMakerFileConfig.FileInfoConfig();
        fileInfoConfig2.setPath(fileInputPath2);
        templateMakerFileConfig.setFiles(Arrays.asList(fileInfoConfig1, fileInfoConfig2));

        // 模型分组
        TemplateMakerModelConfig templateMakerModelConfig = new TemplateMakerModelConfig();
        // - 模型组配置
        TemplateMakerModelConfig.ModelGroupConfig modelGroupConfig = new TemplateMakerModelConfig.ModelGroupConfig();
        modelGroupConfig.setGroupKey("mysql");
        modelGroupConfig.setGroupName("数据库配置");
        templateMakerModelConfig.setModelGroupConfig(modelGroupConfig);

        // - 模型配置
        TemplateMakerModelConfig.ModelInfoConfig modelInfoConfig1 = new TemplateMakerModelConfig.ModelInfoConfig();
        modelInfoConfig1.setFieldName("${className}");
        modelInfoConfig1.setType("String");
        modelInfoConfig1.setReplaceText("BaseResponse");  //需要被替换的内容

        TemplateMakerModelConfig.ModelInfoConfig modelInfoConfig2 = new TemplateMakerModelConfig.ModelInfoConfig();
        modelInfoConfig2.setFieldName("username");
        modelInfoConfig2.setType("String");
        modelInfoConfig2.setDefaultValue("root");
        modelInfoConfig2.setReplaceText("root");

        List<TemplateMakerModelConfig.ModelInfoConfig> modelInfoConfigList = Arrays.asList(modelInfoConfig1, modelInfoConfig2);
        templateMakerModelConfig.setModels(modelInfoConfigList);

        TemplateMaker.makeTemplate(meta, originFilePath, templateMakerModelConfig, templateMakerFileConfig, 1L,null);
    }

    @Test
    public void testByMakerJSON() {
        String str = ResourceUtil.readUtf8Str("templateMaker.json");
        System.out.println(str);
        TemplateMakerConfig templateMakerconfig = JSONUtil.toBean(str, TemplateMakerConfig.class);
        TemplateMaker.makeTemplate(templateMakerconfig);
    }


    //制作 SpringBoot 模板
    @Test
    public void makeSpringBootTemplate() {
        String rootPath = "examples/springboot-init";
        String configStr = ResourceUtil.readUtf8Str(rootPath + File.separator + "templateMaker.json");
        TemplateMakerConfig templateMakerConfig = JSONUtil.toBean(configStr, TemplateMakerConfig.class);
        long id = TemplateMaker.makeTemplate(templateMakerConfig);
        System.out.println(id);

        configStr = ResourceUtil.readUtf8Str(rootPath + File.separator +"templateMaker1.json");
        templateMakerConfig = JSONUtil.toBean(configStr, TemplateMakerConfig.class);
        TemplateMaker.makeTemplate(templateMakerConfig);
        System.out.println(id);

        configStr = ResourceUtil.readUtf8Str(rootPath + File.separator +"templateMaker2.json");
        templateMakerConfig = JSONUtil.toBean(configStr, TemplateMakerConfig.class);
        TemplateMaker.makeTemplate(templateMakerConfig);

        configStr = ResourceUtil.readUtf8Str(rootPath + File.separator +"templateMaker3.json");
        templateMakerConfig = JSONUtil.toBean(configStr, TemplateMakerConfig.class);
        TemplateMaker.makeTemplate(templateMakerConfig);

        configStr = ResourceUtil.readUtf8Str(rootPath + File.separator +"templateMaker4.json");
        templateMakerConfig = JSONUtil.toBean(configStr, TemplateMakerConfig.class);
        TemplateMaker.makeTemplate(templateMakerConfig);

        configStr = ResourceUtil.readUtf8Str(rootPath + File.separator +"templateMaker4_1.json");
        templateMakerConfig = JSONUtil.toBean(configStr, TemplateMakerConfig.class);
        TemplateMaker.makeTemplate(templateMakerConfig);

        configStr = ResourceUtil.readUtf8Str(rootPath + File.separator +"templateMaker5.json");
        templateMakerConfig = JSONUtil.toBean(configStr, TemplateMakerConfig.class);
        TemplateMaker.makeTemplate(templateMakerConfig);

        configStr = ResourceUtil.readUtf8Str(rootPath + File.separator +"templateMaker6.json");
        templateMakerConfig = JSONUtil.toBean(configStr, TemplateMakerConfig.class);
        TemplateMaker.makeTemplate(templateMakerConfig);

        configStr = ResourceUtil.readUtf8Str(rootPath + File.separator +"templateMaker7.json");
        templateMakerConfig = JSONUtil.toBean(configStr, TemplateMakerConfig.class);
        TemplateMaker.makeTemplate(templateMakerConfig);
    }
}
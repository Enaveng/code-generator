package com.enaveng.generator;

import com.enaveng.model.MainTemplatesConfig;
import freemarker.template.TemplateException;

import java.io.File;
import java.io.IOException;

public class MainGenerator {

    public static void main(String[] args) throws Exception {
        MainTemplatesConfig mainTemplateConfig = new MainTemplatesConfig();
        mainTemplateConfig.setAuthor("yupi");
        mainTemplateConfig.setLoop(false);
        mainTemplateConfig.setOutputText("结果：");
        doGenerate(mainTemplateConfig);
    }

    /**
     * 生成
     *
     * @param model 数据模型
     * @throws TemplateException
     * @throws IOException
     */
    public static void doGenerate(Object model) throws Exception {
        String projectPath = System.getProperty("user.dir");
        //获得项目路径的父路径
        File parentFile = new File(projectPath).getParentFile();
//        String projectPath = "D:\\MyProject\\code-generator\\code-generator-basic";
//        String keyword = "\\code-generator-basic";
//        String result = null;
//        if (projectPath.contains(keyword)) {
//            result = projectPath.replace(keyword, "");
////            System.out.println(result);
//        } else {
//            result = projectPath;
//        }
        // 整个项目的根路径
//        File parentFile = new File(projectPath);
        // 输入路径
        String inputPath = new File(parentFile, "code-generator-demo-projects/acm-template").getAbsolutePath();
        String outputPath = projectPath;
        // 生成静态文件
        StaticGenerator.copyFilesByRecursive(inputPath, outputPath);
        // 生成动态文件
        String inputDynamicFilePath = projectPath + File.separator + "src/main/resources/templates/MainTemplates.java.ftl";
        String outputDynamicFilePath = outputPath + File.separator + "acm-template/src/com/yupi/acm/MainTemplate.java";
        DynamicGenerator.freeMarkerGenerator(inputDynamicFilePath, outputDynamicFilePath, model);
    }
}

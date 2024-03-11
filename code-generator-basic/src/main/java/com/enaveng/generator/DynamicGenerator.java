package com.enaveng.generator;

import com.enaveng.model.MainTemplatesConfig;
import freemarker.template.Configuration;
import freemarker.template.Template;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;

public class DynamicGenerator {
    public static void main(String[] args) throws Exception {
        //得到该项目根路径
        String projectPath = System.getProperty("user.dir"); // D:\MyProject\code-generator\code-generator-basic
        // 测试FreeMarker
        // 创建数据模型
        MainTemplatesConfig mainTemplatesConfig = new MainTemplatesConfig();
        mainTemplatesConfig.setLoop(false);
        mainTemplatesConfig.setAuthor("dlwlrma");
        mainTemplatesConfig.setOutputText("得到的结果为:");
        String inputPath = projectPath + File.separator + "src/main/resources/templates/MainTemplates.java.ftl";
        System.out.println(inputPath);
        String outputPath = projectPath + File.separator + "MainTemplates.java";
        freeMarkerGenerator(inputPath, outputPath, mainTemplatesConfig);
    }

    /**
     * 使用FreeMarker动态生成文件
     *
     * @param inputPath  输入路径
     * @param outputPath 输出路径
     * @param model      数据模型
     * @throws Exception
     */
    public static void freeMarkerGenerator(String inputPath, String outputPath, Object model) throws Exception {
        // new 出 Configuration 对象，参数为 FreeMarker 版本号
        Configuration configuration = new Configuration(Configuration.VERSION_2_3_32);

        // 指定模板文件所在的路径
        File inputFile = new File(inputPath).getParentFile();
        configuration.setDirectoryForTemplateLoading(inputFile);

        // 设置模板文件使用的字符集
        configuration.setDefaultEncoding("utf-8");

        //解决 FreeMarker 打印奇怪的数字格式 (比如 1,000,000 或 1 000 000 而不是 1000000)
        configuration.setNumberFormat("0.######");

        // 创建模板对象，加载指定模板
        String path = new File(inputPath).getName();
        Template template = configuration.getTemplate(path);

        // 生成对应的文件
        Writer out = new FileWriter(outputPath);
        template.process(model, out);

        // 生成文件后关闭
        out.close();
    }

}

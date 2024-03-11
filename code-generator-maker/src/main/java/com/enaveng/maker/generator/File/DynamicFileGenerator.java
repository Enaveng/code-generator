package com.enaveng.maker.generator.File;

import cn.hutool.core.io.FileUtil;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class DynamicFileGenerator {
    /**
     * 生成文件
     *
     * @param inputPath  模板文件输入路径
     * @param outputPath 输出路径
     * @param modelInfo  数据模型
     * @throws IOException
     * @throws TemplateException
     */
    public static void doGenerate(String inputPath, String outputPath, Object modelInfo) throws IOException, TemplateException {
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
        Template template = configuration.getTemplate(path,"utf-8");

        //判断outputPath文件是否存在
        if (!FileUtil.exist(outputPath)) {
            FileUtil.touch(outputPath);
        }

        // 生成对应的文件
//        Writer out = new FileWriter(outputPath);
//        Template template = configuration.getTemplate(templateName,"utf-8");
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(Paths.get(outputPath)), StandardCharsets.UTF_8));
        template.process(modelInfo, out);

        // 生成文件后关闭
        out.close();
    }

}

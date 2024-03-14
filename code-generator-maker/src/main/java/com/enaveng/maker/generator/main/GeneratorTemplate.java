package com.enaveng.maker.generator.main;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ClassPathResource;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.ZipUtil;
import com.enaveng.maker.generator.File.DynamicFileGenerator;
import com.enaveng.maker.generator.JarGenerator;
import com.enaveng.maker.generator.ScriptGenerator;
import com.enaveng.maker.meta.Meta;
import com.enaveng.maker.meta.MetaManager;
import freemarker.template.TemplateException;

import java.io.File;
import java.io.IOException;

//模板方法实现生成文件代码
public class GeneratorTemplate {
    public void doGenerator() throws TemplateException, IOException, InterruptedException {
        Meta meta = MetaManager.getMetaObject();
        System.out.println(meta);

        //得到当前工作目录
        String projectPath = System.getProperty("user.dir"); // D:\MyProject\code-generator\code-generator-maker
//        System.out.println(projectPath);
        //复制文件的输出目录
        String outputPath = projectPath + File.separator + "generated" + File.separator + meta.getName();
        if (!FileUtil.exist(outputPath)) {  //目录不存在则创建
            FileUtil.mkdir(outputPath);
        }
        ClassPathResource classPathResource = new ClassPathResource("");
        String inputResourcePath = classPathResource.getAbsolutePath();

        //将原始代码文件复制到生成的代码文件包中
        String copyRootPath = copyResourceFile(meta, outputPath);

        //生成文件
        generatorCode(meta, outputPath, inputResourcePath);

        // 构建 jar 包
        String jarPath = buildJar(outputPath, meta);

        // 封装脚本
        String shellOutputFilePath = buildScript(outputPath, jarPath);

        //生成精简版代码文件
        generatorDistFile(outputPath, copyRootPath, shellOutputFilePath, jarPath);
    }

    /**
     * 生成精简版代码
     *
     * @param outputPath          生成代码文件目录
     * @param copyRootPath        被拷贝的项目源文件目录
     * @param shellOutputFilePath 脚本文件目录
     * @param jarPath             jar包文件目录
     */
    public String generatorDistFile(String outputPath, String copyRootPath, String shellOutputFilePath, String jarPath) {
        //优化代码空间 同时生成精简版代码文件
        //首先创建精简版代码文件目录
        String distOutputPath = outputPath + "-dist";
        if (!FileUtil.exist(distOutputPath)) {
            FileUtil.mkdir(distOutputPath); // 创建文件
        }
        //拷贝jar包
        String originalJarPath = outputPath + File.separator + jarPath;
        String targetJarPath = distOutputPath + File.separator + "target";
        //创建target文件
        FileUtil.mkdir(targetJarPath);
        FileUtil.copy(originalJarPath, targetJarPath, false);
        //拷贝脚本文件
        FileUtil.copy(shellOutputFilePath, distOutputPath, false);
        FileUtil.copy(shellOutputFilePath + ".bat", distOutputPath, false);
        //拷贝源代码文件
        FileUtil.copy(copyRootPath, distOutputPath, false);
        return distOutputPath;
    }

    /**
     * 生成对应的产物包zip压缩文件
     *
     * @param srcPath 源文件路径
     * @return 压缩完成文件路径
     */
    public String buildZip(String srcPath) {
        String zipPath = srcPath + ".zip";
        ZipUtil.zip(srcPath, zipPath);
        return zipPath;
    }

    public void generatorCode(Meta meta, String outputPath, String inputResourcePath) throws IOException, TemplateException {
        //Java包基础路径
        // com.enaveng
        String outputBasePackage = meta.getBasePackage();
        String outputBasePackagePath = StrUtil.join("/", StrUtil.split(outputBasePackage, "."));
        // code-generator-maker/generated/acm-template-pro-generator/src/main/java
        String outputBaseJavaPackagePath = outputPath + File.separator + "src/main/java/" + outputBasePackagePath;

        String inputFilePath;
        String outputFilePath;

        //输入路径
        // model.DataModel
        inputFilePath = inputResourcePath + File.separator + "templates/java/model/DataModel.java.ftl";
        outputFilePath = outputBaseJavaPackagePath + "/model/DataModel.java";
        DynamicFileGenerator.doGenerate(inputFilePath, outputFilePath, meta);

        // cli.command.ConfigCommand  outputBaseJavaPackagePath = D:\MyProject\code-generator\code-generator-maker\generated\acm-template-pro-generator\src\main\java\com\enaveng
        inputFilePath = inputResourcePath + File.separator + "templates/java/cli/command/ConfigCommand.java.ftl";  //定义模板文件位置
        outputFilePath = outputBaseJavaPackagePath + "/cli/command/ConfigCommand.java";                            //代码文件生成位置
        DynamicFileGenerator.doGenerate(inputFilePath, outputFilePath, meta);

        // cli.command.GenerateCommand
        inputFilePath = inputResourcePath + File.separator + "templates/java/cli/command/GenerateCommand.java.ftl";
        outputFilePath = outputBaseJavaPackagePath + "/cli/command/GenerateCommand.java";
        DynamicFileGenerator.doGenerate(inputFilePath, outputFilePath, meta);

        // cli.command.ListCommand
        inputFilePath = inputResourcePath + File.separator + "templates/java/cli/command/ListCommand.java.ftl";
        outputFilePath = outputBaseJavaPackagePath + "/cli/command/ListCommand.java";
        DynamicFileGenerator.doGenerate(inputFilePath, outputFilePath, meta);

        // cli.CommandExecutor
        inputFilePath = inputResourcePath + File.separator + "templates/java/cli/CommandExecutor.java.ftl";
        outputFilePath = outputBaseJavaPackagePath + "/cli/CommandExecutor.java";
        DynamicFileGenerator.doGenerate(inputFilePath, outputFilePath, meta);

        // Main
        inputFilePath = inputResourcePath + File.separator + "templates/java/Main.java.ftl";
        outputFilePath = outputBaseJavaPackagePath + "/Main.java";
        DynamicFileGenerator.doGenerate(inputFilePath, outputFilePath, meta);

        // generator.DynamicGenerator 生成动态文件生成代码
        inputFilePath = inputResourcePath + File.separator + "templates/java/generator/DynamicGenerator.java.ftl";
        outputFilePath = outputBaseJavaPackagePath + "/generator/DynamicGenerator.java";
        DynamicFileGenerator.doGenerate(inputFilePath, outputFilePath, meta);

        // generator.MainGenerator 执行方法代码
        inputFilePath = inputResourcePath + File.separator + "templates/java/generator/MainGenerator.java.ftl";
        outputFilePath = outputBaseJavaPackagePath + "/generator/MainGenerator.java";
        DynamicFileGenerator.doGenerate(inputFilePath, outputFilePath, meta);

        // generator.StaticGenerator 生成静态文件生成代码
        inputFilePath = inputResourcePath + File.separator + "templates/java/generator/StaticGenerator.java.ftl";
        outputFilePath = outputBaseJavaPackagePath + "/generator/StaticGenerator.java";
        DynamicFileGenerator.doGenerate(inputFilePath, outputFilePath, meta);

        // pom.xml
        inputFilePath = inputResourcePath + File.separator + "templates/pom.xml.ftl";
        outputFilePath = outputPath + File.separator + "pom.xml";
        DynamicFileGenerator.doGenerate(inputFilePath, outputFilePath, meta);

//        //生成README文件
//        inputFilePath = inputResourcePath + File.separator + "templates/README.md.ftl";
//        outputFilePath = outputPath + File.separator + "README.md";
//        DynamicFileGenerator.doGenerate(inputFilePath, outputFilePath, meta);
    }

    public String buildJar(String outputPath, Meta meta) throws IOException, InterruptedException {
        JarGenerator.doGenerate(outputPath);
        String jarName = String.format("%s-%s-jar-with-dependencies.jar", meta.getName(), meta.getVersion());
        return "target/" + jarName;
    }

    public String buildScript(String outputPath, String jarPath) {
        String shellOutputFilePath = outputPath + File.separator + "generator";
        ScriptGenerator.doGenerate(shellOutputFilePath, jarPath);
        return shellOutputFilePath;
    }

    public String copyResourceFile(Meta meta, String outputPath) {
        String sourceRootPath = meta.getFileConfig().getSourceRootPath();
        String copyRootPath = outputPath + File.separator + ".source";
        FileUtil.copy(sourceRootPath, copyRootPath, false);
        return copyRootPath;
    }
}

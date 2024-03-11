package com.enaveng.generator;


import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ArrayUtil;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;


//使用第三方库实现静态文件的生成
public class StaticGenerator {
    public static void main(String[] args) throws Exception {
        //得到该项目根路径
        String projectPath = System.getProperty("user.dir"); // D:\MyProject\code-generator\code-generator-basic
        //得到该项目的父路径
        File parentFile = new File(projectPath).getParentFile();
        String input = parentFile + File.separator + "code-generator-demo-projects" + File.separator + "acm-template";  //D:\MyProject\code-generator\code-generator-demo-projects\acm-template
        String output = projectPath;
        //copyFileByHuTool(input, output);     //测试hutool工具类生成文件(简单但不够灵活)
        copyFilesByRecursive(input, output); //测试递归生成文件
    }


    public static void copyFilesByRecursive(String inputPath, String outputPath) {
        File inputFile = new File(inputPath);  //以给定的文件路径创建新的文件实例
        File outputFile = new File(outputPath);
        try {
            copyFileByRecursive(inputFile, outputFile);
        } catch (Exception e) {
            System.err.println("文件复制失败");
            e.printStackTrace();
        }
    }


    /**
     * 使用递归进行文件的复制
     *
     * @param inputFile
     * @param outputFile
     */
    public static void copyFileByRecursive(File inputFile, File outputFile) throws IOException {
        // 校验
        Assert.notNull(inputFile, "源文件为空!");
        if (!inputFile.exists()) {
            throw new IORuntimeException("File not exist: " + inputFile);
        }
        Assert.notNull(outputFile, "目标文件或者目录为空!");
        if (FileUtil.equals(inputFile, outputFile)) {
            throw new IORuntimeException("Files '{}' and '{}' are equal", inputFile, outputFile);
        }
        // 是目录 进行目录的复制
        if (inputFile.isDirectory()) {
            if (outputFile.exists() && !outputFile.isDirectory()) {
                //源为目录，目标为文件，抛出IO异常
                throw new IORuntimeException("Src is a directory but dest is a file!");
            }
            //目标文件为源文件的字文件或者目录
            if (FileUtil.isSub(inputFile, outputFile)) {
                throw new IORuntimeException("Dest is a sub directory of src !");
            }
            //先创建对应的目标目录
            File destOutputFile = new File(outputFile, inputFile.getName());
//            System.out.println(destOutputFile.getAbsolutePath());
            //如果不存在则创建目录
            if (!destOutputFile.exists()) {
                FileUtil.mkdir(destOutputFile);
            }
            //获取目录下的所有文件和子目录
            File[] files = inputFile.listFiles();
            //无子文件，直接结束
            if (ArrayUtil.isEmpty(files)) {
                return;
            }
            for (File file : files) {
                //递归拷贝下一层文件
                copyFileByRecursive(file, destOutputFile);
            }
        } else {
            //是文件，直接复制到目标目录下
            Path destPath = outputFile.toPath().resolve(inputFile.getName());
            Files.copy(inputFile.toPath(), destPath, StandardCopyOption.REPLACE_EXISTING);
        }
    }


    /**
     * 根据Hutool工具库实现静态文件的生成
     *
     * @param input  输入路径  源文件或目录
     * @param output 输出路径  目标文件或目录，目标不存在会自动创建（目录、文件都创建）
     */
    public static void copyFileByHuTool(String input, String output) {
        FileUtil.copy(input, output, false);
    }

}


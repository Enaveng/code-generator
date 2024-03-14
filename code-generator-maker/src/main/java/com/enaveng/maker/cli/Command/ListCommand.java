package com.enaveng.maker.cli.Command;

import cn.hutool.core.io.FileUtil;
import picocli.CommandLine;

import java.io.File;
import java.util.List;

//查看要生成的文件原始列表信息
@CommandLine.Command(name = "list", description = "查看文件列表", mixinStandardHelpOptions = true)
public class ListCommand implements Runnable {
    @Override
    public void run() {
        String property = System.getProperty("user.dir");
        File parentFile = new File(property).getParentFile();
        String inputPath = new File(parentFile, "code-generator-demo-projects/acm-template").getAbsolutePath();
        List<File> files = FileUtil.loopFiles(inputPath);
        for (File file : files) {
            System.out.println(file);
        }
    }


    public static void main(String[] args) {
//        String property = System.getProperty("user.dir");
//        File file = new File(property);
//        String absolutePath = new File(file, "code-generator-demo-projects/acm-template").getAbsolutePath();
//        List<File> files = FileUtil.loopFiles(absolutePath);
//        for (File file1 : files) {
//            System.out.println(file1);
//        }
    }
}

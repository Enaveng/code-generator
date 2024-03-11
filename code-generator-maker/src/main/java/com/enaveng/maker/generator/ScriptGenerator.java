package com.enaveng.maker.generator;

import cn.hutool.core.io.FileUtil;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;

//生成运行脚本文件 分为linux环境与windows环境
public class ScriptGenerator {
    /**
     * @param outputPath 文件生成的路径
     * @param jarPath    jar包文件的路径
     */
    public static void doGenerate(String outputPath, String jarPath) {
//        #!/bin/bash
//        java -jar code-generator-basic-1.0-SNAPSHOT-jar-with-dependencies.jar "$@"
        //使用字符串拼接的方式
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("#!/bin/bash").append("\n");
        stringBuilder.append(String.format("java -jar %s \"$@\"", jarPath)).append("\n");
        //将文件写入到指定位置
        FileUtil.writeString(stringBuilder.toString(), outputPath, StandardCharsets.UTF_8);
        //给文件添加可执行权限
        try {
            Set<PosixFilePermission> permissions = PosixFilePermissions.fromString("rwxrwxrwx");
            Files.setPosixFilePermissions(Paths.get(outputPath), permissions);
        } catch (Exception e) {

        }

        // windows
        stringBuilder = new StringBuilder();
        stringBuilder.append("@echo off").append("\n");
        stringBuilder.append(String.format("java -jar %s %%*", jarPath)).append("\n");
        FileUtil.writeBytes(stringBuilder.toString().getBytes(StandardCharsets.UTF_8), outputPath + ".bat");
    }
}

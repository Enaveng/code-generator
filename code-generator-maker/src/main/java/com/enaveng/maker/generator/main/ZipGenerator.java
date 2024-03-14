package com.enaveng.maker.generator.main;

public class ZipGenerator extends GeneratorTemplate {
    @Override
    public String generatorDistFile(String outputPath, String copyRootPath, String shellOutputFilePath, String jarPath) {
        String distFilePath = super.generatorDistFile(outputPath, copyRootPath, shellOutputFilePath, jarPath);
        //生成压缩文件
        return super.buildZip(distFilePath);
    }
}

package com.enaveng.maker.generator.main;

import freemarker.template.TemplateException;

import java.io.IOException;

public class MainGenerator extends GeneratorTemplate {

    @Override
    public void doGenerator() throws TemplateException, IOException, InterruptedException {
        super.doGenerator();
    }

    @Override
    public void generatorDistFile(String outputPath, String copyRootPath, String shellOutputFilePath, String jarPath) {
        System.out.println("不生成dist文件");
    }
}

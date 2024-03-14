package com.enaveng.maker;

import com.enaveng.maker.cli.CommandExecutor;
import com.enaveng.maker.generator.main.GeneratorTemplate;
import com.enaveng.maker.generator.main.MainGenerator;
import com.enaveng.maker.generator.main.ZipGenerator;
import freemarker.template.TemplateException;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws TemplateException, IOException, InterruptedException {
//        args = new String[]{"generate", "-l", "-a", "-o"};
//        args = new String[]{"list"};
//        args = new String[]{"config"};
//        CommandExecutor commandExecutor = new CommandExecutor();
//        commandExecutor.doExecute(args);
//        MainGenerator mainGenerator = new MainGenerator();
//        mainGenerator.doGenerator();
        GeneratorTemplate generatorTemplate = new ZipGenerator();
        generatorTemplate.doGenerator();

    }
}

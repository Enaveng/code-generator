package com.enaveng.cli.command;

import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONUtil;
import com.enaveng.generator.MainGenerator;
import com.enaveng.model.DataModel;
import lombok.Data;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.concurrent.Callable;

/**
 * 通过读取json文件来直接生成代码文件 用户不需要采用交互式生成
 */
@Command(name = "json-generate", description = "读取json文件生成代码", mixinStandardHelpOptions = true)
@Data
public class JSONGenerateCommand implements Callable<Integer> {

    @Option(names = {"-f","--jsonFile"}, arity = "0..1", description = "json文件路径", interactive = true, echo = true)
    private String jsonFile;

    public Integer call() throws Exception {
        //读取json文件
        String jsonStr = FileUtil.readUtf8String(jsonFile);
        //转换对象
        DataModel dataModel = JSONUtil.toBean(jsonStr, DataModel.class);
        MainGenerator.doGenerate(dataModel);
        return 0;
    }
}
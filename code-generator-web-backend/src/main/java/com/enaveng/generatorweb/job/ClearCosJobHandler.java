package com.enaveng.generatorweb.job;

import cn.hutool.core.util.StrUtil;
import com.enaveng.generatorweb.manager.CosManager;
import com.enaveng.generatorweb.mapper.GeneratorMapper;
import com.enaveng.generatorweb.model.entity.Generator;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ClearCosJobHandler {

    @Resource
    private CosManager cosManager;

    @Resource
    private GeneratorMapper generatorMapper;

    /**
     * @throws InterruptedException
     */
    @XxlJob("ClearCosJobHandler")
    public void test() throws InterruptedException {
        log.info("clearCosJobHandler start");
        // 编写业务逻辑
        // 1. 删除用户上传的模板制作文件（generator_make_template）
        cosManager.deleteDir("/generator_make_template/");
        // 2. 已删除的代码生成器对应的产物包文件（generator_dist)
        List<Generator> generatorList = generatorMapper.listDeleteGenerator();
        //得到对应的产物包文件 并去除开头的"/"
        List<String> distPathList = generatorList.stream()
                .map(Generator::getDistPath)
                .filter(StrUtil::isNotBlank)
                //去除 前缀当中的"/"
                .map(distPath -> distPath.substring(1))
                .collect(Collectors.toList());
        cosManager.deleteObjects(distPathList);
        log.info("clearCosJobHandler end");
    }
}

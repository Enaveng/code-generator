package com.enaveng.generatorweb.Sql;

import com.enaveng.generatorweb.model.entity.Generator;
import com.enaveng.generatorweb.service.GeneratorService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
public class SqlTest {

    @Resource
    private GeneratorService generatorService;

    @Test
    public void insertSQL() {
        //查入数据十万条
        Generator generator = generatorService.getById(18);
        for (int i = 0; i < 100000; i++) {
            generator.setId(null);
            generatorService.save(generator);
        }
    }
}

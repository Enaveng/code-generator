package com.enaveng.generatorweb.manager;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.Arrays;


@SpringBootTest
class CosManagerTest {

    @Resource
    private CosManager cosManager;

    @Test
    void deleteObject() {
        cosManager.deleteObject("test/test01.jpg");
    }

    @Test
    void deleteObjects() {
        cosManager.deleteObjects(Arrays.asList("test/test02.jpg","test/test03.jpg"));
    }

    @Test
    void deleteDir() {
        cosManager.deleteDir("test/");
    }
}
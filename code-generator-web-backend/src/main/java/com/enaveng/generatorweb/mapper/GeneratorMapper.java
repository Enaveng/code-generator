package com.enaveng.generatorweb.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.enaveng.generatorweb.model.entity.Generator;

import java.util.List;

/**
 * @author 86158
 * @description 针对表【generator(代码生成器)】的数据库操作Mapper
 * @createDate 2024-03-12 09:43:58
 * @Entity com.enaveng.generatorweb.model.entity.Generator
 */
public interface GeneratorMapper extends BaseMapper<Generator> {

    /**
     * 查询已经被删除的生成器
     * @return
     */
    List<Generator> listDeleteGenerator();

}





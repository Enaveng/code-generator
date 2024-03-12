package com.enaveng.generatorweb.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.enaveng.generatorweb.model.entity.Post;

import java.util.Date;
import java.util.List;

/**
 * 代码生成器数据库操作
 */
public interface PostMapper extends BaseMapper<Post> {

    /**
     * 查询 代码生成器列表（包括已被删除的数据）
     */
    List<Post> listPostWithDelete(Date minUpdateTime);

}





package com.enaveng.generatorweb.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.enaveng.generatorweb.common.ErrorCode;
import com.enaveng.generatorweb.constant.CommonConstant;
import com.enaveng.generatorweb.exception.BusinessException;
import com.enaveng.generatorweb.exception.ThrowUtils;
import com.enaveng.generatorweb.mapper.GeneratorMapper;
import com.enaveng.generatorweb.model.dto.generator.GeneratorQueryRequest;
import com.enaveng.generatorweb.model.entity.Generator;
import com.enaveng.generatorweb.model.entity.User;
import com.enaveng.generatorweb.model.vo.GeneratorVO;
import com.enaveng.generatorweb.model.vo.UserVO;
import com.enaveng.generatorweb.service.GeneratorService;
import com.enaveng.generatorweb.service.UserService;
import com.enaveng.generatorweb.utils.SqlUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 代码生成器服务实现
 */
@Service
@Slf4j
public class GeneratorServiceImpl extends ServiceImpl<GeneratorMapper, Generator> implements GeneratorService {

    @Resource
    private UserService userService;


    @Override
    public void validGenerator(Generator generator, boolean add) {
        if (generator == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String name = generator.getName();
        String description = generator.getDescription();
        // 创建时，参数不能为空
        if (add) { //判断请求是否为添加 添加时name以及description不能为空
            ThrowUtils.throwIf(StringUtils.isAnyBlank(name, description), ErrorCode.PARAMS_ERROR);
        }
        // 有参数则校验
        if (StringUtils.isNotBlank(name) && name.length() > 80) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "代码生成器名称过长");
        }
        if (StringUtils.isNotBlank(description) && description.length() > 256) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "代码生成器描述内容过长");
        }
    }

    /**
     * 获取查询包装类
     *
     * @param generatorQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<Generator> getQueryWrapper(GeneratorQueryRequest generatorQueryRequest) {
        QueryWrapper<Generator> queryWrapper = new QueryWrapper<>();
        if (generatorQueryRequest == null) {
            return queryWrapper;
        }
        Long id = generatorQueryRequest.getId();
        Long notId = generatorQueryRequest.getNotId();
        String searchText = generatorQueryRequest.getSearchText();

        List<String> tags = generatorQueryRequest.getTags();
        Long userId = generatorQueryRequest.getUserId();
        String name = generatorQueryRequest.getName();
        String description = generatorQueryRequest.getDescription();
        String basePackage = generatorQueryRequest.getBasePackage();
        String version = generatorQueryRequest.getVersion();
        String author = generatorQueryRequest.getAuthor();
        String distPath = generatorQueryRequest.getDistPath();
        Integer status = generatorQueryRequest.getStatus();

        String sortField = generatorQueryRequest.getSortField();
        String sortOrder = generatorQueryRequest.getSortOrder();

        // 拼接查询条件
        if (StringUtils.isNotBlank(searchText)) {
            queryWrapper.and(qw -> qw.like("name", searchText).or().like("description", searchText));
        }
        queryWrapper.like(StringUtils.isNotBlank(name), "name", name);
        queryWrapper.like(StringUtils.isNotBlank(description), "description", description);
        if (CollUtil.isNotEmpty(tags)) {
            for (String tag : tags) {
                queryWrapper.like("tags", "\"" + tag + "\"");
            }
        }
        queryWrapper.ne(ObjectUtils.isNotEmpty(notId), "id", notId); //不等于
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(basePackage), "basePackage", basePackage);
        queryWrapper.eq(ObjectUtils.isNotEmpty(version), "version", version);
        queryWrapper.eq(ObjectUtils.isNotEmpty(author), "author", author);
        queryWrapper.eq(ObjectUtils.isNotEmpty(distPath), "distPath", distPath);
        queryWrapper.eq(ObjectUtils.isNotEmpty(status), "status", status);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }


    @Override
    public GeneratorVO getGeneratorVO(Generator generator, HttpServletRequest request) {
        GeneratorVO generatorVO = GeneratorVO.objToVo(generator);
        //对应查询UserVO对象
        Long userId = generator.getUserId();
        User user = null;
        if (userId != null && userId > 0) {
            user = userService.getById(userId);
        }
        UserVO userVO = userService.getUserVO(user);
        generatorVO.setUser(userVO);
        return generatorVO;
    }

    @Override
    public Page<GeneratorVO> getGeneratorVOPage(Page<Generator> generatorPage, HttpServletRequest request) {
        //需要将查询出来的generatorPage对象封装为generatorVOPage对象
        List<Generator> generatorList = generatorPage.getRecords();
        Page<GeneratorVO> generatorVOPage = new Page<>(generatorPage.getCurrent(), generatorPage.getSize(), generatorPage.getTotal());
        if (CollUtil.isEmpty(generatorList)) {
            return generatorVOPage;
        }
        // 1. 得到每一个generator对象对应的userId
        List<Long> userIdList = generatorList.stream()
                .map(Generator::getUserId)
                .collect(Collectors.toList());
        // 2. 得到具体的User对象 并根据id进行分组
        List<User> userList = userService.listByIds(userIdList);
        Map<Long, List<User>> userMapList = userList.stream()
                .collect(Collectors.groupingBy(User::getId)); //值为id 键为对应id的user对象
        // 3. 进行数据的填充
        List<GeneratorVO> generatorVOList = generatorList.stream()
                .map(generator -> {
                    //对每一个generator对象进行操作
                    Long userId = generator.getUserId();
                    //将generator对象转换为对应的vo对象
                    GeneratorVO generatorVO = GeneratorVO.objToVo(generator);
                    User user = null;
                    if (userMapList.containsKey(userId)) {
                        user = userMapList.get(userId).get(0);
                    }
                    generatorVO.setUser(userService.getUserVO(user));
                    return generatorVO;
                }).collect(Collectors.toList());
        generatorVOPage.setRecords(generatorVOList);
        return generatorVOPage;
    }

}





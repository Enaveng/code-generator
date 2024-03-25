package com.enaveng.generatorweb.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.TypeReference;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.ZipUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enaveng.generatorweb.annotation.AuthCheck;
import com.enaveng.generatorweb.common.*;
import com.enaveng.generatorweb.constant.UserConstant;
import com.enaveng.generatorweb.exception.BusinessException;
import com.enaveng.generatorweb.exception.ThrowUtils;
import com.enaveng.generatorweb.manager.CaffeineManager;
import com.enaveng.generatorweb.manager.CosManager;
import com.enaveng.generatorweb.model.dto.generator.*;
import com.enaveng.generatorweb.model.entity.Generator;
import com.enaveng.generatorweb.model.entity.User;
import com.enaveng.generatorweb.model.vo.GeneratorVO;
import com.enaveng.generatorweb.service.GeneratorService;
import com.enaveng.generatorweb.service.UserService;
import com.enaveng.maker.generator.main.GeneratorTemplate;
import com.enaveng.maker.generator.main.ZipGenerator;
import com.enaveng.maker.meta.Meta;
import com.enaveng.maker.meta.MetaValidator;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.COSObjectInputStream;
import com.qcloud.cos.utils.IOUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * 代码生成器接口
 */
@RestController
@RequestMapping("/generator")
@Slf4j
public class GeneratorController {

    @Resource
    private GeneratorService generatorService;

    @Resource
    private UserService userService;

    @Resource
    private CosManager cosManager;

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private CaffeineManager caffeineManager;

    /**
     * 创建代码生成器
     *
     * @param generatorAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addGenerator(@RequestBody GeneratorAddRequest generatorAddRequest, HttpServletRequest request) {
        if (generatorAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Generator generator = new Generator();
        BeanUtils.copyProperties(generatorAddRequest, generator);
        List<String> tags = generatorAddRequest.getTags();
        if (tags != null) {
            generator.setTags(JSONUtil.toJsonStr(tags));
        }
        Meta.ModelConfig modelConfig = generatorAddRequest.getModelConfig();
        if (modelConfig != null) {
            generator.setModelConfig(JSONUtil.toJsonStr(modelConfig));
        }
        Meta.FileConfig fileConfig = generatorAddRequest.getFileConfig();
        if (fileConfig != null) {
            generator.setFileConfig(JSONUtil.toJsonStr(fileConfig));
        }
        generatorService.validGenerator(generator, true);
        User loginUser = userService.getLoginUser(request);
        generator.setUserId(loginUser.getId());
        boolean result = generatorService.save(generator);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        long newGeneratorId = generator.getId();
        return ResultUtils.success(newGeneratorId);
    }

    /**
     * 删除
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteGenerator(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断要删除的对象是否存在
        Generator oldGenerator = generatorService.getById(id);
        ThrowUtils.throwIf(oldGenerator == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldGenerator.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = generatorService.removeById(id);
        return ResultUtils.success(b);
    }


    /**
     * 更新（仅管理员）
     *
     * @param generatorUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateGenerator(@RequestBody GeneratorUpdateRequest generatorUpdateRequest) {
        if (generatorUpdateRequest == null || generatorUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Generator generator = new Generator();
        BeanUtils.copyProperties(generatorUpdateRequest, generator);
        List<String> tags = generatorUpdateRequest.getTags();
        if (tags != null) {
            generator.setTags(JSONUtil.toJsonStr(tags));
        }
        Meta.ModelConfig modelConfig = generatorUpdateRequest.getModelConfig();
        if (modelConfig != null) {
            generator.setModelConfig(JSONUtil.toJsonStr(modelConfig));
        }
        Meta.FileConfig fileConfig = generatorUpdateRequest.getFileConfig();
        if (fileConfig != null) {
            generator.setFileConfig(JSONUtil.toJsonStr(fileConfig));
        }
        // 参数校验
        generatorService.validGenerator(generator, false);
        long id = generatorUpdateRequest.getId();
        // 判断是否存在
        Generator oldGenerator = generatorService.getById(id);
        ThrowUtils.throwIf(oldGenerator == null, ErrorCode.NOT_FOUND_ERROR);
        boolean result = generatorService.updateById(generator);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 获取
     *
     * @param id
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<GeneratorVO> getGeneratorVOById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Generator generator = generatorService.getById(id);
        if (generator == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success(generatorService.getGeneratorVO(generator, request));
    }

    /**
     * 分页获取列表（仅管理员）
     * 生成器管理页面接口
     *
     * @param generatorQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Generator>> listGeneratorByPage(@RequestBody GeneratorQueryRequest generatorQueryRequest) {
        long current = generatorQueryRequest.getCurrent();
        long size = generatorQueryRequest.getPageSize();
        Page<Generator> generatorPage = generatorService.page(new Page<>(current, size),
                generatorService.getQueryWrapper(generatorQueryRequest));
        return ResultUtils.success(generatorPage);
    }

    /**
     * 分页获取列表（封装类）
     *
     * @param generatorQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<GeneratorVO>> listGeneratorVOByPage(@RequestBody GeneratorQueryRequest generatorQueryRequest,
                                                                 HttpServletRequest request) {
        long current = generatorQueryRequest.getCurrent();
        long size = generatorQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        //current 当前页 前端默认传递1 size每页显示条数
        Page<Generator> generatorPage = generatorService.page(new Page<>(current, size),
                generatorService.getQueryWrapper(generatorQueryRequest));
        return ResultUtils.success(generatorService.getGeneratorVOPage(generatorPage, request));
    }

    /**
     * 优化查询 不查询不需要的字段
     * 分页获取列表（封装类）
     *
     * @param generatorQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo/fast")
    public BaseResponse<Page<GeneratorVO>> listGeneratorVOFast(@RequestBody GeneratorQueryRequest generatorQueryRequest,
                                                               HttpServletRequest request) {
        long current = generatorQueryRequest.getCurrent();
        long size = generatorQueryRequest.getPageSize();
        String key = GeneratorUtils.getPageCacheKey(generatorQueryRequest);
        //优先读取redis缓存数据
        //String generatorCacheValue = (String) redisTemplate.opsForValue().get(key);
        //改造为使用Caffeine本地缓存
        Object generatorCacheValue = caffeineManager.get(key);
        if (generatorCacheValue != null) {
            //将json对象转换为实体类对象
//            Page<GeneratorVO> generatorVOPage = JSONUtil.toBean(generatorCacheValue, new TypeReference<Page<GeneratorVO>>() {
//            }, false);
            return ResultUtils.success((Page<GeneratorVO>) generatorCacheValue);
        }
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        //current 当前页 前端默认传递1 size每页显示条数
        QueryWrapper<Generator> queryWrapper = generatorService.getQueryWrapper(generatorQueryRequest);
        //精简查询字段
        queryWrapper.select("id", "name", "description", "tags", "picture", "status", "userId", "createTime", "updateTime");
        Page<Generator> generatorPage = generatorService.page(new Page<>(current, size), queryWrapper);
        Page<GeneratorVO> generatorVOPage = generatorService.getGeneratorVOPage(generatorPage, request);
        //写缓存
        //redisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(generatorVOPage), 120, TimeUnit.SECONDS);
        caffeineManager.put(key, generatorVOPage);
        return ResultUtils.success(generatorVOPage);
    }

    /**
     * 分页获取当前用户创建的资源列表
     *
     * @param generatorQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<GeneratorVO>> listMyGeneratorVOByPage(@RequestBody GeneratorQueryRequest generatorQueryRequest,
                                                                   HttpServletRequest request) {
        if (generatorQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        generatorQueryRequest.setUserId(loginUser.getId());
        long current = generatorQueryRequest.getCurrent();
        long size = generatorQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Generator> generatorPage = generatorService.page(new Page<>(current, size),
                generatorService.getQueryWrapper(generatorQueryRequest));
        return ResultUtils.success(generatorService.getGeneratorVOPage(generatorPage, request));
    }


    /**
     * 编辑（用户）
     *
     * @param generatorEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editGenerator(@RequestBody GeneratorEditRequest generatorEditRequest, HttpServletRequest request) {
        if (generatorEditRequest == null || generatorEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Generator generator = new Generator();
        BeanUtils.copyProperties(generatorEditRequest, generator);
        List<String> tags = generatorEditRequest.getTags();
        if (tags != null) {
            generator.setTags(JSONUtil.toJsonStr(tags));
        }
        Meta.ModelConfig modelConfig = generatorEditRequest.getModelConfig();
        if (modelConfig != null) {
            generator.setModelConfig(JSONUtil.toJsonStr(modelConfig));
        }
        Meta.FileConfig fileConfig = generatorEditRequest.getFileConfig();
        if (fileConfig != null) {
            generator.setFileConfig(JSONUtil.toJsonStr(fileConfig));
        }
        // 参数校验
        generatorService.validGenerator(generator, false);
        User loginUser = userService.getLoginUser(request);
        long id = generatorEditRequest.getId();
        // 判断是否存在
        Generator oldGenerator = generatorService.getById(id);
        ThrowUtils.throwIf(oldGenerator == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldGenerator.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean result = generatorService.updateById(generator);
        return ResultUtils.success(result);
    }

    /**
     * 根据生成器id进行下载
     *
     * @param id
     * @param request
     */
    @GetMapping("/download")
    public void downloadGeneratorById(@RequestParam long id, HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (id < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //根据生成器id得到对应用户id
        Generator generator = generatorService.getById(id);
        if (generator == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        //得到对应的登录用户
        User loginUser = userService.getLoginUser(request);

        log.info("登录用户 " + loginUser + " 下载了id为 " + id + " 的生成器");
        String filepath = generator.getDistPath();
        if (StrUtil.isBlank(filepath)) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "对应的产物包不存在");
        }
        //优先使用本地缓存进行文件的下载
        // 设置响应头
        response.setContentType("application/octet-stream;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=" + filepath);
        String cachePath = GeneratorUtils.getCachePath(id, filepath);
        if (FileUtil.exist(cachePath)) {
            Files.copy(Paths.get(cachePath), response.getOutputStream());
            return;
        }
        COSObjectInputStream cosObjectInput = null;
        try {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            COSObject cosObject = cosManager.getObject(filepath);  //下载文件得到cosObject对象
            cosObjectInput = cosObject.getObjectContent();
            // 处理下载到的流
            byte[] bytes = IOUtils.toByteArray(cosObjectInput);
            // 写入响应
            response.getOutputStream().write(bytes);
            response.getOutputStream().flush();
            stopWatch.stop();
            log.info("下载文件主要耗时时间为:{}", stopWatch.getTotalTimeMillis());
        } catch (Exception e) {
            log.error("file download error, filepath = " + filepath, e);
            log.error("file download error, filepath = ");
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "下载失败");
        } finally {
            if (cosObjectInput != null) {
                cosObjectInput.close(); //关闭流对象
            }
        }
    }

    /**
     * 在线使用生成器接口  通过生成器生成对应的文件
     *
     * @param generatorRequest 请求参数
     * @param request
     * @param response
     */
    @PostMapping("/use")
    public void useGenerator(@RequestBody GeneratorRequest generatorRequest, HttpServletRequest request, HttpServletResponse response) throws Exception {
        //1. 获取请求参数
        Long id = generatorRequest.getId();
        Map<String, Object> dataModel = generatorRequest.getDataModel();

        Generator generator = generatorService.getById(id);
        if (generator == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        log.info("用户: {} 使用了代码生成器 {} ", loginUser, id);
        // 2. 获取生成生成器的制作工具的产物包的路径
        String distPath = generator.getDistPath();
        if (StrUtil.isBlank(distPath)) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "产物包路径不存在");
        }
        // 3. 下载产物包到本地解压
        // 3.1 创建独立的工作空间 用来存放从对象存储上下载的产物包文件
        String projectPath = System.getProperty("user.dir");
        String tempDirPath = String.format("%s/.temp/use/%s", projectPath, id);
        // 将每个压缩包统一命名为dist.zip
        String zipFilePath = tempDirPath + "/dist.zip";
        // 文件不存在先创建
        if (!FileUtil.exist(zipFilePath)) {
            FileUtil.touch(zipFilePath);
        }
        // 下载文件
        try {
            cosManager.download(distPath, zipFilePath);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成器下载失败");
        }
        // 4. 操作解压后的文件夹 调用脚本文件 得到生成的代码
        File unzipFile = ZipUtil.unzip(zipFilePath);  //将压缩文件解压到当前文件夹
        // 将用户的请求封装成对应的json文件
        String modelFilePath = tempDirPath + "/model.json";  //json文件的位置
        String jsonStr = JSONUtil.toJsonStr(dataModel);
        FileUtil.writeUtf8String(jsonStr, modelFilePath);
        // 遍历得到 generator 脚本文件
        File scriptFile = FileUtil.loopFiles(unzipFile, 2, null)
                .stream()
                .filter(file -> file.isFile() && "generator.bat".equals(file.getName()))  //在windows环境下使用bat运行
                .findFirst()
                .orElseThrow(Exception::new);
        //给文件添加可执行权限
        try {
            Set<PosixFilePermission> permissions = PosixFilePermissions.fromString("rwxrwxrwx");
            Files.setPosixFilePermissions(scriptFile.toPath(), permissions);
        } catch (Exception e) {

        }

        // 构造命令
        File scriptDir = scriptFile.getParentFile();
        // 注意，如果是 mac / linux 系统，要用 "./generator"
        String scriptAbsolutePath = scriptFile.getAbsolutePath().replace("\\", "/");
        String[] commands = new String[]{scriptAbsolutePath, "json-generate", "--jsonFile=" + modelFilePath};

        // 这里一定要拆分！
        ProcessBuilder processBuilder = new ProcessBuilder(commands);
        processBuilder.directory(scriptDir); //设置待执行命令的工作目录
        try {
            Process process = processBuilder.start();
            // 读取命令的输出
            InputStream inputStream = process.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            // 等待命令执行完成
            int exitCode = process.waitFor();
            System.out.println("命令执行结束，退出码：" + exitCode);
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "执行生成器脚本错误");
        }
        // 5. 后端将代码返回给用户下载
        // 压缩得到的生成结果
        String generatedPath = scriptDir.getAbsolutePath() + "/generated";  //脚本当中规定了自动生成的模板文件位置
        String resultPath = tempDirPath + "/result.zip";
        File resultFile = ZipUtil.zip(generatedPath, resultPath);
        // 设置响应头
        response.setContentType("application/octet-stream;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=" + resultFile.getName());
        Files.copy(resultFile.toPath(), response.getOutputStream());

        // 6. 清除下载的资源 防止磁盘满溢
        CompletableFuture.runAsync(() -> {
            FileUtil.del(tempDirPath);
        });
    }


    /**
     * 在线制作生成器接口 根据用户上传的模板文件制作生成器 调用的是Maker项目
     *
     * @param generatorMakeRequest
     * @param request
     * @param response
     * @throws Exception
     */
    @PostMapping("/make")
    public void makeGenerator(@RequestBody GeneratorMakeRequest generatorMakeRequest, HttpServletRequest request, HttpServletResponse response) throws Exception {
        //1. 获取请求参数
        String zipFilePath = generatorMakeRequest.getZipFilePath();
        Meta meta = generatorMakeRequest.getMeta();
        //需要登录
        User loginUser = userService.getLoginUser(request);
        log.info("用户{}使用了在线制作代码生成器", loginUser);

        //2. 创建独立的工作空间 将压缩好的模板文件下载到本地
        if (StrUtil.isBlank(zipFilePath)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String projectPath = System.getProperty("user.dir");
        String id = IdUtil.getSnowflakeNextIdStr();
        String tempDirPath = String.format("%s/.temp/make/%s", projectPath, id);
        // 将每个压缩包统一命名为make.zip
        String localZipFilePath = tempDirPath + "/make.zip";
        // 文件不存在先创建
        if (!FileUtil.exist(localZipFilePath)) {
            FileUtil.touch(localZipFilePath);
        }
        // 下载文件
        try {
            cosManager.download(zipFilePath, localZipFilePath);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "模板文件下载失败");
        }
        //3. 将压缩文件进行解压
        File unzipDistDir = ZipUtil.unzip(localZipFilePath);  //将压缩文件解压到文件名相同的目录中
        //4. 构造Meta对象以及生成项目的输出路径
        String sourceRootPath = unzipDistDir.getAbsolutePath();
        meta.getFileConfig().setSourceRootPath(sourceRootPath);
        // 对meta对象进行校验
        MetaValidator.doValidAndFill(meta);
        // 构造项目输出路径
        String outputPath = String.format("%s/generated/%s", tempDirPath, meta.getName());
        //5. 调用Maker项目的制作生成器方法
        GeneratorTemplate generatorTemplate = new ZipGenerator();
        try {
            generatorTemplate.doGenerator(meta, outputPath);
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "制作生成器失败");
        }
        //6. 得到制作完成之后的压缩文件
        String suffix = "-dist.zip";
        String zipFileName = meta.getName() + suffix;  //该压缩文件的文件名
        String distZipFilePath = outputPath + suffix;  //该压缩文件的文件路径
        // 返回前端
        // 设置响应头
        response.setContentType("application/octet-stream;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=" + zipFileName);
        // 写入响应
        Files.copy(Paths.get(distZipFilePath), response.getOutputStream());

        //7. 清理文件
        CompletableFuture.runAsync(() -> {
            FileUtil.del(tempDirPath);
        });
        //测试文件参数  /generator_dist/1767434287893708802/MF8INGu0-acm-template-pro.zip
    }


    /**
     * 优化下载流程 将生成器缓存到本地当中 仅仅测试没有正式在前端使用该接口
     *
     * @param generatorCacheRequest
     * @param request
     */
    @PostMapping("/cache")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public void cacheGenerator(@RequestBody GeneratorCacheRequest generatorCacheRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (generatorCacheRequest == null || generatorCacheRequest.getId() < 1) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long id = generatorCacheRequest.getId();
        //根据生成器id得到对应用户id
        Generator generator = generatorService.getById(id);
        if (generator == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        String filepath = generator.getDistPath();
        if (StrUtil.isBlank(filepath)) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "对应的产物包不存在");
        }
        //得到缓存文件并创建本地缓存文件
        String cachePath = GeneratorUtils.getCachePath(id, filepath);
        if (!FileUtil.exist(cachePath)) {
            //创建
            FileUtil.touch(cachePath);
        }
        //下载文件到本地
        try {
            cosManager.download(filepath, cachePath);
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "下载文件失败");
        }
    }
}

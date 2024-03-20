package com.enaveng.generatorweb.common;

import cn.hutool.core.codec.Base64Encoder;
import cn.hutool.crypto.digest.MD5;
import cn.hutool.json.JSONUtil;
import com.enaveng.generatorweb.model.dto.generator.GeneratorQueryRequest;

public class GeneratorUtils {

    /**
     * 得到本地缓存路径
     *
     * @param id
     * @param distPath
     */
    public static String getCachePath(long id, String distPath) {
        String projectPath = System.getProperty("user.dir");
        String tempDirPath = String.format("%s/.temp/cache/%s", projectPath, id);
        String generatorPath = String.format("%s/%s", tempDirPath, distPath);
        return generatorPath;
    }

    /**
     * 使用redis进行缓存的key
     *
     * @param generatorQueryRequest
     * @return
     */
    public static String getPageCacheKey(GeneratorQueryRequest generatorQueryRequest) {
        String jsonStr = JSONUtil.toJsonStr(generatorQueryRequest);
        String encode = Base64Encoder.encode(jsonStr);
        String key = "generator:page:" + encode;
        return key;
    }
}

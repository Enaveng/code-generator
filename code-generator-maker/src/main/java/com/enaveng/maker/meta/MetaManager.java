package com.enaveng.maker.meta;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.json.JSONUtil;

/**
 * 使用单例模式读取元信息 即将JSON值填充到实体对象当中
 */
public class MetaManager {

    private static volatile Meta meta; //volatile关键字保证多线程情况下对象的可见性

    //单例模式需要将构造器私有化 防止外部实例化
    private MetaManager() {

    }

    public static Meta getMetaObject() {
        //使用双检加锁
        if (meta == null) {
            synchronized (MetaManager.class) {
                if (meta == null) {
                    meta = InitMetaClass();
                }
            }
        }
        return meta;
    }

    public static Meta InitMetaClass() {
        //使用hutool工具类读取元信息文件
        String metaStr = ResourceUtil.readUtf8Str("meta.json");
//        String metaStr = ResourceUtil.readUtf8Str("springboot-init.json");
        Meta metaData = JSONUtil.toBean(metaStr, Meta.class);
        //对元信息文件进行校验
        MetaValidator.doValidAndFill(metaData);
        return metaData;
    }
}

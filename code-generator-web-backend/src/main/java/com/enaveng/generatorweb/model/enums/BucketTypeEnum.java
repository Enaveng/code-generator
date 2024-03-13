package com.enaveng.generatorweb.model.enums;

import org.apache.commons.lang3.ObjectUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * cos对象存储 bucket存储桶类型枚举
 */
public enum BucketTypeEnum {

    Private("私有读写", "Private"),

    PublicReadWrite("公有读私有写", "PublicRead"),

    PublicRead("公有读写", "PublicReadWrite");

    BucketTypeEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    private final String text;

    private final String value;

    /**
     * 获取值列表
     *
     * @return
     */
    public static List<String> getValues() {
        return Arrays.stream(values()).map(item -> item.value).collect(Collectors.toList());
    }

    /**
     * 根据 value 获取枚举
     *
     * @param value
     * @return
     */
    public static BucketTypeEnum getEnumByValue(String value) {
        if (ObjectUtils.isEmpty(value)) {
            return null;
        }
        for (BucketTypeEnum anEnum : BucketTypeEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }

    public String getValue() {
        return value;
    }

    public String getText() {
        return text;
    }


}

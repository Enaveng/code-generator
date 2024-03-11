package com.enaveng.maker.template.Enums;

import lombok.Getter;

/**
 * 文件过滤规则枚举
 */
@Getter
public enum FileFilterRuleEnum {

    CONTAINS("包含", "contains"),
    STARTS_WITH("前缀匹配", "startsWith"),
    ENDS_WITH("后缀匹配", "endsWith"),
    REGEX("正则", "regex"),
    EQUALS("相等", "equals");


    private final String text;
    private final String value;

    FileFilterRuleEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    public static FileFilterRuleEnum getEnumByValue(String value) {
        if (value == null) {
            return null;
        }
        for (FileFilterRuleEnum Enum : FileFilterRuleEnum.values()) {
            if (Enum.value.equals(value)) {
                return Enum;
            }
        }
        return null;
    }
}


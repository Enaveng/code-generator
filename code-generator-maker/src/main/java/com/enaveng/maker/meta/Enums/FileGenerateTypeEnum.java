package com.enaveng.maker.meta.Enums;

//文件生成类型枚举
public enum FileGenerateTypeEnum {
    DYNAMIC("动态文件", "dynamic"),
    STATIC("静态文件", "static");

    final String text;
    final String value;

    FileGenerateTypeEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    public String getText() {
        return text;
    }

    public String getValue() {
        return value;
    }
}

package com.enaveng.maker.template.Enums;

import lombok.Getter;

/**
 * 文件范围枚举
 */
@Getter
public enum FileFilterRangeEnum {
    FILE_NAME("文件名", "fileName"),
    FILE_CONTENT("文件内容", "fileContent");


    private final String text;
    private final String value;

    FileFilterRangeEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据value获取对应的枚举
     *
     * @param value
     * @return
     */
    public static FileFilterRangeEnum getEnumByValue(String value) {
        if (value == null) {
            return null;
        }
        for (FileFilterRangeEnum anEnum : FileFilterRangeEnum.values()) {  //对value列表进行遍历
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }


}

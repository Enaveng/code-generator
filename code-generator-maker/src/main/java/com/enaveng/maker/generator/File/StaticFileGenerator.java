package com.enaveng.maker.generator.File;


import cn.hutool.core.io.FileUtil;


//使用第三方库实现静态文件的生成
public class StaticFileGenerator {

    /**
     * @param input  输入路径  源文件或目录
     * @param output 输出路径  目标文件或目录，目标不存在会自动创建（目录、文件都创建）
     */
    public static void copyFileByHuTool(String input, String output) {
        FileUtil.copy(input, output, false);
    }
}


package com.enaveng.maker.cli.Command;
import com.enaveng.maker.model.DataModel;
import picocli.CommandLine;

import java.lang.reflect.Field;
//查看允许用户传入的动态参数信息
@CommandLine.Command(name = "config", description = "查看参数信息", mixinStandardHelpOptions = true)
public class ConfigCommand implements Runnable {
    @Override
    public void run() {
        //通过反射获取类的属性
        Class<DataModel> configClass = DataModel.class;
        Field[] fields = configClass.getDeclaredFields();
        //或者通过hutool工具库获取
//      Field[] fields = ReflectUtil.getFields(MainTemplatesConfig.class);
        //遍历字段
        for (Field field : fields) {
            System.out.println("字段名称: " + field.getName());
            System.out.println("字段类型: " + field.getType());
            System.out.println("---------------");
        }
    }


    public static void main(String[] args) {
//        Class<MainTemplatesConfig> configClass = MainTemplatesConfig.class;
//        Field[] fields = configClass.getDeclaredFields();
//        for (Field field : fields) {
//            System.out.println(field.getName());
//        }
    }
}

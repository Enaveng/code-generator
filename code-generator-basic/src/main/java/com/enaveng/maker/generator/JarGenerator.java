package com.enaveng.maker.generator;

import java.io.*;

//执行jar包构建
public class JarGenerator {

    public static void doGenerate(String filePath) throws InterruptedException, IOException {
        //构建打包命令 先清除再进行打包并且跳过测试 不同操作系统下的打包命令不同
        String winPackageCommand = "mvn.cmd clean package -DskipTests=true";
        String otherPackageCommand = "mvn clean package -DskipTests=true";
        String mavenCommand = winPackageCommand;
        //调用process类实现maven打包
        ProcessBuilder processBuilder = new ProcessBuilder(mavenCommand.split(" "));
        processBuilder.directory(new File(filePath));
        Process process = processBuilder.start();
        //读取运行输出
        InputStream inputStream = process.getInputStream();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            System.out.println(line);
        }
        int runCode = process.waitFor();  //得到运行完成之后的返回码
        System.out.println("命令执行完成 返回码为: " + runCode);
    }

    //测试运行
    public static void main(String[] args) throws IOException, InterruptedException {
        doGenerate("D:\\MyProject\\code-generator\\code-generator-basic");
    }
}

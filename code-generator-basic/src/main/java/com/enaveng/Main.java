package com.enaveng;

import com.enaveng.cli.CommandExecutor;

//命令模式客户端
public class Main {
    public static void main(String[] args) {
        args = new String[]{"generate", "-l", "-a", "-o"};
//        args = new String[]{"list"};
//        args = new String[]{"config"};
        CommandExecutor commandExecutor = new CommandExecutor();
        commandExecutor.doExecute(args);
    }
}

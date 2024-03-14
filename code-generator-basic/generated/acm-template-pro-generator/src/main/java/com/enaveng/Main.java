package com.enaveng;

import com.enaveng.cli.CommandExecutor;

public class Main {
    public static void main(String[] args) {
        CommandExecutor commandExecutor = new CommandExecutor();
//        args = new String[]{"generate", "-l"};
        args = new String[]{"json-generate", "-f=D:/MyProject/code-generator/code-generator-basic/generated/acm-template-pro-generator/test.json"};
        commandExecutor.doExecute(args);
    }
}
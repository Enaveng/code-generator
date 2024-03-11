package com.enaveng.cli.pattern;

/**
 * 相当于遥控器操作按钮的行为规范
 */
//命令接口 定义了执行操作的方法
public interface Command {
    void execute();
}

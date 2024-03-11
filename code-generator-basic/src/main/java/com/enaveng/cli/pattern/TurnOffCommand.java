package com.enaveng.cli.pattern;

/**
 * 具体的操作按钮
 */
//定义一个关闭设备的命令
public class TurnOffCommand implements Command {

    private Device device;

    public TurnOffCommand(Device device) {
        this.device = device;
    }

    @Override
    public void execute() {
        device.TurnOffCommand();
    }
}

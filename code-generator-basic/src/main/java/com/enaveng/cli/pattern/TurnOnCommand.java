package com.enaveng.cli.pattern;

/**
 * 具体的操作按钮
 */
//开启设备的命令
public class TurnOnCommand implements Command{
    private Device device;

    public TurnOnCommand(Device device) {
        this.device = device;
    }

    @Override
    public void execute() {
         device.TurnOnCommand();
    }
}

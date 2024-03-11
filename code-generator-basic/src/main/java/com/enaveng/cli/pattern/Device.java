package com.enaveng.cli.pattern;

/**
 * 接收者 相当于被遥控的设备
 */
public class Device {
    private String name;

    public Device(String name) {
        this.name = name;
    }


    public void TurnOffCommand() {
        System.out.println("设备关闭");
    }

    public void TurnOnCommand() {
        System.out.println("设备开启");
    }

}

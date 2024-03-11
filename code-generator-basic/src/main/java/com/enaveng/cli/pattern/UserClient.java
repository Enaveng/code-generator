package com.enaveng.cli.pattern;

//客户端 相当于使用遥控器的人
public class UserClient {
    public static void main(String[] args) {
        //创建接收者对象
        Device device = new Device("Tv");
        //创建命令
        TurnOffCommand turnOffCommand = new TurnOffCommand(device);
        TurnOnCommand turnOnCommand = new TurnOnCommand(device);
        //创建调用者
        RemoteControl remoteControl = new RemoteControl();
        remoteControl.setCommand(turnOffCommand);
        remoteControl.pressButton();
        remoteControl.setCommand(turnOnCommand);
        remoteControl.pressButton();
    }

}

package com.enaveng.cli;


import com.enaveng.cli.Command.ConfigCommand;
import com.enaveng.cli.Command.GenerateCommand;
import com.enaveng.cli.Command.ListCommand;
import picocli.CommandLine;

/**
 * 负责绑定所有子命令 调用方 相当于遥控器 每一个具体命令相当于遥控器上的按钮
 * 命令执行器
 */
@CommandLine.Command(name = "exec", mixinStandardHelpOptions = true)
public class CommandExecutor implements Runnable {

    private final CommandLine commandLine;

    {
        commandLine = new CommandLine(this) //将遥控器与按钮进行绑定
                .addSubcommand(new GenerateCommand())
                .addSubcommand(new ConfigCommand())
                .addSubcommand(new ListCommand());
    }

    @Override
    public void run() {
        // 不输入子命令时，给出友好提示
        System.out.println("请输入具体命令，或者输入 --help 查看命令提示");
    }

    /**
     * 执行命令
     *
     * @param args
     * @return
     */
    public Integer doExecute(String[] args) {
        return commandLine.execute(args);
    }
}

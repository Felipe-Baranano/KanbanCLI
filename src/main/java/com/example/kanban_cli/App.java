package com.example.kanban_cli;

import picocli.CommandLine;
import picocli.CommandLine.Command;


@Command(name = "kanban",
        aliases = {"kb"},
        description = "KanbanCLI - Manage your tasks",
        mixinStandardHelpOptions = true,
        version = "KanbanCLI 1.0.0",
        subcommands = {})
public class App implements Runnable {

    @Override
    public void run() {
        // If no subcommand is provided, display help
        CommandLine.usage(this, System.out);
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new App()).execute(args);
        System.exit(exitCode);
    }
}

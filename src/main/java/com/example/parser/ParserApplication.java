package com.example.parser;

import com.example.parser.cli.ParserCommand;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import picocli.CommandLine;
import picocli.CommandLine.IFactory;

@SpringBootApplication
public class ParserApplication {

    public static void main(String[] args) {
        // Spring Boot has no native picocli runner; the starter only registers a
        // Spring-backed IFactory. So we start the context, then run the @Command
        // bean explicitly. The IFactory resolves any subcommands as Spring beans.
        ConfigurableApplicationContext context = SpringApplication.run(ParserApplication.class, args);
        CommandLine commandLine = new CommandLine(
                context.getBean(ParserCommand.class), context.getBean(IFactory.class));
        System.exit(commandLine.execute(args));
    }

}

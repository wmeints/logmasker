package com.infosupport.knownow.logmasker;

import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Program {
    private static final String COMMANDLINE_ARG_INPUT = "in";
    private static final String COMMANDLINE_ARG_OUTPUT = "out";

    private Logger logger = LoggerFactory.getLogger(Program.class);

    public static void main(String[] args) {
        new Program().run(args);
    }

    public void run(String[] args) {
        CommandLine commandLine = parseArguments(args);

        if (commandLine == null) {
            System.exit(1);
            return;
        }

        HashedValueRegistry valueRegistry = new HashedValueRegistry();

        Path inputFolder = Paths.get(commandLine.getOptionValue(COMMANDLINE_ARG_INPUT));
        Path outputFolder = Paths.get(commandLine.getOptionValue(COMMANDLINE_ARG_OUTPUT));

        try (DirectoryStream<Path> inputFiles = Files.newDirectoryStream(inputFolder)) {
            for (Path inputFile : inputFiles) {
                LogFileProcessor processor = new LogFileProcessor(valueRegistry, inputFile, outputFolder);
                processor.process();
            }
        } catch (IOException e) {
            logger.error("Could not read from the input folder.", e);
        }
    }

    private Options commandLineArguments() {
        return new Options()
                .addOption(Option.builder(COMMANDLINE_ARG_INPUT).hasArg().required().build())
                .addOption(Option.builder(COMMANDLINE_ARG_OUTPUT).hasArg().required().build());
    }

    private CommandLine parseArguments(String[] args) {
        CommandLineParser parser = new DefaultParser();

        try {
            return parser.parse(commandLineArguments(), args);
        } catch (ParseException e) {
            logger.error("Failed to parse the commandline arguments", e);
            return null;
        }
    }
}

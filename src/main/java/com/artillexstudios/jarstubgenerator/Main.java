package com.artillexstudios.jarstubgenerator;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.nio.file.Path;

public class Main {

    public static void main(String[] args) {
        Options options = new Options();

        options.addOption(Option.builder("p")
                .longOpt("path")
                .hasArg(true)
                .required()
                .type(Path.class)
                .build()
        );

        options.addOption(new Option("keep-fields", ""));

        options.addOption(Option.builder("f")
                .longOpt("field-whitelist")
                .hasArg()
                .build()
        );

        options.addOption(Option.builder("o")
                .longOpt("out")
                .hasArg(true)
                .type(Path.class)
                .build()
        );

        options.addOption(Option.builder("s")
                .longOpt("suffix")
                .hasArg()
                .build()
        );

        CommandLine line;
        CommandLineParser parser = new DefaultParser();
        try {
            line = parser.parse(options, args);
        } catch (ParseException exception) {
            System.out.println("Failed to parse options!");
            exception.printStackTrace();
            return;
        }

        try {
            JarTransformer transformer = new JarTransformer(line, line.getParsedOptionValue("p"));
            transformer.run();
        } catch (ParseException exception) {
            exception.printStackTrace();
        }
    }
}
package com.infosupport.knownow.logmasker;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class LogFileProcessor {
    private Logger logger = LoggerFactory.getLogger(LogFileProcessor.class);
    private HashedValueRegistry valueRegistry;
    private Path inputPath;
    private Path outputPath;
    private List<Integer> obfuscatedFields;
    private int urlFieldOffset;

    public LogFileProcessor(HashedValueRegistry valueRegistry, Path inputPath, Path outputPath) {
        this.inputPath = inputPath;
        this.outputPath = outputPath;
        this.valueRegistry = valueRegistry;
    }

    public void process() {
        logger.info("Processing '{}' saving in '{}'", inputPath, fileOutputPath());

        try {
            List<String> logStatements = FileUtils.readLines(inputPath.toFile());
            List<String> processedLogStatements = new ArrayList<>();

            for (String line : logStatements) {
                // Copy lines that contain comments only
                if (line.startsWith("#")) {
                    processedLogStatements.add(line);

                    // Check if this is a fields line and extract the fields that need to be obfuscated.
                    // This line appears first before any log records, and appears again when the layout
                    // of the next record is different from the previous records.
                    if (line.startsWith("#Fields:")) {
                        String[] fieldNames = line.substring("#Fields: ".length()).split(" ");
                        List<Integer> savedOffsets = new ArrayList<>();

                        for (int offset = 0; offset < fieldNames.length; offset++) {
                            // cs-username and c-ip fields should be obfuscated
                            if (fieldNames[offset].equals("cs-username") || fieldNames[offset].equals("c-ip")) {
                                savedOffsets.add(offset);
                            }

                            // cs-uri-stem is stored so that status log statements can be removed
                            // from the log file.
                            if (fieldNames[offset].equals("cs-uri-stem")) {
                                urlFieldOffset = offset;
                            }
                        }

                        obfuscatedFields = savedOffsets;
                    }

                    continue;
                }

                String processedStatement = processLogStatement(line);

                // Skipped lines are not returned by the previous line.
                // When this happens, don't write anything to the output.
                if (processedStatement != null) {
                    processedLogStatements.add(processedStatement);
                }
            }

            FileUtils.writeLines(fileOutputPath().toFile(), processedLogStatements);
        } catch (IOException e) {
            logger.error("Failed to process log file", e);
        }
    }

    private String processLogStatement(String line) {
        String[] fields = line.split(" ");

        if (!shouldProcess(fields)) {
            return null;
        }

        List<String> processedFields = IntStream.range(0, fields.length).mapToObj(offset -> {
            if (hashed(offset)) {
                return valueRegistry.get(fields[offset]);
            } else {
                return fields[offset];
            }
        }).collect(Collectors.toList());

        return String.join(" ", processedFields);
    }

    private boolean hashed(int offset) {
        for (int hashedFieldOffset : obfuscatedFields) {
            if (hashedFieldOffset == offset) {
                return true;
            }
        }

        return false;
    }

    private boolean shouldProcess(String[] fields) {
        // cs-uri-stem should not point to the status URL
        if (fields[urlFieldOffset].equals("/status") || fields[urlFieldOffset].equals("/knownow/status")) {
            return false;
        }

        return true;
    }

    private Path fileOutputPath() {
        return Paths.get(outputPath.toString(), inputPath.getFileName().toString());
    }
}

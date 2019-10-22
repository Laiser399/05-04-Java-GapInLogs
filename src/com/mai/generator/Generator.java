package com.mai.generator;

import com.mai.Log;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class Generator {
    private static final String filename = "D:\\_BIG_FILES_\\BigFileForJavaLab\\query_logs.txt";
    private static final int logsCount = 1_000;
    private static final double randomMultiplier = 10_000;

    private Random random = new Random(123);

    private void start() {
        List<Log> logs = generateLogs();
        saveLogs(logs, filename);
    }

    private List<Log> generateLogs() {
        List<Log> logs = new ArrayList<>(logsCount * 2);
        for (int i = 0; i < logsCount; ++i) {
            Log queryLog = new Log(new Date(i * 1000));
            Log resultQuery = queryLog.createResultLog(
                    (int) Math.abs(random.nextGaussian() * randomMultiplier));
            logs.add(queryLog);
            logs.add(resultQuery);
        }
        logs.sort(Comparator.comparing(Log::getDate));
        return logs;
    }

    private void saveLogs(List<Log> logs, String filename) {
        try (FileWriter writer = new FileWriter(filename)) {
            for (Log log : logs) {
                writer.write(log.toString());
                writer.write('\n');
            }
        }
        catch (IOException e) {
            System.out.println("IO error while write to \"" + filename + "\"");
        }
    }


    public static void main(String[] args) {
        Generator generator = new Generator();
        generator.start();
    }
}

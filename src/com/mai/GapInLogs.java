package com.mai;

import com.mai.exceptions.LogFormatException;
import com.sun.xml.internal.ws.api.model.wsdl.WSDLOutput;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GapInLogs {
    private static final Pattern logPattern =
            Pattern.compile("^([\\d\\-]+ [\\d:]+) - INFO - ([A-Z ]+) FOR ID = (\\d+)$");
    private static final SimpleDateFormat dateFormat =
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private void start() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            while (true) {
                String filename = "D:\\_BIG_FILES_\\BigFileForJavaLab\\query_logs.txt";
                String saveFilename = "D:\\_BIG_FILES_\\BigFileForJavaLab\\gap_results.txt";
                long deviation = 19_000;

//                System.out.print("Enter input filename: ");
//                String filename = reader.readLine();
//                System.out.print("Enter save filename: ");
//                String saveFilename = reader.readLine();
//                long deviation = fetchDeviationInMs(reader);

                List<LogPair> logs = parseFile(filename);
                System.out.println("Read file done.");
                double averageGap = calcAverageGap(logs);
                if (deviation == 0)
                    deviation = getDeviationBy(averageGap);
                System.out.println("Deviation: " + deviation + "ms");

                System.out.println("Average gap: " + averageGap + "ms");
                List<LogPair> deviatedLogs = getDeviatedLogs(logs, averageGap, deviation);
                System.out.println("Found " + deviatedLogs.size() + " deviated logs.");
                try {
                    saveResults(saveFilename, deviatedLogs, averageGap);
                }
                catch (IOException e) {
                    System.out.println("Error saving results in \"" + saveFilename + "\".");
                }
                System.out.println("Results saved");

                break;//TODO
            }
        }
        catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private long fetchDeviationInMs(BufferedReader reader) throws IOException {
        Long deviation = null;
        while (deviation == null) {
            System.out.print("Enter deviation (s): ");
            try {
                String line = reader.readLine();
                if (line.isEmpty())
                    return 0;

                deviation = Long.parseUnsignedLong(line) * 1000;
            }
            catch (NumberFormatException e) {
                System.out.println("Wrong number format.");
            }
        }
        return deviation;
    }

    private long getDeviationBy(double averageGap) {
        return (long) (averageGap * 1.25);
    }

    private List<LogPair> parseFile(String filename) throws IOException {
        long fileSize = new File(filename).length();
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(filename))) {
            Map<Integer, Log> queryLogs = new HashMap<>();
            List<LogPair> pairs = new ArrayList<>();
            long charsRead = 0;
            long flagToDisplayStatus = 1;
            while (reader.ready()) {
                String line = reader.readLine();
                try {
                    Log log = parseLog(line);
                    if (log.getType() == Log.Type.Query) {
                        queryLogs.put(log.getId(), log);
                    }
                    else if (log.getType() == Log.Type.Result) {
                        Log queryLog = queryLogs.get(log.getId());
                        if (queryLog != null) {
                            queryLogs.remove(queryLog.getId(), queryLog);
                            pairs.add(new LogPair(queryLog, log));
                        }
                    }
                }
                catch (LogFormatException e) {}

                charsRead += line.length() + 1;
                if (flagToDisplayStatus++ % 500 == 0) {
                    System.out.printf("Read %.2f%s of file.\n", (double) charsRead / fileSize * 100, "%");
                }
            }
            return pairs;
        }
    }

    private Log parseLog(String line) throws LogFormatException {
        Matcher matcher = logPattern.matcher(line);
        if (matcher.matches()) {
            try {
                Date date = dateFormat.parse(matcher.group(1));
                String logType = matcher.group(2);
                int id = Integer.parseUnsignedInt(matcher.group(3));

                if (logType.equals("QUERY"))
                    return new Log(date, Log.Type.Query, id);
                else if (logType.equals("RESULT QUERY"))
                    return new Log(date, Log.Type.Result, id);
                else
                    throw new LogFormatException("Wrong log format.");
            }
            catch (ParseException e) {
                throw new LogFormatException("Wrong log format.");
            }
        }
        else
            throw new LogFormatException("Wrong log format.");
    }

    private List<LogPair> getDeviatedLogs(List<LogPair> logs, double averageGap, double deviation) {
        List<LogPair> deviatedLogs = new ArrayList<>();
        for (LogPair pair : logs) {
            if (Math.abs(pair.getGap() - averageGap) > deviation)
                deviatedLogs.add(pair);
        }
        return deviatedLogs;
    }

    private double calcAverageGap(List<LogPair> logs) {
        long sum = 0;
        int count = 0;
        for (LogPair pair : logs) {
            sum += pair.getGap();
            count++;
        }
        return count == 0 ? 0 : (double) sum / count;
    }

    private void saveResults(String filename, List<LogPair> logs, double averageGap)
            throws IOException
    {
        try (FileWriter writer = new FileWriter(filename)) {
            writer.write("Average gap: ");
            writer.write(Double.toString(averageGap));
            writer.write("\n\n");

            for (LogPair pair : logs) {
                writer.write("Deviation: ");
                writer.write(Long.toString((long) (pair.getGap() - averageGap)));
                writer.write("ms");
                writer.write('\n');
                writer.write(pair.getQueryLog().toString());
                writer.write('\n');
                writer.write(pair.getResultLog().toString());
                writer.write("\n\n");
            }
        }
    }



    public static void main(String[] args) {
        GapInLogs gapInLogs = new GapInLogs();
        gapInLogs.start();
    }
}

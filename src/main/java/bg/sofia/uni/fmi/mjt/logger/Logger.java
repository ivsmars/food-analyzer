package bg.sofia.uni.fmi.mjt.logger;

import bg.sofia.uni.fmi.mjt.exceptions.LogException;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;

public class Logger {
    private Path logsDir;
    private Path logFilePath;

    //this member is used for testing purposes
    private boolean isFile = true;

    private BufferedWriter bufferedLogsWriter;
    private static final String LOG_FORMAT = "%s|%s\n%s";
    private static final String LOG_FILE_FORMAT = "logs-%d.txt";

    private int logWritesCount;
    private static final int MAX_LOG_FILE_SIZE = 1024 * 64;

    private int logFileCount = 1;


    public Logger(Path logsDir) throws LogException {
        checkNull(logsDir, "Log files directory");

        this.logsDir = logsDir;
        logFilePath = generateLogFilePath();
        try {
            if (!Files.exists(logsDir)) {
                Files.createDirectories(logsDir);
            }
            openLogFile();
        } catch (IOException e) {
            throw new LogException("An error occurred while trying to create/open log file", e);
        }
    }

    public Logger(BufferedWriter writer) {
        checkNull(writer, "Log writer");
        isFile = false;
        bufferedLogsWriter = writer;
    }


    public synchronized void log(Level level, Throwable e) {
        checkNull(level, "Log level");
        checkNull(e, "Throwable");

        try (StringWriter sw = new StringWriter();
             PrintWriter pw = new PrintWriter(sw)) {

            e.printStackTrace(pw);
            if (isFile && isFileTooBig()) {
                close();
                logFilePath = generateLogFilePath();
                openLogFile();
            }

            bufferedLogsWriter.write(String.format(LOG_FORMAT, level, LocalDateTime.now(), sw));
            bufferedLogsWriter.flush();
        } catch (LogException | IOException ex) {
            throw new RuntimeException(ex);
        }
        logWritesCount++;
    }

    private synchronized Path generateLogFilePath() {
        return Path.of(logsDir.toString(), String.format(LOG_FILE_FORMAT, logFileCount++));
    }

    private synchronized void openLogFile() throws IOException {
        bufferedLogsWriter = Files.newBufferedWriter(logFilePath,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);
    }

    private synchronized boolean isFileTooBig() throws IOException {
        return Files.size(logFilePath) > MAX_LOG_FILE_SIZE;
    }

    public synchronized void close() throws LogException {
        try {
            bufferedLogsWriter.close();
        } catch (IOException e) {
            throw new LogException("An error occurred while closing log file", e);
        }
    }

    public int getLogWritesCount() {
        return logWritesCount;
    }

    private void checkNull(Object object, String name) {
        if (object == null) {
            throw new IllegalArgumentException(String.format("%s can't be null", name));
        }
    }

}

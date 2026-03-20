/*import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;

enum LogLevel {
    INFO, WARN, ERROR
}

record LogRecord(LogLevel level, String logger, String message, Throwable exception) {
    LogRecord(LogLevel level, String logger, String message) {
        this(level, logger, message, null);
    }

    String format() {
        String base = String.format("[%s] %s - %s", level, logger, message);
        if (exception != null) {
            StringWriter sw = new StringWriter();
            exception.printStackTrace(new PrintWriter(sw));
            return base + "\n" + sw;
        }
        return base;
    }
}

abstract class LogHandler {
    abstract void log(LogRecord record) throws IOException;
}

class ConsoleLogHandler extends LogHandler {
    @Override
    void log(LogRecord record) {
        System.out.println(record.format());
    }
}

class FileLogHandler extends LogHandler {

    private static final long MAX_SIZE = 1024;
    private static final int MAX_FILES = 3;

    private final String fileName;
    private BufferedWriter writer;

    FileLogHandler(String fileName) throws IOException {
        this.fileName = fileName;
        writer = new BufferedWriter(new FileWriter(fileName, true));
    }


    @Override
    public void log(LogRecord record) throws IOException {
        rotateIfNeeded();
        writer.write(record.format());
        writer.newLine();
        writer.flush();
    }

    private void rotateIfNeeded() throws IOException {
        File file = new File(fileName);
        if (file.length() < MAX_SIZE) return;
        writer.close();
        for (int i = MAX_FILES - 1; i >= 1; i--) {
            Path src = Paths.get(fileName + "." + i);
            Path destination = Paths.get(fileName + "." + (i + 1));

            if (Files.exists(src)) {
                Files.move(src, destination, StandardCopyOption.REPLACE_EXISTING);
            }
        }
        Files.move(Paths.get(fileName), Paths.get(fileName + ".1"), StandardCopyOption.REPLACE_EXISTING);
        writer = new BufferedWriter(new FileWriter(fileName));
    }
}

class TimeStampLogHandler extends FileLogHandler {
    TimeStampLogHandler(String fileName) throws IOException {
        super(fileName);
    }

    @Override
    public void log(LogRecord record) throws IOException {
        String newMessage = LocalDateTime.now() + " " + record.message();
        LogRecord newRecord = new LogRecord(record.level(), record.logger(), newMessage, record.exception());
        super.log(newRecord);
    }
}

class Logger {

    private final String name;
    private final BlockingQueue<LogRecord> queue = new LinkedBlockingQueue<>(10000);
    private final List<LogHandler> handlers = new ArrayList<>();

    public Logger(Class<?> cls) {
        this.name = cls.getName();
        Runtime.getRuntime().addShutdownHook(new Thread(this::flush));
        startWorker();
    }

    public void addConsoleLogger() {
        handlers.add(new ConsoleLogHandler());
    }

    public void addFileLogger(String path) throws IOException {
        handlers.add(new FileLogHandler(path));
    }

    public void addTimestampLogger(String path) throws IOException {
        handlers.add(new TimeStampLogHandler(path));
    }

    private void flush() { //in the current main thread, exits after writing the unwritten logs.
        LogRecord record;
        while ((record = queue.poll()) != null) {
            for (LogHandler h : handlers) {
                try {
                    h.log(record);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void log(LogLevel logLevel, String message) {
        LogRecord record = new LogRecord(logLevel, name, message);
        if (!queue.offer(record)) {
            System.err.println("Log queue full, dropping message: " + record.format());
        }
    }

    private void log(String message, Throwable ex) {
        LogRecord record = new LogRecord(LogLevel.ERROR, name, message, ex);

        if (!queue.offer(record)) {
            System.err.println("Log queue full, dropping message: " + record.format());
        }
    }

    public void info(String msg) {
        log(LogLevel.INFO, msg);
    }

    public void warn(String msg) {
        log(LogLevel.WARN, msg);
    }

    public void error(String msg, Throwable ex) {
        log(msg, ex);
    }

    private void startWorker() {
        Thread worker = new Thread(() -> {
            try {
                while (true) {
                    LogRecord record = queue.take();
                    for (LogHandler h : handlers) {
                        try {
                            h.log(record);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

        });
        worker.setDaemon(true);
        worker.start();
    }
}*/
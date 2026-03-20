import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.*;
import java.util.*;
import java.io.*;
import java.nio.file.*;

enum LogLevel {
    INFO, WARN, ERROR
}

record LogRecord(LogLevel level, String logger, String message, Throwable exception) {

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

abstract class Handler {
    abstract void log(LogRecord record) throws IOException;
}

class ConsoleHandler extends Handler {

    @Override
    void log(LogRecord record) {
        System.out.println(record.format());
    }
}

class FileHandler extends Handler {
    private static final long MAX_SIZE = 1024;
    private static final int MAX_FILES = 3;

    private final String fileName;
    private BufferedWriter writer;

    private static final DateTimeFormatter TS_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss.SSS");

    FileHandler( String fileName) throws IOException {
        this.fileName = fileName;
        writer = new BufferedWriter(new FileWriter(fileName, true));
    }

    @Override
    void log(LogRecord record) throws IOException {
        rotateIfNeeded();

        writer.write(record.format());
        writer.newLine();
        writer.flush();
    }

    private void rotateIfNeeded() throws IOException {

        File file = new File(fileName);

        if (file.length() < MAX_SIZE) return;

        writer.close();
        String timestamp = LocalDateTime.now().format(TS_FORMAT);

        Path rotated = Paths.get(fileName + "." + timestamp);

        Path source = Paths.get(fileName);

        if (!Files.exists(source)) {
            writer = new BufferedWriter(new FileWriter(fileName, true));
            return;
        }

        Files.move(source, rotated);

        writer = new BufferedWriter(new FileWriter(fileName, true));
        cleanupOldFiles();
    }

    private void cleanupOldFiles() {

        File file = new File(fileName);
        String baseName = file.getName();

        File dir = file.getParentFile();
        if (dir == null || !dir.exists()) return;

        File[] files = dir.listFiles((d, name) -> name.startsWith(baseName + "."));

        if (files == null || files.length < MAX_FILES) return;

        // sort new to old
        Arrays.sort(files, (a, b) -> b.getName().compareTo(a.getName()));

        for (int i = MAX_FILES; i < files.length; i++) {
            try {
                Files.deleteIfExists(files[i].toPath());
            } catch (IOException e) {
                System.err.println("Failed to delete log file: " + files[i].getAbsolutePath());
                e.printStackTrace();
            }
        }
    }
}

class TimeStampLogHandler extends FileHandler {
    TimeStampLogHandler(String fileName) throws IOException {
        super(fileName);
    }

    @Override
    public void log(LogRecord record) throws IOException {
        String newMessage = LocalDateTime.now() + " " + record.message();
        LogRecord newRecord = new LogRecord(
                record.level(), record.logger(), newMessage, record.exception());
        super.log(newRecord);
    }
}

class LogDispatcher {

    private static final BlockingQueue<LogRecord> queue = new LinkedBlockingQueue<>(10000);

    private static final List<Handler> handlers = new ArrayList<>();

    static {
        startWorker();
        Runtime.getRuntime().addShutdownHook(new Thread(LogDispatcher::flush));
    }

    public static void addHandler(Handler handler) {
        handlers.add(handler);
    }

    public static void dispatch(LogRecord record) {

        if (!queue.offer(record)) {
            System.err.println("Log queue full: " + record.format());
        }
    }

    private static void startWorker() {

        Thread worker = new Thread(() -> {

            try {

                while (true) {

                    LogRecord record = queue.take();

                    for (Handler a : handlers) {
                        a.log(record);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        });

        worker.setDaemon(true);
        worker.start();
    }

    public static void flush() {

        LogRecord record;

        while ((record = queue.poll()) != null) {

            for (Handler a : handlers) {

                try {
                    a.log(record);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

class Logger {

    private final String name;

    Logger(Class<?> cls) {
        this.name = cls.getName();
    }

    public void info(String msg) {
        LogDispatcher.dispatch(new LogRecord(LogLevel.INFO, name, msg, null));
    }

    public void warn(String msg) {
        LogDispatcher.dispatch(new LogRecord(LogLevel.WARN, name, msg, null));
    }

    public void error(String msg, Throwable ex) {
        LogDispatcher.dispatch(new LogRecord(LogLevel.ERROR, name, msg, ex));
    }
}


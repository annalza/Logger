import java.io.IOException;

class LoggingConfig {

    static {

        try {
            LogDispatcher.addHandler(new ConsoleHandler());

            LogDispatcher.addHandler(
                    new FileHandler("logs/app.log")
            );
            LogDispatcher.addHandler(
                    new TimeStampLogHandler("logs/client-2.log")
            );

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
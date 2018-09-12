package rs.lukaj.upisstats.scraper.download;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DownloadLogger {

    public static final String GENERAL = "general.log", SMEROVI = "smerovi.log", OSNOVNE = "osnovne.log", UCENICI = "ucenici.log";

    public enum Level {
        VERBOSE(2), DEBUG(1), NORMAL(0), WARNING(-1), ERROR(-2), PANIC(-5);
        private int level;
        Level(int level) {
            this.level = level;
        }
    }
    private static File LOG_DIR = new File(DownloadController.DATA_FOLDER, "logs");
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static String PANIC_LOG_NAME = "panic";

    private static Level logLevel = Level.NORMAL;

    static {
        LOG_DIR.mkdirs();
    }

    public static void setLogLevel(Level level) {
        logLevel = level;
    }

    private static Map<String, DownloadLogger> loggers = new HashMap<>();

    public static DownloadLogger getLogger(String name) {
        if(!loggers.containsKey(name)) {
            try {
                loggers.put(name, new DownloadLogger(name));
            } catch (IOException e) {
                if(name.equals(PANIC_LOG_NAME)) {
                    System.err.println("Double log panic!");
                    e.printStackTrace();
                    return null;
                }
                getLogger(PANIC_LOG_NAME).log(Level.PANIC, e);
            }
        }
        return loggers.get(name);
    }

    private String name;
    private FileWriter writer;

    private DownloadLogger(String name) throws IOException {
        this.name = name;
        File logFile = new File(LOG_DIR, name);
        logFile.createNewFile();
        writer = new FileWriter(logFile);
    }

    public void log(Level level, String msg) {
        if(level.level > logLevel.level) return;
        executor.submit(() -> {
            try {
                writer.append(msg).append("\n").flush();
            } catch (IOException ex) {
                if(level == Level.PANIC) {
                    System.err.println("Double log panic!");
                    ex.printStackTrace();
                    return;
                }
                getLogger(PANIC_LOG_NAME).log(Level.PANIC, ex);
            }
        });
    }

    public void log(Level level, Exception ex) {
        if(level.level > logLevel.level) return;
        executor.submit(() -> {
            ex.printStackTrace(new PrintWriter(writer, true));
            try {
                writer.flush();
            } catch (IOException e) {
                if(level == Level.PANIC) {
                    System.err.println("Double log panic!");
                    e.printStackTrace();
                    return;
                }
                getLogger(PANIC_LOG_NAME).log(Level.PANIC, e);
            }
        });
    }

    public String getName() {
        return name;
    }
}

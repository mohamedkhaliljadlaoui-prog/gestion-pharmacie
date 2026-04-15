package utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LoggerUtil {
    private static final DateTimeFormatter dtf = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    public static void info(String message) {
        log("INFO", message);
    }
    
    public static void warn(String message) {
        log("WARN", message);
    }
    
    public static void error(String message) {
        log("ERROR", message);
    }
    
    public static void error(String message, Exception e) {
        log("ERROR", message + " - " + e.getMessage());
        e.printStackTrace();
    }
    
    private static void log(String level, String message) {
        String timestamp = LocalDateTime.now().format(dtf);
        String logMessage = String.format("[%s] %s: %s", timestamp, level, message);
        System.out.println(logMessage);
    }
}
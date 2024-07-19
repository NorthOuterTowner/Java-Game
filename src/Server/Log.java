package server;

import java.util.logging.LogManager;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.Logger;

public class Log {
    private static final Logger logger = Logger.getLogger(Log.class.getName());
    
    public Log() {
        try {
            LogManager.getLogManager().readConfiguration(new FileInputStream("logging.properties"));
        } catch (IOException e) {
            System.err.println("Could not load logging.properties file");
            e.printStackTrace();
        }

        logger.info("Logging to a file!");
    }
    
    public static void info(String info) {
    	logger.info(info);
    }
    public static void warning(String info) {
    	logger.warning(info);
    }
    public static void severe(String info) {
    	logger.severe(info);
    }
    
}

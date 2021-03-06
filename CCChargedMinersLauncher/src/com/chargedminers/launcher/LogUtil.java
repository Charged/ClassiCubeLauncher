package com.chargedminers.launcher;

import com.chargedminers.launcher.gui.ErrorScreen;
import com.chargedminers.shared.SharedUpdaterCode;
import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

// Global logging class (to make life easier)
public final class LogUtil {

    private static final Logger logger = Logger.getLogger(LogUtil.class.getName());

    // Sets up logging to file (%AppData%/charge/launcher.log)
    public static void init() throws IOException {
        logger.setLevel(Level.ALL);

        final File logFile = new File(SharedUpdaterCode.getDataDir(), PathUtil.LOG_FILE_NAME);
        final File logOldFile = new File(SharedUpdaterCode.getDataDir(), PathUtil.LOG_OLD_FILE_NAME);

        // If a logfile already exists, rename it to "launcher.old.log"
        if (logFile.exists()) {
            if (logOldFile.exists()) {
                logOldFile.delete();
            }
            logFile.renameTo(logOldFile);
        }

        // Set up log file handler for this session
        try {
            final FileHandler handler = new FileHandler(logFile.getAbsolutePath());
            handler.setFormatter(new SimpleFormatter());
            logger.addHandler(handler);
        } catch (final IOException | SecurityException ex) {
            ErrorScreen.show("Error creating log file", ex.getMessage(), ex);
            System.exit(2);
        }
    }

    public static Logger getLogger() {
        return logger;
    }

    private LogUtil() {
    }
}

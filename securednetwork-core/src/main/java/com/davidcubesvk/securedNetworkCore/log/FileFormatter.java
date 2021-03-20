package com.davidcubesvk.securedNetworkCore.log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * Formats log records into strings, which are then saved into a log file.
 */
public class FileFormatter extends Formatter {

    @Override
    public String format(LogRecord record) {
        return "[" + formatDateTime(record.getMillis()) + "] " + record.getLevel().getName() + ": " +
                record.getMessage() + "\n" + formatStackTrace(record.getThrown());
    }

    /**
     * Formats the given milliseconds (UNIX time) into the log file pattern.
     *
     * @param millis the UNIX time to format
     * @return milliseconds formatted
     */
    private String formatDateTime(long millis) {
        //Format to pattern
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date(millis));
    }

    /**
     * Formats a throwable's stack trace into a string.
     *
     * @param throwable an exception to format
     * @return the formatted stack trace as a string
     */
    private String formatStackTrace(Throwable throwable) {
        //Create StringWriter
        StringWriter stringWriter = new StringWriter();

        //If throwable is available, print to the StringWriter
        if (throwable != null)
            throwable.printStackTrace(new PrintWriter(stringWriter));

        //Return as a string
        return stringWriter.toString();
    }

}
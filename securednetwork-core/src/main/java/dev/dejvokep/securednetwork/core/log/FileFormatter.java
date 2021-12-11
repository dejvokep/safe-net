/*
 * Copyright 2021 https://dejvokep.dev/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.dejvokep.securednetwork.core.log;

import org.jetbrains.annotations.Nullable;

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
        // Format to pattern
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date(millis));
    }

    /**
     * Formats the given stack trace into a string.
     *
     * @param throwable the exception to format
     * @return the formatted stack trace as a string
     */
    private String formatStackTrace(@Nullable Throwable throwable) {
        // Create StringWriter
        StringWriter stringWriter = new StringWriter();

        // If throwable is available, print to the StringWriter
        if (throwable != null)
            throwable.printStackTrace(new PrintWriter(stringWriter));

        // Return as a string
        return stringWriter.toString();
    }

}
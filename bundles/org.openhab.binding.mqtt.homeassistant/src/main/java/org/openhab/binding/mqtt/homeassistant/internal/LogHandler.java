/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.mqtt.homeassistant.internal;

import java.util.logging.Handler;
import java.util.logging.LogRecord;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.event.Level;

/**
 * Forwards logging from Graal to SLF4J
 *
 * @author Cody Cutrer - Initial contribution
 */
@NonNullByDefault
public class LogHandler extends Handler {
    private final Logger logger;

    public LogHandler(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void publish(@Nullable LogRecord record) {
        if (record == null || record.getMessage() == null || record.getLevel() == java.util.logging.Level.OFF) {
            return;
        }
        final Level level = toSlf4jLevel(record.getLevel().intValue());
        if (logger.isEnabledForLevel(level)) {
            String message = getFormatter() != null ? getFormatter().format(record) : record.getMessage();
            logger.atLevel(level).log(message, record.getThrown());
        }

        if (!isLoggable(record))
            return;
    }

    @Override
    public void flush() {
        // No flush needed for SLF4J
    }

    @Override
    public void close() throws SecurityException {
        // No resources to close
    }

    private static Level toSlf4jLevel(final int level) {
        switch (level) {
            case 1000: // SEVERE
                return Level.ERROR;
            case 900: // WARNING
                return Level.WARN;
            case 800: // INFO
                return Level.INFO;
            case 700: // CONFIG
            case 500: // FINE
                return Level.DEBUG;
            default:
                return Level.TRACE;
        }
    }
}

/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.knx.internal.projectstore;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

/**
 * Collects the log events of every logger while it is open, down to TRACE whatever level the loggers are set to.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
public final class LogCapture implements AutoCloseable {

    // the backend of the tests is the only way to read the events
    private final ch.qos.logback.classic.Logger root = rootLogger(); // NOPMD
    private final ListAppender<ILoggingEvent> appender = new ListAppender<>();
    private final Level earlierLevel;

    private static ch.qos.logback.classic.Logger rootLogger() { // NOPMD
        return (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME); // NOPMD
    }

    public LogCapture() {
        earlierLevel = root.getLevel();
        root.setLevel(Level.TRACE);
        appender.start();
        root.addAppender(appender);
    }

    /** {@return the formatted messages of all events, with the text of a thrown exception if any} */
    public List<String> all() {
        return events(Level.TRACE);
    }

    /** {@return the formatted messages of the WARN and ERROR events} */
    public List<String> warnings() {
        return events(Level.WARN);
    }

    /** {@return the formatted messages of the ERROR events} */
    public List<String> errors() {
        return events(Level.ERROR);
    }

    private List<String> events(Level threshold) {
        synchronized (appender.list) {
            return appender.list.stream().filter(event -> event.getLevel().isGreaterOrEqual(threshold))
                    .map(event -> event.getFormattedMessage()
                            + (event.getThrowableProxy() == null ? "" : " " + event.getThrowableProxy().getMessage()))
                    .toList();
        }
    }

    @Override
    public void close() {
        root.detachAppender(appender);
        root.setLevel(earlierLevel);
        appender.stop();
    }
}

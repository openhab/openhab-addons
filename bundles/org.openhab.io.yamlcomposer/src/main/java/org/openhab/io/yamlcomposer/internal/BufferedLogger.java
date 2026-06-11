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
package org.openhab.io.yamlcomposer.internal;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.helpers.MessageFormatter;

/**
 * A wrapper/decorator for SLF4J loggers that intercepts warnings for
 * consolidation through {@link LogSession} while passing other
 * log levels through immediately.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
public class BufferedLogger {
    private final Logger delegate;
    private final LogSession session;

    public BufferedLogger(Logger delegate, LogSession session) {
        this.delegate = delegate;
        this.session = session;
    }

    public LogSession getLogSession() {
        return session;
    }

    /**
     * Intercepts warnings and sends them to the session for count consolidation.
     */
    public void warn(String template, Object... args) {
        String resolvedMessage = Objects.requireNonNull(MessageFormatter.arrayFormat(template, args).getMessage());
        session.trackWarning(delegate, resolvedMessage);
    }

    // Pass-through methods for immediate logging
    public void info(String format, Object... args) {
        delegate.info(format, args);
    }

    public void debug(String format, Object... args) {
        delegate.debug(format, args);
    }

    public void trace(String format, Object... args) {
        delegate.trace(format, args);
    }

    public void error(String format, Object... args) {
        delegate.error(format, args);
    }

    // Simple string overloads
    public void info(String msg) {
        delegate.info(msg);
    }

    public void debug(String msg) {
        delegate.debug(msg);
    }

    public void trace(String msg) {
        delegate.trace(msg);
    }

    public void error(String msg) {
        delegate.error(msg);
    }
}

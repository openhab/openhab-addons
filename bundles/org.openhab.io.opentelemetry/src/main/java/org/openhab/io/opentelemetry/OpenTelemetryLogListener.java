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
package org.openhab.io.opentelemetry;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.osgi.service.log.LogEntry;
import org.osgi.service.log.LogLevel;
import org.osgi.service.log.LogListener;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.logs.Logger;
import io.opentelemetry.api.logs.Severity;

/**
 * The {@link OpenTelemetryLogListener} class implements the OSGi {@link LogListener} interface
 * and forwards logs to OpenTelemetry.
 *
 * @author Florian Hotze - Initial contribution
 */
@NonNullByDefault
public class OpenTelemetryLogListener implements LogListener {
    private final Logger otelLogger;

    public OpenTelemetryLogListener(Logger otelLogger) {
        this.otelLogger = otelLogger;
    }

    @Override
    public void logged(@NonNullByDefault({}) LogEntry logEntry) {
        Severity severity = mapSeverity(logEntry.getLogLevel());

        var logRecordBuilder = otelLogger.logRecordBuilder().setTimestamp(Instant.ofEpochMilli(logEntry.getTime())) //
                .setObservedTimestamp(Instant.now()) //
                .setSeverity(severity) //
                .setSeverityText(severity.name()) //
                .setBody(logEntry.getMessage());

        AttributesBuilder attributesBuilder = Attributes.builder() //
                .put("log.logger.name", logEntry.getLoggerName()) //
                .put("thread.name", logEntry.getThreadInfo());

        @Nullable
        Throwable exception = logEntry.getException();
        if (exception != null) {
            StringWriter sw = new StringWriter();
            try (PrintWriter pw = new PrintWriter(sw)) {
                exception.printStackTrace(pw);
            }
            String stackTrace = sw.toString();

            String exceptionMessage = exception.getMessage();
            if (exceptionMessage == null) {
                exceptionMessage = exception.getClass().getName();
            }

            attributesBuilder //
                    .put("exception.type", exception.getClass().getName()) //
                    .put("exception.message", exceptionMessage) //
                    .put("exception.stacktrace", stackTrace);
        }

        logRecordBuilder.setAllAttributes(attributesBuilder.build());
        logRecordBuilder.emit();
    }

    private Severity mapSeverity(LogLevel logLevel) {
        return switch (logLevel) {
            case AUDIT -> Severity.INFO4; // high-priority info
            case ERROR -> Severity.ERROR;
            case WARN -> Severity.WARN;
            case INFO -> Severity.INFO;
            case DEBUG -> Severity.DEBUG;
            case TRACE -> Severity.TRACE;
        };
    }
}

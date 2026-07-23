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
package org.openhab.io.opentelemetry.internal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.osgi.service.log.LogEntry;
import org.osgi.service.log.LogLevel;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.logs.LogRecordBuilder;
import io.opentelemetry.api.logs.Logger;
import io.opentelemetry.api.logs.Severity;

/**
 * The {@link OpenTelemetryLogListenerTest} class contains tests for logging mapping logic.
 *
 * @author Florian Hotze - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@NonNullByDefault
@SuppressWarnings("null")
public class OpenTelemetryLogListenerTest {

    private @Mock @NonNullByDefault({}) Logger otelLogger;
    private @Mock(answer = Answers.RETURNS_SELF) @NonNullByDefault({}) LogRecordBuilder logRecordBuilder;

    @Test
    public void testLoggedBasic() {
        when(otelLogger.logRecordBuilder()).thenReturn(logRecordBuilder);

        OpenTelemetryLogListener listener = new OpenTelemetryLogListener(otelLogger);

        LogEntry logEntry = mock(LogEntry.class);
        when(logEntry.getLogLevel()).thenReturn(LogLevel.INFO);
        when(logEntry.getTime()).thenReturn(123456789L);
        when(logEntry.getMessage()).thenReturn("Hello, World!");
        when(logEntry.getLoggerName()).thenReturn("org.openhab.test");
        when(logEntry.getThreadInfo()).thenReturn("main-thread");
        when(logEntry.getException()).thenReturn(null);

        listener.logged(logEntry);

        verify(otelLogger).logRecordBuilder();
        verify(logRecordBuilder).setTimestamp(Instant.ofEpochMilli(123456789L));
        verify(logRecordBuilder).setSeverity(Severity.INFO);
        verify(logRecordBuilder).setSeverityText("INFO");
        verify(logRecordBuilder).setBody("Hello, World!");

        ArgumentCaptor<Attributes> attributesCaptor = ArgumentCaptor.forClass(Attributes.class);
        verify(logRecordBuilder).setAllAttributes(attributesCaptor.capture());
        Attributes attributes = attributesCaptor.getValue();

        assertEquals("org.openhab.test", attributes.get(AttributeKey.stringKey("log.logger.name")));
        assertEquals("main-thread", attributes.get(AttributeKey.stringKey("thread.name")));
        verify(logRecordBuilder).emit();
    }

    @ParameterizedTest
    @CsvSource({ "AUDIT, INFO4", "ERROR, ERROR", "WARN, WARN", "INFO, INFO", "DEBUG, DEBUG", "TRACE, TRACE" })
    public void testSeverityMapping(LogLevel level, Severity expectedSeverity) {
        when(otelLogger.logRecordBuilder()).thenReturn(logRecordBuilder);
        OpenTelemetryLogListener listener = new OpenTelemetryLogListener(otelLogger);

        LogEntry logEntry = mock(LogEntry.class);
        when(logEntry.getLogLevel()).thenReturn(level);
        when(logEntry.getTime()).thenReturn(0L);
        when(logEntry.getMessage()).thenReturn("");
        when(logEntry.getLoggerName()).thenReturn("");
        when(logEntry.getThreadInfo()).thenReturn("");

        listener.logged(logEntry);

        verify(logRecordBuilder).setSeverity(expectedSeverity);
        verify(logRecordBuilder).setSeverityText(expectedSeverity.name());
    }

    @Test
    public void testLoggedWithException() {
        when(otelLogger.logRecordBuilder()).thenReturn(logRecordBuilder);
        OpenTelemetryLogListener listener = new OpenTelemetryLogListener(otelLogger);

        LogEntry logEntry = mock(LogEntry.class);
        when(logEntry.getLogLevel()).thenReturn(LogLevel.ERROR);
        when(logEntry.getTime()).thenReturn(1000L);
        when(logEntry.getMessage()).thenReturn("Oops");
        when(logEntry.getLoggerName()).thenReturn("org.openhab.test");
        when(logEntry.getThreadInfo()).thenReturn("main");

        RuntimeException exception = new RuntimeException("Test Exception");
        when(logEntry.getException()).thenReturn(exception);

        listener.logged(logEntry);

        ArgumentCaptor<Attributes> attributesCaptor = ArgumentCaptor.forClass(Attributes.class);
        verify(logRecordBuilder).setAllAttributes(attributesCaptor.capture());
        Attributes attributes = attributesCaptor.getValue();

        assertEquals("java.lang.RuntimeException", attributes.get(AttributeKey.stringKey("exception.type")));
        assertEquals("Test Exception", attributes.get(AttributeKey.stringKey("exception.message")));

        String stacktrace = attributes.get(AttributeKey.stringKey("exception.stacktrace"));
        assertNotNull(stacktrace);
        assertTrue(stacktrace.contains("java.lang.RuntimeException"));
        assertTrue(stacktrace.contains("testLoggedWithException"));
    }

    @Test
    public void testLoggedWithExceptionNullMessage() {
        when(otelLogger.logRecordBuilder()).thenReturn(logRecordBuilder);
        OpenTelemetryLogListener listener = new OpenTelemetryLogListener(otelLogger);

        LogEntry logEntry = mock(LogEntry.class);
        when(logEntry.getLogLevel()).thenReturn(LogLevel.ERROR);
        when(logEntry.getTime()).thenReturn(1000L);
        when(logEntry.getMessage()).thenReturn("Oops");
        when(logEntry.getLoggerName()).thenReturn("org.openhab.test");
        when(logEntry.getThreadInfo()).thenReturn("main");

        RuntimeException exception = new RuntimeException((String) null);
        when(logEntry.getException()).thenReturn(exception);

        listener.logged(logEntry);

        ArgumentCaptor<Attributes> attributesCaptor = ArgumentCaptor.forClass(Attributes.class);
        verify(logRecordBuilder).setAllAttributes(attributesCaptor.capture());
        Attributes attributes = attributesCaptor.getValue();

        assertEquals("java.lang.RuntimeException", attributes.get(AttributeKey.stringKey("exception.type")));
        assertEquals("java.lang.RuntimeException", attributes.get(AttributeKey.stringKey("exception.message")));
    }

    @Test
    public void testOpenTelemetryInternalLoggingIgnored() {
        OpenTelemetryLogListener listener = new OpenTelemetryLogListener(otelLogger);

        LogEntry logEntry = mock(LogEntry.class);
        when(logEntry.getLoggerName()).thenReturn("io.opentelemetry.exporter.internal.http.HttpExporter");
        when(logEntry.getLogLevel()).thenReturn(LogLevel.ERROR);
        when(logEntry.getMessage()).thenReturn("Failed to export logs");
        when(logEntry.getTime()).thenReturn(System.currentTimeMillis());

        listener.logged(logEntry);

        // Verify that the listener returns early and does not call any methods on the OTel Logger
        verifyNoInteractions(otelLogger);
    }
}

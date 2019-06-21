/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.homeconnect.internal.logger;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.storage.Storage;
import org.openhab.binding.homeconnect.internal.client.HomeConnectApiClient;
import org.openhab.binding.homeconnect.internal.client.HomeConnectSseClient;
import org.openhab.binding.homeconnect.internal.factory.HomeConnectHandlerFactory;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 *
 * Home Connect logger.
 *
 * @author Jonas Brüstel - Initial Contribution
 */
@NonNullByDefault
public class LogWriter {

    private final org.slf4j.Logger slf4jLogger;
    private final Gson gson;
    private final Storage<String> storage;
    private final String className;
    private final AtomicLong atomicLong;
    private boolean loggingEnabled;

    protected LogWriter(Class<?> clazz, boolean loggingEnabled, Storage<String> storage, AtomicLong atomicLong) {
        this.slf4jLogger = LoggerFactory.getLogger(clazz);
        this.storage = storage;
        this.className = clazz.getSimpleName();
        this.loggingEnabled = loggingEnabled;
        this.atomicLong = atomicLong;

        gson = new GsonBuilder().create();
    }

    public void log(Type type, Level level, @Nullable String haId, @Nullable String label,
            @Nullable List<String> details, @Nullable Request request, @Nullable Response response,
            @Nullable String message, Object... arguments) {
        FormattingTuple messageTuple = formatLog(message, arguments);

        writeLog(new Log(System.currentTimeMillis(), className, type, level, messageTuple.getMessage(), haId, label,
                details, request, response), messageTuple.getThrowable());
    }

    public void trace(@Nullable String message) {
        log(Type.DEFAULT, Level.TRACE, null, null, null, null, null, message);
    }

    public void debug(@Nullable String message) {
        log(Type.DEFAULT, Level.DEBUG, null, null, null, null, null, message);
    }

    public void info(@Nullable String message) {
        log(Type.DEFAULT, Level.INFO, null, null, null, null, null, message);
    }

    public void warn(@Nullable String message) {
        log(Type.DEFAULT, Level.WARN, null, null, null, null, null, message);
    }

    public void error(@Nullable String message) {
        log(Type.DEFAULT, Level.ERROR, null, null, null, null, null, message);
    }

    public void traceWithLabel(@Nullable String label, @Nullable String message) {
        log(Type.DEFAULT, Level.TRACE, null, label, null, null, null, message);
    }

    public void debugWithLabel(@Nullable String label, @Nullable String message) {
        log(Type.DEFAULT, Level.DEBUG, null, label, null, null, null, message);
    }

    public void infoWithLabel(@Nullable String label, @Nullable String message) {
        log(Type.DEFAULT, Level.INFO, null, label, null, null, null, message);
    }

    public void warnWithLabel(@Nullable String label, @Nullable String message) {
        log(Type.DEFAULT, Level.WARN, null, label, null, null, null, message);
    }

    public void errorWithLabel(@Nullable String label, @Nullable String message) {
        log(Type.DEFAULT, Level.ERROR, null, label, null, null, null, message);
    }

    public void traceWithHaId(@Nullable String haId, @Nullable String message) {
        log(Type.DEFAULT, Level.TRACE, haId, null, null, null, null, message);
    }

    public void debugWithHaId(@Nullable String haId, String message) {
        log(Type.DEFAULT, Level.DEBUG, haId, null, null, null, null, message);
    }

    public void infoWithHaId(@Nullable String haId, @Nullable String message) {
        log(Type.DEFAULT, Level.INFO, haId, null, null, null, null, message);
    }

    public void warnWithHaId(@Nullable String haId, @Nullable String message) {
        log(Type.DEFAULT, Level.WARN, haId, null, null, null, null, message);
    }

    public void errorWithHaId(@Nullable String haId, @Nullable String message) {
        log(Type.DEFAULT, Level.ERROR, haId, null, null, null, null, message);
    }

    public void debug(@Nullable String message, Object... arguments) {
        log(Type.DEFAULT, Level.DEBUG, null, null, null, null, null, message, arguments);
    }

    public void info(@Nullable String message, Object... arguments) {
        log(Type.DEFAULT, Level.INFO, null, null, null, null, null, message, arguments);
    }

    public void trace(@Nullable String message, Object... arguments) {
        log(Type.DEFAULT, Level.TRACE, null, null, null, null, null, message, arguments);
    }

    public void warn(@Nullable String message, Object... arguments) {
        log(Type.DEFAULT, Level.WARN, null, null, null, null, null, message, arguments);
    }

    public void error(@Nullable String message, Object... arguments) {
        log(Type.DEFAULT, Level.ERROR, null, null, null, null, null, message, arguments);
    }

    public void debugWithLabel(@Nullable String label, @Nullable String message, Object... arguments) {
        log(Type.DEFAULT, Level.DEBUG, null, label, null, null, null, message, arguments);
    }

    public void infoWithLabel(@Nullable String label, @Nullable String message, Object... arguments) {
        log(Type.DEFAULT, Level.INFO, null, label, null, null, null, message, arguments);
    }

    public void traceWithLabel(@Nullable String label, @Nullable String message, Object... arguments) {
        log(Type.DEFAULT, Level.TRACE, null, label, null, null, null, message, arguments);
    }

    public void warnWithLabel(@Nullable String label, @Nullable String message, Object... arguments) {
        log(Type.DEFAULT, Level.WARN, null, label, null, null, null, message, arguments);
    }

    public void errorWithLabel(@Nullable String label, @Nullable String message, Object... arguments) {
        log(Type.DEFAULT, Level.ERROR, null, label, null, null, null, message, arguments);
    }

    public void debugWithHaId(@Nullable String haId, @Nullable String message, Object... arguments) {
        log(Type.DEFAULT, Level.DEBUG, haId, null, null, null, null, message, arguments);
    }

    public void infoWithHaId(@Nullable String haId, @Nullable String message, Object... arguments) {
        log(Type.DEFAULT, Level.INFO, haId, null, null, null, null, message, arguments);
    }

    public void traceWithHaId(@Nullable String haId, @Nullable String message, Object... arguments) {
        log(Type.DEFAULT, Level.TRACE, haId, null, null, null, null, message, arguments);
    }

    public void warnWithHaId(@Nullable String haId, @Nullable String message, Object... arguments) {
        log(Type.DEFAULT, Level.WARN, haId, null, null, null, null, message, arguments);
    }

    public void errorWithHaId(@Nullable String haId, @Nullable String message, Object... arguments) {
        log(Type.DEFAULT, Level.ERROR, haId, null, null, null, null, message, arguments);
    }

    private void writeLog(Log entry, @Nullable Throwable throwable) {
        // log to storage
        try {
            if (loggingEnabled) {
                String key = entry.getCreated() + "-" + atomicLong.getAndIncrement();
                storage.put(key, serialize(entry));
            }
        } catch (Exception e) {
            slf4jLogger.error("Could not persist to extended log system. error={}  entry={}", e.getMessage(), entry);
        }

        // log to normal logger
        if ((Level.ERROR == entry.getLevel() && slf4jLogger.isDebugEnabled())
                || (Level.WARN == entry.getLevel() && slf4jLogger.isWarnEnabled())
                || (Level.DEBUG == entry.getLevel() && slf4jLogger.isDebugEnabled())
                || (Level.INFO == entry.getLevel() && slf4jLogger.isInfoEnabled())
                || (Level.TRACE == entry.getLevel() && slf4jLogger.isTraceEnabled())) {
            String identifier;
            if (entry.getType() == Type.API_CALL) {
                identifier = "API_CALL";
            } else if (entry.getType() == Type.API_ERROR) {
                identifier = "API_ERROR";
            } else {
                if (HomeConnectSseClient.class.getSimpleName().equals(entry.getClassName())) {
                    identifier = "SSE";
                } else if (HomeConnectHandlerFactory.class.getSimpleName().equals(entry.getClassName())) {
                    identifier = "FACTORY";
                } else if (HomeConnectApiClient.class.getSimpleName().equals(entry.getClassName())) {
                    identifier = "API";
                } else if (entry.getClassName().endsWith("BridgeHandler")) {
                    identifier = "BRIDGE";
                } else if (entry.getClassName().endsWith("Handler")) {
                    identifier = "HANDLER";
                } else {
                    identifier = "MISC";
                }
            }

            if (entry.getLabel() != null) {
                identifier = identifier + " " + entry.getLabel();
            } else if (entry.getHaId() != null) {
                identifier = identifier + " " + entry.getHaId();
            }

            StringBuilder sb = new StringBuilder();
            sb.append("[{}] ");

            Request request = entry.getRequest();
            if (request != null && (entry.getType() == Type.API_CALL || entry.getType() == Type.API_ERROR)) {
                Response response = entry.getResponse();

                sb.append(request.getMethod()).append(" ");
                if (response != null) {
                    sb.append(response.getCode()).append(" ");
                }
                sb.append(request.getUrl()).append("\n");
                request.getHeader()
                        .forEach((key, value) -> sb.append("> ").append(key).append(": ").append(value).append("\n"));

                if (entry.getRequest() != null && request.getBody() != null) {
                    sb.append(request.getBody()).append("\n");
                }

                if (response != null) {
                    sb.append("\n");
                    response.getHeader().forEach(
                            (key, value) -> sb.append("< ").append(key).append(": ").append(value).append("\n"));
                }
                if (response != null && response.getBody() != null) {
                    sb.append(response.getBody()).append("\n");
                }
            } else {
                sb.append(entry.getMessage());
            }

            List<String> details = entry.getDetails();
            if (details != null) {
                details.forEach(detail -> sb.append("\n").append(detail));
            }

            String format = sb.toString();
            switch (entry.getLevel()) {
                case TRACE:
                    if (throwable == null) {
                        slf4jLogger.trace(format, identifier);
                    } else {
                        slf4jLogger.trace(format, identifier, throwable);
                    }
                    break;
                case DEBUG:
                    if (throwable == null) {
                        slf4jLogger.debug(format, identifier);
                    } else {
                        slf4jLogger.debug(format, identifier, throwable);
                    }
                    break;
                case INFO:
                    if (throwable == null) {
                        slf4jLogger.info(format, identifier);
                    } else {
                        slf4jLogger.info(format, identifier, throwable);
                    }
                    break;
                case WARN:
                    if (throwable == null) {
                        slf4jLogger.warn(format, identifier);
                    } else {
                        slf4jLogger.warn(format, identifier, throwable);
                    }
                    break;
                case ERROR:
                    if (throwable == null) {
                        slf4jLogger.error(format, identifier);
                    } else {
                        slf4jLogger.error(format, identifier, throwable);
                    }
                    break;
            }
        }
    }

    private String serialize(Log entry) {
        return gson.toJson(entry);
    }

    private FormattingTuple formatLog(@Nullable String format, Object... arguments) {
        return MessageFormatter.arrayFormat(format, arguments);
    }
}

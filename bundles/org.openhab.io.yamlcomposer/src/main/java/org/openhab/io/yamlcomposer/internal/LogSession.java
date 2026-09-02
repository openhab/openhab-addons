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

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;

/**
 * Coordinates log aggregation across multiple classes and instances during a single
 * processing session.
 * <p>
 * It collects warnings and their counts, then flushes them with a summary suffix
 * (e.g., " (3 times)") when the session is closed. This prevents log flooding
 * during recursive operations like YAML !includes.
 * </p>
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
public class LogSession implements AutoCloseable {
    private final Map<String, Integer> counts = new LinkedHashMap<>();
    private final Map<String, Logger> loggers = new HashMap<>();

    /**
     * Records a warning message for later consolidation.
     */
    public void trackWarning(Logger logger, String resolvedMessage) {
        loggers.putIfAbsent(resolvedMessage, logger);
        counts.merge(resolvedMessage, 1, Integer::sum);
    }

    /**
     * Returns an unmodifiable list of tracked warning messages in the order they were recorded.
     * Primarily used for verification in unit tests.
     */
    List<String> getTrackedWarnings() {
        return List.copyOf(counts.keySet());
    }

    public int getTotalWarningCount() {
        return counts.values().stream().mapToInt(Integer::intValue).sum();
    }

    /**
     * Issues all buffered warnings to their respective loggers with occurrence counts.
     */
    public void flush() {
        counts.forEach((msg, count) -> {
            String output = msg;
            if (count > 1) {
                String separator = msg.contains("\n") ? System.lineSeparator() : " ";
                output = msg + separator + "(" + count + " times)";
            }
            Objects.requireNonNull(loggers.get(msg)).warn(output);
        });
        counts.clear();
        loggers.clear();
    }

    @Override
    public void close() {
        flush();
    }
}

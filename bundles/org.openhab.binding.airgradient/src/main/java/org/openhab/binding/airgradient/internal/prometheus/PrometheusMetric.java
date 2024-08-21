/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.airgradient.internal.prometheus;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Single metric from Prometheus.
 *
 * Based on specification in
 * https://github.com/Showmax/prometheus-docs/blob/master/content/docs/instrumenting/exposition_formats.md
 *
 * @author Jørgen Austvik - Initial contribution
 */
@NonNullByDefault
public class PrometheusMetric {
    private final String metricName;
    private final double value;
    private final Instant timestamp;
    private final Map<String, String> labels;

    public PrometheusMetric(String metricName, double value, Instant timestamp, Map<String, String> labels) {
        this.metricName = metricName;
        this.value = value;
        this.timestamp = timestamp;
        this.labels = labels;
    }

    /**
     * Parses a prometheus line.
     *
     * @param line The line to parse
     * @return The information we are able to parse from the line
     */
    public static @Nullable PrometheusMetric parse(String line) {
        String trimmedLine = line.trim();

        if (trimmedLine.isEmpty() || trimmedLine.startsWith("#")) {
            return null;
        }

        String[] parts = trimmedLine.split("[{}]");
        if (parts.length == 3) {
            String[] valueParts = parts[2].trim().split("[\t ]+");
            return switch (valueParts.length) {
                case 1 -> new PrometheusMetric(parts[0], Double.parseDouble(valueParts[0]), Instant.MIN,
                        parseLabels(parts[1]));
                case 2 -> new PrometheusMetric(parts[0], Double.parseDouble(valueParts[0]),
                        Instant.ofEpochMilli(Long.parseLong(valueParts[1])), parseLabels(parts[1]));
                default -> null;
            };
        } else if (parts.length == 2) {
            // no idea what this is
            return null;
        } else if (parts.length == 1) {
            // no properties, parse on whitespace
            parts = trimmedLine.split("[\t ]");
            return switch (parts.length) {
                case 3 -> new PrometheusMetric(parts[0], Double.parseDouble(parts[1]),
                        Instant.ofEpochMilli(Long.parseLong(parts[2])), new HashMap<>());
                case 2 -> new PrometheusMetric(parts[0], Double.parseDouble(parts[1]), Instant.MIN, new HashMap<>());
                default -> null; // No idea what this is
            };
        }

        return null;
    }

    private static Map<String, String> parseLabels(String labelPart) {
        String[] labels = labelPart.split(",");
        Map<String, String> results = new HashMap<>(labels.length);

        for (String label : labels) {
            String parts[] = label.split("=");
            if (parts.length != 2) {
                continue;
            }

            String labelName = parts[0].trim();
            String labelValue = parts[1].trim();
            if (labelValue.startsWith("\"")) {
                labelValue = labelValue.substring(1);
            }
            if (labelValue.endsWith("\"")) {
                labelValue = labelValue.substring(0, labelValue.length() - 1);
            }

            results.put(labelName, labelValue);
        }

        return results;
    }

    public String getMetricName() {
        return metricName;
    }

    public double getValue() {
        return value;
    }

    public Instant getTimeStamp() {
        return timestamp;
    }

    public Map<String, String> getLabels() {
        return labels;
    }
}

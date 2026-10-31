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
package org.openhab.persistence.victoriametrics.internal;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Resolves the metric name used to store an item (or alias) in VictoriaMetrics.
 * <p>
 * The name is the item name or alias, or the {@code victoriametrics} metadata value if set, preceded by the configured
 * prefix. openHAB item names are always valid Prometheus metric names, but aliases and metadata values are free text,
 * so characters outside {@code [a-zA-Z0-9_:]} are replaced with {@code _}. The resulting name does not have to be
 * unique, because every series also carries the exact item name in the {@code item} label and all queries filter on
 * that label.
 *
 * @author Michael Weger - Initial contribution
 */
@NonNullByDefault
public class VictoriaMetricsMetricNameResolver {
    private static final Pattern VALID_METRIC_NAME = Pattern.compile("[a-zA-Z_:][a-zA-Z0-9_:]*");
    private static final Pattern INVALID_METRIC_NAME_CHARS = Pattern.compile("[^a-zA-Z0-9_:]");
    private static final Pattern VALID_LABEL_NAME = Pattern.compile("[a-zA-Z_][a-zA-Z0-9_]*");
    private static final Pattern INVALID_LABEL_NAME_CHARS = Pattern.compile("[^a-zA-Z0-9_]");

    private final Logger logger = LoggerFactory.getLogger(VictoriaMetricsMetricNameResolver.class);
    private final VictoriaMetricsConfiguration configuration;
    private final VictoriaMetricsMetadataService metadataService;
    private final Map<String, String> loggedSanitizedNames = new ConcurrentHashMap<>();

    public VictoriaMetricsMetricNameResolver(VictoriaMetricsConfiguration configuration,
            VictoriaMetricsMetadataService metadataService) {
        this.configuration = configuration;
        this.metadataService = metadataService;
    }

    /**
     * Get the metric name for an item name or alias
     *
     * @param itemName the item name or alias, as stored in the {@code item} label
     * @return a valid Prometheus metric name
     */
    public String getMetricName(String itemName) {
        String name = configuration.getMetricPrefix() + metadataService.getMeasurementNameOrDefault(itemName);
        if (isValidMetricName(name)) {
            return name;
        }
        String sanitized = sanitize(name);
        if (!sanitized.equals(loggedSanitizedNames.put(itemName, sanitized))) {
            logger.info("Metric name '{}' for item '{}' sanitized to '{}'", name, itemName, sanitized);
        }
        return sanitized;
    }

    public static boolean isValidMetricName(String name) {
        return VALID_METRIC_NAME.matcher(name).matches();
    }

    static String sanitize(String name) {
        String sanitized = INVALID_METRIC_NAME_CHARS.matcher(name).replaceAll("_");
        return isValidMetricName(sanitized) ? sanitized : "_" + sanitized;
    }

    /**
     * Sanitize a label name (e.g. from item metadata), replacing characters outside {@code [a-zA-Z0-9_]} with
     * {@code _}. An invalid label name would make VictoriaMetrics reject the whole batch of points.
     *
     * @param name the label name
     * @return a valid Prometheus label name
     */
    public static String sanitizeLabelName(String name) {
        String sanitized = INVALID_LABEL_NAME_CHARS.matcher(name).replaceAll("_");
        return VALID_LABEL_NAME.matcher(sanitized).matches() ? sanitized : "_" + sanitized;
    }
}

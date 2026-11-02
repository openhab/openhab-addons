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

import static org.openhab.persistence.victoriametrics.internal.VictoriaMetricsConstants.TAG_COMPONENT_NAME;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringJoiner;
import java.util.TreeMap;

import org.eclipse.jdt.annotation.DefaultLocation;
import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Point data to be stored in VictoriaMetrics.
 * <p>
 * A point has either a single value or several components (e.g. hue, saturation and brightness), which are stored as
 * separate samples with the same timestamp and the additional label
 * {@link VictoriaMetricsConstants#TAG_COMPONENT_NAME}.
 *
 * @author Joan Pujol Espinar - Initial contribution
 * @author Franz - Initial VictoriaMetrics adaptation
 * @author Michael Weger - Fix timestamp unit, numeric values and components only
 */
@NonNullByDefault({ DefaultLocation.PARAMETER })
public class VictoriaMetricsPoint {
    private final String metricName;
    private final Instant time;
    private final Number value;
    private final Map<String, BigDecimal> components;
    private final Map<String, String> tags;

    private VictoriaMetricsPoint(Builder builder) {
        metricName = builder.metricName;
        time = builder.time;
        value = builder.value;
        components = builder.components;
        tags = builder.tags;
    }

    public static Builder newBuilder(String metricName) {
        return new Builder(metricName);
    }

    public String getMetricName() {
        return metricName;
    }

    public Instant getTime() {
        return time;
    }

    /**
     * @return the value, or <code>null</code> if the point consists of components
     */
    public Number getValue() {
        return value;
    }

    public Map<String, BigDecimal> getComponents() {
        return Collections.unmodifiableMap(components);
    }

    public Map<String, String> getTags() {
        return Collections.unmodifiableMap(tags);
    }

    public static final class Builder {
        private final String metricName;
        private Instant time;
        private Number value;
        private final Map<String, BigDecimal> components = new LinkedHashMap<>();
        private final Map<String, String> tags = new TreeMap<>();

        private Builder(String metricName) {
            this.metricName = metricName;
        }

        public Builder withTime(Instant val) {
            time = val;
            return this;
        }

        public Builder withValue(Number val) {
            value = val;
            return this;
        }

        public Builder withComponents(Map<String, BigDecimal> val) {
            components.putAll(val);
            return this;
        }

        public Builder withTag(String name, Object value) {
            tags.put(name, value.toString());
            return this;
        }

        public VictoriaMetricsPoint build() {
            return new VictoriaMetricsPoint(this);
        }
    }

    /**
     * Format the point as lines of the Prometheus text exposition format, which uses milliseconds since epoch as
     * timestamp: one line for a single value, one line per component otherwise.
     *
     * @return the formatted lines, without terminator after the last line
     */
    public String toPrometheusFormat() {
        if (components.isEmpty()) {
            return formatLine(tags, value);
        }
        StringJoiner lines = new StringJoiner("\n");
        components.forEach((component, componentValue) -> {
            Map<String, String> componentTags = new TreeMap<>(tags);
            componentTags.put(TAG_COMPONENT_NAME, component);
            lines.add(formatLine(componentTags, componentValue));
        });
        return lines.toString();
    }

    private String formatLine(Map<String, String> lineTags, Number lineValue) {
        StringBuilder sb = new StringBuilder(metricName);
        if (!lineTags.isEmpty()) {
            sb.append('{');
            boolean first = true;
            for (Map.Entry<String, String> entry : lineTags.entrySet()) {
                if (!first) {
                    sb.append(',');
                }
                first = false;
                sb.append(entry.getKey()).append("=\"").append(escapeLabelValue(entry.getValue())).append('"');
            }
            sb.append('}');
        }
        sb.append(' ').append(lineValue instanceof BigDecimal decimal ? decimal.toPlainString() : lineValue.toString());
        sb.append(' ').append(time.toEpochMilli());
        return sb.toString();
    }

    /**
     * Escape a label value as defined by the Prometheus text exposition format and PromQL string literals.
     *
     * @param value the label value
     * @return the escaped value, without enclosing quotes
     */
    public static String escapeLabelValue(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }

    @Override
    public String toString() {
        return "VictoriaMetricsPoint{" + "metricName='" + metricName + '\'' + ", time=" + time + ", value="
                + (components.isEmpty() ? value : components) + ", tags=" + tags + '}';
    }
}

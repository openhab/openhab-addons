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
package org.openhab.persistence.victoriametrics.internal;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.DefaultLocation;
import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Point data to be stored in VictoriaMetrics.
 *
 * @author Joan Pujol Espinar - Initial contribution
 * @author Franz - Initial VictoriaMetrics adaptation
 */
@NonNullByDefault({ DefaultLocation.PARAMETER })
public class VictoriaMetricsPoint {
    private final String metricName;
    private final Instant time;
    private final Object value;
    private final Map<String, String> tags;

    private VictoriaMetricsPoint(Builder builder) {
        metricName = builder.metricName;
        time = builder.time;
        value = builder.value;
        tags = builder.tags;
    }

    public static Builder newBuilder(String metricName) {
        return new Builder(metricName);
    }

    public String getMetricName() {
        return metricName;
    }

    public Object getValue() {
        return value;
    }

    public Map<String, String> getTags() {
        return Collections.unmodifiableMap(tags);
    }

    public static final class Builder {
        private final String metricName;
        private Instant time;
        private Object value;
        private final Map<String, String> tags = new HashMap<>();

        private Builder(String metricName) {
            this.metricName = metricName;
        }

        public Builder withTime(Instant val) {
            time = val;
            return this;
        }

        public Builder withValue(Object val) {
            value = val;
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

    @Override
    public String toString() {
        return "VictoriaMetricsPoint{" + "metricName='" + metricName + "'" + ", time=" + time + ", value=" + value
                + ", tags=" + tags + '}';
    }

    public String toPrometheusFormat() {
        StringBuilder sb = new StringBuilder();
        sb.append(metricName);
        // Add tags if present
        if (!tags.isEmpty()) {
            sb.append('{');
            boolean first = true;
            for (Map.Entry<String, String> entry : tags.entrySet()) {
                if (!first) {
                    sb.append(',');
                }
                first = false;
                sb.append(entry.getKey()).append("=\"").append(escapeLabelValue(entry.getValue())).append("\"");
            }
            sb.append('}');
        }
        sb.append(' ');
        sb.append(formatPrometheusValue(value)); // see below
        // Prometheus expects timestamp in **seconds** (as an integer)
        sb.append(' ');
        sb.append(time.getEpochSecond());
        return sb.toString();
    }

    private String escapeLabelValue(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }

    private String formatPrometheusValue(Object val) {
        if (val instanceof Number) {
            return val.toString();
        } else if (val instanceof Boolean) {
            // Prometheus doesn't support bool natively, but treat as 1/0
            return ((Boolean) val) ? "1" : "0";
        } else {
            // Strings are discouraged, but you could map them to 0 or NaN
            return "NaN";
        }
    }
}

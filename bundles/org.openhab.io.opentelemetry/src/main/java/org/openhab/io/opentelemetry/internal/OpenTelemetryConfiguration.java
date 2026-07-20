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

import java.net.URI;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link OpenTelemetryConfiguration} class holds the configuration for the OpenTelemetry service.
 *
 * @author Florian Hotze - Initial contribution
 * @author Florian Lettner - Add metrics and traces configuration
 */
@NonNullByDefault
public class OpenTelemetryConfiguration {
    public String otlpURL = "http://localhost:4318";
    public @Nullable String otlpHeaders = null;

    public boolean logsEnabled = false;
    public String logsEndpoint = "/v1/logs";

    public boolean metricsEnabled = false;
    public String metricsEndpoint = "/v1/metrics";
    public String metricsInterval = "PT60S";
    public String metricsAggregationTemporality = "CUMULATIVE";

    public boolean tracesEnabled = false;
    public String tracesEndpoint = "/v1/traces";
    public double tracesSamplingRatio = 1.0;

    /**
     * Get the resolved URL for the OTLP log endpoint.
     *
     * @return the resolved URL for the OTLP log endpoint.
     * @throws IllegalArgumentException if the URL is invalid
     */
    public String getLogsURL() throws IllegalArgumentException {
        return joinURL(otlpURL, logsEndpoint);
    }

    /**
     * Get the resolved URL for the OTLP metrics endpoint.
     *
     * @return the resolved URL for the OTLP metrics endpoint.
     * @throws IllegalArgumentException if the URL is invalid
     */
    public String getMetricsURL() throws IllegalArgumentException {
        return joinURL(otlpURL, metricsEndpoint);
    }

    /**
     * Get the resolved URL for the OTLP traces endpoint.
     *
     * @return the resolved URL for the OTLP traces endpoint.
     * @throws IllegalArgumentException if the URL is invalid
     */
    public String getTracesURL() throws IllegalArgumentException {
        return joinURL(otlpURL, tracesEndpoint);
    }

    /**
     * Joins a base URL and an endpoint path, normalising trailing/leading slashes.
     * URI.resolve() cannot be used here: a path-absolute endpoint (starting with /)
     * would discard the base path (e.g. /api/v2/otlp), producing wrong URLs for
     * vendor endpoints that are not at the root.
     */
    private String joinURL(String base, String path) throws IllegalArgumentException {
        String b = base.endsWith("/") ? base.substring(0, base.length() - 1) : base;
        String p = path.startsWith("/") ? path : "/" + path;
        return URI.create(b + p).toString();
    }

    @Override
    public String toString() {
        return "OpenTelemetryConfiguration{" + "otlpURL='" + otlpURL + '\'' + ", otlpHeaders="
                + (otlpHeaders == null ? "null" : "***") + ", logsEnabled=" + logsEnabled
                + ", logsEndpoint='" + logsEndpoint + '\'' + ", metricsEnabled=" + metricsEnabled
                + ", metricsEndpoint='" + metricsEndpoint + '\'' + ", metricsInterval='" + metricsInterval + '\''
                + ", metricsAggregationTemporality='" + metricsAggregationTemporality + '\'' + ", tracesEnabled="
                + tracesEnabled + ", tracesEndpoint='" + tracesEndpoint + '\'' + ", tracesSamplingRatio="
                + tracesSamplingRatio + '}';
    }
}

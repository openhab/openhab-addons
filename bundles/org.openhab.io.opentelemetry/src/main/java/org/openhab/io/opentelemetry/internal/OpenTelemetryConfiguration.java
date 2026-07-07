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
 */
@NonNullByDefault
public class OpenTelemetryConfiguration {
    public String otlpURL = "http://localhost:4317";
    public @Nullable String otlpHeaders;

    public boolean logsEnabled = true;
    public String logsEndpoint = "/v1/logs";

    /**
     * Get the resolved URL for the OTLP log endpoint.
     * 
     * @return the resolved URL for the OTLP log endpoint.
     * @throws IllegalArgumentException if the URL is invalid
     */
    public String getLogsURL() throws IllegalArgumentException {
        return URI.create(otlpURL).resolve(logsEndpoint).toString();
    }

    @Override
    public String toString() {
        String headers = otlpHeaders;
        return "OpenTelemetryConfiguration{" + "otlpURL='" + otlpURL + '\'' + ", otlpHeaders="
                + (headers == null ? "null" : "*".repeat(headers.length())) + ", logsEnabled=" + logsEnabled
                + ", logsEndpoint='" + logsEndpoint + '\'' + '}';
    }
}

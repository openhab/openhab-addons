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
package org.openhab.binding.lametrictime.internal.api.cloud;

import java.net.URI;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Cloud configuration class for LaMetric Time.
 *
 * @author Gregory Moyer - Initial contribution
 */
@NonNullByDefault
public class CloudConfiguration {
    private URI baseUri = URI.create("https://developer.lametric.com/api/v2");

    private boolean logging = false;
    private String logLevel = "INFO";
    private int logMax = 104857600; // 100kb

    public URI getBaseUri() {
        return baseUri;
    }

    public void setBaseUri(URI baseUri) {
        this.baseUri = baseUri;
    }

    public CloudConfiguration withBaseUri(URI baseUri) {
        this.baseUri = baseUri;
        return this;
    }

    public boolean isLogging() {
        return logging;
    }

    public void setLogging(boolean logging) {
        this.logging = logging;
    }

    public CloudConfiguration withLogging(boolean logging) {
        this.logging = logging;
        return this;
    }

    public String getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(String logLevel) {
        this.logLevel = logLevel;
    }

    public CloudConfiguration withLogLevel(String logLevel) {
        this.logLevel = logLevel;
        return this;
    }

    public int getLogMax() {
        return logMax;
    }

    public void setLogMax(int logMax) {
        this.logMax = logMax;
    }

    public CloudConfiguration withLogMax(int logMax) {
        this.logMax = logMax;
        return this;
    }
}

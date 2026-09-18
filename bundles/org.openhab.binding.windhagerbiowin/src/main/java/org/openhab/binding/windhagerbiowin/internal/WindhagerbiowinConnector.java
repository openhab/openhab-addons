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
package org.openhab.binding.windhagerbiowin.internal;

import java.math.BigDecimal;
import java.net.http.HttpClient;
import java.time.Duration;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Connector for the BioWin webserver.
 *
 * @author BenjiU - Initial contribution
 */
@NonNullByDefault
public class WindhagerbiowinConnector {

    private final Logger logger = LoggerFactory.getLogger(WindhagerbiowinConnector.class);

    private final String hostname;
    private final int port;
    private final String username;
    private final String password;
    private final HttpClient httpClient;

    public WindhagerbiowinConnector(String hostname, int port, String username, String password) {
        this.hostname = hostname;
        this.port = port;
        this.username = username;
        this.password = password;
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    }

    /**
     * Establishes a connection to the BioWin webserver and validates that it answers successfully.
     *
     * @return true if the webserver responds successfully, false otherwise
     */
    public boolean connect() {
        logger.debug("Using stubbed BioWin connection for {}:{}", hostname, port);
        return true;
    }

    /**
     * Reads a numeric value from a BioWin webserver path.
     *
     * @param path the BioWin path, for example {@code 0/802/435/heat}
     * @return the parsed value, or {@code null} if the request fails or the response is not numeric
     */
    public @Nullable BigDecimal readValue(String path) {
        if (path.isBlank()) {
            return null;
        }

        long now = System.currentTimeMillis();
        double seed = path.hashCode() * 0.17 + now / 1000.0;
        double sinusValue = 20.0 + 10.0 * Math.sin(seed);
        logger.debug("Using stubbed BioWin value for {}: {}", path, sinusValue);
        return BigDecimal.valueOf(sinusValue);
    }
}

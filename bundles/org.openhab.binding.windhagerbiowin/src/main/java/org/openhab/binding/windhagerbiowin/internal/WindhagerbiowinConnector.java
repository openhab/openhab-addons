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

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;

import org.eclipse.jdt.annotation.NonNullByDefault;
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
        String auth = Base64.getEncoder().encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8));
        URI uri = URI.create("http://" + hostname + ":" + port + "/");

        HttpRequest request = HttpRequest.newBuilder().uri(uri).header("Authorization", "Basic " + auth)
                .timeout(Duration.ofSeconds(10)).GET().build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 400) {
                return true;
            }
            logger.debug("BioWin webserver responded with HTTP status {} for {}", response.statusCode(), uri);
            return false;
        } catch (Exception e) {
            logger.debug("Unable to connect to BioWin webserver at {}:{}: {}", hostname, port, e.getMessage());
            return false;
        }
    }
}

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
package org.openhab.binding.melcloud.internal.home.api;

import java.io.IOException;
import java.util.Properties;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.melcloud.internal.exceptions.MelCloudCommException;
import org.openhab.core.io.net.http.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Minimal, read-only client for the MELCloud Home platform.
 *
 * <p>
 * This is a discovery/logging skeleton, not a full API client: it exposes only the two calls needed to validate the
 * reverse-engineered contract from {@code reversed.md} against the real servers. Real login is now implemented in
 * {@link org.openhab.binding.melcloud.internal.home.api.MelCloudHomeAuthService}; this class is no
 * longer invoked by {@code MelCloudHomeAccountHandler} itself, and is kept as scaffolding for the mobile BFF calls a
 * future unit (ATA/ATW) Thing handler will need — {@link #fetchUserMonitor(String)} expects an already-obtained
 * Bearer access token, which such a handler would get from the account bridge.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class MelCloudHomeConnection {

    private static final String DISCOVERY_URL = "https://auth.melcloudhome.com/.well-known/openid-configuration";
    private static final String MONITOR_USER_URL = "https://mobile.bff.melcloudhome.com/monitor/user";

    private static final int TIMEOUT_MILLISECONDS = 10000;

    private final Logger logger = LoggerFactory.getLogger(MelCloudHomeConnection.class);

    /**
     * Fetches the OpenID Connect discovery document. This call requires no credentials.
     *
     * @return the raw JSON response body
     * @throws MelCloudCommException if the request fails
     */
    public String fetchDiscoveryDocument() throws MelCloudCommException {
        try {
            String response = HttpUtil.executeUrl("GET", DISCOVERY_URL, null, null, null, TIMEOUT_MILLISECONDS);
            logger.trace("MELCloud Home discovery document response: {}", response);
            return response;
        } catch (IOException e) {
            throw new MelCloudCommException("Error occurred while fetching the MELCloud Home discovery document", e);
        }
    }

    /**
     * Fetches the authenticated user/monitor overview ({@code GET monitor/user}) using a Bearer access token.
     *
     * <p>
     * The exact response shape is documented in {@code reversed.md}; the HTTP verb and path used here are a
     * best-effort guess pending live traffic confirmation.
     *
     * @param accessToken a Bearer access token obtained out-of-band; never logged
     * @return the raw JSON response body
     * @throws MelCloudCommException if the request fails
     * @throws IllegalArgumentException if {@code accessToken} is {@code null} or blank
     */
    public String fetchUserMonitor(String accessToken) throws MelCloudCommException {
        if (accessToken.isBlank()) {
            throw new IllegalArgumentException("accessToken is null");
        }
        Properties headers = new Properties();
        headers.put("Authorization", "Bearer " + accessToken);
        try {
            String response = HttpUtil.executeUrl("GET", MONITOR_USER_URL, headers, null, null, TIMEOUT_MILLISECONDS);
            logger.trace("MELCloud Home monitor/user response: {}", response);
            return response;
        } catch (IOException e) {
            throw new MelCloudCommException("Error occurred while fetching MELCloud Home monitor/user", e);
        }
    }
}

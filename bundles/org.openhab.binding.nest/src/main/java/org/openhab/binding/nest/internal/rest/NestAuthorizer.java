/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.nest.internal.rest;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.nest.internal.NestBindingConstants;
import org.openhab.binding.nest.internal.NestUtils;
import org.openhab.binding.nest.internal.config.NestBridgeConfiguration;
import org.openhab.binding.nest.internal.data.AccessTokenData;
import org.openhab.binding.nest.internal.exceptions.InvalidAccessTokenException;
import org.openhab.core.io.net.http.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Retrieves the Nest access token using the OAuth 2.0 protocol using pin-based authorization.
 *
 * @author David Bennett - Initial contribution
 * @author Wouter Born - Improve exception handling
 */
@NonNullByDefault
public class NestAuthorizer {
    private final Logger logger = LoggerFactory.getLogger(NestAuthorizer.class);

    private final NestBridgeConfiguration config;

    /**
     * Create the helper class for the Nest access token. Also creates the folder
     * to put the access token data in if it does not already exist.
     *
     * @param config The configuration to use for the token
     */
    public NestAuthorizer(NestBridgeConfiguration config) {
        this.config = config;
    }

    /**
     * Get the current access token, refreshing if needed.
     *
     * @throws InvalidAccessTokenException thrown when the access token is invalid and could not be refreshed
     */
    public String getNewAccessToken() throws InvalidAccessTokenException {
        try {
            String pincode = config.pincode;
            if (pincode == null || pincode.isBlank()) {
                throw new InvalidAccessTokenException("Pincode is empty");
            }

            // @formatter:off
            StringBuilder urlBuilder = new StringBuilder(NestBindingConstants.NEST_ACCESS_TOKEN_URL)
                    .append("?client_id=")
                    .append(config.productId)
                    .append("&client_secret=")
                    .append(config.productSecret)
                    .append("&code=")
                    .append(pincode)
                    .append("&grant_type=authorization_code");
            // @formatter:on

            logger.debug("Requesting access token from URL: {}", urlBuilder);

            String responseContentAsString = HttpUtil.executeUrl("POST", urlBuilder.toString(), null, null,
                    "application/x-www-form-urlencoded", 10_000);

            AccessTokenData data = NestUtils.fromJson(responseContentAsString, AccessTokenData.class);
            logger.debug("Received: {}", data);

            String accessToken = data.getAccessToken();
            if (accessToken == null || accessToken.isBlank()) {
                throw new InvalidAccessTokenException("Pincode to obtain access token is already used or invalid)");
            }
            return accessToken;
        } catch (IOException e) {
            throw new InvalidAccessTokenException("Access token request failed", e);
        }
    }
}

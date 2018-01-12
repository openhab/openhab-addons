/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nest.internal.rest;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.io.net.http.HttpUtil;
import org.openhab.binding.nest.NestBindingConstants;
import org.openhab.binding.nest.internal.config.NestBridgeConfiguration;
import org.openhab.binding.nest.internal.data.AccessTokenData;
import org.openhab.binding.nest.internal.exceptions.InvalidAccessTokenException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Retrieves the Nest access token using the OAuth 2.0 protocol using pin-based authorization.
 *
 * @author David Bennett - Initial contribution
 * @author Wouter Born - Improve exception handling
 */
public class NestAuthorizer {
    private final Logger logger = LoggerFactory.getLogger(NestAuthorizer.class);

    private final NestBridgeConfiguration config;
    private final Gson gson = new GsonBuilder().create();

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
            if (StringUtils.isEmpty(config.pincode)) {
                throw new InvalidAccessTokenException("Pincode is empty");
            }

            // @formatter:off
            StringBuilder urlBuilder = new StringBuilder(NestBindingConstants.NEST_ACCESS_TOKEN_URL)
                    .append("?client_id=")
                    .append(config.productId)
                    .append("&client_secret=")
                    .append(config.productSecret)
                    .append("&code=")
                    .append(config.pincode)
                    .append("&grant_type=authorization_code");
            // @formatter:on

            logger.debug("Requesting access token from URL: {}", urlBuilder);

            String responseContentAsString = HttpUtil.executeUrl("POST", urlBuilder.toString(), null, null,
                    "application/x-www-form-urlencoded", 10_000);

            AccessTokenData data = gson.fromJson(responseContentAsString, AccessTokenData.class);
            logger.debug("Received: {}", data);

            if (StringUtils.isEmpty(data.getAccessToken())) {
                throw new InvalidAccessTokenException("Pincode to obtain access token is already used or invalid)");
            }

            return data.getAccessToken();
        } catch (IOException e) {
            throw new InvalidAccessTokenException("Access token request failed", e);
        }
    }

}

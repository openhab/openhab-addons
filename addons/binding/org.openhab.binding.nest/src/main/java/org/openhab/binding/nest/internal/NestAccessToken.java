/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nest.internal;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Request;
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
 * Keeps track of the access token, refreshing it if needed.
 *
 * @author David Bennett - Initial contribution
 */
public class NestAccessToken {
    private final Logger logger = LoggerFactory.getLogger(NestAccessToken.class);

    private final NestBridgeConfiguration config;
    private final Gson gson;

    /**
     * Create the helper class for the nest access token. Also creates the folder
     * to put the access token data in if it does not already exist.
     *
     * @param config The configuration to use for the token
     */
    public NestAccessToken(NestBridgeConfiguration config) {
        this.config = config;
        this.gson = new GsonBuilder().create();
    }

    /**
     * Get the current access token, refreshing if needed. Also reads it from the disk
     * if it is stored there.
     *
     * @throws InvalidAccessTokenException thrown when the access token is invalid and could not be refreshed
     */
    public String getAccessToken() throws InvalidAccessTokenException {
        try {
            // @formatter:off
            StringBuilder urlBuilder = new StringBuilder(NestBindingConstants.NEST_ACCESS_TOKEN_URL)
                    .append("?client_id=")
                    .append(config.clientId)
                    .append("&client_secret=")
                    .append(config.clientSecret)
                    .append("&code=")
                    .append(config.pincode)
                    .append("&grant_type=authorization_code");
            // @formatter:on

            logger.debug("Requesting accesstoken from url: {}", urlBuilder);

            String responseContentAsString = HttpUtil.executeUrl("POST", urlBuilder.toString(), null,
                    null, "application/x-www-form-urlencoded", 10_000);

            AccessTokenData data = gson.fromJson(responseContentAsString, AccessTokenData.class);
            logger.debug("Access token {}", data);
            if (data.getAccessToken() != null) {
                return data.getAccessToken();
            } else {
                throw new InvalidAccessTokenException("Received empty token");
            }
        } catch (IOException e) {
            throw new InvalidAccessTokenException(e);
        }
    }

}

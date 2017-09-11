/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nest.internal;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Request;
import org.openhab.binding.nest.NestBindingConstants;
import org.openhab.binding.nest.internal.config.NestBridgeConfiguration;
import org.openhab.binding.nest.internal.data.AccessTokenData;
import org.openhab.binding.nest.internal.exceptions.InvalidAccessTokenException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Keeps track of the access token, refreshing it if needed.
 *
 * @author David Bennett - Initial contribution
 */
public class NestAccessToken {
    private final Logger logger = LoggerFactory.getLogger(NestAccessToken.class);

    private final NestBridgeConfiguration config;
    private final HttpClient httpClient;

    /**
     * Create the helper class for the nest access token. Also creates the folder
     * to put the access token data in if it does not already exist.
     *
     * @param config The configuration to use for the token
     */
    public NestAccessToken(NestBridgeConfiguration config, HttpClient httpClient) {
        this.config = config;
        this.httpClient = httpClient;
    }

    /**
     * Get the current access token, refreshing if needed. Also reads it from the disk
     * if it is stored there.
     *
     * @throws InvalidAccessTokenException thrown when the access token is invalid and could not be refreshed
     */
    public String getAccessToken() throws InvalidAccessTokenException {
        try {
            StringBuilder urlBuilder = new StringBuilder(NestBindingConstants.NEST_ACCESS_TOKEN_URL)
                    .append("?client_id=")
                    .append(config.clientId)
                    .append("&client_secret=")
                    .append(config.clientSecret)
                    .append("&code=")
                    .append(config.pincode)
                    .append("&grant_type=authorization_code");

            logger.debug("Requesting accesstoken from url: {}", urlBuilder);

            Request request = httpClient.POST(urlBuilder.toString())
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .timeout(10, TimeUnit.SECONDS);

            // TODO should we store this instead of recreating it each and every time?
            Gson gson = new GsonBuilder().create();
            String responseContentAsString = request.send().getContentAsString();

            AccessTokenData data = gson.fromJson(responseContentAsString, AccessTokenData.class);
            if (data.getAccessToken() != null) {
                logger.debug("Access token {}, expiration Time {} ", data.getAccessToken(), data.getExpiresIn());
                return data.getAccessToken();
            } else {
                throw new InvalidAccessTokenException("Received empty token");
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new InvalidAccessTokenException(e);
        }
    }

}

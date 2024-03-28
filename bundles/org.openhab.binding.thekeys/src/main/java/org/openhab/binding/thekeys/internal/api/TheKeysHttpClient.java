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
package org.openhab.binding.thekeys.internal.api;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.io.net.http.HttpUtil;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

/**
 * HTTP client to query the gateway
 *
 * @author Jordan Martin - Initial contribution
 */
@NonNullByDefault
public class TheKeysHttpClient {

    private final Gson gson;

    public TheKeysHttpClient() {
        this.gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
    }

    /**
     * Make a HTTP Get
     *
     * @param url The target url
     * @param timeoutMs The request timeout in ms
     * @param responseType The type of the response to be parsed
     * @return The parsed response or null if empty response
     * @throws TheKeysException If the request failed
     */
    public <T> T get(String url, int timeoutMs, Class<@NonNull T> responseType) throws TheKeysException {
        try {
            String json = HttpUtil.executeUrl("GET", url, timeoutMs);
            return parseResponse(json, responseType);
        } catch (IOException | JsonParseException | TheKeysException e) {
            throw new TheKeysException("Failed to execute GET request to %s".formatted(url), e);
        }
    }

    /**
     * Make a HTTP Post
     *
     * @param url The target url
     * @param body The body to be posted
     * @param timeout The request timeout in ms
     * @param responseType The type of the response to be parsed
     * @return The parsed response or null if empty response
     * @throws TheKeysException If the request failed
     */
    public <T> T post(String url, String body, int timeout, Class<T> responseType) throws TheKeysException {
        try {
            ByteArrayInputStream bodyInputStream = new ByteArrayInputStream(body.getBytes());
            String json = HttpUtil.executeUrl("POST", url, bodyInputStream, "application/x-www-form-urlencoded",
                    timeout);
            return parseResponse(json, responseType);
        } catch (IOException | JsonParseException | TheKeysException e) {
            throw new TheKeysException("Failed to execute POST request to %s".formatted(url), e);
        }
    }

    /**
     * Deserialize a json string to the corresponding class
     * 
     * @param json The json
     * @param responseType The target class
     * @return The deserialized class
     * @throws TheKeysException If the response is null
     */
    private <T> T parseResponse(String json, Class<T> responseType) throws TheKeysException {
        T parsed = gson.fromJson(json, responseType);
        if (parsed == null) {
            throw new TheKeysException("The gateway returns an empty response");
        }
        return parsed;
    }
}

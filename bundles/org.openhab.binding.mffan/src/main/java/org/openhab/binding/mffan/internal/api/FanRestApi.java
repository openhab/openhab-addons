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
package org.openhab.binding.mffan.internal.api;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.ws.rs.core.MediaType;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link FanRestApi} is implements provides access to the smart fan's REST services.
 *
 * @author Mark Brooks - Initial contribution
 */
@NonNullByDefault
public class FanRestApi {
    private final Logger logger = LoggerFactory.getLogger(FanRestApi.class);

    private final String ipAddress;
    private final String url;
    private final HttpClient client;
    private final Gson gson;

    public FanRestApi(String ipAddress, HttpClientFactory httpClientFactory) {
        this.ipAddress = ipAddress;
        this.url = String.format("http://%s/mf", this.ipAddress);
        this.client = httpClientFactory.getCommonHttpClient();
        this.gson = new Gson();
    }

    @Nullable
    public ShadowBufferDto getShadowBuffer() throws RestApiException {
        return doPost("{\"queryDynamicShadowData\" : 1}");
    }

    @Nullable
    public ShadowBufferDto setFanPower(boolean power) throws RestApiException {
        return doPost(String.format("{\"fanOn\" : %s}", String.valueOf(power)));
    }

    @Nullable
    public ShadowBufferDto setFanSpeed(int speed) throws RestApiException {
        return doPost(String.format("{\"fanSpeed\" : %d}", speed));
    }

    @Nullable
    public ShadowBufferDto setFanDirection(ShadowBufferDto.FanDirection direction) throws RestApiException {
        return doPost(String.format("{\"fanDirection\" : \"%s\"}", direction.name()));
    }

    @Nullable
    public ShadowBufferDto setWindPower(boolean power) throws RestApiException {
        return doPost(String.format("{\"wind\" : %s}", String.valueOf(power)));
    }

    @Nullable
    public ShadowBufferDto setWindSpeed(int speed) throws RestApiException {
        return doPost(String.format("{\"windSpeed\" : %d}", speed));
    }

    @Nullable
    public ShadowBufferDto setLightPower(boolean power) throws RestApiException {
        return doPost(String.format("{\"lightOn\" : %s}", String.valueOf(power)));
    }

    @Nullable
    public ShadowBufferDto setLightIntensity(int intensity) throws RestApiException {
        return doPost(String.format("{\"lightBrightness\" : %d}", intensity));
    }

    @Nullable
    private ShadowBufferDto doPost(String payloadJson) throws RestApiException {
        try {
            this.logger.debug("Performing Post: 'URL: {}, Payload: '{}'", this.url, payloadJson);
            Request postRequest = this.client.POST(this.url);
            postRequest.timeout(10, TimeUnit.SECONDS);
            postRequest.header(HttpHeader.ACCEPT, MediaType.APPLICATION_JSON);
            postRequest.header(HttpHeader.CONTENT_TYPE, MediaType.APPLICATION_JSON);
            postRequest.content(new StringContentProvider(payloadJson, Charset.forName(StandardCharsets.UTF_8.name())));
            ContentResponse postResponse = postRequest.send();
            this.logger.debug("Response status: {}", postResponse.getStatus());
            if (postResponse.getStatus() == 200) {
                this.logger.trace("Post Response Content = '{}'", postResponse.getContentAsString());
                return this.gson.fromJson(postResponse.getContentAsString(), ShadowBufferDto.class);
            }
        } catch (JsonSyntaxException | InterruptedException | TimeoutException | ExecutionException e) {
            this.logger.warn("Exception on post: {}", e.getMessage());
            throw new RestApiException(e);
        }
        return null;
    }
}

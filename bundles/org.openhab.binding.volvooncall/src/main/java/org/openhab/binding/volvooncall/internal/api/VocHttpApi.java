/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.volvooncall.internal.api;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentProvider;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpField;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.volvooncall.internal.VolvoOnCallException;
import org.openhab.binding.volvooncall.internal.VolvoOnCallException.ErrorType;
import org.openhab.binding.volvooncall.internal.config.ApiBridgeConfiguration;
import org.openhab.binding.volvooncall.internal.dto.PostResponse;
import org.openhab.binding.volvooncall.internal.dto.VocAnswer;
import org.openhab.core.cache.ExpiringCacheMap;
import org.openhab.core.id.InstanceUUID;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * {@link VocHttpApi} wraps the VolvoOnCall REST API.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class VocHttpApi {
    // The URL to use to connect to VocAPI.
    // For North America and China syntax changes to vocapi-cn.xxx
    private static final String SERVICE_URL = "https://vocapi.wirelesscar.net/customerapi/rest/v3.0/";
    private static final int TIMEOUT_MS = 10000;
    private static final String JSON_CONTENT_TYPE = "application/json";

    private final Logger logger = LoggerFactory.getLogger(VocHttpApi.class);
    private final Gson gson;
    private final ExpiringCacheMap<String, @Nullable String> cache;
    private final HttpClient httpClient;
    private final ApiBridgeConfiguration configuration;

    public VocHttpApi(String clientName, ApiBridgeConfiguration configuration, Gson gson,
            HttpClientFactory httpClientFactory) throws VolvoOnCallException {
        this.gson = gson;
        this.cache = new ExpiringCacheMap<>(120 * 1000);
        this.configuration = configuration;
        this.httpClient = httpClientFactory.createHttpClient(clientName);

        httpClient.setUserAgentField(new HttpField(HttpHeader.USER_AGENT, "openhab/voc_binding/" + InstanceUUID.get()));
        try {
            httpClient.start();
        } catch (Exception e) {
            throw new VolvoOnCallException(new IOException("Unable to start Jetty HttpClient", e));
        }
    }

    public void dispose() throws Exception {
        httpClient.stop();
    }

    private @Nullable String getResponse(HttpMethod method, String url, @Nullable String body) {
        try {
            Request request = httpClient.newRequest(url).header(HttpHeader.CACHE_CONTROL, "no-cache")
                    .header(HttpHeader.CONTENT_TYPE, JSON_CONTENT_TYPE).header(HttpHeader.ACCEPT, "*/*")
                    .header(HttpHeader.AUTHORIZATION, configuration.getAuthorization()).header("x-device-id", "Device")
                    .header("x-originator-type", "App").header("x-os-type", "Android").header("x-os-version", "22")
                    .timeout(TIMEOUT_MS, TimeUnit.MILLISECONDS);
            if (body != null) {
                ContentProvider content = new StringContentProvider(JSON_CONTENT_TYPE, body, StandardCharsets.UTF_8);
                request = request.content(content);
            }
            ContentResponse contentResponse = request.method(method).send();
            return contentResponse.getContentAsString();
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            return null;
        }
    }

    private <T extends VocAnswer> T callUrl(HttpMethod method, String endpoint, Class<T> objectClass,
            @Nullable String body) throws VolvoOnCallException {
        try {
            String url = endpoint.startsWith("http") ? endpoint : SERVICE_URL + endpoint;
            String jsonResponse = method == HttpMethod.GET
                    ? cache.putIfAbsentAndGet(endpoint, () -> getResponse(method, url, body))
                    : getResponse(method, url, body);
            if (jsonResponse == null) {
                throw new IOException();
            } else {
                logger.debug("Request to `{}` answered : {}", url, jsonResponse);
                T responseDTO = Objects.requireNonNull(gson.fromJson(jsonResponse, objectClass));
                String error = responseDTO.getErrorLabel();
                if (error != null) {
                    throw new VolvoOnCallException(error, responseDTO.getErrorDescription());
                }
                return responseDTO;
            }
        } catch (JsonSyntaxException | IOException e) {
            throw new VolvoOnCallException(e);
        }
    }

    public <T extends VocAnswer> T getURL(String endpoint, Class<T> objectClass) throws VolvoOnCallException {
        return callUrl(HttpMethod.GET, endpoint, objectClass, null);
    }

    public @Nullable PostResponse postURL(String endpoint, @Nullable String body) throws VolvoOnCallException {
        try {
            return callUrl(HttpMethod.POST, endpoint, PostResponse.class, body);
        } catch (VolvoOnCallException e) {
            if (e.getType() == ErrorType.SERVICE_UNABLE_TO_START) {
                logger.info("Unable to start service request sent to VoC");
                return null;
            } else {
                throw e;
            }
        }
    }

    public <T extends VocAnswer> T getURL(Class<T> objectClass, String vin) throws VolvoOnCallException {
        String url = String.format("vehicles/%s/%s", vin, objectClass.getSimpleName().toLowerCase());
        return getURL(url, objectClass);
    }
}

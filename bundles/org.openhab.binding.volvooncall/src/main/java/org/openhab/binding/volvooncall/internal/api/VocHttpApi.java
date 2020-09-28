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
package org.openhab.binding.volvooncall.internal.api;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.Properties;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.volvooncall.internal.VolvoOnCallException;
import org.openhab.binding.volvooncall.internal.VolvoOnCallException.ErrorType;
import org.openhab.binding.volvooncall.internal.config.VolvoOnCallBridgeConfiguration;
import org.openhab.binding.volvooncall.internal.dto.PostResponse;
import org.openhab.binding.volvooncall.internal.dto.VocAnswer;
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

    private static final int REQUEST_TIMEOUT_S = 10;
    // The URL to use to connect to VocAPI.
    // TODO : for North America and China syntax changes to vocapi-cn.xxx
    private static final String SERVICE_URL = "https://vocapi.wirelesscar.net/customerapi/rest/v3.0/";
    private final HttpClient client = HttpClient.newBuilder().version(Version.HTTP_2).followRedirects(Redirect.ALWAYS)
            .build();
    private final Logger logger = LoggerFactory.getLogger(VocHttpApi.class);
    private final Properties httpHeader = new Properties();
    private final Gson gson;

    public VocHttpApi(VolvoOnCallBridgeConfiguration configuration, Gson gson) {
        this.gson = gson;
        httpHeader.put("cache-control", "no-cache");
        httpHeader.put("content-type", "application/json");
        httpHeader.put("Accept", "*/*");
        httpHeader.put("Authorization", configuration.getAuthorization());
        httpHeader.put("x-device-id", "Device");
        httpHeader.put("x-originator-type", "App");
        httpHeader.put("x-os-type", "Android");
        httpHeader.put("x-os-version", "22");
    }

    private Builder prepareRequest() {
        Builder request = HttpRequest.newBuilder().timeout(Duration.ofSeconds(REQUEST_TIMEOUT_S));
        httpHeader.stringPropertyNames()
                .forEach(headerKey -> request.header(headerKey, httpHeader.getProperty(headerKey)));
        return request;
    }

    private URI getUri(String endpoint) throws URISyntaxException {
        String url = endpoint.startsWith("http") ? endpoint : SERVICE_URL + endpoint;
        return new URI(url);
    }

    private <T extends VocAnswer> T callUrl(String method, String endpoint, Class<T> objectClass, String body)
            throws VolvoOnCallException {
        try {
            URI uri = getUri(endpoint);
            HttpResponse<String> clientResponse = client.send(
                    prepareRequest().method(method, BodyPublishers.ofString(body)).uri(uri).build(),
                    BodyHandlers.ofString());
            String jsonResponse = clientResponse.body();
            logger.debug("Request to `{}` answered : {}", uri, jsonResponse);
            T responseDTO = gson.fromJson(jsonResponse, objectClass);
            String error = responseDTO.getErrorLabel();
            if (error != null) {
                throw new VolvoOnCallException(error, responseDTO.getErrorDescription());
            }
            return responseDTO;
        } catch (JsonSyntaxException | InterruptedException | IOException | URISyntaxException e) {
            throw new VolvoOnCallException(e);
        }
    }

    public <T extends VocAnswer> T getURL(String endpoint, Class<T> objectClass) throws VolvoOnCallException {
        return callUrl("GET", endpoint, objectClass, "");
    }

    public @Nullable PostResponse postURL(String endpoint, @Nullable String body) throws VolvoOnCallException {
        try {
            return callUrl("POST", endpoint, PostResponse.class, body == null ? "" : body);
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

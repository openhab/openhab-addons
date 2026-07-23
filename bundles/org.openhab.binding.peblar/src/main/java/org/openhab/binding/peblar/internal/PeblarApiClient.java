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
package org.openhab.binding.peblar.internal;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpResponseException;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * @author Hilbrand Bouwkamp - Initial contribution
 */
@NonNullByDefault
class PeblarApiClient {

    private static final String API_BASE = "http://%s/api/wlac/v1";
    private static final int TIMEOUT_S = 10;

    private final Logger logger = LoggerFactory.getLogger(PeblarApiClient.class);

    private final HttpClient httpClient;
    private final Gson gson;
    private final String baseUrl;
    private final String apiToken;

    public PeblarApiClient(HttpClient httpClient, String hostname, String apiToken) {
        this.httpClient = httpClient;
        this.gson = new Gson().newBuilder().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();
        this.baseUrl = String.format(API_BASE, hostname);
        this.apiToken = apiToken;
    }

    public PeblarMeterDTO getMeter() throws PeblarApiException {
        return get("/meter", PeblarMeterDTO.class);
    }

    public PeblarEvInterfaceDTO getEvInterface() throws PeblarApiException {
        return get("/evinterface", PeblarEvInterfaceDTO.class);
    }

    public PeblarSystemDTO getSystem() throws PeblarApiException {
        return get("/system", PeblarSystemDTO.class);
    }

    public void setChargeCurrentLimit(long milliAmps) throws PeblarApiException {
        final JsonObject body = new JsonObject();

        body.addProperty("ChargeCurrentLimit", milliAmps);
        patch("/evinterface", body);
    }

    public void setForce1Phase(boolean force) throws PeblarApiException {
        final JsonObject body = new JsonObject();

        body.addProperty("Force1Phase", force);
        patch("/evinterface", body);
    }

    private <T> T get(String path, Class<T> type) throws PeblarApiException {
        logger.debug("GET {}", path);
        Response response = send(HttpMethod.GET, path, Function.identity());
        if (response instanceof ContentResponse cr) {
            return gson.fromJson(cr.getContentAsString(), type);
        }
        throw new PeblarApiException("Unable to read response: " + response.getReason());
    }

    private void patch(String path, JsonObject body) throws PeblarApiException {
        logger.debug("PATCH {} body={}", path, body);
        send(HttpMethod.PATCH, path, b -> b.content(new StringContentProvider(gson.toJson(body)), "application/json"));
    }

    private Response send(HttpMethod method, String path, Function<Request, Request> builder)
            throws PeblarApiException {
        final String url = baseUrl + path;

        try {
            final Response response = builder.apply(buildRequest(method, url)).send();

            checkStatus(response, url);
            return response;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new PeblarApiException("Request interrupted: " + url, e);
        } catch (ExecutionException | TimeoutException e) {
            if (e.getCause() instanceof HttpResponseException re) {
                checkStatus(re.getResponse(), url);
            }
            throw new PeblarApiException("Request failed: " + url, e);
        }
    }

    private Request buildRequest(HttpMethod method, String url) {
        return httpClient.newRequest(url).method(method).header(HttpHeader.AUTHORIZATION, apiToken)
                .header(HttpHeader.ACCEPT, "application/json").timeout(TIMEOUT_S, TimeUnit.SECONDS);
    }

    private void checkStatus(Response response, String url) throws PeblarApiException {
        final int status = response.getStatus();

        if (logger.isTraceEnabled()) {
            logger.trace("response:({}) {}", status, response.getReason());
        }
        if (status == HttpStatus.UNAUTHORIZED_401) {
            throw new PeblarApiAuthenticationException("@text/addon.peblar.error.authorization.failed");
        }
        if (status < HttpStatus.OK_200 || status >= HttpStatus.MULTIPLE_CHOICES_300) {
            throw new PeblarApiException("HTTP " + status + " for " + url);
        }
    }
}

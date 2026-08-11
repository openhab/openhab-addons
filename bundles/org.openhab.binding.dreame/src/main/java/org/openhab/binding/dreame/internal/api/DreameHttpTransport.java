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
package org.openhab.binding.dreame.internal.api;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.dreame.internal.util.DreameDiagnostics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

/**
 * Performs authenticated HTTP requests against the selected regional cloud API.
 *
 * @author Ronny Grun - Initial contribution
 */
@NonNullByDefault
final class DreameHttpTransport {
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(15);

    private final Logger logger = LoggerFactory.getLogger(DreameHttpTransport.class);
    private final HttpClient httpClient;
    private final DreameAuthenticationService authentication;
    private final Gson gson = new Gson();

    DreameHttpTransport(HttpClient httpClient, DreameAuthenticationService authentication) {
        this.httpClient = httpClient;
        this.authentication = authentication;
    }

    JsonObject post(String path, @Nullable String body, boolean authenticated) throws DreameCloudException {
        long started = System.nanoTime();
        String service = authentication.cloudService().label();
        logger.trace("{} POST {} authenticated={} body={}", service, path, authenticated,
                DreameDiagnostics.sanitize(body));
        Request request = httpClient.newRequest(apiUrl() + path).method(HttpMethod.POST)
                .timeout(REQUEST_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS).header(HttpHeader.ACCEPT, "*/*")
                .header(HttpHeader.ACCEPT_LANGUAGE, "en-US;q=0.8").header(HttpHeader.ACCEPT_ENCODING, "gzip, deflate")
                .header(HttpHeader.USER_AGENT, authentication.cloudService().userAgent())
                .header(HttpHeader.AUTHORIZATION, authentication.cloudService().authorization())
                .header("Tenant-Id", authentication.tenantId());
        if (authenticated) {
            request.header(HttpHeader.CONTENT_TYPE, "application/json").header("Dreame-Auth",
                    authentication.accessToken());
        } else {
            request.header(HttpHeader.CONTENT_TYPE, "application/x-www-form-urlencoded");
        }
        if (body != null) {
            request.content(new StringContentProvider(body, StandardCharsets.UTF_8));
        }

        try {
            ContentResponse response = request.send();
            long elapsed = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started);
            logger.debug("{} POST {} returned HTTP {} in {} ms", service, path, response.getStatus(), elapsed);
            if (path.endsWith("/iotuserdata/getDeviceData")) {
                logger.trace("{} map response for {} contains {} characters", service, path,
                        response.getContentAsString().length());
            } else {
                logger.trace("{} response for {}: {}", service, path,
                        DreameDiagnostics.sanitize(response.getContentAsString()));
            }
            if (!HttpStatus.isSuccess(response.getStatus())) {
                throw new DreameCloudException(service + " returned HTTP " + response.getStatus());
            }
            JsonElement json = gson.fromJson(response.getContentAsString(), JsonElement.class);
            if (!(json instanceof JsonObject object)) {
                throw new DreameCloudException(service + " returned an invalid response");
            }
            return object;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new DreameCloudException(service + " request was interrupted", e);
        } catch (ExecutionException | TimeoutException | JsonParseException e) {
            throw new DreameCloudException(service + " request failed", e);
        }
    }

    private String apiUrl() {
        return authentication.cloudService().apiUrl(authentication.country());
    }
}

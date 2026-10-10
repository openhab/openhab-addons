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
package org.openhab.binding.windhager.internal;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Authentication;
import org.eclipse.jetty.client.api.AuthenticationStore;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.util.DigestAuthentication;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Connector for the BioWin webserver.
 *
 * @author BenjiU - Initial contribution
 */
@NonNullByDefault
public class WindhagerConnector {

    private static final String DATAPOINT_API_PATH = "api/1.0/datapoint/";
    private static final int REQUEST_TIMEOUT_SECONDS = 10;

    private final Logger logger = LoggerFactory.getLogger(WindhagerConnector.class);

    private final HttpClient httpClient;
    private final URI serverUri;
    private final DigestAuthentication authentication;

    public WindhagerConnector(HttpClient httpClient, String hostname, int port, String username, String password)
            throws URISyntaxException {
        this.httpClient = httpClient;
        this.serverUri = new URI("http", null, hostname, port, "/", null, null);
        this.authentication = new DigestAuthentication(serverUri, Authentication.ANY_REALM, username, password);
        httpClient.getAuthenticationStore().addAuthentication(authentication);
    }

    public void dispose() {
        AuthenticationStore authenticationStore = httpClient.getAuthenticationStore();
        var authenticationResult = authenticationStore.findAuthenticationResult(serverUri);
        if (authenticationResult != null) {
            authenticationStore.removeAuthenticationResult(authenticationResult);
        }
        authenticationStore.removeAuthentication(authentication);
    }

    /**
     * Reads a numeric value from a BioWin webserver OID.
     *
     * @param oid the BioWin OID, for example {@code 1/60/0/23/103/0}
     * @return the parsed value, or {@code null} if the request fails or the response is not numeric
     */
    public ValueReadResult readValue(String oid) {
        if (oid.isBlank()) {
            return new ValueReadResult(null, false);
        }

        ContentResponse response = sendRequest(DATAPOINT_API_PATH + normalizeOid(oid));
        if (response == null) {
            return new ValueReadResult(null, true);
        }
        if (response.getStatus() == HttpStatus.UNAUTHORIZED_401 || response.getStatus() >= 500) {
            return new ValueReadResult(null, true);
        }
        if (!HttpStatus.isSuccess(response.getStatus())) {
            return new ValueReadResult(null, false);
        }

        try {
            return new ValueReadResult(new BigDecimal(readValueElement(response.getContentAsString()).getAsString()),
                    false);
        } catch (RuntimeException e) {
            logger.debug("BioWin returned a non-numeric value for oid {}", oid);
            return new ValueReadResult(null, false);
        }
    }

    private @Nullable ContentResponse sendRequest(String relativePath) {
        try {
            URI uri = createRequestUri(relativePath);
            return httpClient.newRequest(uri).method(HttpMethod.GET).timeout(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .send();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.debug("Interrupted while requesting BioWin path {}", relativePath);
        } catch (ExecutionException | TimeoutException | URISyntaxException | IllegalArgumentException e) {
            logger.debug("Unable to request BioWin path {}: {}", relativePath, e.getMessage());
        }
        return null;
    }

    private URI createRequestUri(String relativePath) throws URISyntaxException {
        if (!relativePath.startsWith(DATAPOINT_API_PATH)) {
            throw new URISyntaxException(relativePath, "Unexpected BioWin API path");
        }
        String path = "/" + relativePath;
        return new URI(serverUri.getScheme(), null, serverUri.getHost(), serverUri.getPort(), path, null, null);
    }

    private static JsonElement readValueElement(String body) {
        JsonObject response = JsonParser.parseString(body).getAsJsonObject();
        JsonElement value = response.get("value");
        if (value == null || value.isJsonNull()) {
            throw new IllegalArgumentException("BioWin response does not contain a value");
        }
        return value;
    }

    private static String normalizeOid(String oid) {
        return oid.replaceFirst("^/+", "");
    }

    public record ValueReadResult(@Nullable BigDecimal value, boolean communicationFailure) {
    }
}

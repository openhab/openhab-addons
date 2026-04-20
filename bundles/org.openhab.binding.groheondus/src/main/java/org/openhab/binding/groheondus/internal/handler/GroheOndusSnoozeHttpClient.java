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
package org.openhab.binding.groheondus.internal.handler;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import io.github.floriansw.ondus.api.OndusService;
import io.github.floriansw.ondus.api.client.ApiClient;
import io.github.floriansw.ondus.api.model.BaseAppliance;

/**
 * Helper for ONDUS endpoints that are not yet exposed by the upstream library.
 */
@NonNullByDefault
public final class GroheOndusSnoozeHttpClient {
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String CONTENT_TYPE_HEADER = "Content-Type";
    private static final String CONTENT_TYPE_JSON = "application/json";
    private static final String SNOOZE_ENDPOINT = "iot/locations/%d/rooms/%d/appliances/%s/snooze";

    private GroheOndusSnoozeHttpClient() {
    }

    public static void setPauseDuration(OndusService service, BaseAppliance appliance, int durationMinutes)
            throws IOException {
        String endpoint = String.format(SNOOZE_ENDPOINT, appliance.getRoom().getLocation().getId(),
                appliance.getRoom().getId(), appliance.getApplianceId());
        if (durationMinutes == 0) {
            sendRequest(service, endpoint, "DELETE", null);
        } else {
            sendRequest(service, endpoint, "PUT", String.format("{\"snooze_duration\":%d}", durationMinutes));
        }
    }

    private static void sendRequest(OndusService service, String endpoint, String method, @Nullable String body)
            throws IOException {
        ApiClient apiClient = getApiClient(service);
        HttpURLConnection connection = (HttpURLConnection) URI
                .create(getBaseUrl(apiClient) + apiClient.apiPath() + endpoint).toURL().openConnection();
        connection.setRequestMethod(method);
        connection.setRequestProperty(AUTHORIZATION_HEADER, getAuthorization(apiClient));
        connection.setRequestProperty(CONTENT_TYPE_HEADER, CONTENT_TYPE_JSON);
        connection.setDoInput(true);

        if (body != null) {
            byte[] bodyBytes = body.getBytes(StandardCharsets.UTF_8);
            connection.setDoOutput(true);
            try (var outputStream = connection.getOutputStream()) {
                outputStream.write(bodyBytes);
            }
        }

        int responseCode = connection.getResponseCode();
        if (responseCode / 100 != 2) {
            throw new IOException("Snooze request failed with HTTP " + responseCode + ": "
                    + readResponse(connection.getErrorStream()));
        }
    }

    private static String readResponse(@Nullable InputStream stream) throws IOException {
        if (stream == null) {
            return "";
        }
        try (stream) {
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static ApiClient getApiClient(OndusService service) throws IOException {
        try {
            Field apiClientField = OndusService.class.getDeclaredField("apiClient");
            apiClientField.setAccessible(true);
            Object apiClient = apiClientField.get(service);
            if (apiClient instanceof ApiClient typedApiClient) {
                return typedApiClient;
            }
            throw new IOException("Unexpected API client type: " + apiClient);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new IOException("Could not access ONDUS API client", e);
        }
    }

    private static String getAuthorization(ApiClient apiClient) throws IOException {
        try {
            Method authorizationMethod = ApiClient.class.getDeclaredMethod("authorization");
            authorizationMethod.setAccessible(true);
            Object authorization = authorizationMethod.invoke(apiClient);
            if (authorization instanceof String authorizationHeader && !authorizationHeader.isBlank()) {
                return authorizationHeader;
            }
            throw new IOException("Missing authorization header for ONDUS API client");
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new IOException("Could not access ONDUS authorization header", e);
        }
    }

    private static String getBaseUrl(ApiClient apiClient) throws IOException {
        try {
            Field baseUrlField = ApiClient.class.getDeclaredField("baseUrl");
            baseUrlField.setAccessible(true);
            Object baseUrl = baseUrlField.get(apiClient);
            if (baseUrl instanceof String baseUrlString) {
                return baseUrlString;
            }
            throw new IOException("Missing ONDUS API base URL");
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new IOException("Could not access ONDUS API base URL", e);
        }
    }
}

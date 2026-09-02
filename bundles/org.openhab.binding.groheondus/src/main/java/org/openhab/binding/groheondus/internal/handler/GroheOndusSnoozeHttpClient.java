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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.core.io.net.http.HttpUtil;

import io.github.floriansw.ondus.api.model.BaseAppliance;

/**
 * @author Michael Parment - Initial contribution
 *
 *         Helper for ONDUS endpoints that are not yet exposed by the upstream library.
 */
@NonNullByDefault
public final class GroheOndusSnoozeHttpClient {
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String CONTENT_TYPE_HEADER = "Content-Type";
    private static final String CONTENT_TYPE_JSON = "application/json";
    private static final String API_PATH = "/v3/";
    private static final String SNOOZE_ENDPOINT = "iot/locations/%d/rooms/%d/appliances/%s/snooze";
    private static final int REQUEST_TIMEOUT_MS = 10_000;

    private GroheOndusSnoozeHttpClient() {
    }

    public static void setPauseDuration(String baseUrl, String authorizationHeader, BaseAppliance appliance,
            int durationMinutes) throws IOException {
        String endpoint = String.format(SNOOZE_ENDPOINT, appliance.getRoom().getLocation().getId(),
                appliance.getRoom().getId(), appliance.getApplianceId());
        if (durationMinutes == 0) {
            sendRequest(baseUrl, authorizationHeader, endpoint, HttpMethod.DELETE, null);
        } else {
            sendRequest(baseUrl, authorizationHeader, endpoint, HttpMethod.PUT,
                    String.format("{\"snooze_duration\":%d}", durationMinutes));
        }
    }

    private static void sendRequest(String baseUrl, String authorizationHeader, String endpoint, HttpMethod method,
            @Nullable String body) throws IOException {
        Properties headers = new Properties();
        headers.setProperty(AUTHORIZATION_HEADER, authorizationHeader);
        if (body != null) {
            headers.setProperty(CONTENT_TYPE_HEADER, CONTENT_TYPE_JSON);
            HttpUtil.executeUrl(method.asString(), baseUrl + API_PATH + endpoint, headers,
                    new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)), CONTENT_TYPE_JSON,
                    REQUEST_TIMEOUT_MS);
        } else {
            HttpUtil.executeUrl(method.asString(), baseUrl + API_PATH + endpoint, headers, null, null,
                    REQUEST_TIMEOUT_MS);
        }
    }
}

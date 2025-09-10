/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.homekit.internal;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * @author Andrew Fiddian-Green - Initial contribution
 */
public class SecureAccessoryClient {

    private final SecureSession session;
    private final HttpClient httpClient;
    private final String baseUrl;

    public SecureAccessoryClient(HttpClient httpClient, SecureSession session, String baseUrl) {
        this.httpClient = httpClient;
        this.session = session;
        this.baseUrl = baseUrl;
    }

    public String readCharacteristic(String aid, String iid) throws Exception {
        String query = String.format("?id=%s.%s", aid, iid);
        URI uri = URI.create(baseUrl + "/characteristics" + query);

        HttpRequest request = HttpRequest.newBuilder().uri(uri).timeout(Duration.ofSeconds(5))
                .header("Accept", "application/json").GET().build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body(); // Optionally decrypt if accessory uses encrypted reads
    }

    public void writeCharacteristic(String aid, String iid, Object value) throws Exception {
        String json = String.format("{\"characteristics\":[{\"aid\":%s,\"iid\":%s,\"value\":%s}]}", aid, iid,
                formatValue(value));

        byte[] encryptedPayload = session.encrypt(json.getBytes());
        URI uri = URI.create(baseUrl + "/characteristics");

        HttpRequest request = HttpRequest.newBuilder().uri(uri).timeout(Duration.ofSeconds(5))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofByteArray(encryptedPayload)).build();

        HttpResponse<Void> response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());
        if (response.statusCode() != 200) {
            throw new RuntimeException("Write failed: HTTP " + response.statusCode());
        }
    }

    private String formatValue(Object value) {
        if (value instanceof Boolean || value instanceof Number) {
            return value.toString();
        }
        return "\"" + value.toString() + "\"";
    }
}
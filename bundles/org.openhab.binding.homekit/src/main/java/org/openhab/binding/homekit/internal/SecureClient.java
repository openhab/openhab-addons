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

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.BytesContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;

import com.google.gson.JsonSyntaxException;

/**
 * HTTP client methods for reading and writing HomeKit accessory characteristics over a secure session.
 * It handles encryption and decryption of requests and responses.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
public class SecureClient {

    private final SecureSession session;
    private final HttpClient httpClient;
    private final String baseUrl;

    public SecureClient(HttpClient httpClient, SecureSession session, String baseUrl) {
        this.httpClient = httpClient;
        this.session = session;
        this.baseUrl = baseUrl;
    }

    /**
     * Reads a characteristic from the accessory.
     *
     * @param aid Accessory ID
     * @param iid Instance ID
     * @return JSON response as String
     */
    public String readCharacteristic(String aid, String iid) {
        String query = String.format("?id=%s.%s", aid, iid);
        Request request = httpClient.newRequest(baseUrl + "/characteristics" + query) //
                .timeout(5, TimeUnit.SECONDS) //
                .method(HttpMethod.GET) //
                .header(HttpHeader.ACCEPT, "application/json");
        try {
            ContentResponse response = request.send();
            if (response.getStatus() == 200) {
                byte[] encrypted = response.getContent();
                return new String(session.decrypt(encrypted), StandardCharsets.UTF_8);
            }
        } catch (TimeoutException | ExecutionException | InterruptedException | JsonSyntaxException e) {
        }
        return "";
    }

    /**
     * Writes a characteristic to the accessory.
     *
     * @param aid Accessory ID
     * @param iid Instance ID
     * @param value Value to write (String, Number, Boolean)
     * @throws Exception on communication or encryption errors
     */
    public void writeCharacteristic(String aid, String iid, Object value) throws Exception {
        String json = String.format("{\"characteristics\":[{\"aid\":%s,\"iid\":%s,\"value\":%s}]}", aid, iid,
                formatValue(value));
        byte[] encryptedPayload = session.encrypt(json.getBytes());
        Request request = httpClient.newRequest(baseUrl + "/characteristics") //
                .timeout(5, TimeUnit.SECONDS) //
                .method(HttpMethod.PUT) //
                .header(HttpHeader.CONTENT_TYPE, "application/json") //
                .content(new BytesContentProvider(encryptedPayload));
        try {
            ContentResponse response = request.send();
            if (response.getStatus() != 200) {
                throw new RuntimeException("Write failed: HTTP " + response.getStatus());
            }
        } catch (TimeoutException | ExecutionException | InterruptedException | JsonSyntaxException e) {
        }
    }

    /*
     * Formats the value for JSON. Strings are quoted, numbers and booleans are not.
     */
    private String formatValue(Object value) {
        if (value instanceof Boolean || value instanceof Number) {
            return value.toString();
        }
        return "\"" + value.toString() + "\"";
    }
}
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
package org.openhab.binding.jellyfin.internal.client.api.client;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Raw API response container
 *
 * @author Patrik Gfeller, based on Android SDK by Peter Feller - Initial contribution (AI generated code by "Claude
 *         Sonnet 3.7")
 */
public class RawResponse {

    private final int status;
    private final Map<String, String> headers;
    private final ByteBuffer body;

    /**
     * Create a new raw response
     *
     * @param status HTTP status code
     * @param headers Response headers
     * @param body Response body as ByteBuffer
     */
    public RawResponse(int status, Map<String, String> headers, ByteBuffer body) {
        this.status = status;
        this.headers = headers;
        this.body = body;
    }

    /**
     * Get the HTTP status code
     *
     * @return HTTP status code
     */
    public int getStatus() {
        return status;
    }

    /**
     * Get the response headers
     *
     * @return Map of response headers
     */
    public Map<String, String> getHeaders() {
        return headers;
    }

    /**
     * Get the response body as ByteBuffer
     *
     * @return ByteBuffer containing the response body
     */
    public ByteBuffer getBody() {
        return body;
    }

    /**
     * Get the response body as a String using UTF-8 encoding
     *
     * @return String representation of the body
     */
    public String getBodyAsString() {
        if (body == null) {
            return null;
        }

        ByteBuffer duplicate = body.duplicate();
        byte[] bytes = new byte[duplicate.remaining()];
        duplicate.get(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }
}

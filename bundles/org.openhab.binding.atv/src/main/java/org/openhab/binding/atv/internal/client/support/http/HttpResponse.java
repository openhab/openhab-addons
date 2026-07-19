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
package org.openhab.binding.atv.internal.client.support.http;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.atv.internal.client.support.BinaryPlist;

/**
 * Generic HTTP (or RTSP) response message.
 *
 * <p>
 * The status line protocol is split into {@code protocol} ("HTTP", "RTSP", ...) and
 * {@code version} ("1.1", "1.0", ...). The body is either a {@link String} (text bodies), a
 * {@code byte[]} (binary bodies, i.e. when the Content-Type starts with "application" or the
 * payload is not valid UTF-8) or a {@link Map} (formatted as a binary property list by
 * {@link HttpParser#formatResponse(HttpResponse)}).
 *
 * @param protocol protocol name from the status line, e.g. {@code "HTTP"}
 * @param version protocol version from the status line, e.g. {@code "1.1"}
 * @param code numeric status code
 * @param message status reason phrase
 * @param headers response headers (a case-insensitive map for parsed responses)
 * @param body response body ({@link String}, {@code byte[]} or {@link Map}; never {@code null},
 *            use an empty string/array for "no body")
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public record HttpResponse(String protocol, String version, int code, String message, Map<String, ?> headers,
        Object body) {

    /**
     * Returns the body as raw bytes: a {@link String} body is encoded as UTF-8, a {@code byte[]}
     * body is returned as-is and a {@link Map} body is serialized as a binary property list.
     *
     * @return the body as bytes (empty array when there is no body)
     */
    public byte[] bodyBytes() {
        if (body == null) {
            return new byte[0];
        }
        if (body instanceof byte[] bytes) {
            return bytes;
        }
        if (body instanceof String text) {
            return text.getBytes(StandardCharsets.UTF_8);
        }
        return BinaryPlist.dump(body);
    }
}

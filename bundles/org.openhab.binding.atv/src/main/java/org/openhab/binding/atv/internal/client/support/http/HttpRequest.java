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

/**
 * Generic HTTP (or RTSP) request message.
 *
 * <p>
 * The request line protocol is split into {@code protocol} ("HTTP", "RTSP", ...) and
 * {@code version} ("1.1", "1.0", ...). The body is either a {@link String} or a {@code byte[]}.
 *
 * @param method request method, e.g. {@code "GET"}
 * @param path request path/URI, e.g. {@code "/info"}
 * @param protocol protocol name from the request line, e.g. {@code "HTTP"}
 * @param version protocol version from the request line, e.g. {@code "1.1"}
 * @param headers request headers (a case-insensitive map for parsed requests)
 * @param body request body ({@link String} or {@code byte[]}; never {@code null}, use an empty
 *            string/array for "no body")
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public record HttpRequest(String method, String path, String protocol, String version, Map<String, ?> headers,
        Object body) {

    /**
     * Returns the body as raw bytes; a {@link String} body is encoded as UTF-8.
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
        return body.toString().getBytes(StandardCharsets.UTF_8);
    }
}

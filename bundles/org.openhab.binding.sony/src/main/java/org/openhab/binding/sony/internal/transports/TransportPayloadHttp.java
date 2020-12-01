/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.sony.internal.transports;

import org.apache.commons.lang.Validate;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * This class represents an HTTP payload. An HTTP payload includes both the URL address to send something to and
 * possibly a string to include as the body
 * 
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class TransportPayloadHttp implements TransportPayload {
    /** The URL to call */
    private final String url;

    /** Possibly the body (XML or JSON) */
    private final @Nullable String body;

    /**
     * Constructs the HTTP payload with a URL and no body
     * 
     * @param url a non-null, non-empty URL
     */
    public TransportPayloadHttp(final String url) {
        this(url, null);
    }

    /**
     * Constructs the HTTP payload with a URL and body
     * 
     * @param url a non-null, non-empty URL
     * @param body a possibly null, possibly empty body
     */
    public TransportPayloadHttp(final String url, final @Nullable String body) {
        Validate.notEmpty(url, "url cannot be empty");
        this.url = url;
        this.body = body == null ? "" : body; // convert null to empty
    }

    /**
     * The URL for the HTTP request
     * 
     * @return a non-null, non-empty URL
     */
    public String getUrl() {
        return url;
    }

    /**
     * The body for the HTTP request
     * 
     * @return the body for the HTTP request or null if not applicable
     */
    public @Nullable String getBody() {
        return body;
    }
}

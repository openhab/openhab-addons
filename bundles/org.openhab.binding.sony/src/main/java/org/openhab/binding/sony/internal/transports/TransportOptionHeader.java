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

import java.util.Objects;

import org.apache.commons.lang.Validate;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.http.HttpHeader;
import org.openhab.binding.sony.internal.net.Header;

/**
 * The transport option to specify a header value (a string key/value)
 * 
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class TransportOptionHeader implements TransportOption {
    /** The header to use */
    private final Header header;

    /**
     * Construct the transport option from a header
     * 
     * @param header a non-null header
     */
    public TransportOptionHeader(final Header header) {
        Objects.requireNonNull(header, "header cannot be null");
        this.header = header;
    }

    /**
     * Constructs the transport option from a http header and a value
     * 
     * @param hdr a non-null http header
     * @param value a non-null, non-empty header value
     */
    public TransportOptionHeader(final HttpHeader hdr, final String value) {
        Objects.requireNonNull(hdr, "hdr cannot be null");
        Validate.notEmpty(value, "value cannot be empty");

        this.header = new Header(hdr.asString(), value);
    }

    /**
     * Constructs the transport option from a http header and a value
     * 
     * @param key a non-null, non-empty header key
     * @param value a non-null, non-empty header value
     */
    public TransportOptionHeader(final String key, final String value) {
        Validate.notEmpty(value, "value cannot be empty");
        Validate.notEmpty(value, "value cannot be empty");
        this.header = new Header(key, value);
    }

    /**
     * Get's the header for this transport option
     * 
     * @return a non-null header
     */
    public Header getHeader() {
        return header;
    }
}

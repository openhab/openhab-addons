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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.sony.internal.net.HttpResponse;

/**
 * This class represents an HTTP response
 * 
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class TransportResultHttpResponse implements TransportResult {
    /** The http response */
    private final HttpResponse response;

    /**
     * Constructs the response from the HTTP response
     * 
     * @param response a non-null HTTP response
     */
    public TransportResultHttpResponse(final HttpResponse response) {
        Objects.requireNonNull(response, "response cannot be null");
        this.response = response;
    }

    /**
     * Gets the HTTP response
     * 
     * @return the non-null HTTP response
     */
    public HttpResponse getResponse() {
        return response;
    }
}

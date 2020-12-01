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
import org.openhab.binding.sony.internal.scalarweb.models.ScalarWebRequest;

/**
 * This class represents a ScalarWebRequest payload
 * 
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class TransportPayloadScalarWebRequest implements TransportPayload {
    /** The request payload */
    private final ScalarWebRequest request;

    /**
     * Constructs the payload from the request
     * 
     * @param request a non-null request
     */
    public TransportPayloadScalarWebRequest(final ScalarWebRequest request) {
        Objects.requireNonNull(request, "request cannot be null");
        this.request = request;
    }

    /**
     * Gets the request for this payload
     * 
     * @return a non-null request
     */
    public ScalarWebRequest getRequest() {
        return request;
    }
}

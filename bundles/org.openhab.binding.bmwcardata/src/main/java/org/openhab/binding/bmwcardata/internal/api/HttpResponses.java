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
package org.openhab.binding.bmwcardata.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpResponseException;

/**
 * Helps to interpret failed requests of openHAB's common HTTP client.
 *
 * @author Mark Herwege - Initial contribution
 */
@NonNullByDefault
final class HttpResponses {

    private HttpResponses() {
        // utility class
    }

    /**
     * BMW answers 401 without WWW-Authenticate header. The authentication protocol handler of openHAB's common HTTP
     * client turns such a response into a failure, an {@link HttpResponseException} that keeps the status but not the
     * body. A 401 with the header is returned as a normal response, so callers have to check the status as well.
     *
     * @param e failure of a request, or one wrapping it
     * @return whether the request failed because the server answered HTTP 401
     */
    static boolean isUnauthorizedResponse(@Nullable Throwable e) {
        for (Throwable cause = e; cause != null; cause = cause.getCause()) {
            if (cause instanceof HttpResponseException responseException
                    && responseException.getResponse().getStatus() == 401) {
                return true;
            }
        }
        return false;
    }
}

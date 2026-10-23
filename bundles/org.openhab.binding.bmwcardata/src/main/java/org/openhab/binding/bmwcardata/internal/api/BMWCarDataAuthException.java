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
 * The {@link BMWCarDataAuthException} signals that the stored authorization is no longer accepted and the user has
 * to authorize the binding again.
 *
 * @author Michael Weger - Initial contribution
 */
@NonNullByDefault
public class BMWCarDataAuthException extends BMWCarDataException {

    private static final long serialVersionUID = 1L;

    public BMWCarDataAuthException(String message, int httpStatus) {
        super(message, httpStatus);
    }

    /**
     * BMW answers 401 without WWW-Authenticate header. The authentication protocol handler of openHAB's common HTTP
     * client turns such a response into a failure, an {@link HttpResponseException} that keeps the status but not the
     * body.
     *
     * @param e failure of a request, or one wrapping it
     * @return whether the request failed because the server answered HTTP 401
     */
    public static boolean isUnauthorizedResponse(@Nullable Throwable e) {
        for (Throwable cause = e; cause != null; cause = cause.getCause()) {
            if (cause instanceof HttpResponseException responseException
                    && responseException.getResponse().getStatus() == 401) {
                return true;
            }
        }
        return false;
    }
}

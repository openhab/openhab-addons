/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.ecovacs.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.api.Response;

/**
 * @author Danny Baumann - Initial contribution
 */
@NonNullByDefault
public class EcovacsApiException extends Exception {
    private static final long serialVersionUID = -5903398729974682356L;
    public final boolean isAuthFailure;

    public EcovacsApiException(String reason) {
        this(reason, false);
    }

    public EcovacsApiException(String reason, boolean isAuthFailure) {
        super(reason);
        this.isAuthFailure = isAuthFailure;
    }

    public EcovacsApiException(Response response) {
        super("HTTP status " + response.getStatus());
        isAuthFailure = response.getStatus() == 401;
    }

    public EcovacsApiException(Throwable cause) {
        this(cause, false);
    }

    public EcovacsApiException(Throwable cause, boolean isAuthFailure) {
        super(cause);
        this.isAuthFailure = isAuthFailure;
    }
}

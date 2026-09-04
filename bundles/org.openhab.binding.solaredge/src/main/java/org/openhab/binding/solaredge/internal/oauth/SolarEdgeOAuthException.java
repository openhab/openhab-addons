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
package org.openhab.binding.solaredge.internal.oauth;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Indicates that SolarEdge OAuth authorization or token refresh failed.
 *
 * @author Ronny Grun - Initial contribution
 */
@NonNullByDefault
public class SolarEdgeOAuthException extends Exception {
    private static final long serialVersionUID = 1L;
    private final boolean authorizationRequired;

    public SolarEdgeOAuthException(String message) {
        this(message, false);
    }

    public SolarEdgeOAuthException(String message, boolean authorizationRequired) {
        super(message);
        this.authorizationRequired = authorizationRequired;
    }

    public SolarEdgeOAuthException(String message, Throwable cause) {
        super(message, cause);
        authorizationRequired = false;
    }

    public boolean isAuthorizationRequired() {
        return authorizationRequired;
    }
}

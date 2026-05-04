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
package org.openhab.binding.unifi.access.internal.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.unifi.api.UniFiException;

/**
 * Exception for UniFi Access API errors. Extends the shared {@link UniFiException} so the
 * {@link UniFiException.AuthState} classification is consistent across Access, Protect, and Network.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class UnifiAccessApiException extends UniFiException {
    private static final long serialVersionUID = 1L;

    public UnifiAccessApiException(String message) {
        super(message);
    }

    public UnifiAccessApiException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnifiAccessApiException(String message, AuthState authState) {
        super(message, authState);
    }
}

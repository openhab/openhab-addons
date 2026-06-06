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
package org.openhab.binding.unifiprotect.internal.api.priv.exception;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Base exception for all UniFi Protect API errors
 * Extends RuntimeException for better usability with CompletableFuture
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class UniFiProtectException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public UniFiProtectException(String message) {
        super(message);
    }

    public UniFiProtectException(String message, Throwable cause) {
        super(message, cause);
    }

    public UniFiProtectException(Throwable cause) {
        super(cause);
    }
}

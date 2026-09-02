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
package org.openhab.binding.mideaac.internal.connection.exception;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link MideaAuthenticationException} represents a binding
 * Authentication specific {@link Exception}.
 *
 * @author Leo Siepel - Initial contribution
 */

@NonNullByDefault
public class MideaAuthenticationException extends Exception {

    private static final long serialVersionUID = 1L;

    public MideaAuthenticationException(String message) {
        super(message);
    }

    public MideaAuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }

    public MideaAuthenticationException(Throwable cause) {
        super(cause);
    }
}

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
package org.openhab.binding.unifi.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Base exception thrown by the shared UniFi parent binding for errors talking to a UniFi console.
 * <p>
 * Subclasses (e.g. authentication failure, expired session, communication error) are defined in the internal
 * implementation and surface through this type so child bindings only need to handle a single exception class.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class UniFiException extends Exception {

    private static final long serialVersionUID = 1L;

    public UniFiException(String message) {
        super(message);
    }

    public UniFiException(String message, Throwable cause) {
        super(message, cause);
    }

    public UniFiException(Throwable cause) {
        super(cause);
    }
}

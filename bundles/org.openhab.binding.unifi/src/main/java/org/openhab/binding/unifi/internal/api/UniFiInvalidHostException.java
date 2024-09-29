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
package org.openhab.binding.unifi.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link UniFiInvalidHostException} signals there was a problem with the hostname of the controller.
 *
 * @author Matthew Bowman - Initial contribution
 */
@NonNullByDefault
public class UniFiInvalidHostException extends UniFiException {

    private static final long serialVersionUID = -7261308872245069364L;

    public UniFiInvalidHostException(final String message) {
        super(message);
    }

    public UniFiInvalidHostException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public UniFiInvalidHostException(final Throwable cause) {
        super(cause);
    }
}

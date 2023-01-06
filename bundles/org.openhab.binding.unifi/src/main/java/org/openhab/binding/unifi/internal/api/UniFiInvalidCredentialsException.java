/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
 * The {@link UniFiInvalidCredentialsException} signals the credentials used to authenticate with the controller are
 * invalid.
 *
 * @author Matthew Bowman - Initial contribution
 */
@NonNullByDefault
public class UniFiInvalidCredentialsException extends UniFiException {

    private static final long serialVersionUID = -7159360851783088458L;

    public UniFiInvalidCredentialsException(final String message) {
        super(message);
    }
}

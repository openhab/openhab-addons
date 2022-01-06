/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

/**
 * The {@link UniFiNotAuthorizedException} signals the controller denied a request due to non-admin credentials.
 *
 * @author Matthew Bowman - Initial contribution
 */
public class UniFiNotAuthorizedException extends UniFiException {

    private static final long serialVersionUID = 1379973398415636322L;

    public UniFiNotAuthorizedException(String message) {
        super(message);
    }

    public UniFiNotAuthorizedException(String message, Throwable cause) {
        super(message, cause);
    }

    public UniFiNotAuthorizedException(Throwable cause) {
        super(cause);
    }
}

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
package org.openhab.binding.tidal.internal.api.exception;

/**
 * Tidal exception indicating the access token has expired.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
public class TidalTokenExpiredException extends TidalAuthorizationException {

    private static final long serialVersionUID = 709275673779738436L;

    /**
     * Constructor
     *
     * @param message Tidal error message
     */
    public TidalTokenExpiredException(String message) {
        super(message);
    }
}

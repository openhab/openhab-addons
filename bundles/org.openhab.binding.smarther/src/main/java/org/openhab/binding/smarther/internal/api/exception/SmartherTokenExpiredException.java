/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.smarther.internal.api.exception;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Smarther exception indicating the access token has expired.
 *
 * @author Fabio Possieri - Initial contribution
 */
@NonNullByDefault
public class SmartherTokenExpiredException extends SmartherAuthorizationException {

    private static final long serialVersionUID = 6967072975936269922L;

    /**
     * Constructor
     *
     * @param message BTicino/Legrand API gateway error message
     */
    public SmartherTokenExpiredException(String message) {
        super(message);
    }

}

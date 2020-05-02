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
 * Generic BTicino/Legrand API gateway exception class.
 *
 * @author Fabio Possieri - Initial contribution
 */
@NonNullByDefault
public class SmartherGatewayException extends RuntimeException {

    private static final long serialVersionUID = -3614645621941830547L;

    /**
     * Constructor.
     *
     * @param message BTicino/Legrand API gateway error message
     */
    public SmartherGatewayException(String message) {
        super(message);
    }

    /**
     * Constructor.
     *
     * @param message BTicino/Legrand API gateway error message
     * @param cause Original cause of this exception
     */
    public SmartherGatewayException(String message, Throwable cause) {
        super(message, cause);
    }

}

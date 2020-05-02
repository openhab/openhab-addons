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
 * BTicino/Legrand API gateway authorization problems exception class.
 *
 * @author Fabio Possieri - Initial contribution
 */
@NonNullByDefault
public class SmartherAuthorizationException extends RuntimeException {

    private static final long serialVersionUID = 6206604010680564607L;

    /**
     * Constructor.
     *
     * @param message BTicino/Legrand API gateway error message
     */
    public SmartherAuthorizationException(String message) {
        super(message);
    }

    /**
     * Constructor.
     *
     * @param message BTicino/Legrand API gateway error message
     * @param exception Original cause of this exception
     */
    public SmartherAuthorizationException(String message, Throwable exception) {
        super(message, exception);
    }

}

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
 * Smarther exception indicating that an invalid property value has been received from API Gateway.
 *
 * @author Fabio Possieri - Initial contribution
 */
@NonNullByDefault
public class SmartherInvalidPropertyValueException extends SmartherGatewayException {

    private static final long serialVersionUID = -2549779559688846805L;

    /**
     * Constructor.
     *
     * @param message Specific error message
     */
    public SmartherInvalidPropertyValueException(String message) {
        super(message);
    }

    /**
     * Constructor.
     *
     * @param message Specific error message
     */
    public SmartherInvalidPropertyValueException(String propertyName, String invalidValue) {
        super(String.format("Invalid value '%s' received for enum '%s'", invalidValue, propertyName));
    }

    /**
     * Constructor.
     *
     * @param message Specific error message
     * @param cause Original cause of this exception
     */
    public SmartherInvalidPropertyValueException(String message, Throwable cause) {
        super(message, cause);
    }

}

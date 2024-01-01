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
package org.openhab.binding.boschindego.internal.exceptions;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * {@link IndegoInvalidResponseException} is thrown in case of invalid response from the
 * Bosch Indego service.
 * 
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class IndegoInvalidResponseException extends IndegoException {

    private static final long serialVersionUID = -4236849226899489934L;
    private int httpStatusCode = 0;

    public IndegoInvalidResponseException(String message, int httpStatusCode) {
        super(message);
        this.httpStatusCode = httpStatusCode;
    }

    public IndegoInvalidResponseException(String message, Throwable cause, int httpStatusCode) {
        super(message, cause);
        this.httpStatusCode = httpStatusCode;
    }

    public int getHttpStatusCode() {
        return httpStatusCode;
    }
}

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
package org.openhab.binding.bluetooth.airthings.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Exception for data parsing errors.
 *
 * @author Pauli Anttila - Initial contribution
 */
@NonNullByDefault
public class AirthingsParserException extends Exception {

    private static final long serialVersionUID = 1;

    public AirthingsParserException() {
    }

    public AirthingsParserException(String message) {
        super(message);
    }

    public AirthingsParserException(String message, Throwable cause) {
        super(message, cause);
    }

    public AirthingsParserException(Throwable cause) {
        super(cause);
    }
}

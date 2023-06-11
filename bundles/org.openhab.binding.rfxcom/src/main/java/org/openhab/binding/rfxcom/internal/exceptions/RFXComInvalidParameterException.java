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
package org.openhab.binding.rfxcom.internal.exceptions;

/**
 * Exception for when RFXCOM messages have a value that we don't understand.
 *
 * @author James Hewitt-Thomas - Initial contribution
 */
public class RFXComInvalidParameterException extends RFXComException {

    private static final long serialVersionUID = -2778120072474013560L;

    public RFXComInvalidParameterException(String parameter, String value) {
        super("Invalid value '" + value + "' for parameter " + parameter);
    }

    public RFXComInvalidParameterException(String parameter, String value, String reason) {
        super("Invalid value '" + value + "' for parameter " + parameter + ": " + reason);
    }

    public RFXComInvalidParameterException(String parameter, String value, Throwable cause) {
        super("Invalid value '" + value + "' for parameter " + parameter, cause);
    }

    public RFXComInvalidParameterException(String parameter, String value, String reason, Throwable cause) {
        super("Invalid value '" + value + "' for parameter " + parameter + ": " + reason, cause);
    }
}

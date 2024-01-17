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
package org.openhab.binding.rfxcom.internal.exceptions;

/**
 * Exception for when RFXCOM messages have a value that we don't understand.
 *
 * @author James Hewitt-Thomas - Initial contribution
 */
public class RFXComInvalidStateException extends RFXComException {

    private static final long serialVersionUID = -2770653643474013560L;

    public RFXComInvalidStateException(String channel, String state) {
        super("Invalid state '" + state + "' for parameter " + channel);
    }

    public RFXComInvalidStateException(String channel, String state, String reason) {
        super("Invalid state '" + state + "' for parameter " + channel + ": " + reason);
    }
}

/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
 * Exception for when RFXCOM messages are too long for the spec.
 *
 * @author James Hewitt-Thomas - Initial contribution
 */
public class RFXComMessageTooLongException extends RFXComException {

    private static final long serialVersionUID = -3352067410289719335L;

    public RFXComMessageTooLongException() {
    }

    public RFXComMessageTooLongException(String message) {
        super(message);
    }

    public RFXComMessageTooLongException(String message, Throwable cause) {
        super(message, cause);
    }

    public RFXComMessageTooLongException(Throwable cause) {
        super(cause);
    }
}

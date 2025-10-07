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
package org.openhab.binding.pioneeravr.internal.protocol.avr;

/**
 * Exception for eISCP errors.
 *
 * Based on the Onkyo binding by Pauli Anttila and others.
 *
 * @author Rainer Ostendorf - Initial contribution
 */
public class AvrConnectionException extends RuntimeException {

    private static final long serialVersionUID = -7970958467980752003L;

    public AvrConnectionException() {
    }

    public AvrConnectionException(String message) {
        super(message);
    }

    public AvrConnectionException(String message, Throwable cause) {
        super(message, cause);
    }

    public AvrConnectionException(Throwable cause) {
        super(cause);
    }
}

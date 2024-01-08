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
package org.openhab.binding.nibeheatpump.internal;

/**
 * The {@link NibeHeatPumpException} define Exceptions for Nibe heat pump errors.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class NibeHeatPumpException extends Exception {

    private static final long serialVersionUID = 8030315127747955747L;

    public NibeHeatPumpException(String message) {
        super(message);
    }

    public NibeHeatPumpException(String message, Throwable cause) {
        super(message, cause);
    }

    public NibeHeatPumpException(Throwable cause) {
        super(cause);
    }
}

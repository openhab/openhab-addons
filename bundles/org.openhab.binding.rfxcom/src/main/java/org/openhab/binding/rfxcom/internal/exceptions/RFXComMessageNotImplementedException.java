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
 * Exception for RFXCOM errors.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class RFXComMessageNotImplementedException extends RFXComException {

    private static final long serialVersionUID = 5958462009164173495L;

    public RFXComMessageNotImplementedException(String message, Throwable cause) {
        super(message, cause);
    }

    public RFXComMessageNotImplementedException(String message) {
        super(message);
    }
}

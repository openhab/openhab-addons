/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

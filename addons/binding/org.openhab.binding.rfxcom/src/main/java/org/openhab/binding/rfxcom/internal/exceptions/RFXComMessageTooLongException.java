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
 * Exception for when RFXCOM messages are too long for the spec.
 *
 * @author James Hewitt-Thomas - Initial contribution
 */
public class RFXComMessageTooLongException extends RFXComException {

    private static final long serialVersionUID = -3352067410289719335L;

    public RFXComMessageTooLongException() {
        super();
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

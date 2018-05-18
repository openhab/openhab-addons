/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.pioneeravr.protocol;

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
        super();
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

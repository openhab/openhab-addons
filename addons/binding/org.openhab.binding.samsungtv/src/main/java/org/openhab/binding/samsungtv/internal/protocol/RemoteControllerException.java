/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.samsungtv.internal.protocol;

/**
 * Exception for Samsung TV communication
 *
 * @author Pauli Anttila - Initial contribution
 */
public class RemoteControllerException extends Exception {

    private static final long serialVersionUID = -5292218577704635666L;

    public RemoteControllerException(String message) {
        super(message);
    }

    public RemoteControllerException(String message, Throwable cause) {
        super(message, cause);
    }

    public RemoteControllerException(Throwable cause) {
        super(cause);
    }

}

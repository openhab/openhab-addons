/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.robonect.internal;

/**
 * This exception is thrown if there was an error in communication with the mower. As a mower is a moving object, this
 * error is kind of expected and the error situation is handled in the handler.
 * 
 * @author Marco Meyer - Initial contribution
 */
public class RobonectCommunicationException extends RuntimeException {

    public RobonectCommunicationException(String message) {
        super(message);
    }

    public RobonectCommunicationException(String message, Throwable cause) {
        super(message, cause);
    }
}

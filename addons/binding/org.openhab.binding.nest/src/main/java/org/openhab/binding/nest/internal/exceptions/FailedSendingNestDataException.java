/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nest.internal.exceptions;

/**
 * Will be thrown when the bridge was unable to send data.
 *
 * @author Wouter Born - Improve exception handling while sending data
 */
public class FailedSendingNestDataException extends Exception {
    public FailedSendingNestDataException(String message) {
        super(message);
    }

    public FailedSendingNestDataException(String message, Throwable cause) {
        super(message, cause);
    }

    public FailedSendingNestDataException(Throwable cause) {
        super(cause);
    }
}

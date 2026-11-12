/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.robonect.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * This exception is thrown if there was an error in communication with the mower. As a mower is a moving object, this
 * error is kind of expected and the error situation is handled in the handler.
 * 
 * @author Marco Meyer - Initial contribution
 */
@NonNullByDefault
public class RobonectCommunicationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a communication failure.
     *
     * @param message the failure description
     */
    public RobonectCommunicationException(String message) {
        super(message);
    }

    /**
     * Creates a communication failure with its underlying cause.
     *
     * @param message the failure description
     * @param cause the underlying cause
     */
    public RobonectCommunicationException(String message, Throwable cause) {
        super(message, cause);
    }
}

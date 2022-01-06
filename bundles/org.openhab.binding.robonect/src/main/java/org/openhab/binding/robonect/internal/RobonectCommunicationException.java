/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

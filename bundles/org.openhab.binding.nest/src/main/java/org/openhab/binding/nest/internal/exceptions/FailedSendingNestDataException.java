/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.nest.internal.exceptions;

/**
 * Will be thrown when the bridge was unable to send data.
 *
 * @author Wouter Born - Initial contribution
 * @author Wouter Born - Improve exception handling while sending data
 */
@SuppressWarnings("serial")
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

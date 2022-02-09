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
package org.openhab.binding.smsmodem.internal.smslib.modem.driver;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 *
 * Wrapper for communication exception
 *
 * @author Gwendal ROULLEAU - Initial contribution
 */
@NonNullByDefault
public class CommunicationException extends Exception {

    private static final long serialVersionUID = -5175636461754717860L;

    public CommunicationException(String message, Exception cause) {
        super(message, cause);
    }

    public CommunicationException(String message) {
        super(message);
    }
}

/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.silvercrestwifisocket.internal.exceptions;

/**
 * Exception throwed when some packet is not one response packet.
 *
 * @author Jaime Vaz - Initial contribution
 *
 */
public class NotOneResponsePacketException extends Exception {

    private static final long serialVersionUID = -8531181654734497851L;

    /**
     * Default constructor.
     *
     * @param message the error message
     */
    public NotOneResponsePacketException(final String message) {
        super(message);
    }
}

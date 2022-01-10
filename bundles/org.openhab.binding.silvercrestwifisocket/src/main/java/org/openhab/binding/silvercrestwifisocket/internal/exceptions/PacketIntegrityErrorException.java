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
package org.openhab.binding.silvercrestwifisocket.internal.exceptions;

/**
 * Exception throwed when some packet has one integrity error.
 *
 * @author Jaime Vaz - Initial contribution
 *
 */
public class PacketIntegrityErrorException extends Exception {

    private static final long serialVersionUID = -8531181654734497851L;

    /**
     * Default constructor.
     *
     * @param message the error message
     */
    public PacketIntegrityErrorException(final String message) {
        super(message);
    }
}

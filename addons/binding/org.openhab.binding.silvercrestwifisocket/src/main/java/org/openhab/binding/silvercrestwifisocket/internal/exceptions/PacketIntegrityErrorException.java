/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

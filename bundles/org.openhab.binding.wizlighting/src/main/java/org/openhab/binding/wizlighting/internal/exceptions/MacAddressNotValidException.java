/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.wizlighting.internal.exceptions;

/**
 * Exception thrown when Mac address is not valid.
 *
 * @author Sriram Balakrishnan - Initial contribution
 *
 */
public class MacAddressNotValidException extends Exception {

    private final String macAddress;

    /**
     * Default constructor.
     *
     * @param message the error message
     * @param macAddress the wrong mac address.
     */
    public MacAddressNotValidException(final String message, final String macAddress) {
        super(message);
        this.macAddress = macAddress;
    }

    public String getMacAddress() {
        return this.macAddress;
    }
}
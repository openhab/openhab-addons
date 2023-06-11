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
 * Exception throwed when one Mac address is not valid.
 *
 * @author Jaime Vaz - Initial contribution
 *
 */
public class MacAddressNotValidException extends Exception {

    private static final long serialVersionUID = 6131138252323778017L;
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

    // SETTERS AND GETTERS

    public String getMacAddress() {
        return this.macAddress;
    }
}

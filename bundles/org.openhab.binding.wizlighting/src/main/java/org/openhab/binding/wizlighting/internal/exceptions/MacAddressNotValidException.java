/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.wizlighting.internal.exceptions;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Exception thrown when Mac address is not valid.
 *
 * @author Sriram Balakrishnan - Initial contribution
 *
 */
@NonNullByDefault
public class MacAddressNotValidException extends Exception {
    private static final long serialVersionUID = 1L;

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

    public String getBulbMacAddress() {
        return this.macAddress;
    }
}

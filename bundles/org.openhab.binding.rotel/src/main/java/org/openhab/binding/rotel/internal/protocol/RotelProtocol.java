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
package org.openhab.binding.rotel.internal.protocol;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.rotel.internal.RotelException;

/**
 * Represents the different kinds of protocols
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
public enum RotelProtocol {

    HEX("HEX"),
    ASCII_V1("ASCII_V1"),
    ASCII_V2("ASCII_V2");

    private String name;

    /**
     * Constructor
     *
     * @param name the protocol name
     */
    private RotelProtocol(String name) {
        this.name = name;
    }

    /**
     * Get the protocol name
     *
     * @return the protocol name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the protocol associated to a name
     *
     * @param name the name used to identify the protocol
     *
     * @return the protocol associated to the searched name
     *
     * @throws RotelException - If no protocol is associated to the searched name
     */
    public static RotelProtocol getFromName(String name) throws RotelException {
        for (RotelProtocol value : RotelProtocol.values()) {
            if (value.getName().equals(name)) {
                return value;
            }
        }
        throw new RotelException("Invalid protocol name: " + name);
    }
}

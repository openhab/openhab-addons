/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.rotel.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Represents the different repeat modes
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
public enum RotelRepeatMode {
    TRACK,
    DISC,
    OFF;

    /**
     * Get the repeat mode from its name
     *
     * @param name the searched name (case is ignored)
     *
     * @return the repeat mode associated to the searched name
     *
     * @throws RotelException - If no repeat mode is associated to the searched name
     */
    public static RotelRepeatMode getFromName(String name) throws RotelException {
        for (RotelRepeatMode value : RotelRepeatMode.values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        throw new RotelException("Invalid name for a repeat mode: " + name);
    }
}

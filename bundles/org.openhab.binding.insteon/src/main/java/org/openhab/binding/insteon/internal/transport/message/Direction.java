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
package org.openhab.binding.insteon.internal.transport.message;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link Direction} represents a message direction
 *
 * @author Jeremy Setton - Initial contribution
 */
@NonNullByDefault
public enum Direction {
    FROM_MODEM("IN"),
    TO_MODEM("OUT");

    private final String label;

    private Direction(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }

    public static @Nullable Direction get(String value) {
        try {
            return Direction.valueOf(value);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}

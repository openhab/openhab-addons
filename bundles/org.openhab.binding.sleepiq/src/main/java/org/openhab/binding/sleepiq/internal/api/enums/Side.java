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
package org.openhab.binding.sleepiq.internal.api.enums;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link Side} represents the possible sides of the bed (i.e. left and right).
 *
 * @author Mark Hilbush - Initial contribution
 */
@NonNullByDefault
public enum Side {
    LEFT(0),
    RIGHT(1);

    private final int side;

    Side(final int side) {
        this.side = side;
    }

    public int value() {
        return side;
    }

    public static Side forValue(int value) {
        for (Side s : Side.values()) {
            if (s.side == value) {
                return s;
            }
        }
        throw new IllegalArgumentException("Invalid side: " + value);
    }

    public static Side convertFromGroup(@Nullable String channelGroup) {
        return "left".equalsIgnoreCase(channelGroup) ? Side.LEFT : Side.RIGHT;
    }

    @Override
    public String toString() {
        return side == 0 ? LEFT.name() : RIGHT.name();
    }
}

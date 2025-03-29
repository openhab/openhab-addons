/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.danfossairunit.internal.protocol;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * This represents bitwise flags for read and write permissions.
 *
 * <p>
 * These flags use bitwise operations to support combinations:
 * </p>
 * <ul>
 * <li>{@code WRITE} (0x02) - Represents write access.</li>
 * <li>{@code READ} (0x04) - Represents read access.</li>
 * <li>{@code READ_WRITE} (0x06) - Combination of read and write ({@code READ | WRITE}).</li>
 * </ul>
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public enum Flag {
    WRITE((byte) 0x02),
    READ((byte) 0x04),
    READ_WRITE((byte) (READ.value | WRITE.value));

    private final byte value;

    Flag(byte value) {
        this.value = value;
    }

    /**
     * Returns the byte value representing this flag.
     *
     * @return the byte value of the flag
     */
    public byte getValue() {
        return value;
    }
}

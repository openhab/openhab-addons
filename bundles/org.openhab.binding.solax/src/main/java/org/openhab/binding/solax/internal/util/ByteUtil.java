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
package org.openhab.binding.solax.internal.util;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link ByteUtil} Utility method for manipulating byte-level data
 *
 * @author Konstantin Polihronov - Initial contribution
 */
@NonNullByDefault
public class ByteUtil {

    public static final int MASK = 0xFFFF;
    public static final int SHIFT_VALUE = 16;

    public static int read32BitSigned(short low, short high) {
        return (high << SHIFT_VALUE) | (low & MASK);
    }
}

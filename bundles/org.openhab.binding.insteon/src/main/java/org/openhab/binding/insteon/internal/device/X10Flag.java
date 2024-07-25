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
package org.openhab.binding.insteon.internal.device;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link X10Flag} represents an X10 flag
 *
 * @author Jeremy Setton - Initial contribution
 */
@NonNullByDefault
public enum X10Flag {
    ADDRESS(0x00),
    COMMAND(0x80);

    private final byte code;

    private X10Flag(int code) {
        this.code = (byte) code;
    }

    public byte code() {
        return code;
    }
}

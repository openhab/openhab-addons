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
package org.openhab.binding.bluetooth.grundfosalpha.internal.protocol;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * This defines the start delimiters for different kinds of messages.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public enum MessageStartDelimiter {
    Reply((byte) 0x24),
    Message((byte) 0x26),
    Request((byte) 0x27),
    Echo((byte) 0x30);

    private final byte value;

    MessageStartDelimiter(byte value) {
        this.value = value;
    }

    public byte value() {
        return value;
    }
}

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
package org.openhab.binding.onewire.internal.owserver;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link OwserverMessageType} provides the owserver protocol message type
 *
 * @author Jan N. Klug - Initial contribution
 */

@NonNullByDefault
public enum OwserverMessageType {
    ERROR(0x00000000),
    NOP(0x00000001),
    READ(0x00000002),
    WRITE(0x00000003),
    DIR(0x00000004),
    SIZE(0x00000005),
    PRESENT(0x00000006),
    DIRALL(0x00000007),
    GET(0x00000008),
    DIRALLSLASH(0x00000009),
    GETSLASH(0x0000000a);

    private final int messageType;

    OwserverMessageType(int messageType) {
        this.messageType = messageType;
    }

    /**
     * get the this message type's numeric representation
     *
     * @return integer value of this message type
     */
    public int getValue() {
        return messageType;
    }

    /**
     * return a new OwMessageType from an integer
     *
     * @param messageType the message type as integer
     * @return OwMessageType
     */
    public static OwserverMessageType fromInt(int messageType) throws IllegalArgumentException {
        for (OwserverMessageType value : values()) {
            if (value.getValue() == messageType) {
                return value;
            }
        }
        throw new IllegalArgumentException();
    }
}

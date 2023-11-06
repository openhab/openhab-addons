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
package org.openhab.binding.paradoxalarm.internal.communication.messages;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * From Jean's excel:
 * 0x03: IP Request
 * 0x01: IP Response
 * 0x04: Serial/pass through command request
 * 0x02: Serial/pass through command response
 *
 * @author Konstantin Polihronov - Initial contribution
 */
@NonNullByDefault
public enum HeaderMessageType {
    IP_REQUEST((byte) 0x03),
    IP_RESPONSE((byte) 0x01),
    SERIAL_PASSTHRU_REQUEST((byte) 0x04),
    SERIAL_PASSTHRU_RESPONSE((byte) 0x02);

    private byte value;

    HeaderMessageType(byte value) {
        this.value = value;
    }

    public byte getValue() {
        return value;
    }
}

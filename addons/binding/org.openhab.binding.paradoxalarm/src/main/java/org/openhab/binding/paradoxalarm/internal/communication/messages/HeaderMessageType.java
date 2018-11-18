/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.paradoxalarm.internal.communication.messages;

/**
 * From Jean's excel:
 * 0x03: IP Request
 * 0x01: IP Response
 * 0x04: Serial/pass through command request
 * 0x02: Serial/pass through command response
 *
 * @author Konstantin_Polihronov - Initial contribution
 */

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

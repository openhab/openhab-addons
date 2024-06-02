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
package org.openhab.binding.bluetooth.govee.internal.command.hygrometer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bluetooth.gattserial.GattMessage;

/**
 * @author Connor Petty - Initial contribution
 *
 */
@NonNullByDefault
public class GoveeMessage implements GattMessage {

    private byte[] payload;

    public GoveeMessage(byte[] payload) {
        this.payload = payload;
    }

    public GoveeMessage(byte commandType, byte commandCode, byte @Nullable [] data) {
        payload = new byte[20];
        payload[0] = commandType;
        payload[1] = commandCode;
        if (data != null) {
            System.arraycopy(data, 0, payload, 2, data.length);
        }
        payload[19] = calculateCrc(payload, 19);
    }

    public byte getCommandType() {
        return payload[0];
    }

    public byte getCommandCode() {
        return payload[1];
    }

    protected static byte calculateCrc(byte[] bArr, int i) {
        byte b = bArr[0];
        for (int i2 = 1; i2 < i; i2++) {
            b = (byte) (b ^ bArr[i2]);
        }
        return b;
    }

    public byte @Nullable [] getData() {
        byte[] data = new byte[17];
        System.arraycopy(payload, 2, data, 0, Math.min(payload.length - 2, 17));
        return data;
    }

    @Override
    public byte[] getPayload() {
        return payload;
    }
}

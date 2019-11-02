/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.ism8.server;

import java.nio.ByteBuffer;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link SetDatapointValueMessage} is a message within the KNX frame containing
 * the information of one data point
 *
 * @author Hans-Reiner Hoffmann - Initial contribution
 */
@NonNullByDefault
public class SetDatapointValueMessage {
    private int __Id;
    private byte __Command;
    private byte[] __Data = new byte[0];
    private byte __Length;

    public SetDatapointValueMessage() throws Exception {
    }

    public SetDatapointValueMessage(byte[] data) throws Exception {
        if (data.length < 5) {
            throw new Error("Data size too small for a SetDatapointValueMessage.");
        }

        this.setId(Byte.toUnsignedInt(data[0]) * 256 + Byte.toUnsignedInt(data[1]));
        this.setCommand(data[2]);
        this.setLength(data[3]);
        if (data.length < (this.getLength() + 4)) {
            throw new Error("Data size incorrect (" + data.length + "/" + this.getLength() + ").");
        }

        ByteBuffer list = ByteBuffer.allocate(this.getLength() + 4);
        for (int i = 0; i < this.getLength() + 4; i++) {
            list.put(data[i]);
        }

        this.setData(list.array());
    }

    public int getId() {
        return __Id;
    }

    public void setId(int value) {
        __Id = value;
    }

    public byte getCommand() {
        return __Command;
    }

    public void setCommand(byte value) {
        __Command = value;
    }

    public byte getLength() {
        return __Length;
    }

    public void setLength(byte value) {
        __Length = value;
    }

    public byte[] getData() {
        return __Data;
    }

    public void setData(byte[] value) {
        __Data = value;
    }
}
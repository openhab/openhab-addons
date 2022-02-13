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
    private int id;
    private byte command;
    private byte[] data = new byte[0];
    private byte length;

    public SetDatapointValueMessage() {
    }

    public SetDatapointValueMessage(byte[] data) throws IllegalArgumentException {
        if (data.length < 5) {
            throw new IllegalArgumentException("Data size too small for a SetDatapointValueMessage.");
        }

        this.setId(Byte.toUnsignedInt(data[0]) * 256 + Byte.toUnsignedInt(data[1]));
        this.setCommand(data[2]);
        this.setLength(data[3]);
        if (data.length < (this.getLength() + 4)) {
            throw new IllegalArgumentException("Data size incorrect (" + data.length + "/" + this.getLength() + ").");
        }

        ByteBuffer list = ByteBuffer.allocate(this.getLength() + 4);
        list.put(data, 0, this.getLength() + 4);
        this.setData(list.array());
    }

    /**
     * Gets the ID of the data-point message
     *
     */
    public int getId() {
        return this.id;
    }

    /**
     * Sets the ID of the data-point message
     *
     */
    public void setId(int value) {
        this.id = value;
    }

    /**
     * Gets the command of the data-point message
     *
     */
    public byte getCommand() {
        return this.command;
    }

    /**
     * Sets the command of the data-point message
     *
     */
    public void setCommand(byte value) {
        this.command = value;
    }

    /**
     * Gets the length of the data-point message
     *
     */
    public byte getLength() {
        return this.length;
    }

    /**
     * Sets the length of the data-point message
     *
     */
    public void setLength(byte value) {
        this.length = value;
    }

    /**
     * Gets the data array of the data-point message
     *
     */
    public byte[] getData() {
        return this.data;
    }

    /**
     * Sets the data array of the data-point message
     *
     */
    public void setData(byte[] value) {
        this.data = value;
    }
}

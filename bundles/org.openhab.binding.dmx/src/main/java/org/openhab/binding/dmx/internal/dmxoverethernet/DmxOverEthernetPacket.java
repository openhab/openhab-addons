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
package org.openhab.binding.dmx.internal.dmxoverethernet;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link DmxOverEthernetPacket} is an abstract class for
 * DMX over Ethernet packets (ArtNet, sACN)
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public abstract class DmxOverEthernetPacket {
    protected int universeId;
    protected int payloadSize;
    protected byte[] rawPacket = new byte[0];

    /**
     * set payload size
     *
     * @param payloadSize payload size (number of DMX channels in this packet)
     */
    public abstract void setPayloadSize(int payloadSize);

    /**
     * sets universe, calculates sender name and broadcast-address
     *
     * @param universeId
     */
    public abstract void setUniverse(int universeId);

    /**
     * set sequence number
     *
     * @param sequenceNo sequence number (0-255)
     */
    public abstract void setSequence(int sequenceNo);

    /**
     * set DMX payload data
     *
     * @param payload byte array containing DMX channel data
     */
    public abstract void setPayload(byte[] payload);

    /**
     * set payload data
     *
     * @param payload byte array containing DMX channel data
     * @param payloadSize length of data (no. of channels)
     */
    public abstract void setPayload(byte[] payload, int payloadSize);

    /**
     * get packet for transmission
     *
     * @return byte array with raw packet data
     */
    public byte[] getRawPacket() {
        return rawPacket;
    }

    /**
     * get packet length
     *
     * @return full packet length
     */
    public abstract int getPacketLength();

    /**
     * get universe of this packet
     *
     * @return universe number
     *
     */
    public int getUniverse() {
        return this.universeId;
    }

    /**
     * get payload size
     *
     * @return number of DMX channels in this packet
     */
    public int getPayloadSize() {
        return this.payloadSize;
    }
}

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
package org.openhab.binding.dmx.internal.dmxoverethernet;

import org.openhab.binding.dmx.internal.multiverse.Universe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ArtnetPacket} is a ArtNet packet template
 *
 * @author Jan N. Klug - Initial contribution
 */
public class ArtnetPacket extends DmxOverEthernetPacket {
    public static final int ARTNET_MAX_PACKET_LEN = 530;
    public static final int ARTNET_MAX_PAYLOAD_SIZE = 512;

    private final Logger logger = LoggerFactory.getLogger(ArtnetPacket.class);

    /**
     * default constructor, creates a packet
     *
     */

    public ArtnetPacket() {
        payloadSize = ARTNET_MAX_PAYLOAD_SIZE;
        rawPacket = new byte[ARTNET_MAX_PACKET_LEN];

        /* init Artnet header, total length 38 bytes */
        rawPacket[0] = 0x41; // packet identifier, 8 bytes
        rawPacket[1] = 0x72;
        rawPacket[2] = 0x74;
        rawPacket[3] = 0x2d;
        rawPacket[4] = 0x4e;
        rawPacket[5] = 0x65;
        rawPacket[6] = 0x74;
        rawPacket[7] = 0x00;
        rawPacket[8] = 0x00; // OpCode, 2 bytes
        rawPacket[9] = 0x50;
        rawPacket[10] = 0x00; // protocol version, 2 bytes
        rawPacket[11] = 0x0e;
        rawPacket[12] = 0x00; // sequence number, 1 byte
        rawPacket[13] = 0x00; // physical input
        rawPacket[14] = 0x00; // universe, 15 bit
        rawPacket[15] = 0x00;
        rawPacket[16] = 0x00; // payload size, 2 bytes
        rawPacket[17] = 0x01;
    }

    @Override
    public void setPayloadSize(int payloadSize) {
        if (payloadSize < Universe.MIN_UNIVERSE_SIZE) {
            payloadSize = Universe.MIN_UNIVERSE_SIZE;
            logger.error("payload minimum is {} slots", Universe.MIN_UNIVERSE_SIZE);
        } else if (payloadSize > Universe.MAX_UNIVERSE_SIZE) {
            payloadSize = Universe.MAX_UNIVERSE_SIZE;
            logger.warn("coercing payload size to allowed maximum of {} slots", Universe.MAX_UNIVERSE_SIZE);
        }

        rawPacket[16] = (byte) (payloadSize / 256);
        rawPacket[17] = (byte) (payloadSize % 256);

        this.payloadSize = payloadSize;
    }

    @Override
    public void setUniverse(int universeId) {
        /* observe limits from standard (coerce to range) */
        this.universeId = universeId;

        /* set universe in packet to universe-1 */
        rawPacket[14] = (byte) (this.universeId % 256);
        rawPacket[15] = (byte) (this.universeId / 256);

        logger.trace("set packet universe to {}", this.universeId);
    }

    @Override
    public void setSequence(int sequenceNo) {
        rawPacket[12] = (byte) (sequenceNo % 256);
    }

    @Override
    public void setPayload(byte[] payload) {
        System.arraycopy(payload, 0, rawPacket, 18, payloadSize);
    }

    @Override
    public void setPayload(byte[] payload, int payloadSize) {
        if (payloadSize != this.payloadSize) {
            setPayloadSize(payloadSize);
        }
        setPayload(payload);
    }

    @Override
    public int getPacketLength() {
        return (18 + this.payloadSize);
    }
}

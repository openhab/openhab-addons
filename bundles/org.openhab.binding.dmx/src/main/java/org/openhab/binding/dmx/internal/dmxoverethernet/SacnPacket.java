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

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.dmx.internal.Util;
import org.openhab.binding.dmx.internal.multiverse.Universe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SacnPacket} is responsible for handling commands, which are
 * sent to the bridge.
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class SacnPacket extends DmxOverEthernetPacket {
    public static final int SACN_MAX_PACKET_LEN = 638;
    public static final int SACN_MAX_PAYLOAD_SIZE = 512;

    private final Logger logger = LoggerFactory.getLogger(SacnPacket.class);

    /**
     * default constructor, creates a packet
     *
     * @param uuid UUID is mandatory
     */

    public SacnPacket(UUID uuid) {
        payloadSize = SACN_MAX_PAYLOAD_SIZE;
        rawPacket = new byte[SACN_MAX_PACKET_LEN];

        /* init E1.31 root layer, total length 38 bytes */
        rawPacket[0] = 0x00; // preamble size, 2 bytes
        rawPacket[1] = 0x10;
        rawPacket[2] = 0x00; // postamble size, 2 bytes
        rawPacket[3] = 0x00;
        rawPacket[4] = 0x41; // packet identifier, 12 bytes
        rawPacket[5] = 0x53;
        rawPacket[6] = 0x43;
        rawPacket[7] = 0x2d;
        rawPacket[8] = 0x45;
        rawPacket[9] = 0x31;
        rawPacket[10] = 0x2e;
        rawPacket[11] = 0x31;
        rawPacket[12] = 0x37;
        rawPacket[13] = 0x00;
        rawPacket[14] = 0x00;
        rawPacket[15] = 0x00;
        rawPacket[16] = 0x72; // flags & length, 2 bytes
        rawPacket[17] = 0x6e;
        rawPacket[18] = 0x00; // vector, 4 bytes;
        rawPacket[19] = 0x00;
        rawPacket[20] = 0x00;
        rawPacket[21] = 0x04;

        // UUID 16 bytes
        ByteBuffer uuidBytes = ByteBuffer.wrap(new byte[16]);
        uuidBytes.putLong(uuid.getMostSignificantBits());
        uuidBytes.putLong(uuid.getLeastSignificantBits());
        System.arraycopy(uuidBytes.array(), 0, rawPacket, 22, 16);

        /* init sACN/E1.31 framing layer, total length 77 bytes */
        rawPacket[38] = 0x72; // flags & length, 2 bytes
        rawPacket[39] = 0x58;
        rawPacket[40] = 0x00; // vector, 4 bytes;
        rawPacket[41] = 0x00;
        rawPacket[42] = 0x00;
        rawPacket[43] = 0x02;
        for (int i = 44; i < 108; i++) { // senderName, 64 bytes
            rawPacket[i] = 0x00;
        }
        rawPacket[108] = 0x64; // priority (default 100), 1 byte
        rawPacket[109] = 0x00; // reserved, 2 bytes
        rawPacket[110] = 0x00;
        rawPacket[111] = 0x00; // sequence number, 1 byte
        rawPacket[112] = 0x00; // options, 1 byte
        rawPacket[113] = 0x00; // universe, 2 bytes
        rawPacket[114] = 0x00;

        /* sACN/E1.31 DMP layer, total length 11 + channel count */
        rawPacket[115] = 0x72; // flags & length, 2 bytes
        rawPacket[116] = 0x0b;
        rawPacket[117] = 0x02; // vector, 1 byte
        rawPacket[118] = (byte) 0xa1; // address type, 1 byte
        rawPacket[119] = 0x00; // start address, 2 bytes
        rawPacket[120] = 0x00;
        rawPacket[121] = 0x00; // address increment, 2 bytes
        rawPacket[122] = 0x01;
        rawPacket[123] = 0x02; // payload size, 2 bytes (including start code)
        rawPacket[123] = 0x01;
        rawPacket[125] = 0x00; // DMX start code, 1 byte
    }

    @Override
    public void setPayloadSize(int payloadSize) throws IllegalArgumentException {
        if (payloadSize < Universe.MIN_UNIVERSE_SIZE) {
            throw new IllegalArgumentException(
                    String.format("payload minimum size is %d slots (>%d)", Universe.MIN_UNIVERSE_SIZE, payloadSize));
        } else if (payloadSize > Universe.MAX_UNIVERSE_SIZE) {
            throw new IllegalArgumentException(
                    String.format("payload maximum size is %d slots (<%d)", Universe.MAX_UNIVERSE_SIZE, payloadSize));
        }

        /* root Layer */
        rawPacket[16] = (byte) ((28672 + 110 + payloadSize) / 256);
        rawPacket[17] = (byte) ((28672 + 110 + payloadSize) % 256);

        /* framing layer */
        rawPacket[38] = (byte) ((28672 + 88 + payloadSize) / 256);
        rawPacket[39] = (byte) ((28672 + 88 + payloadSize) % 256);

        /* DMP layer */
        rawPacket[115] = (byte) ((28672 + 11 + payloadSize) / 256);
        rawPacket[116] = (byte) ((28672 + 11 + payloadSize) % 256);
        rawPacket[123] = (byte) ((payloadSize + 1) / 256);
        rawPacket[124] = (byte) ((payloadSize + 1) % 256);

        this.payloadSize = payloadSize;
    }

    @Override
    public void setUniverse(int universeId) {
        this.universeId = universeId;

        /* set universe in packet */
        rawPacket[113] = (byte) (this.universeId / 256);
        rawPacket[114] = (byte) (this.universeId % 256);

        /* set sender name in packet */
        String senderName = new String("openHAB DMX binding (sACN) <" + String.format("%05d", this.universeId) + ">");
        byte[] senderNameBytes = senderName.getBytes(StandardCharsets.UTF_8);
        System.arraycopy(senderNameBytes, 0, rawPacket, 44, senderName.length());

        logger.trace("set packet universe to {}", this.universeId);
    }

    @Override
    public void setSequence(int sequenceNo) {
        rawPacket[111] = (byte) (sequenceNo % 256);
    }

    /**
     * set priority
     *
     * @param priority data priority (for multiple senders), allowed values are 0-200, default 100
     */
    public void setPriority(int priority) {
        /* observe limits (coerce to range) */
        rawPacket[108] = (byte) Util.coerceToRange(priority, 0, 200, logger, "packet priority");
        logger.debug("set packet priority to {}", priority);
    }

    @Override
    public void setPayload(byte[] payload) {
        System.arraycopy(payload, 0, rawPacket, 126, payloadSize);
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
        return (126 + this.payloadSize);
    }
}

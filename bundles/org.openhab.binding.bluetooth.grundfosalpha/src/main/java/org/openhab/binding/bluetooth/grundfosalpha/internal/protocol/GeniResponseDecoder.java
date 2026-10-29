/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.util.HexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Assembles a GENI reply from ordered Bluetooth notifications and decodes supported class-10 objects.
 * The first notification must contain the transport header. Header-like continuation bytes are also considered as
 * possible new replies; length and checksum validation determine which assembly to accept.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class GeniResponseDecoder {

    private final Logger logger = LoggerFactory.getLogger(GeniResponseDecoder.class);

    private static final int CHECKSUM_LENGTH = 2;
    private static final int OBJECT_HEADER_LENGTH = 6;

    private int responseTotalSize;
    private boolean complete;
    private byte[] response = new byte[0];
    private final List<ReplyCandidate> candidates = new ArrayList<>();

    /**
     * Add packet from response payload.
     *
     * @param packet A notification containing the start or continuation of a reply
     * @return true if a complete reply with a valid checksum has been received
     */
    public boolean addPacket(byte[] packet) {
        if (logger.isTraceEnabled()) {
            logger.trace("GENI response: {}", HexUtils.bytesToHex(packet));
        }

        if (packet.length == 0) {
            return false;
        }
        if (complete) {
            reset();
        }
        if (GeniFrameHeader.isInitialResponsePacket(packet)) {
            int totalSize = GeniFrameHeader.getTotalSize(packet);
            if (totalSize >= GeniFrameHeader.LENGTH + CHECKSUM_LENGTH && packet.length <= totalSize) {
                candidates.add(new ReplyCandidate(totalSize));
            }
        }
        if (candidates.isEmpty()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Response bytes {} don't match GENI header", HexUtils.bytesToHex(packet));
            }
            return false;
        }

        // Retain both interpretations of a header-like notification until length or CRC resolves them.
        // Candidates are bounded by the 259-byte frame limit and the four-byte minimum header notification.
        for (Iterator<ReplyCandidate> iterator = candidates.iterator(); iterator.hasNext();) {
            ReplyCandidate candidate = iterator.next();
            if (packet.length > candidate.data.length - candidate.offset) {
                iterator.remove();
                continue;
            }
            System.arraycopy(packet, 0, candidate.data, candidate.offset, packet.length);
            candidate.offset += packet.length;
            if (candidate.offset == candidate.data.length) {
                iterator.remove();
                if (CRC16Calculator.check(candidate.data)) {
                    response = candidate.data;
                    responseTotalSize = response.length;
                    complete = true;
                    candidates.clear();
                    return true;
                } else if (logger.isDebugEnabled()) {
                    logger.debug("CRC16 check failed for {}", HexUtils.bytesToHex(candidate.data));
                }
            }
        }

        return false;
    }

    public Map<GeniMeasurand, BigDecimal> decode() {
        Map<GeniMeasurand, BigDecimal> values = new EnumMap<>(GeniMeasurand.class);
        if (!complete) {
            return values;
        }

        ByteBuffer apdus = ByteBuffer
                .wrap(response, GeniFrameHeader.LENGTH, responseTotalSize - GeniFrameHeader.LENGTH - CHECKSUM_LENGTH)
                .slice().order(ByteOrder.BIG_ENDIAN);
        while (apdus.hasRemaining()) {
            int dataClass = Byte.toUnsignedInt(apdus.get());
            // Skip single-byte extension fields between APDUs.
            if ((dataClass & 0x80) != 0) {
                continue;
            }
            if (!apdus.hasRemaining()) {
                logger.debug("Truncated GENI application header");
                return Map.of();
            }
            int acknowledgementAndLength = Byte.toUnsignedInt(apdus.get());
            int acknowledgement = acknowledgementAndLength >>> 6;
            int payloadLength = acknowledgementAndLength & 0x3f;
            if (payloadLength > apdus.remaining()) {
                logger.debug("GENI application payload exceeds reply length");
                return Map.of();
            }
            ByteBuffer payload = apdus.slice().order(ByteOrder.BIG_ENDIAN);
            payload.limit(payloadLength);
            apdus.position(apdus.position() + payloadLength);
            if (acknowledgement != 0) {
                logger.debug("Ignoring GENI class {} reply with acknowledgement {}", dataClass, acknowledgement);
            } else if (dataClass == GeniReadRequest.DATA_CLASS && !decodeClass10(payload, values)) {
                return Map.of();
            }
        }

        return values;
    }

    private boolean decodeClass10(ByteBuffer payload, Map<GeniMeasurand, BigDecimal> values) {
        if (!payload.hasRemaining()) {
            logger.debug("Missing GENI class-10 object status");
            return false;
        }
        int objectStatus = Byte.toUnsignedInt(payload.get());
        if (objectStatus != 0) {
            logger.debug("Ignoring GENI class-10 object with status {}", objectStatus);
            return true;
        }
        if (payload.remaining() < OBJECT_HEADER_LENGTH) {
            logger.debug("Truncated GENI class-10 object header");
            return false;
        }

        int objectType = Short.toUnsignedInt(payload.getShort());
        int objectVersion = Byte.toUnsignedInt(payload.get());
        int objectLength = (Byte.toUnsignedInt(payload.get()) << 16) | (Byte.toUnsignedInt(payload.get()) << 8)
                | Byte.toUnsignedInt(payload.get());
        // The supported telemetry objects fit in one APDU. Do not decode partial objects or CRC bytes as sensor data.
        if (objectLength != payload.remaining()) {
            logger.debug("GENI class-10 object length {} does not match payload length {}", objectLength,
                    payload.remaining());
            return false;
        }

        int dataOffset = payload.position();
        boolean supportedObject = false;
        for (GeniMeasurand measurand : GeniMeasurand.values()) {
            if (measurand.readRequest().matchesObject(objectType, objectVersion, objectLength)
                    && measurand.offset() <= objectLength - measurand.valueLength()) {
                supportedObject = true;
                double value = measurand.readValue(payload, dataOffset);
                if (measurand.isValid(value)) {
                    values.put(measurand, measurand.scale(new BigDecimal(value)));
                } else {
                    logger.debug("Ignoring invalid {} reading: {}", measurand, value);
                }
            }
        }
        if (!supportedObject) {
            logger.debug("Ignoring unsupported GENI class-10 object: type {}, version {}, length {}", objectType,
                    objectVersion, objectLength);
        }
        return true;
    }

    private void reset() {
        responseTotalSize = 0;
        complete = false;
        candidates.clear();
    }

    private static class ReplyCandidate {
        private final byte[] data;
        private int offset;

        ReplyCandidate(int totalSize) {
            data = new byte[totalSize];
        }
    }
}

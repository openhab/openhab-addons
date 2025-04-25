/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.util.HexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This represents a message received from the Alpha3 pump, made of individual
 * packets received. Packets are expected to be in correct order, otherwise
 * they will be skipped.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class ResponseMessage {

    private final Logger logger = LoggerFactory.getLogger(ResponseMessage.class);

    private static final int GENI_RESPONSE_MAX_SIZE = 259;
    private static final int GENI_RESPONSE_TYPE_LENGTH = 8;

    private int responseTotalSize;
    private int responseOffset;
    private int responseRemaining = Integer.MAX_VALUE;
    private byte[] response = new byte[GENI_RESPONSE_MAX_SIZE];

    /**
     * Add packet from response payload.
     *
     * @param packet
     * @return true if response is now complete
     */
    public boolean addPacket(byte[] packet) {
        if (logger.isTraceEnabled()) {
            logger.trace("GENI response: {}", HexUtils.bytesToHex(packet));
        }

        boolean isFirstPacket = MessageHeader.isInitialResponsePacket(packet);

        if (responseRemaining == Integer.MAX_VALUE) {
            if (!MessageHeader.isInitialResponsePacket(packet)) {
                if (logger.isDebugEnabled()) {
                    byte[] header = new byte[MessageHeader.LENGTH];
                    System.arraycopy(packet, 0, header, 0, MessageHeader.LENGTH);
                    if (logger.isDebugEnabled()) {
                        logger.debug("Response bytes {} don't match GENI header", HexUtils.bytesToHex(header));
                    }
                }
                return false;
            }

            responseTotalSize = MessageHeader.getTotalSize(packet);
            responseOffset = 0;
            responseRemaining = responseTotalSize;
        } else if (isFirstPacket && responseRemaining > 0) {
            logger.debug("Received new first packet while awaiting continuation, resetting");

            responseTotalSize = MessageHeader.getTotalSize(packet);
            responseOffset = 0;
            responseRemaining = responseTotalSize;
        }

        System.arraycopy(packet, 0, response, responseOffset, packet.length);
        responseOffset += packet.length;
        responseRemaining -= packet.length;

        if (responseRemaining < 0) {
            responseRemaining = Integer.MAX_VALUE;
            responseOffset = 0;
            logger.debug("Received too many bytes");
            return false;
        }

        if (responseRemaining == 0) {
            if (!CRC16Calculator.check(response)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("CRC16 check failed for {}", HexUtils.bytesToHex(response));
                }
                responseRemaining = Integer.MAX_VALUE;
                responseOffset = 0;

                return false;
            }
            return true;
        }

        return false;
    }

    public Map<SensorDataType, BigDecimal> decode() {
        HashMap<SensorDataType, BigDecimal> values = new HashMap<>();

        for (SensorDataType dataType : SensorDataType.values()) {
            decode(dataType).ifPresent(value -> values.put(dataType, new BigDecimal(value.getFloat())
                    .multiply(dataType.factor()).setScale(dataType.decimals(), RoundingMode.HALF_UP)));
        }

        return values;
    }

    private Optional<ByteBuffer> decode(SensorDataType dataType) {
        byte[] expectedResponseType = dataType.messageType().responseType();
        byte[] responseType = new byte[GENI_RESPONSE_TYPE_LENGTH];
        System.arraycopy(response, MessageHeader.LENGTH, responseType, 0, GENI_RESPONSE_TYPE_LENGTH);

        if (!Arrays.equals(expectedResponseType, responseType)) {
            return Optional.empty();
        }

        int valueOffset = MessageHeader.LENGTH + GENI_RESPONSE_TYPE_LENGTH + dataType.offset();
        byte[] valueBuffer = Arrays.copyOfRange(response, valueOffset, valueOffset + 4);

        return Optional.of(ByteBuffer.wrap(valueBuffer).order(ByteOrder.BIG_ENDIAN));
    }
}

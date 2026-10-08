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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.openhab.core.util.HexUtils;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

/**
 * Tests for {@link GeniResponseDecoder}.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class GeniResponseDecoderTest {
    private static final String FLOW_REPLY = "2423F8E70A1F000130010000183952A66C468F48AC7FFFFFFF7FFFFFFF41FF21397FFFFFFF44A8";
    private static final String POWER_REPLY = "2430F8E70A2C000100010000254357878B439781803D21B00040F19C0040EA4A404536FDB4FFC00000421C000042040000017317";

    @BeforeEach
    public void setUp() {
        setLogLevel(Level.OFF);
    }

    private static void setLogLevel(Level level) {
        final Logger logger = (Logger) LoggerFactory.getLogger(GeniResponseDecoder.class);
        logger.setLevel(level);
    }

    @Test
    void addPacketFullFlowRateResponseIsParsedWhenValid() {
        byte[] packet1 = HexUtils.hexToBytes("2423F8E70A1F000130010000183952A66C468F48");
        byte[] packet2 = HexUtils.hexToBytes("AC7FFFFFFF7FFFFFFF41FF21397FFFFFFF44A8");
        var decoder = new GeniResponseDecoder();
        boolean isFull;
        isFull = decoder.addPacket(packet1);
        assertThat(isFull, is(false));
        isFull = decoder.addPacket(packet2);
        assertThat(isFull, is(true));
        if (isFull) {
            Map<GeniMeasurand, BigDecimal> values = decoder.decode();
            assertThat(values.entrySet(), hasSize(2));
            assertThat(values, hasEntry(GeniMeasurand.Flow, new BigDecimal("0.723")));
            assertThat(values, hasEntry(GeniMeasurand.Head, new BigDecimal("1.83403")));
        }
    }

    @Test
    void addPacketFullPowerResponseIsParsedWhenValid() {
        byte[] packet1 = HexUtils.hexToBytes("2430F8E70A2C000100010000254357878B439781");
        byte[] packet2 = HexUtils.hexToBytes("803D21B00040F19C0040EA4A404536FDB4FFC000");
        byte[] packet3 = HexUtils.hexToBytes("00421C000042040000017317");
        var decoder = new GeniResponseDecoder();
        boolean isFull;
        isFull = decoder.addPacket(packet1);
        assertThat(isFull, is(false));
        isFull = decoder.addPacket(packet2);
        assertThat(isFull, is(false));
        isFull = decoder.addPacket(packet3);
        assertThat(isFull, is(true));
        if (isFull) {
            Map<GeniMeasurand, BigDecimal> values = decoder.decode();
            assertThat(values.entrySet(), hasSize(3));
            assertThat(values, hasEntry(GeniMeasurand.VoltageAC, new BigDecimal("215.5")));
            assertThat(values, hasEntry(GeniMeasurand.PowerConsumption, new BigDecimal("7.6")));
            assertThat(values, hasEntry(GeniMeasurand.MotorSpeed, new BigDecimal("2928")));
        }
    }

    @Test
    void addPacketFullPowerResponseWhileAwaitingContinuationIsParsedWhenValid() {
        byte[] packet1 = HexUtils.hexToBytes("2430F8E70A2C000100010000254357878B439781");
        byte[] packet2 = HexUtils.hexToBytes("2430F8E70A2C000100010000254357878B439781");
        byte[] packet3 = HexUtils.hexToBytes("803D21B00040F19C0040EA4A404536FDB4FFC000");
        byte[] packet4 = HexUtils.hexToBytes("00421C000042040000017317");
        var decoder = new GeniResponseDecoder();
        boolean isFull;
        isFull = decoder.addPacket(packet1);
        assertThat(isFull, is(false));
        isFull = decoder.addPacket(packet2);
        assertThat(isFull, is(false));
        isFull = decoder.addPacket(packet3);
        assertThat(isFull, is(false));
        isFull = decoder.addPacket(packet4);
        assertThat(isFull, is(true));
        if (isFull) {
            Map<GeniMeasurand, BigDecimal> values = decoder.decode();
            assertThat(values.entrySet(), hasSize(3));
            assertThat(values, hasEntry(GeniMeasurand.VoltageAC, new BigDecimal("215.5")));
            assertThat(values, hasEntry(GeniMeasurand.PowerConsumption, new BigDecimal("7.6")));
            assertThat(values, hasEntry(GeniMeasurand.MotorSpeed, new BigDecimal("2928")));
        }
    }

    @Test
    void addPacketFullPowerResponseAfterOutOfSyncPacketIsParsedWhenValid() {
        byte[] packet1 = HexUtils.hexToBytes("AC7FFFFFFF7FFFFFFF41FF21397FFFFFFF44A8");
        byte[] packet2 = HexUtils.hexToBytes("2430F8E70A2C000100010000254357878B439781");
        byte[] packet3 = HexUtils.hexToBytes("803D21B00040F19C0040EA4A404536FDB4FFC000");
        byte[] packet4 = HexUtils.hexToBytes("00421C000042040000017317");
        var decoder = new GeniResponseDecoder();
        boolean isFull;
        isFull = decoder.addPacket(packet1);
        assertThat(isFull, is(false));
        isFull = decoder.addPacket(packet2);
        assertThat(isFull, is(false));
        isFull = decoder.addPacket(packet3);
        assertThat(isFull, is(false));
        isFull = decoder.addPacket(packet4);
        assertThat(isFull, is(true));
        if (isFull) {
            Map<GeniMeasurand, BigDecimal> values = decoder.decode();
            assertThat(values.entrySet(), hasSize(3));
            assertThat(values, hasEntry(GeniMeasurand.VoltageAC, new BigDecimal("215.5")));
            assertThat(values, hasEntry(GeniMeasurand.PowerConsumption, new BigDecimal("7.6")));
            assertThat(values, hasEntry(GeniMeasurand.MotorSpeed, new BigDecimal("2928")));
        }
    }

    @Test
    void decodeBeforeCompletionReturnsNoValues() {
        var decoder = new GeniResponseDecoder();
        assertThat(decoder.decode(), is(anEmptyMap()));
        assertThat(decoder.addPacket(Arrays.copyOf(HexUtils.hexToBytes(FLOW_REPLY), 25)), is(false));
        assertThat(decoder.decode(), is(anEmptyMap()));
    }

    @Test
    void everySplitAfterTransportHeaderPreservesMeasurements() {
        byte[] response = HexUtils.hexToBytes(FLOW_REPLY);
        for (int split = 4; split < response.length; split++) {
            var decoder = new GeniResponseDecoder();
            assertThat(decoder.addPacket(Arrays.copyOf(response, split)), is(false));
            assertThat(decoder.decode(), is(anEmptyMap()));
            assertThat(decoder.addPacket(Arrays.copyOfRange(response, split, response.length)), is(true));
            assertFlowValues(decoder);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { "", "24", "2423", "2423F8", "00000000", "2423E7F8" })
    void shortAndUnrelatedPacketsAreSafeWithDebugLogging(String packet) {
        setLogLevel(Level.DEBUG);
        try {
            var decoder = new GeniResponseDecoder();
            assertThat(decoder.addPacket(HexUtils.hexToBytes(packet)), is(false));
            assertThat(decoder.decode(), is(anEmptyMap()));
            assertThat(decoder.addPacket(HexUtils.hexToBytes(FLOW_REPLY)), is(true));
            assertFlowValues(decoder);
        } finally {
            setLogLevel(Level.OFF);
        }
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, 1, 2, 16, 48, 255 })
    void headerLikeContinuationPreservesValidReply(int apparentLength) {
        byte[] response = HexUtils.hexToBytes(POWER_REPLY);
        // These bytes belong to members not exposed by the binding and straddle a notification boundary.
        ByteBuffer.wrap(response).putInt(20, 0x2400f8e7 | apparentLength << 16);
        CRC16Calculator.put(response, response.length - 4);
        var decoder = new GeniResponseDecoder();
        assertThat(decoder.addPacket(Arrays.copyOf(response, 20)), is(false));
        assertThat(decoder.addPacket(Arrays.copyOfRange(response, 20, 40)), is(false));
        assertThat(decoder.decode(), is(anEmptyMap()));
        assertThat(decoder.addPacket(Arrays.copyOfRange(response, 40, response.length)), is(true));
        assertThat(decoder.decode(),
                is(Map.of(GeniMeasurand.VoltageAC, new BigDecimal("215.5"), GeniMeasurand.PowerConsumption,
                        new BigDecimal("7.6"), GeniMeasurand.MotorSpeed, new BigDecimal("2928"))));
    }

    @Test
    void shorterNewReplyCanCompleteBeforeAbandonedReply() {
        var decoder = new GeniResponseDecoder();
        assertThat(decoder.addPacket(HexUtils.hexToBytes("24FFF8E7")), is(false));
        byte[] response = HexUtils.hexToBytes(FLOW_REPLY);
        assertThat(decoder.addPacket(Arrays.copyOf(response, 20)), is(false));
        assertThat(decoder.addPacket(Arrays.copyOfRange(response, 20, response.length)), is(true));
        assertFlowValues(decoder);
    }

    @Test
    void invalidCompletedCandidateDoesNotDiscardLongerNewReply() {
        var decoder = new GeniResponseDecoder();
        byte[] abandoned = Arrays.copyOf(HexUtils.hexToBytes(FLOW_REPLY), 19);
        assertThat(decoder.addPacket(abandoned), is(false));
        byte[] response = HexUtils.hexToBytes(POWER_REPLY);
        // The old candidate reaches its advertised length here, but fails its CRC.
        assertThat(decoder.addPacket(Arrays.copyOf(response, 20)), is(false));
        assertThat(decoder.addPacket(Arrays.copyOfRange(response, 20, 40)), is(false));
        assertThat(decoder.addPacket(Arrays.copyOfRange(response, 40, response.length)), is(true));
        assertThat(decoder.decode().size(), is(3));
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, 1 })
    void invalidTransportLengthIsRejectedAndNextReplyIsAccepted(int length) {
        var decoder = new GeniResponseDecoder();
        assertThat(decoder.addPacket(new byte[] { 0x24, (byte) length, (byte) 0xf8, (byte) 0xe7 }), is(false));
        assertThat(decoder.decode(), is(anEmptyMap()));
        assertThat(decoder.addPacket(HexUtils.hexToBytes(FLOW_REPLY)), is(true));
        assertFlowValues(decoder);
    }

    @Test
    void oversizedInitialPacketIsRejectedBeforeCopying() {
        var decoder = new GeniResponseDecoder();
        byte[] oversized = Arrays.copyOf(HexUtils.hexToBytes(FLOW_REPLY), 300);
        assertThat(decoder.addPacket(oversized), is(false));
        assertThat(decoder.decode(), is(anEmptyMap()));
        assertThat(decoder.addPacket(HexUtils.hexToBytes(FLOW_REPLY)), is(true));
        assertFlowValues(decoder);
    }

    @Test
    void oversizedContinuationIsRejectedBeforeCopying() {
        var decoder = new GeniResponseDecoder();
        assertThat(decoder.addPacket(Arrays.copyOf(HexUtils.hexToBytes(FLOW_REPLY), 20)), is(false));
        assertThat(decoder.addPacket(new byte[260]), is(false));
        assertThat(decoder.decode(), is(anEmptyMap()));
        assertThat(decoder.addPacket(HexUtils.hexToBytes(FLOW_REPLY)), is(true));
        assertFlowValues(decoder);
    }

    @Test
    void badChecksumClearsPreviousValuesAndAllowsRecovery() {
        var decoder = new GeniResponseDecoder();
        byte[] response = HexUtils.hexToBytes(FLOW_REPLY);
        assertThat(decoder.addPacket(response), is(true));
        response[response.length - 1] ^= 1;
        assertThat(decoder.addPacket(response), is(false));
        assertThat(decoder.decode(), is(anEmptyMap()));
        assertThat(decoder.addPacket(HexUtils.hexToBytes(FLOW_REPLY)), is(true));
        assertFlowValues(decoder);
    }

    @Test
    void emptyNotificationDoesNotDiscardPartialReply() {
        var decoder = new GeniResponseDecoder();
        byte[] response = HexUtils.hexToBytes(FLOW_REPLY);
        assertThat(decoder.addPacket(Arrays.copyOf(response, 20)), is(false));
        assertThat(decoder.addPacket(new byte[0]), is(false));
        assertThat(decoder.addPacket(Arrays.copyOfRange(response, 20, response.length)), is(true));
        assertFlowValues(decoder);
    }

    @Test
    void completedDecoderCanReceiveAnotherReply() {
        var decoder = new GeniResponseDecoder();
        assertThat(decoder.addPacket(HexUtils.hexToBytes(POWER_REPLY)), is(true));
        assertThat(decoder.decode().size(), is(3));
        assertThat(decoder.addPacket(HexUtils.hexToBytes(FLOW_REPLY)), is(true));
        assertFlowValues(decoder);
    }

    @ParameterizedTest
    @ValueSource(ints = { 127, 128, 255 })
    void unsignedTransportLengthIsSupported(int length) {
        byte[] apdus = new byte[length - 2];
        Arrays.fill(apdus, (byte) 0x80);
        byte[] flow = applicationData(FLOW_REPLY);
        System.arraycopy(flow, 0, apdus, apdus.length - flow.length, flow.length);
        byte[] response = reply(apdus);
        var decoder = new GeniResponseDecoder();
        for (int offset = 0; offset < response.length; offset += 19) {
            int end = Math.min(offset + 19, response.length);
            assertThat(decoder.addPacket(Arrays.copyOfRange(response, offset, end)), is(end == response.length));
        }
        assertFlowValues(decoder);
    }

    @Test
    void multipleApplicationDataUnitsAreDecoded() {
        byte[] flow = applicationData(FLOW_REPLY);
        byte[] power = applicationData(POWER_REPLY);
        // An unrelated class preceding the supported objects must not prevent their decoding.
        byte[] apdus = ByteBuffer.allocate(3 + flow.length + power.length).put(new byte[] { 2, 1, 0 }).put(flow)
                .put(power).array();
        var decoder = new GeniResponseDecoder();
        assertThat(decoder.addPacket(reply(apdus)), is(true));
        Map<GeniMeasurand, BigDecimal> values = decoder.decode();
        assertThat(values.size(), is(5));
        assertThat(values, hasEntry(GeniMeasurand.Flow, new BigDecimal("0.723")));
        assertThat(values, hasEntry(GeniMeasurand.Head, new BigDecimal("1.83403")));
        assertThat(values, hasEntry(GeniMeasurand.VoltageAC, new BigDecimal("215.5")));
        assertThat(values, hasEntry(GeniMeasurand.PowerConsumption, new BigDecimal("7.6")));
        assertThat(values, hasEntry(GeniMeasurand.MotorSpeed, new BigDecimal("2928")));
    }

    @ParameterizedTest
    @ValueSource(ints = { 1, 2, 3 })
    void negativeAcknowledgementDoesNotDecodePayload(int acknowledgement) {
        byte[] apdu = applicationData(FLOW_REPLY);
        apdu[1] |= (byte) (acknowledgement << 6);
        var decoder = new GeniResponseDecoder();
        assertThat(decoder.addPacket(reply(apdu)), is(true));
        assertThat(decoder.decode(), is(anEmptyMap()));
        assertThat(decoder.addPacket(HexUtils.hexToBytes(FLOW_REPLY)), is(true));
        assertFlowValues(decoder);
    }

    @ParameterizedTest
    @ValueSource(strings = { "0A40", "0A80", "0AC0", "0A0101", "0A01FF", "0200" })
    void errorAndUnrelatedRepliesAllowSubsequentTelemetry(String apdu) {
        var decoder = new GeniResponseDecoder();
        byte[] unrelated = HexUtils.hexToBytes(apdu);
        assertThat(decoder.addPacket(reply(unrelated)), is(true));
        assertThat(decoder.decode(), is(anEmptyMap()));
        byte[] flow = applicationData(FLOW_REPLY);
        byte[] combined = ByteBuffer.allocate(unrelated.length + flow.length).put(unrelated).put(flow).array();
        assertThat(decoder.addPacket(reply(combined)), is(true));
        assertFlowValues(decoder);
        assertThat(decoder.addPacket(HexUtils.hexToBytes(FLOW_REPLY)), is(true));
        assertFlowValues(decoder);
    }

    @ParameterizedTest
    @CsvSource({ "0,11", "3,2", "4,49", "5,2", "6,1", "7,1", "8,23", "8,25", "8,255", "1,30", "1,32", "2,1" })
    void unsupportedOrMalformedObjectDoesNotProduceValues(int offset, int value) {
        byte[] apdu = applicationData(FLOW_REPLY);
        apdu[offset] = (byte) value;
        var decoder = new GeniResponseDecoder();
        assertThat(decoder.addPacket(reply(apdu)), is(true));
        assertThat(decoder.decode(), is(anEmptyMap()));
        assertThat(decoder.addPacket(HexUtils.hexToBytes(FLOW_REPLY)), is(true));
        assertFlowValues(decoder);
    }

    @ParameterizedTest
    @ValueSource(strings = { "0A", "0A00", "0A0100", "0A06000130010000", "0A0B000130010000183952A66C" })
    void truncatedApplicationDataDoesNotProduceZeroPaddedValues(String apdu) {
        var decoder = new GeniResponseDecoder();
        assertThat(decoder.addPacket(reply(HexUtils.hexToBytes(apdu))), is(true));
        assertThat(decoder.decode(), is(anEmptyMap()));
    }

    @Test
    void malformedTrailingApduInvalidatesEntireReply() {
        byte[] flow = applicationData(FLOW_REPLY);
        byte[] apdus = Arrays.copyOf(flow, flow.length + 1);
        apdus[flow.length] = 10;
        var decoder = new GeniResponseDecoder();
        assertThat(decoder.addPacket(reply(apdus)), is(true));
        assertThat(decoder.decode(), is(anEmptyMap()));
    }

    @ParameterizedTest
    @ValueSource(ints = { 0x7fffffff, 0x7fc00000, 0xffc00000, 0x7f800000, 0xff800000 })
    void nonFiniteSensorIsSkippedWhileOtherSensorsAreDecoded(int bits) {
        byte[] apdu = applicationData(FLOW_REPLY);
        ByteBuffer.wrap(apdu).putInt(9, bits);
        var decoder = new GeniResponseDecoder();
        assertThat(decoder.addPacket(reply(apdu)), is(true));
        assertThat(decoder.decode(), is(Map.of(GeniMeasurand.Head, new BigDecimal("1.83403"))));
    }

    @Test
    void finiteZeroRemainsAValidMeasurement() {
        byte[] apdu = applicationData(FLOW_REPLY);
        ByteBuffer.wrap(apdu).putFloat(9, 0);
        var decoder = new GeniResponseDecoder();
        assertThat(decoder.addPacket(reply(apdu)), is(true));
        assertThat(decoder.decode(), hasEntry(GeniMeasurand.Flow, new BigDecimal("0.000")));
    }

    private static void assertFlowValues(GeniResponseDecoder decoder) {
        assertThat(decoder.decode(),
                is(Map.of(GeniMeasurand.Flow, new BigDecimal("0.723"), GeniMeasurand.Head, new BigDecimal("1.83403"))));
    }

    private static byte[] applicationData(String capturedReply) {
        byte[] response = HexUtils.hexToBytes(capturedReply);
        return Arrays.copyOfRange(response, 4, response.length - 2);
    }

    private static byte[] reply(byte[] apdus) {
        byte[] response = new byte[apdus.length + 6];
        response[0] = 0x24;
        response[1] = (byte) (apdus.length + 2);
        response[2] = (byte) 0xf8;
        response[3] = (byte) 0xe7;
        System.arraycopy(apdus, 0, response, 4, apdus.length);
        CRC16Calculator.put(response, apdus.length + 2);
        return response;
    }
}

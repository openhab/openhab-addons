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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.math.BigDecimal;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.core.util.HexUtils;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

/**
 * Tests for {@link ResponseMessage}.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class ResponseMessageTest {
    @BeforeEach
    public void setUp() {
        final Logger logger = (Logger) LoggerFactory.getLogger(ResponseMessage.class);
        logger.setLevel(Level.OFF);
    }

    @Test
    void addPacketFullFlowRateResponseIsParsedWhenValid() {
        byte[] packet1 = HexUtils.hexToBytes("2423F8E70A1F000130010000183952A66C468F48");
        byte[] packet2 = HexUtils.hexToBytes("AC7FFFFFFF7FFFFFFF41FF21397FFFFFFF44A8");
        var decoder = new ResponseMessage();
        boolean isFull;
        isFull = decoder.addPacket(packet1);
        assertThat(isFull, is(false));
        isFull = decoder.addPacket(packet2);
        assertThat(isFull, is(true));
        if (isFull) {
            Map<SensorDataType, BigDecimal> values = decoder.decode();
            assertThat(values.entrySet(), hasSize(2));
            assertThat(values, hasEntry(SensorDataType.Flow, new BigDecimal("0.723")));
            assertThat(values, hasEntry(SensorDataType.Head, new BigDecimal("1.83403")));
        }
    }

    @Test
    void addPacketFullPowerResponseIsParsedWhenValid() {
        byte[] packet1 = HexUtils.hexToBytes("2430F8E70A2C000100010000254357878B439781");
        byte[] packet2 = HexUtils.hexToBytes("803D21B00040F19C0040EA4A404536FDB4FFC000");
        byte[] packet3 = HexUtils.hexToBytes("00421C000042040000017317");
        var decoder = new ResponseMessage();
        boolean isFull;
        isFull = decoder.addPacket(packet1);
        assertThat(isFull, is(false));
        isFull = decoder.addPacket(packet2);
        assertThat(isFull, is(false));
        isFull = decoder.addPacket(packet3);
        assertThat(isFull, is(true));
        if (isFull) {
            Map<SensorDataType, BigDecimal> values = decoder.decode();
            assertThat(values.entrySet(), hasSize(3));
            assertThat(values, hasEntry(SensorDataType.VoltageAC, new BigDecimal("215.5")));
            assertThat(values, hasEntry(SensorDataType.PowerConsumption, new BigDecimal("7.6")));
            assertThat(values, hasEntry(SensorDataType.MotorSpeed, new BigDecimal("2928")));
        }
    }

    @Test
    void addPacketFullPowerResponseWhileAwaitingContinuationIsParsedWhenValid() {
        byte[] packet1 = HexUtils.hexToBytes("2430F8E70A2C000100010000254357878B439781");
        byte[] packet2 = HexUtils.hexToBytes("2430F8E70A2C000100010000254357878B439781");
        byte[] packet3 = HexUtils.hexToBytes("803D21B00040F19C0040EA4A404536FDB4FFC000");
        byte[] packet4 = HexUtils.hexToBytes("00421C000042040000017317");
        var decoder = new ResponseMessage();
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
            Map<SensorDataType, BigDecimal> values = decoder.decode();
            assertThat(values.entrySet(), hasSize(3));
            assertThat(values, hasEntry(SensorDataType.VoltageAC, new BigDecimal("215.5")));
            assertThat(values, hasEntry(SensorDataType.PowerConsumption, new BigDecimal("7.6")));
            assertThat(values, hasEntry(SensorDataType.MotorSpeed, new BigDecimal("2928")));
        }
    }

    @Test
    void addPacketFullPowerResponseAfterOutOfSyncPacketIsParsedWhenValid() {
        byte[] packet1 = HexUtils.hexToBytes("AC7FFFFFFF7FFFFFFF41FF21397FFFFFFF44A8");
        byte[] packet2 = HexUtils.hexToBytes("2430F8E70A2C000100010000254357878B439781");
        byte[] packet3 = HexUtils.hexToBytes("803D21B00040F19C0040EA4A404536FDB4FFC000");
        byte[] packet4 = HexUtils.hexToBytes("00421C000042040000017317");
        var decoder = new ResponseMessage();
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
            Map<SensorDataType, BigDecimal> values = decoder.decode();
            assertThat(values.entrySet(), hasSize(3));
            assertThat(values, hasEntry(SensorDataType.VoltageAC, new BigDecimal("215.5")));
            assertThat(values, hasEntry(SensorDataType.PowerConsumption, new BigDecimal("7.6")));
            assertThat(values, hasEntry(SensorDataType.MotorSpeed, new BigDecimal("2928")));
        }
    }
}

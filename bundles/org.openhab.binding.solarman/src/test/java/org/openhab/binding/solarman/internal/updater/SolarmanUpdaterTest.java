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
package org.openhab.binding.solarman.internal.updater;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openhab.binding.solarman.internal.DefinitionParser;
import org.openhab.binding.solarman.internal.SolarmanBindingConstants;
import org.openhab.binding.solarman.internal.defmodel.ParameterItem;
import org.openhab.binding.solarman.internal.defmodel.Request;
import org.openhab.binding.solarman.internal.modbus.SolarmanLoggerConnection;
import org.openhab.binding.solarman.internal.modbus.SolarmanLoggerConnector;
import org.openhab.binding.solarman.internal.modbus.SolarmanProtocol;
import org.openhab.binding.solarman.internal.modbus.exception.SolarmanException;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.library.unit.MetricPrefix;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.slf4j.Logger;

/**
 * Covers legacy conversion corrections while retaining configured channel mappings.
 *
 * @author Ricardo Costa - Initial contribution
 */
@NonNullByDefault
class SolarmanUpdaterTest {
    private static final ChannelUID CHANNEL = new ChannelUID(
            new ThingUID(SolarmanBindingConstants.THING_TYPE_SOLARMAN_LOGGER, "test"), "value");
    private final SolarmanLoggerConnector connector = mock(SolarmanLoggerConnector.class);
    private final SolarmanLoggerConnection connection = mock(SolarmanLoggerConnection.class);
    private final SolarmanProtocol protocol = mock(SolarmanProtocol.class);

    private final Map<Integer, byte[]> deviceRegisters = new HashMap<>();

    @BeforeEach
    void setupConnection() throws SolarmanException {
        for (int register = 0; register < 1000; register++) {
            deviceRegisters.put(register, new byte[] { 0, 0 });
        }
        when(connector.createConnection()).thenReturn(connection);
        when(connection.isConnected()).thenReturn(true);
        when(protocol.writeRegisters(any(), anyInt(), any())).thenAnswer(invocation -> {
            int first = invocation.getArgument(1);
            byte[] data = invocation.getArgument(2);
            for (int i = 0; i < data.length / 2; i++) {
                deviceRegisters.put(first + i, Arrays.copyOfRange(data, i * 2, i * 2 + 2));
            }
            return true;
        });
        when(protocol.readRegisters(any(), eq((byte) 3), anyInt(), anyInt())).thenAnswer(invocation -> {
            int first = invocation.getArgument(2);
            int last = invocation.getArgument(3);
            Map<Integer, byte[]> result = new HashMap<>();
            deviceRegisters.forEach((register, value) -> {
                if (register >= first && register <= last) {
                    result.put(register, value);
                }
            });
            return result;
        });
    }

    @Test
    void writesFullWidthLowWordFirstAndConvertsQuantityUnits() throws SolarmanException {
        ParameterItem wide = item(3, List.of(100, 101), "");
        wide.setScale(new BigDecimal("0.1"));
        updater(wide).updateLoggerRegisters(CHANNEL, new DecimalType("30541989.6"));
        verify(protocol).writeRegisters(same(connection), eq(100), ary(new byte[] { 0x56, 0x78, 0x12, 0x34 }));
        clearInvocations(protocol);
        updater(item(1, List.of(100), "W")).updateLoggerRegisters(CHANNEL,
                new QuantityType<>(1, MetricPrefix.KILO(Units.WATT)));
        verify(protocol).writeRegisters(same(connection), eq(100), ary(new byte[] { 3, (byte) 232 }));
    }

    @Test
    void acceptsValidHhmmAndRejectsMalformedTimes() throws SolarmanException {
        SolarmanRegisterUpdater updater = updater(item(9, List.of(100), ""));
        updater.updateLoggerRegisters(CHANNEL, new StringType("12:34"));
        verify(protocol).writeRegisters(same(connection), eq(100), ary(new byte[] { 4, (byte) 210 }));
        clearInvocations(protocol, connector);
        updater.updateLoggerRegisters(CHANNEL, new StringType("24:00"));
        updater.updateLoggerRegisters(CHANNEL, new StringType("12"));
        updater.updateLoggerRegisters(CHANNEL, new StringType("12:99"));
        verifyNoInteractions(protocol, connector);
    }

    @Test
    void rejectsOverflowAndIgnoresRefreshOrUnsupportedTypes() {
        SolarmanRegisterUpdater updater = updater(item(1, List.of(100), ""));
        updater.updateLoggerRegisters(CHANNEL, RefreshType.REFRESH);
        updater.updateLoggerRegisters(CHANNEL, UpDownType.UP);
        assertThrows(IllegalArgumentException.class,
                () -> updater.updateLoggerRegisters(CHANNEL, new DecimalType(65536)));
        assertThrows(IllegalArgumentException.class, () -> updater.updateLoggerRegisters(CHANNEL, new DecimalType(-1)));
        verifyNoInteractions(protocol, connector);
    }

    @Test
    void usesUtf8ByteCapacityWithoutOverwritingNeighborRegisters() throws SolarmanException {
        SolarmanRegisterUpdater updater = updater(item(5, List.of(100), ""));
        updater.updateLoggerRegisters(CHANNEL, new StringType("\u00E9"));
        verify(protocol).writeRegisters(same(connection), eq(100), ary(new byte[] { (byte) 195, (byte) 169 }));
        clearInvocations(protocol, connector);
        assertThrows(IllegalArgumentException.class,
                () -> updater.updateLoggerRegisters(CHANNEL, new StringType("\u20AC")));
        verifyNoInteractions(protocol, connector);
    }

    @Test
    void publishesSignedFullWidthPowerAsQuantityFromOneBatchedRead() throws SolarmanException {
        ParameterItem item = item(4, List.of(100, 101), "W");
        when(protocol.readRegisters(connection, (byte) 3, 100, 101))
                .thenReturn(Map.of(100, new byte[] { (byte) 255, (byte) 255 }, 101, new byte[] { 0, 0 }));
        AtomicReference<State> state = new AtomicReference<>();
        SolarmanChannelUpdater reader = new SolarmanChannelUpdater((uid, value) -> state.set(value));
        assertTrue(reader
                .fetchDataFromLogger(List.of(new Request(3, 100, 101)), connector, protocol, Map.of(item, CHANNEL))
                .hasSuccessfulResponses());
        QuantityType<?> power = assertInstanceOf(QuantityType.class, Objects.requireNonNull(state.get()));
        assertEquals(65535, power.intValue());
        assertEquals(Units.WATT, power.getUnit());
        verify(protocol).readRegisters(connection, (byte) 3, 100, 101);
    }

    @ParameterizedTest
    @ValueSource(ints = { 20, 100 })
    void writesAllSixSocPeriodsInReportedOrderAndRecoversOneIgnoredSoc6Write(int target) throws SolarmanException {
        var definition = new DefinitionParser().parseDefinition("deye_hybrid");
        assertNotNull(definition);
        Map<ParameterItem, ChannelUID> channels = new HashMap<>();
        for (int period = 1; period <= 6; period++) {
            String name = "Time of Use SOC " + period;
            ParameterItem parameter = definition.getParameters().stream().flatMap(group -> group.getItems().stream())
                    .filter(candidate -> candidate.getName().equals(name)).findFirst().orElseThrow();
            assertEquals(List.of(267 + period), parameter.getRegisters());
            channels.put(parameter, new ChannelUID(CHANNEL.getThingUID(), "soc-" + period));
        }
        SolarmanRegisterUpdater writer = new SolarmanRegisterUpdater(channels, connector, protocol);
        int initial = target == 100 ? 20 : 100;
        channels.values().forEach(uid -> writer.updateLoggerRegisters(uid, new QuantityType<>(initial + " %")));
        AtomicReference<Boolean> ignored = new AtomicReference<>(false);
        doAnswer(invocation -> {
            int register = invocation.getArgument(1);
            byte[] data = invocation.getArgument(2);
            if (register == 273 && !ignored.getAndSet(true)) {
                return true;
            }
            deviceRegisters.put(register, data.clone());
            return true;
        }).when(protocol).writeRegisters(any(), anyInt(), any());
        for (int period : List.of(2, 3, 4, 5, 6, 1)) {
            writer.updateLoggerRegisters(new ChannelUID(CHANNEL.getThingUID(), "soc-" + period),
                    new QuantityType<>(target + " %"));
        }
        for (int register = 268; register <= 273; register++) {
            assertArrayEquals(new byte[] { 0, (byte) target }, deviceRegisters.get(register));
        }
        verify(protocol, times(3)).writeRegisters(same(connection), eq(273), any());
    }

    @Test
    void rejectsAcknowledgedButUnchangedOrMissingNumericReadback() throws SolarmanException {
        SolarmanRegisterUpdater writer = updater(item(1, List.of(273), "%"));
        doReturn(Map.of(273, new byte[] { 0, 100 })).when(protocol).readRegisters(any(), eq((byte) 3), eq(273),
                eq(273));
        IllegalStateException failure = assertThrows(IllegalStateException.class,
                () -> writer.updateLoggerRegisters(CHANNEL, new QuantityType<>("20 %")));
        assertTrue(Objects.requireNonNull(failure.getMessage()).contains("0x0111"));
        verify(protocol, times(2)).writeRegisters(same(connection), eq(273), ary(new byte[] { 0, 20 }));
        clearInvocations(protocol);
        doReturn(Map.of(273, new byte[] { 0, 100 }), Map.of()).when(protocol).readRegisters(any(), eq((byte) 3),
                eq(273), eq(273));
        assertThrows(IllegalStateException.class, () -> writer.updateLoggerRegisters(CHANNEL, new DecimalType(20)));
        verify(protocol, times(1)).writeRegisters(any(), eq(273), any());
    }

    @Test
    void doesNotRepeatAWriteAfterPartialOrMalformedMultiRegisterReadback() throws SolarmanException {
        SolarmanRegisterUpdater writer = updater(item(3, List.of(100, 101), ""));
        doReturn(Map.of(100, new byte[] { 0, 0 }, 101, new byte[] { 0, 0 }), Map.of(100, new byte[] { 0, 20 }))
                .when(protocol).readRegisters(any(), eq((byte) 3), eq(100), eq(101));
        assertThrows(IllegalStateException.class, () -> writer.updateLoggerRegisters(CHANNEL, new DecimalType(20)));
        verify(protocol, times(1)).writeRegisters(any(), eq(100), any());
        clearInvocations(protocol);
        doReturn(Map.of(100, new byte[] { 0, 0 }, 101, new byte[] { 0, 0 }),
                Map.of(100, new byte[] { 20 }, 101, new byte[] { 0, 0 })).when(protocol)
                .readRegisters(any(), eq((byte) 3), eq(100), eq(101));
        assertThrows(IllegalStateException.class, () -> writer.updateLoggerRegisters(CHANNEL, new DecimalType(20)));
        verify(protocol, times(1)).writeRegisters(any(), eq(100), any());
    }

    @Test
    void doesNotRetryAnUncertainTransportFailure() throws SolarmanException {
        doThrow(new org.openhab.binding.solarman.internal.modbus.exception.SolarmanConnectionException(
                "Lost acknowledgement")).when(protocol).writeRegisters(any(), anyInt(), any());
        assertThrows(IllegalStateException.class,
                () -> updater(item(1, List.of(273), "%")).updateLoggerRegisters(CHANNEL, new DecimalType(20)));
        verify(protocol, times(1)).writeRegisters(any(), anyInt(), any());
        verify(protocol, times(1)).readRegisters(any(), eq((byte) 3), eq(273), eq(273));
    }

    @Test
    void logsOnlyActualConfirmedChangesAndIncludesRequestedAndObservedValues() throws SolarmanException {
        Logger auditLogger = mock(Logger.class);
        deviceRegisters.put(273, new byte[] { 0, 20 });
        SolarmanRegisterUpdater writer = new SolarmanRegisterUpdater(Map.of(item(1, List.of(273), "%"), CHANNEL),
                connector, protocol, auditLogger);
        writer.updateLoggerRegisters(CHANNEL, new DecimalType(100));
        verify(auditLogger).info(
                "Confirmed register change: channel={}, registers=[{}], previous={}, requested={}, confirmed={}",
                CHANNEL, "0x0111", "20 %", new DecimalType(100), "100 %");
        var order = inOrder(protocol);
        order.verify(protocol).readRegisters(connection, (byte) 3, 273, 273);
        order.verify(protocol).writeRegisters(same(connection), eq(273), any());
        order.verify(protocol).readRegisters(connection, (byte) 3, 273, 273);
        clearInvocations(auditLogger);
        writer.updateLoggerRegisters(CHANNEL, new DecimalType(100));
        verify(auditLogger, never()).info(anyString(), any(Object[].class));
        verify(auditLogger).debug("Confirmed unchanged value: channel={}, registers=[{}], requested={}, confirmed={}",
                CHANNEL, "0x0111", new DecimalType(100), "100 %");
    }

    @Test
    void failedConfirmationNeverProducesSuccessInfo() throws SolarmanException {
        Logger auditLogger = mock(Logger.class);
        doReturn(Map.of(273, new byte[] { 0, 20 })).when(protocol).readRegisters(any(), eq((byte) 3), eq(273), eq(273));
        SolarmanRegisterUpdater writer = new SolarmanRegisterUpdater(Map.of(item(1, List.of(273), "%"), CHANNEL),
                connector, protocol, auditLogger);
        assertThrows(IllegalStateException.class, () -> writer.updateLoggerRegisters(CHANNEL, new DecimalType(100)));
        verify(auditLogger, never()).info(anyString(), any(Object[].class));
        verify(auditLogger).warn(
                "Repeating numeric write after confirmed mismatch: channel={}, registers=[{}], requested={}", CHANNEL,
                "0x0111", new DecimalType(100));
        verify(auditLogger).warn(
                eq("Write or confirmation failed: channel={}, registers=[{}], requested={}, reason={}"), eq(CHANNEL),
                eq("0x0111"), eq(new DecimalType(100)), any());
    }

    @Test
    void unavailableBaselinePreventsAssignmentAndSuccessInfo() throws SolarmanException {
        Logger auditLogger = mock(Logger.class);
        doReturn(Map.of()).when(protocol).readRegisters(any(), eq((byte) 3), eq(273), eq(273));
        SolarmanRegisterUpdater writer = new SolarmanRegisterUpdater(Map.of(item(1, List.of(273), "%"), CHANNEL),
                connector, protocol, auditLogger);
        assertThrows(IllegalStateException.class, () -> writer.updateLoggerRegisters(CHANNEL, new DecimalType(100)));
        verify(protocol, never()).writeRegisters(any(), anyInt(), any());
        verify(auditLogger, never()).info(anyString(), any(Object[].class));
    }

    @Test
    void timeControlLogsTheActualConfirmedTime() {
        Logger auditLogger = mock(Logger.class);
        SolarmanRegisterUpdater writer = new SolarmanRegisterUpdater(Map.of(item(9, List.of(100), ""), CHANNEL),
                connector, protocol, auditLogger);
        writer.updateLoggerRegisters(CHANNEL, new StringType("12:34"));
        verify(auditLogger).info(
                "Confirmed register change: channel={}, registers=[{}], previous={}, requested={}, confirmed={}",
                CHANNEL, "0x0064", "00:00", new StringType("12:34"), "12:34");
    }

    @Test
    void tickingClockDoesNotProduceAnUnverifiedSuccessInfo() throws SolarmanException {
        Logger auditLogger = mock(Logger.class);
        Map<Integer, byte[]> before = Map.of(100, new byte[] { 26, 10 }, 101, new byte[] { 31, 13 }, 102,
                new byte[] { 0, 0 });
        Map<Integer, byte[]> after = Map.of(100, new byte[] { 26, 10 }, 101, new byte[] { 31, 13 }, 102,
                new byte[] { 1, 1 });
        doReturn(before, after).when(protocol).readRegisters(any(), eq((byte) 3), eq(100), eq(102));
        SolarmanRegisterUpdater writer = new SolarmanRegisterUpdater(
                Map.of(item(8, List.of(100, 101, 102), ""), CHANNEL), connector, protocol, auditLogger);
        writer.updateLoggerRegisters(CHANNEL,
                new DateTimeType(LocalDateTime.of(2026, 10, 31, 13, 1).atZone(ZoneId.systemDefault())));
        verify(auditLogger, never()).info(anyString(), any(Object[].class));
        verify(auditLogger, never()).warn(anyString(), any(Object[].class));
        verify(protocol, times(1)).writeRegisters(any(), eq(100), any());
    }

    private SolarmanRegisterUpdater updater(ParameterItem item) {
        return new SolarmanRegisterUpdater(Map.of(item, CHANNEL), connector, protocol);
    }

    private static ParameterItem item(int rule, List<Integer> registers, String unit) {
        ParameterItem item = new ParameterItem();
        item.setName("Test");
        item.setRule(rule);
        item.setRegisters(registers);
        item.setUom(unit);
        item.setIsReadOnly(false);
        return item;
    }

    private static byte[] ary(byte[] expected) {
        return org.mockito.AdditionalMatchers.aryEq(expected);
    }
}

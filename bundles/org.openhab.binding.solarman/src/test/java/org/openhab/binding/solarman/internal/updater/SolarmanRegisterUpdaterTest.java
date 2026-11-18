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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.openhab.binding.solarman.internal.defmodel.Lookup;
import org.openhab.binding.solarman.internal.defmodel.ParameterItem;
import org.openhab.binding.solarman.internal.defmodel.Validation;
import org.openhab.binding.solarman.internal.modbus.SolarmanLoggerConnection;
import org.openhab.binding.solarman.internal.modbus.SolarmanLoggerConnector;
import org.openhab.binding.solarman.internal.modbus.SolarmanProtocol;
import org.openhab.binding.solarman.internal.modbus.exception.SolarmanException;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;

/**
 * Tests for {@link SolarmanRegisterUpdater}.
 *
 * @author Oleksandr Mishchuk - Initial contribution
 */
@NonNullByDefault
class SolarmanRegisterUpdaterTest {
    private static final int TIME_REGISTER = 0x0094;
    private static final ChannelUID TIME_CHANNEL = new ChannelUID("solarman:logger:test:tou:time1");
    private static final int SOC_REGISTER = 0x0102;
    private static final ChannelUID SOC_CHANNEL = new ChannelUID("solarman:logger:test:tou:soc1");
    private static final int UNBOUNDED_REGISTER = 0x00F0;
    private static final ChannelUID UNBOUNDED_CHANNEL = new ChannelUID("solarman:logger:test:static:power");
    private static final int MODE_REGISTER = 0x00AC;
    private static final ChannelUID MODE_CHANNEL = new ChannelUID("solarman:logger:test:tou:mode1");

    private final SolarmanLoggerConnector connector = (@NonNull SolarmanLoggerConnector) mock(
            SolarmanLoggerConnector.class);
    private final SolarmanLoggerConnection connection = (@NonNull SolarmanLoggerConnection) mock(
            SolarmanLoggerConnection.class);
    private final SolarmanProtocol protocol = (@NonNull SolarmanProtocol) mock(SolarmanProtocol.class);

    private SolarmanRegisterUpdater updater = createUpdater();

    @BeforeEach
    void setUp() throws SolarmanException {
        when(connector.createConnection()).thenReturn(connection);
        when(connection.isConnected()).thenReturn(true);
        when(protocol.writeRegisters(any(), anyInt(), any())).thenReturn(true);
        updater = createUpdater();
    }

    @ParameterizedTest
    @CsvSource({ "00:00, 0", "00:01, 1", "09:30, 930", "19:59, 1959", "20:00, 2000", "23:59, 2359" })
    void validTimeIsWritten(String time, int expected) throws SolarmanException {
        updater.updateLoggerRegisters(TIME_CHANNEL, new StringType(time));

        byte[] data = new byte[] { (byte) (expected >> 8), (byte) expected };
        verify(protocol).writeRegisters(eq(connection), eq(TIME_REGISTER), eq(data));
    }

    @ParameterizedTest
    @ValueSource(strings = { "24:00", "9:30", "12:60", "1200", "00", "12:00:00", " 12:00" })
    void invalidTimeIsRejected(String time) throws SolarmanException {
        updater.updateLoggerRegisters(TIME_CHANNEL, new StringType(time));

        verify(protocol, never()).writeRegisters(any(), anyInt(), any());
    }

    @ParameterizedTest
    @ValueSource(strings = { "0", "65", "100" })
    void valueWithinValidationIsWritten(String value) throws SolarmanException {
        updater.updateLoggerRegisters(SOC_CHANNEL, new DecimalType(value));

        verify(protocol).writeRegisters(eq(connection), eq(SOC_REGISTER), eq(toBytes(Integer.parseInt(value))));
    }

    @ParameterizedTest
    @ValueSource(strings = { "-5", "-0.5", "100.5", "101", "65536" })
    void valueOutsideValidationIsRejected(String value) throws SolarmanException {
        updater.updateLoggerRegisters(SOC_CHANNEL, new DecimalType(value));

        verify(protocol, never()).writeRegisters(any(), anyInt(), any());
    }

    @ParameterizedTest
    @ValueSource(ints = { 101, -5 })
    void quantityOutsideValidationIsRejected(int value) throws SolarmanException {
        updater.updateLoggerRegisters(SOC_CHANNEL, new QuantityType<>(value, Units.PERCENT));

        verify(protocol, never()).writeRegisters(any(), anyInt(), any());
    }

    @ParameterizedTest
    @ValueSource(strings = { "0", "5000", "65535" })
    void emptyValidationDoesNotLimitValue(String value) throws SolarmanException {
        updater.updateLoggerRegisters(UNBOUNDED_CHANNEL, new DecimalType(value));

        verify(protocol).writeRegisters(eq(connection), eq(UNBOUNDED_REGISTER), eq(toBytes(Integer.parseInt(value))));
    }

    @ParameterizedTest
    @CsvSource({ "Charge disabled, 0", "Grid charge, 1" })
    void lookupKeyWithinValidationIsWritten(String mode, int key) throws SolarmanException {
        updater.updateLoggerRegisters(MODE_CHANNEL, new StringType(mode));

        verify(protocol).writeRegisters(eq(connection), eq(MODE_REGISTER), eq(toBytes(key)));
    }

    @ParameterizedTest
    @ValueSource(strings = { "Generator charge" })
    void lookupKeyOutsideValidationIsRejected(String mode) throws SolarmanException {
        updater.updateLoggerRegisters(MODE_CHANNEL, new StringType(mode));

        verify(protocol, never()).writeRegisters(any(), anyInt(), any());
    }

    private static byte[] toBytes(int value) {
        return new byte[] { (byte) (value >> 8), (byte) value };
    }

    private SolarmanRegisterUpdater createUpdater() {
        ParameterItem time = createItem("Time 1", 9, TIME_REGISTER, null);
        ParameterItem soc = createItem("SOC 1", 1, SOC_REGISTER, createValidation(0, 100));
        // Static channels are built with an empty Validation, which must not restrict writes
        ParameterItem unbounded = createItem("Power", 1, UNBOUNDED_REGISTER, new Validation());
        ParameterItem mode = createItem("Mode 1", 1, MODE_REGISTER, createValidation(0, 1));
        mode.setLookup(List.of(createLookup(0, "Charge disabled"), createLookup(1, "Grid charge"),
                createLookup(2, "Generator charge")));
        return new SolarmanRegisterUpdater(
                Map.of(time, TIME_CHANNEL, soc, SOC_CHANNEL, unbounded, UNBOUNDED_CHANNEL, mode, MODE_CHANNEL),
                connector, protocol);
    }

    private static ParameterItem createItem(String name, int rule, int register, @Nullable Validation validation) {
        ParameterItem item = new ParameterItem();
        item.setName(name);
        item.setRule(rule);
        item.setIsReadOnly(false);
        item.setRegisters(List.of(register));
        if (validation != null) {
            item.setValidation(validation);
        }
        return item;
    }

    private static Validation createValidation(int min, int max) {
        Validation validation = new Validation();
        validation.setMin(min);
        validation.setMax(max);
        return validation;
    }

    private static Lookup createLookup(int key, String value) {
        Lookup lookup = new Lookup();
        lookup.setKey(key);
        lookup.setValue(value);
        return lookup;
    }
}

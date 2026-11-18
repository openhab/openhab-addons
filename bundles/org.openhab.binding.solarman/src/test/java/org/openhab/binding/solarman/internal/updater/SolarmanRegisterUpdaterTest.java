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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.openhab.binding.solarman.internal.defmodel.ParameterItem;
import org.openhab.binding.solarman.internal.modbus.SolarmanLoggerConnection;
import org.openhab.binding.solarman.internal.modbus.SolarmanLoggerConnector;
import org.openhab.binding.solarman.internal.modbus.SolarmanProtocol;
import org.openhab.binding.solarman.internal.modbus.exception.SolarmanException;
import org.openhab.core.library.types.StringType;
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

    private SolarmanRegisterUpdater createUpdater() {
        ParameterItem item = new ParameterItem();
        item.setName("Time 1");
        item.setRule(9);
        item.setIsReadOnly(false);
        item.setRegisters(List.of(TIME_REGISTER));
        return new SolarmanRegisterUpdater(Map.of(item, TIME_CHANNEL), connector, protocol);
    }
}

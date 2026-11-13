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
package org.openhab.binding.digitalstrom.internal.lib.manager.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openhab.binding.digitalstrom.internal.lib.config.Config;
import org.openhab.binding.digitalstrom.internal.lib.manager.ConnectionManager;
import org.openhab.binding.digitalstrom.internal.lib.manager.SceneManager;
import org.openhab.binding.digitalstrom.internal.lib.manager.StructureManager;
import org.openhab.binding.digitalstrom.internal.lib.serverconnection.DsAPI;
import org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.CachedMeteringValue;
import org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.constants.MeteringTypeEnum;
import org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.constants.MeteringUnitsEnum;

/**
 * Tests aggregation of fractional metering values before integer conversion.
 *
 * @author Leo Siepel - Initial contribution
 */
@NonNullByDefault
class DeviceStatusManagerImplTest {

    @ParameterizedTest
    @MethodSource("meteringValues")
    void sumsBeforeConvertingAndRetainsCachedTotals(double firstValue, double secondValue, int expected) {
        ConnectionManager connection = mock(ConnectionManager.class);
        DsAPI api = mock(DsAPI.class);
        when(connection.getDigitalSTROMAPI()).thenReturn(api);
        when(connection.getConfig()).thenReturn(new Config());
        when(connection.getSessionToken()).thenReturn("session");
        CachedMeteringValue first = mock(CachedMeteringValue.class);
        CachedMeteringValue second = mock(CachedMeteringValue.class);
        when(first.getValue()).thenReturn(firstValue);
        when(second.getValue()).thenReturn(secondValue);
        when(api.getLatest(eq("session"), any(MeteringTypeEnum.class), eq(DsAPI.ALL_METERS),
                nullable(MeteringUnitsEnum.class))).thenReturn(List.of(first, second))
                .thenReturn(List.of(first, second)).thenReturn(List.of(first, second)).thenReturn(null);
        DeviceStatusManagerImpl manager = new DeviceStatusManagerImpl(connection, mock(StructureManager.class),
                mock(SceneManager.class));

        assertEquals(expected, manager.getTotalPowerConsumption());
        assertEquals(expected, manager.getTotalEnergyMeterValue());
        assertEquals(expected, manager.getTotalEnergyMeterWsValue());
        assertEquals(expected, manager.getTotalPowerConsumption());
        assertEquals(expected, manager.getTotalEnergyMeterValue());
        assertEquals(expected, manager.getTotalEnergyMeterWsValue());
    }

    private static Stream<Arguments> meteringValues() {
        return Stream.of(Arguments.of(0.75, 0.75, 1), Arguments.of(1.75, 2.75, 4), Arguments.of(1.0, 2.0, 3),
                Arguments.of(0.25, 0.25, 0), Arguments.of(0.0, 0.0, 0));
    }
}

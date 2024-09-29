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
package org.openhab.binding.mielecloud.internal.webservice.api;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Objects;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.mielecloud.internal.webservice.api.json.DeviceType;

/**
 * @author Björn Lange - Initial contribution
 */
@NonNullByDefault
public class CoolingDeviceTemperatureStateTest {
    private static final Integer TEMPERATURE_0 = 8;
    private static final Integer TEMPERATURE_1 = -10;

    private static final Integer TARGET_TEMPERATURE_0 = 5;
    private static final Integer TARGET_TEMPERATURE_1 = -18;

    @Nullable
    private DeviceState deviceState;

    private DeviceState getDeviceState() {
        assertNotNull(deviceState);
        return Objects.requireNonNull(deviceState);
    }

    @BeforeEach
    public void setUp() {
        deviceState = mock(DeviceState.class);
        when(getDeviceState().getTemperature(0)).thenReturn(Optional.of(TEMPERATURE_0));
        when(getDeviceState().getTemperature(1)).thenReturn(Optional.of(TEMPERATURE_1));
        when(getDeviceState().getTargetTemperature(0)).thenReturn(Optional.of(TARGET_TEMPERATURE_0));
        when(getDeviceState().getTargetTemperature(1)).thenReturn(Optional.of(TARGET_TEMPERATURE_1));
    }

    @Test
    public void testGetFridgeTemperaturesForFridge() {
        // given:
        when(getDeviceState().getRawType()).thenReturn(DeviceType.FRIDGE);
        CoolingDeviceTemperatureState state = new CoolingDeviceTemperatureState(getDeviceState());

        // when:
        Integer current = state.getFridgeTemperature().get();
        Integer target = state.getFridgeTargetTemperature().get();

        // then:
        assertEquals(TEMPERATURE_0, current);
        assertEquals(TARGET_TEMPERATURE_0, target);
    }

    @Test
    public void testGetFridgeTemperaturesForFridgeFreezerCombination() {
        // given:
        when(getDeviceState().getRawType()).thenReturn(DeviceType.FRIDGE_FREEZER_COMBINATION);
        CoolingDeviceTemperatureState state = new CoolingDeviceTemperatureState(getDeviceState());

        // when:
        Integer current = state.getFridgeTemperature().get();
        Integer target = state.getFridgeTargetTemperature().get();

        // then:
        assertEquals(TEMPERATURE_0, current);
        assertEquals(TARGET_TEMPERATURE_0, target);
    }

    @Test
    public void testGetFridgeTemperaturesForFreezer() {
        // given:
        when(getDeviceState().getRawType()).thenReturn(DeviceType.FREEZER);
        CoolingDeviceTemperatureState state = new CoolingDeviceTemperatureState(getDeviceState());

        // when:
        Optional<Integer> current = state.getFridgeTemperature();
        Optional<Integer> target = state.getFridgeTargetTemperature();

        // then:
        assertFalse(current.isPresent());
        assertFalse(target.isPresent());
    }

    @Test
    public void testGetFreezerTemperaturesForFridge() {
        // given:
        when(getDeviceState().getRawType()).thenReturn(DeviceType.FRIDGE);
        CoolingDeviceTemperatureState state = new CoolingDeviceTemperatureState(getDeviceState());

        // when:
        Optional<Integer> current = state.getFreezerTemperature();
        Optional<Integer> target = state.getFreezerTargetTemperature();

        // then:
        assertFalse(current.isPresent());
        assertFalse(target.isPresent());
    }

    @Test
    public void testGetFreezerTemperaturesForFridgeFreezerCombination() {
        // given:
        when(getDeviceState().getRawType()).thenReturn(DeviceType.FRIDGE_FREEZER_COMBINATION);
        CoolingDeviceTemperatureState state = new CoolingDeviceTemperatureState(getDeviceState());

        // when:
        Integer current = state.getFreezerTemperature().get();
        Integer target = state.getFreezerTargetTemperature().get();

        // then:
        assertEquals(TEMPERATURE_1, current);
        assertEquals(TARGET_TEMPERATURE_1, target);
    }

    @Test
    public void testGetFreezerTemperaturesForFreezer() {
        // given:
        when(getDeviceState().getRawType()).thenReturn(DeviceType.FREEZER);
        CoolingDeviceTemperatureState state = new CoolingDeviceTemperatureState(getDeviceState());

        // when:
        Integer current = state.getFreezerTemperature().get();
        Integer target = state.getFreezerTargetTemperature().get();

        // then:
        assertEquals(TEMPERATURE_0, current);
        assertEquals(TARGET_TEMPERATURE_0, target);
    }
}

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
import org.junit.jupiter.api.Test;
import org.openhab.binding.mielecloud.internal.webservice.api.json.DeviceType;

/**
 * @author Björn Lange - Initial contribution
 */
@NonNullByDefault
public class WineStorageDeviceTemperatureStateTest {
    private static final Integer TEMPERATURE_0 = 8;
    private static final Integer TEMPERATURE_1 = 10;
    private static final Integer TEMPERATURE_2 = 12;

    private static final Integer TARGET_TEMPERATURE_0 = 5;
    private static final Integer TARGET_TEMPERATURE_1 = 9;
    private static final Integer TARGET_TEMPERATURE_2 = 11;

    @Nullable
    private DeviceState deviceState;

    private DeviceState getDeviceState() {
        assertNotNull(deviceState);
        return Objects.requireNonNull(deviceState);
    }

    private void setUpDeviceStateMock(int numberOfTemperatures) {
        deviceState = mock(DeviceState.class);
        if (numberOfTemperatures > 0) {
            when(getDeviceState().getTemperature(0)).thenReturn(Optional.of(TEMPERATURE_0));
            when(getDeviceState().getTargetTemperature(0)).thenReturn(Optional.of(TARGET_TEMPERATURE_0));
        } else {
            when(getDeviceState().getTemperature(0)).thenReturn(Optional.empty());
            when(getDeviceState().getTargetTemperature(0)).thenReturn(Optional.empty());
        }
        if (numberOfTemperatures > 1) {
            when(getDeviceState().getTemperature(1)).thenReturn(Optional.of(TEMPERATURE_1));
            when(getDeviceState().getTargetTemperature(1)).thenReturn(Optional.of(TARGET_TEMPERATURE_1));
        } else {
            when(getDeviceState().getTemperature(1)).thenReturn(Optional.empty());
            when(getDeviceState().getTargetTemperature(1)).thenReturn(Optional.empty());
        }
        if (numberOfTemperatures > 2) {
            when(getDeviceState().getTemperature(2)).thenReturn(Optional.of(TEMPERATURE_2));
            when(getDeviceState().getTargetTemperature(2)).thenReturn(Optional.of(TARGET_TEMPERATURE_2));
        } else {
            when(getDeviceState().getTemperature(2)).thenReturn(Optional.empty());
            when(getDeviceState().getTargetTemperature(2)).thenReturn(Optional.empty());
        }
    }

    @Test
    public void testGetTemperaturesForWineCabinetWithThreeCompartments() {
        // given:
        setUpDeviceStateMock(3);
        when(getDeviceState().getRawType()).thenReturn(DeviceType.WINE_CABINET);
        WineStorageDeviceTemperatureState state = new WineStorageDeviceTemperatureState(getDeviceState());

        // when:
        Optional<Integer> temperature = state.getTemperature();
        Optional<Integer> targetTemperature = state.getTargetTemperature();

        // then:
        assertFalse(temperature.isPresent());
        assertFalse(targetTemperature.isPresent());
    }

    @Test
    public void testGetTemperaturesForWineCabinetWithTwoCompartments() {
        // given:
        setUpDeviceStateMock(2);
        when(getDeviceState().getRawType()).thenReturn(DeviceType.WINE_CABINET);
        WineStorageDeviceTemperatureState state = new WineStorageDeviceTemperatureState(getDeviceState());

        // when:
        Optional<Integer> temperature = state.getTemperature();
        Optional<Integer> targetTemperature = state.getTargetTemperature();

        // then:
        assertFalse(temperature.isPresent());
        assertFalse(targetTemperature.isPresent());
    }

    @Test
    public void testGetTemperaturesForWineCabinetWithOneCompartment() {
        // given:
        setUpDeviceStateMock(1);
        when(getDeviceState().getRawType()).thenReturn(DeviceType.WINE_CABINET);
        WineStorageDeviceTemperatureState state = new WineStorageDeviceTemperatureState(getDeviceState());

        // when:
        Integer temperature = state.getTemperature().get();
        Integer targetTemperature = state.getTargetTemperature().get();

        // then:
        assertEquals(TEMPERATURE_0, temperature);
        assertEquals(TARGET_TEMPERATURE_0, targetTemperature);
    }

    @Test
    public void testGetTemperaturesForWineCabinetFreezerCombination() {
        // given:
        setUpDeviceStateMock(2);
        when(getDeviceState().getRawType()).thenReturn(DeviceType.WINE_CABINET_FREEZER_COMBINATION);
        WineStorageDeviceTemperatureState state = new WineStorageDeviceTemperatureState(getDeviceState());

        // when:
        Optional<Integer> temperature = state.getTemperature();
        Optional<Integer> targetTemperature = state.getTargetTemperature();

        // then:
        assertFalse(temperature.isPresent());
        assertFalse(targetTemperature.isPresent());
    }

    @Test
    public void testGetTemperaturesForOtherDeviceWithOneTemperature() {
        // given:
        setUpDeviceStateMock(1);
        when(getDeviceState().getRawType()).thenReturn(DeviceType.OVEN);
        WineStorageDeviceTemperatureState state = new WineStorageDeviceTemperatureState(getDeviceState());

        // when:
        Optional<Integer> temperature = state.getTemperature();
        Optional<Integer> targetTemperature = state.getTargetTemperature();

        // then:
        assertFalse(temperature.isPresent());
        assertFalse(targetTemperature.isPresent());
    }

    @Test
    public void testGetTemperaturesWhenNoTemperaturesAreAvailable() {
        // given:
        setUpDeviceStateMock(0);
        when(getDeviceState().getRawType()).thenReturn(DeviceType.WINE_CABINET);
        WineStorageDeviceTemperatureState state = new WineStorageDeviceTemperatureState(getDeviceState());

        // when:
        Optional<Integer> temperature = state.getTemperature();
        Optional<Integer> targetTemperature = state.getTargetTemperature();

        // then:
        assertFalse(temperature.isPresent());
        assertFalse(targetTemperature.isPresent());
    }

    @Test
    public void testGetTopTemperaturesForWineCabinetWithThreeCompartments() {
        // given:
        setUpDeviceStateMock(3);
        when(getDeviceState().getRawType()).thenReturn(DeviceType.WINE_CABINET);
        WineStorageDeviceTemperatureState state = new WineStorageDeviceTemperatureState(getDeviceState());

        // when:
        Integer temperature = state.getTopTemperature().get();
        Integer targetTemperature = state.getTopTargetTemperature().get();

        // then:
        assertEquals(TEMPERATURE_0, temperature);
        assertEquals(TARGET_TEMPERATURE_0, targetTemperature);
    }

    @Test
    public void testGetTopTemperaturesForWineCabinetWithTwoCompartments() {
        // given:
        setUpDeviceStateMock(2);
        when(getDeviceState().getRawType()).thenReturn(DeviceType.WINE_CABINET);
        WineStorageDeviceTemperatureState state = new WineStorageDeviceTemperatureState(getDeviceState());

        // when:
        Integer temperature = state.getTopTemperature().get();
        Integer targetTemperature = state.getTopTargetTemperature().get();

        // then:
        assertEquals(TEMPERATURE_0, temperature);
        assertEquals(TARGET_TEMPERATURE_0, targetTemperature);
    }

    @Test
    public void testGetTopTemperaturesForWineCabinetWithOneCompartment() {
        // given:
        setUpDeviceStateMock(1);
        when(getDeviceState().getRawType()).thenReturn(DeviceType.WINE_CABINET);
        WineStorageDeviceTemperatureState state = new WineStorageDeviceTemperatureState(getDeviceState());

        // when:
        Optional<Integer> temperature = state.getTopTemperature();
        Optional<Integer> targetTemperature = state.getTopTargetTemperature();

        // then:
        assertFalse(temperature.isPresent());
        assertFalse(targetTemperature.isPresent());
    }

    @Test
    public void testGetTopTemperaturesForWineCabinetFreezerCombination() {
        // given:
        setUpDeviceStateMock(2);
        when(getDeviceState().getRawType()).thenReturn(DeviceType.WINE_CABINET_FREEZER_COMBINATION);
        WineStorageDeviceTemperatureState state = new WineStorageDeviceTemperatureState(getDeviceState());

        // when:
        Integer temperature = state.getTopTemperature().get();
        Integer targetTemperature = state.getTopTargetTemperature().get();

        // then:
        assertEquals(TEMPERATURE_0, temperature);
        assertEquals(TARGET_TEMPERATURE_0, targetTemperature);
    }

    @Test
    public void testGetTopTemperaturesForOtherDeviceWithTwoTemperatures() {
        // given:
        setUpDeviceStateMock(2);
        when(getDeviceState().getRawType()).thenReturn(DeviceType.OVEN);
        WineStorageDeviceTemperatureState state = new WineStorageDeviceTemperatureState(getDeviceState());

        // when:
        Optional<Integer> temperature = state.getTopTemperature();
        Optional<Integer> targetTemperature = state.getTopTargetTemperature();

        // then:
        assertFalse(temperature.isPresent());
        assertFalse(targetTemperature.isPresent());
    }

    @Test
    public void testGetTopTemperaturesWhenNoTemperaturesAreAvailable() {
        // given:
        setUpDeviceStateMock(0);
        when(getDeviceState().getRawType()).thenReturn(DeviceType.WINE_CABINET);
        WineStorageDeviceTemperatureState state = new WineStorageDeviceTemperatureState(getDeviceState());

        // when:
        Optional<Integer> temperature = state.getTopTemperature();
        Optional<Integer> targetTemperature = state.getTopTargetTemperature();

        // then:
        assertFalse(temperature.isPresent());
        assertFalse(targetTemperature.isPresent());
    }

    @Test
    public void testGetMiddleTemperaturesForWineCabinetWithThreeCompartments() {
        // given:
        setUpDeviceStateMock(3);
        when(getDeviceState().getRawType()).thenReturn(DeviceType.WINE_CABINET);
        WineStorageDeviceTemperatureState state = new WineStorageDeviceTemperatureState(getDeviceState());

        // when:
        Integer temperature = state.getMiddleTemperature().get();
        Integer targetTemperature = state.getMiddleTargetTemperature().get();

        // then:
        assertEquals(TEMPERATURE_1, temperature);
        assertEquals(TARGET_TEMPERATURE_1, targetTemperature);
    }

    @Test
    public void testGetMiddleTemperaturesForWineCabinetWithTwoCompartments() {
        // given:
        setUpDeviceStateMock(2);
        when(getDeviceState().getRawType()).thenReturn(DeviceType.WINE_CABINET);
        WineStorageDeviceTemperatureState state = new WineStorageDeviceTemperatureState(getDeviceState());

        // when:
        Optional<Integer> temperature = state.getMiddleTemperature();
        Optional<Integer> targetTemperature = state.getMiddleTargetTemperature();

        // then:
        assertFalse(temperature.isPresent());
        assertFalse(targetTemperature.isPresent());
    }

    @Test
    public void testGetMiddleTemperaturesForWineCabinetWithOneCompartment() {
        // given:
        setUpDeviceStateMock(1);
        when(getDeviceState().getRawType()).thenReturn(DeviceType.WINE_CABINET);
        WineStorageDeviceTemperatureState state = new WineStorageDeviceTemperatureState(getDeviceState());

        // when:
        Optional<Integer> temperature = state.getMiddleTemperature();
        Optional<Integer> targetTemperature = state.getMiddleTargetTemperature();

        // then:
        assertFalse(temperature.isPresent());
        assertFalse(targetTemperature.isPresent());
    }

    @Test
    public void testGetMiddleTemperaturesForWineCabinetFreezerCombination() {
        // given:
        setUpDeviceStateMock(2);
        when(getDeviceState().getRawType()).thenReturn(DeviceType.WINE_CABINET_FREEZER_COMBINATION);
        WineStorageDeviceTemperatureState state = new WineStorageDeviceTemperatureState(getDeviceState());

        // when:
        Optional<Integer> temperature = state.getMiddleTemperature();
        Optional<Integer> targetTemperature = state.getMiddleTargetTemperature();

        // then:
        assertFalse(temperature.isPresent());
        assertFalse(targetTemperature.isPresent());
    }

    @Test
    public void testGetMiddleTemperaturesForOtherDeviceWithTwoTemperatures() {
        // given:
        setUpDeviceStateMock(2);
        when(getDeviceState().getRawType()).thenReturn(DeviceType.OVEN);
        WineStorageDeviceTemperatureState state = new WineStorageDeviceTemperatureState(getDeviceState());

        // when:
        Optional<Integer> temperature = state.getMiddleTemperature();
        Optional<Integer> targetTemperature = state.getMiddleTargetTemperature();

        // then:
        assertFalse(temperature.isPresent());
        assertFalse(targetTemperature.isPresent());
    }

    @Test
    public void testGetMiddleTemperaturesWhenNoTemperaturesAreAvailable() {
        // given:
        setUpDeviceStateMock(0);
        when(getDeviceState().getRawType()).thenReturn(DeviceType.WINE_CABINET);
        WineStorageDeviceTemperatureState state = new WineStorageDeviceTemperatureState(getDeviceState());

        // when:
        Optional<Integer> temperature = state.getMiddleTemperature();
        Optional<Integer> targetTemperature = state.getMiddleTargetTemperature();

        // then:
        assertFalse(temperature.isPresent());
        assertFalse(targetTemperature.isPresent());
    }

    @Test
    public void testGetBottomTemperaturesForWineCabinetWithThreeCompartments() {
        // given:
        setUpDeviceStateMock(3);
        when(getDeviceState().getRawType()).thenReturn(DeviceType.WINE_CABINET);
        WineStorageDeviceTemperatureState state = new WineStorageDeviceTemperatureState(getDeviceState());

        // when:
        Integer temperature = state.getBottomTemperature().get();
        Integer targetTemperature = state.getBottomTargetTemperature().get();

        // then:
        assertEquals(TEMPERATURE_2, temperature);
        assertEquals(TARGET_TEMPERATURE_2, targetTemperature);
    }

    @Test
    public void testGetBottomTemperaturesForWineCabinetWithTwoCompartments() {
        // given:
        setUpDeviceStateMock(2);
        when(getDeviceState().getRawType()).thenReturn(DeviceType.WINE_CABINET);
        WineStorageDeviceTemperatureState state = new WineStorageDeviceTemperatureState(getDeviceState());

        // when:
        Integer temperature = state.getBottomTemperature().get();
        Integer targetTemperature = state.getBottomTargetTemperature().get();

        // then:
        assertEquals(TEMPERATURE_1, temperature);
        assertEquals(TARGET_TEMPERATURE_1, targetTemperature);
    }

    @Test
    public void testGetBottomTemperaturesForWineCabinetWithOneCompartment() {
        // given:
        setUpDeviceStateMock(1);
        when(getDeviceState().getRawType()).thenReturn(DeviceType.WINE_CABINET);
        WineStorageDeviceTemperatureState state = new WineStorageDeviceTemperatureState(getDeviceState());

        // when:
        Optional<Integer> temperature = state.getBottomTemperature();
        Optional<Integer> targetTemperature = state.getBottomTargetTemperature();

        // then:
        assertFalse(temperature.isPresent());
        assertFalse(targetTemperature.isPresent());
    }

    @Test
    public void testGetBottomTemperaturesForWineCabinetFreezerCombination() {
        // given:
        setUpDeviceStateMock(2);
        when(getDeviceState().getRawType()).thenReturn(DeviceType.WINE_CABINET_FREEZER_COMBINATION);
        WineStorageDeviceTemperatureState state = new WineStorageDeviceTemperatureState(getDeviceState());

        // when:
        Integer temperature = state.getBottomTemperature().get();
        Integer targetTemperature = state.getBottomTargetTemperature().get();

        // then:
        assertEquals(TEMPERATURE_1, temperature);
        assertEquals(TARGET_TEMPERATURE_1, targetTemperature);
    }

    @Test
    public void testGetBottomTemperaturesForOtherDeviceWithTwoTemperatures() {
        // given:
        setUpDeviceStateMock(2);
        when(getDeviceState().getRawType()).thenReturn(DeviceType.OVEN);
        WineStorageDeviceTemperatureState state = new WineStorageDeviceTemperatureState(getDeviceState());

        // when:
        Optional<Integer> temperature = state.getBottomTemperature();
        Optional<Integer> targetTemperature = state.getBottomTargetTemperature();

        // then:
        assertFalse(temperature.isPresent());
        assertFalse(targetTemperature.isPresent());
    }

    @Test
    public void testGetBottomTemperaturesWhenNoTemperaturesAreAvailable() {
        // given:
        setUpDeviceStateMock(0);
        when(getDeviceState().getRawType()).thenReturn(DeviceType.WINE_CABINET);
        WineStorageDeviceTemperatureState state = new WineStorageDeviceTemperatureState(getDeviceState());

        // when:
        Optional<Integer> temperature = state.getBottomTemperature();
        Optional<Integer> targetTemperature = state.getBottomTargetTemperature();

        // then:
        assertFalse(temperature.isPresent());
        assertFalse(targetTemperature.isPresent());
    }
}

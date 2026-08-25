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
package org.openhab.binding.atagone.internal;

import static org.junit.jupiter.api.Assertions.*;
import static org.openhab.binding.atagone.internal.AtagOneBindingConstants.*;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.atagone.internal.dto.ControlUpdateDTO;
import org.openhab.binding.atagone.internal.dto.DeviceConfigUpdateDTO;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.State;

/**
 * Unit tests for {@link AtagOneHandler#buildControlUpdate}, which turns a channel command into the
 * device DTOs to send. Exercised directly (bypassing {@code handleCommand}'s I/O) since it holds all
 * of the binding's write-path business rules: what preset-mode values are accepted, and how the
 * timed-preset "mode + duration must be sent together" protocol quirk is composed.
 *
 * @author Florian Lettner - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@NonNullByDefault
class AtagOneHandlerTest {

    private @Mock @NonNullByDefault({}) Thing thing;
    private @Mock @NonNullByDefault({}) HttpClient httpClient;
    private @NonNullByDefault({}) AtagOneHandler handler;

    @BeforeEach
    void setUp() {
        handler = new AtagOneHandler(thing, httpClient);
    }

    /** Directly seeds the handler's private stateMap, simulating a previously-polled channel value. */
    @SuppressWarnings("unchecked")
    private void seedState(String channelId, State state) throws ReflectiveOperationException {
        Field field = AtagOneHandler.class.getDeclaredField("stateMap");
        field.setAccessible(true);
        Object fieldValue = Objects.requireNonNull(field.get(handler));
        ((Map<String, State>) fieldValue).put(channelId, state);
    }

    /** Directly sets the device's persisted default vacation duration, simulating a prior poll. */
    private void seedDefaultVacationDurationSeconds(long seconds) throws ReflectiveOperationException {
        Field field = AtagOneHandler.class.getDeclaredField("defaultVacationDurationSeconds");
        field.setAccessible(true);
        field.set(handler, seconds);
    }

    @Test
    void unknownPresetModeIsRejected() {
        ControlUpdateDTO control = new ControlUpdateDTO();
        DeviceConfigUpdateDTO configUpdate = new DeviceConfigUpdateDTO();

        boolean accepted = handler.buildControlUpdate(CHANNEL_PRESET_MODE, new StringType("standby"), control,
                configUpdate);

        assertFalse(accepted);
        assertNull(control.ch_mode);
    }

    @Test
    void manualPresetModeIsRejected() {
        ControlUpdateDTO control = new ControlUpdateDTO();
        DeviceConfigUpdateDTO configUpdate = new DeviceConfigUpdateDTO();

        boolean accepted = handler.buildControlUpdate(CHANNEL_PRESET_MODE, new StringType("manual"), control,
                configUpdate);

        assertFalse(accepted);
    }

    @Test
    void extendPresetModeIsRejected() {
        ControlUpdateDTO control = new ControlUpdateDTO();
        DeviceConfigUpdateDTO configUpdate = new DeviceConfigUpdateDTO();

        boolean accepted = handler.buildControlUpdate(CHANNEL_PRESET_MODE, new StringType("extend"), control,
                configUpdate);

        assertFalse(accepted);
    }

    @Test
    void extendDurationChannelIsReadOnly() {
        ControlUpdateDTO control = new ControlUpdateDTO();
        DeviceConfigUpdateDTO configUpdate = new DeviceConfigUpdateDTO();

        boolean accepted = handler.buildControlUpdate(CHANNEL_EXTEND_DURATION,
                new org.openhab.core.library.types.QuantityType<>(1, Units.HOUR), control, configUpdate);

        assertFalse(accepted);
    }

    @Test
    void holidayPresetModeWithNoStoredDurationDefaultsToSevenDays() {
        ControlUpdateDTO control = new ControlUpdateDTO();
        DeviceConfigUpdateDTO configUpdate = new DeviceConfigUpdateDTO();

        boolean accepted = handler.buildControlUpdate(CHANNEL_PRESET_MODE, new StringType("holiday"), control,
                configUpdate);

        assertTrue(accepted);
        assertEquals(CH_MODE_HOLIDAY, control.ch_mode);
        assertEquals(7 * 86400L, control.ch_mode_duration);
        assertEquals(7 * 86400L, control.vacation_duration);
        assertNotNull(configUpdate.start_vacation);
    }

    @Test
    void vacationDurationWriteComposesHolidayModeAtomically() {
        ControlUpdateDTO control = new ControlUpdateDTO();
        DeviceConfigUpdateDTO configUpdate = new DeviceConfigUpdateDTO();

        boolean accepted = handler.buildControlUpdate(CHANNEL_VACATION_DURATION,
                new org.openhab.core.library.types.QuantityType<>(3, Units.DAY), control, configUpdate);

        assertTrue(accepted);
        assertEquals(CH_MODE_HOLIDAY, control.ch_mode);
        assertEquals(3 * 86400L, control.ch_mode_duration);
        assertEquals(3 * 86400L, control.vacation_duration);
        assertNotNull(configUpdate.start_vacation);
    }

    @Test
    void leavingHolidayModeClearsVacationSchedule() throws ReflectiveOperationException {
        seedState(CHANNEL_PRESET_MODE, new StringType("holiday"));
        ControlUpdateDTO control = new ControlUpdateDTO();
        DeviceConfigUpdateDTO configUpdate = new DeviceConfigUpdateDTO();

        boolean accepted = handler.buildControlUpdate(CHANNEL_PRESET_MODE, new StringType("auto"), control,
                configUpdate);

        assertTrue(accepted);
        assertEquals(CH_MODE_AUTO, control.ch_mode);
        assertEquals(0L, control.vacation_duration);
        assertEquals(0L, configUpdate.start_vacation);
    }

    @Test
    void switchingToAutoFromNonHolidayLeavesVacationScheduleUntouched() throws ReflectiveOperationException {
        seedState(CHANNEL_PRESET_MODE, new StringType("fireplace"));
        ControlUpdateDTO control = new ControlUpdateDTO();
        DeviceConfigUpdateDTO configUpdate = new DeviceConfigUpdateDTO();

        boolean accepted = handler.buildControlUpdate(CHANNEL_PRESET_MODE, new StringType("auto"), control,
                configUpdate);

        assertTrue(accepted);
        assertEquals(CH_MODE_AUTO, control.ch_mode);
        assertNull(control.vacation_duration);
        assertNull(configUpdate.start_vacation);
    }

    @Test
    void fireplaceDurationWriteSendsExactDurationWithNoRestart() {
        ControlUpdateDTO control = new ControlUpdateDTO();
        DeviceConfigUpdateDTO configUpdate = new DeviceConfigUpdateDTO();

        boolean accepted = handler.buildControlUpdate(CHANNEL_FIREPLACE_DURATION,
                new org.openhab.core.library.types.QuantityType<>(2, Units.HOUR), control, configUpdate);

        assertTrue(accepted);
        assertEquals(CH_MODE_FIREPLACE, control.ch_mode);
        assertEquals(7200L, control.fireplace_duration);
        // ch_mode_duration must be present (any value) — a missing field is what triggers the boiler's
        // multi-minute API restart.
        assertEquals(7200L, control.ch_mode_duration);
    }

    @Test
    void targetTemperatureWriteConvertsToCelsius() {
        ControlUpdateDTO control = new ControlUpdateDTO();
        DeviceConfigUpdateDTO configUpdate = new DeviceConfigUpdateDTO();

        boolean accepted = handler.buildControlUpdate(CHANNEL_TARGET_TEMPERATURE,
                new org.openhab.core.library.types.QuantityType<>(21.5, SIUnits.CELSIUS), control, configUpdate);

        assertTrue(accepted);
        assertEquals(21.5, Objects.requireNonNull(control.ch_mode_temp), 0.001);
    }

    @Test
    void unhandledChannelIsRejected() {
        ControlUpdateDTO control = new ControlUpdateDTO();
        DeviceConfigUpdateDTO configUpdate = new DeviceConfigUpdateDTO();

        boolean accepted = handler.buildControlUpdate("not-a-real-channel", new StringType("x"), control, configUpdate);

        assertFalse(accepted);
    }

    @Test
    void hvacModeAcceptsHeat() {
        ControlUpdateDTO control = new ControlUpdateDTO();
        DeviceConfigUpdateDTO configUpdate = new DeviceConfigUpdateDTO();

        boolean accepted = handler.buildControlUpdate(CHANNEL_HVAC_MODE, new StringType("heat"), control, configUpdate);

        assertTrue(accepted);
        assertEquals(CH_CONTROL_MODE_HEAT, control.ch_control_mode);
    }

    @Test
    void hvacModeAcceptsAuto() {
        ControlUpdateDTO control = new ControlUpdateDTO();
        DeviceConfigUpdateDTO configUpdate = new DeviceConfigUpdateDTO();

        boolean accepted = handler.buildControlUpdate(CHANNEL_HVAC_MODE, new StringType("auto"), control, configUpdate);

        assertTrue(accepted);
        assertEquals(CH_CONTROL_MODE_AUTO, control.ch_control_mode);
    }

    @Test
    void hvacModeRejectsUnknownValue() {
        ControlUpdateDTO control = new ControlUpdateDTO();
        DeviceConfigUpdateDTO configUpdate = new DeviceConfigUpdateDTO();

        boolean accepted = handler.buildControlUpdate(CHANNEL_HVAC_MODE, new StringType("AUTO_"), control,
                configUpdate);

        assertFalse(accepted);
        assertNull(control.ch_control_mode);
    }

    @Test
    void fireplaceDurationRejectsZero() {
        ControlUpdateDTO control = new ControlUpdateDTO();
        DeviceConfigUpdateDTO configUpdate = new DeviceConfigUpdateDTO();

        boolean accepted = handler.buildControlUpdate(CHANNEL_FIREPLACE_DURATION,
                new org.openhab.core.library.types.QuantityType<>(0, Units.SECOND), control, configUpdate);

        assertFalse(accepted);
    }

    @Test
    void fireplaceDurationRejectsNegativeValue() {
        ControlUpdateDTO control = new ControlUpdateDTO();
        DeviceConfigUpdateDTO configUpdate = new DeviceConfigUpdateDTO();

        boolean accepted = handler.buildControlUpdate(CHANNEL_FIREPLACE_DURATION,
                new org.openhab.core.library.types.QuantityType<>(-100, Units.SECOND), control, configUpdate);

        assertFalse(accepted);
    }

    @Test
    void fireplacePresetModeReusesStoredDuration() throws ReflectiveOperationException {
        seedState(CHANNEL_FIREPLACE_DURATION, new org.openhab.core.library.types.QuantityType<>(2, Units.HOUR));
        ControlUpdateDTO control = new ControlUpdateDTO();
        DeviceConfigUpdateDTO configUpdate = new DeviceConfigUpdateDTO();

        boolean accepted = handler.buildControlUpdate(CHANNEL_PRESET_MODE, new StringType("fireplace"), control,
                configUpdate);

        assertTrue(accepted);
        assertEquals(CH_MODE_FIREPLACE, control.ch_mode);
        assertEquals(7200L, control.fireplace_duration);
        assertEquals(7200L, control.ch_mode_duration);
    }

    @Test
    void fireplacePresetModeDefaultsToOneHourWithNoStoredDuration() {
        ControlUpdateDTO control = new ControlUpdateDTO();
        DeviceConfigUpdateDTO configUpdate = new DeviceConfigUpdateDTO();

        boolean accepted = handler.buildControlUpdate(CHANNEL_PRESET_MODE, new StringType("fireplace"), control,
                configUpdate);

        assertTrue(accepted);
        assertEquals(CH_MODE_FIREPLACE, control.ch_mode);
        assertEquals(3600L, control.fireplace_duration);
        assertEquals(3600L, control.ch_mode_duration);
    }

    @Test
    void holidayPresetModeReusesActiveVacationDurationOverDeviceDefault() throws ReflectiveOperationException {
        seedDefaultVacationDurationSeconds(3 * 86400L);
        seedState(CHANNEL_VACATION_DURATION, new org.openhab.core.library.types.QuantityType<>(5, Units.DAY));
        ControlUpdateDTO control = new ControlUpdateDTO();
        DeviceConfigUpdateDTO configUpdate = new DeviceConfigUpdateDTO();

        boolean accepted = handler.buildControlUpdate(CHANNEL_PRESET_MODE, new StringType("holiday"), control,
                configUpdate);

        assertTrue(accepted);
        // An actively-running vacation-duration takes priority over the device's stored default.
        assertEquals(5 * 86400L, control.ch_mode_duration);
    }

    @Test
    void holidayPresetModeFallsBackToDeviceStoredDefault() throws ReflectiveOperationException {
        seedDefaultVacationDurationSeconds(3 * 86400L);
        ControlUpdateDTO control = new ControlUpdateDTO();
        DeviceConfigUpdateDTO configUpdate = new DeviceConfigUpdateDTO();

        boolean accepted = handler.buildControlUpdate(CHANNEL_PRESET_MODE, new StringType("holiday"), control,
                configUpdate);

        assertTrue(accepted);
        // No vacation currently active — falls back to the device's own persisted default, not a
        // hardcoded 7 days.
        assertEquals(3 * 86400L, control.ch_mode_duration);
        assertEquals(3 * 86400L, control.vacation_duration);
    }
}

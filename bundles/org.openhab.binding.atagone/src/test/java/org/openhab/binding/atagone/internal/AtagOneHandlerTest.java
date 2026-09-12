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
import static org.mockito.Mockito.lenient;
import static org.openhab.binding.atagone.internal.AtagOneBindingConstants.*;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.atagone.internal.dto.ControlUpdateDTO;
import org.openhab.binding.atagone.internal.dto.DeviceConfigUpdateDTO;
import org.openhab.binding.atagone.internal.dto.RetrieveReplyDTO;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.types.State;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Unit tests for {@link AtagOneHandler#buildControlUpdate}, which turns a channel command into the
 * device DTOs to send. Exercised directly (bypassing {@code handleCommand}'s I/O) since it holds all
 * of the binding's write-path business rules: what preset-mode values are accepted, and how each
 * mode's activation/cancellation write is composed (see {@link ControlUpdateDTO} for the per-mode
 * field requirements).
 * <p>
 * Also covers the read path ({@code updateChannels}): {@code vacation-duration} must reflect a
 * freshly-written stored value even outside active holiday mode, since
 * {@code composeVacationActivation}'s stored-value fallback depends on reading it.
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
        lenient().when(thing.getUID()).thenReturn(new ThingUID(THING_TYPE_THERMOSTAT, "test"));
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

    /** Directly sets the device's persisted default extend duration, simulating a prior poll. */
    private void seedDefaultExtendDurationSeconds(long seconds) throws ReflectiveOperationException {
        Field field = AtagOneHandler.class.getDeclaredField("defaultExtendDurationSeconds");
        field.setAccessible(true);
        field.set(handler, seconds);
    }

    /** Directly sets the device's armed (possibly pending) vacation start, simulating a prior poll. */
    private void seedArmedStartVacation(long epochOffset) throws ReflectiveOperationException {
        Field field = AtagOneHandler.class.getDeclaredField("armedStartVacation");
        field.setAccessible(true);
        field.set(handler, epochOffset);
    }

    /** Loads the captured full-device fixture used elsewhere for DTO-parsing tests. */
    private RetrieveReplyDTO loadRetrieveReply() throws IOException {
        try (@Nullable
        InputStream in = AtagOneHandlerTest.class
                .getResourceAsStream("/org/openhab/binding/atagone/internal/dto/retrieve_reply.json")) {
            assertNotNull(in, "Fixture not found: retrieve_reply.json");
            String json = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            RetrieveReplyDTO reply = new Gson().fromJson(root.getAsJsonObject("retrieve_reply"),
                    RetrieveReplyDTO.class);
            return Objects.requireNonNull(reply);
        }
    }

    /** Invokes the private read-path method under test, bypassing the polling loop that calls it. */
    private void invokeUpdateChannels(RetrieveReplyDTO reply) throws ReflectiveOperationException {
        Method method = AtagOneHandler.class.getDeclaredMethod("updateChannels", RetrieveReplyDTO.class);
        method.setAccessible(true);
        method.invoke(handler, reply);
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
    void extendPresetModeReusesStoredExtendDuration() throws ReflectiveOperationException {
        seedState(CHANNEL_EXTEND_DURATION, new org.openhab.core.library.types.QuantityType<>(2, Units.HOUR));
        ControlUpdateDTO control = new ControlUpdateDTO();
        DeviceConfigUpdateDTO configUpdate = new DeviceConfigUpdateDTO();

        boolean accepted = handler.buildControlUpdate(CHANNEL_PRESET_MODE, new StringType("extend"), control,
                configUpdate);

        assertTrue(accepted);
        assertEquals(CH_MODE_EXTEND, control.ch_mode);
        assertEquals(7200L, control.extend_duration);
        // extend_duration is additive to the schedule-boundary time on the device, not
        // ch_mode_duration. Must not be set here.
        assertNull(control.ch_mode_duration);
    }

    @Test
    void extendPresetModeFallsBackToDeviceStoredDefault() throws ReflectiveOperationException {
        seedDefaultExtendDurationSeconds(1800L);
        ControlUpdateDTO control = new ControlUpdateDTO();
        DeviceConfigUpdateDTO configUpdate = new DeviceConfigUpdateDTO();

        boolean accepted = handler.buildControlUpdate(CHANNEL_PRESET_MODE, new StringType("extend"), control,
                configUpdate);

        assertTrue(accepted);
        assertEquals(CH_MODE_EXTEND, control.ch_mode);
        assertEquals(1800L, control.extend_duration);
        assertNull(control.ch_mode_duration);
    }

    @Test
    void extendDurationWriteIsValueSetterOnly() {
        ControlUpdateDTO control = new ControlUpdateDTO();
        DeviceConfigUpdateDTO configUpdate = new DeviceConfigUpdateDTO();

        boolean accepted = handler.buildControlUpdate(CHANNEL_EXTEND_DURATION,
                new org.openhab.core.library.types.QuantityType<>(2, Units.HOUR), control, configUpdate);

        assertTrue(accepted);
        assertEquals(7200L, control.extend_duration);
        // preset-mode is the sole mode-transition trigger — a duration-channel write must never
        // set ch_mode.
        assertNull(control.ch_mode);
        assertNull(control.ch_mode_duration);
    }

    @Test
    void extendDurationRejectsZero() {
        ControlUpdateDTO control = new ControlUpdateDTO();
        DeviceConfigUpdateDTO configUpdate = new DeviceConfigUpdateDTO();

        boolean accepted = handler.buildControlUpdate(CHANNEL_EXTEND_DURATION,
                new org.openhab.core.library.types.QuantityType<>(0, Units.SECOND), control, configUpdate);

        assertFalse(accepted);
    }

    @Test
    void extendDurationRejectsNegativeValue() {
        ControlUpdateDTO control = new ControlUpdateDTO();
        DeviceConfigUpdateDTO configUpdate = new DeviceConfigUpdateDTO();

        boolean accepted = handler.buildControlUpdate(CHANNEL_EXTEND_DURATION,
                new org.openhab.core.library.types.QuantityType<>(-100, Units.SECOND), control, configUpdate);

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
    void vacationDurationWriteIsValueSetterOnly() {
        ControlUpdateDTO control = new ControlUpdateDTO();
        DeviceConfigUpdateDTO configUpdate = new DeviceConfigUpdateDTO();

        boolean accepted = handler.buildControlUpdate(CHANNEL_VACATION_DURATION,
                new org.openhab.core.library.types.QuantityType<>(3, Units.DAY), control, configUpdate);

        assertTrue(accepted);
        assertEquals(3 * 86400L, control.vacation_duration);
        // Vacation never activates via ch_mode alone — a duration-only write must not set ch_mode,
        // ch_mode_duration, or start_vacation.
        assertNull(control.ch_mode);
        assertNull(control.ch_mode_duration);
        assertNull(configUpdate.start_vacation);
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
    void cancelClearsAnArmedPendingVacationEvenWhenPresetModeReadsAuto() throws ReflectiveOperationException {
        // A pending (future-scheduled, not-yet-active) vacation reports preset-mode=auto. Keying
        // cancellation only on reported preset-mode would silently leave the schedule fully armed
        // while returning "nothing to cancel".
        seedState(CHANNEL_PRESET_MODE, new StringType("auto"));
        seedArmedStartVacation(841477166L);
        ControlUpdateDTO control = new ControlUpdateDTO();
        DeviceConfigUpdateDTO configUpdate = new DeviceConfigUpdateDTO();

        boolean requiresPhysicalConfirmation = handler.composeCancel(control, configUpdate);

        assertFalse(requiresPhysicalConfirmation);
        assertEquals(CH_MODE_AUTO, control.ch_mode);
        assertEquals(0L, control.vacation_duration);
        assertEquals(0L, configUpdate.start_vacation);
    }

    @Test
    void cancelWithNothingArmedAndModeAutoTouchesNothing() throws ReflectiveOperationException {
        // Calling cancel when there is genuinely nothing to cancel must not touch
        // vacation_duration/start_vacation.
        seedState(CHANNEL_PRESET_MODE, new StringType("auto"));
        seedArmedStartVacation(0L);
        ControlUpdateDTO control = new ControlUpdateDTO();
        DeviceConfigUpdateDTO configUpdate = new DeviceConfigUpdateDTO();

        boolean requiresPhysicalConfirmation = handler.composeCancel(control, configUpdate);

        assertFalse(requiresPhysicalConfirmation);
        assertEquals(CH_MODE_AUTO, control.ch_mode);
        assertNull(control.vacation_duration);
        assertNull(configUpdate.start_vacation);
    }

    @Test
    void extendDurationRejectsNonWholeHour() {
        ControlUpdateDTO control = new ControlUpdateDTO();
        DeviceConfigUpdateDTO configUpdate = new DeviceConfigUpdateDTO();

        // A non-whole-hour value doesn't fail safely on the device — it triggers the same
        // physical-confirmation/reboot pathway as a real cancel.
        boolean accepted = handler.buildControlUpdate(CHANNEL_EXTEND_DURATION,
                new org.openhab.core.library.types.QuantityType<>(1800, Units.SECOND), control, configUpdate);

        assertFalse(accepted);
        assertNull(control.extend_duration);
    }

    @Test
    void fireplaceDurationRejectsNonWholeHour() {
        ControlUpdateDTO control = new ControlUpdateDTO();
        DeviceConfigUpdateDTO configUpdate = new DeviceConfigUpdateDTO();

        // 2400s (40 min) is the exact value confirmed to trigger a real device reboot — must be
        // rejected before it ever reaches the device.
        boolean accepted = handler.buildControlUpdate(CHANNEL_FIREPLACE_DURATION,
                new org.openhab.core.library.types.QuantityType<>(2400, Units.SECOND), control, configUpdate);

        assertFalse(accepted);
        assertNull(control.fireplace_duration);
    }

    @Test
    void vacationDurationRejectsNonWholeDay() {
        ControlUpdateDTO control = new ControlUpdateDTO();
        DeviceConfigUpdateDTO configUpdate = new DeviceConfigUpdateDTO();

        boolean accepted = handler.buildControlUpdate(CHANNEL_VACATION_DURATION,
                new org.openhab.core.library.types.QuantityType<>(12, Units.HOUR), control, configUpdate);

        assertFalse(accepted);
        assertNull(control.vacation_duration);
    }

    @Test
    void fireplaceDurationWriteIsValueSetterOnly() {
        ControlUpdateDTO control = new ControlUpdateDTO();
        DeviceConfigUpdateDTO configUpdate = new DeviceConfigUpdateDTO();

        boolean accepted = handler.buildControlUpdate(CHANNEL_FIREPLACE_DURATION,
                new org.openhab.core.library.types.QuantityType<>(2, Units.HOUR), control, configUpdate);

        assertTrue(accepted);
        assertEquals(7200L, control.fireplace_duration);
        // preset-mode is the sole mode-transition trigger — a duration-channel write must never
        // set ch_mode or ch_mode_duration.
        assertNull(control.ch_mode);
        assertNull(control.ch_mode_duration);
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
    void chControlModeChannelIsReadOnly() {
        ControlUpdateDTO control = new ControlUpdateDTO();
        DeviceConfigUpdateDTO configUpdate = new DeviceConfigUpdateDTO();

        boolean accepted = handler.buildControlUpdate(CHANNEL_CH_CONTROL_MODE, new StringType("room"), control,
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

    @Test
    void vacationDurationIsReadUnconditionallyOutsideActiveHoliday() throws IOException, ReflectiveOperationException {
        RetrieveReplyDTO reply = loadRetrieveReply();
        // Fixture reports ch_mode=2 (auto, not holiday) — this is the exact scenario the fix targets:
        // a value the user just wrote (to be picked up by the next preset-mode=holiday activation) must
        // remain visible even though holiday mode isn't currently active.
        assertEquals(CH_MODE_AUTO, reply.control.ch_mode);
        reply.control.vacation_duration = 5 * 86400L;

        invokeUpdateChannels(reply);

        Field field = AtagOneHandler.class.getDeclaredField("stateMap");
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<String, State> stateMap = (Map<String, State>) Objects.requireNonNull(field.get(handler));
        State vacationDuration = stateMap.get(CHANNEL_VACATION_DURATION);
        assertTrue(vacationDuration instanceof QuantityType<?>, "Expected a QuantityType, not UNDEF");
        QuantityType<?> asSeconds = ((QuantityType<?>) vacationDuration).toUnit(Units.SECOND);
        assertNotNull(asSeconds);
        assertEquals(5 * 86400L, asSeconds.longValue());
    }
}

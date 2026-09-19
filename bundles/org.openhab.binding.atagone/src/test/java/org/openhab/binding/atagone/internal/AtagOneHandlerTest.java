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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.openhab.binding.atagone.internal.AtagOneBindingConstants.*;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
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
import org.openhab.binding.atagone.internal.dto.DeviceConfigDTO;
import org.openhab.binding.atagone.internal.dto.DeviceConfigUpdateDTO;
import org.openhab.binding.atagone.internal.dto.RetrieveReplyDTO;
import org.openhab.binding.atagone.internal.dto.ScheduleDTO;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
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
        handler = new AtagOneHandler(thing, httpClient, new AtagOneStateDescriptionProvider());
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

    /** Directly sets the last-polled configuration, simulating a prior poll. */
    private void seedLastConfiguration(DeviceConfigDTO config) throws ReflectiveOperationException {
        Field field = AtagOneHandler.class.getDeclaredField("lastConfiguration");
        field.setAccessible(true);
        field.set(handler, config);
    }

    /** A representative configuration block, matching values from a real device capture. */
    private DeviceConfigDTO sampleConfiguration() {
        DeviceConfigDTO config = new DeviceConfigDTO();
        config.ch_heating_type = 5;
        config.ch_isolation = 3;
        config.ch_building_size = 2;
        config.wdr_temps_influence = 2;
        config.climate_zone = -10.0;
        config.wd_temp_offs = 0.0;
        config.summer_eco_mode = 0;
        config.summer_eco_temp = 18.5;
        config.frost_prot_enabled = 0;
        config.frost_prot_temp_room = 4.0;
        config.frost_prot_temp_outs = 0.0;
        config.max_preheat = 1440;
        config.ch_vacation_temp = 14.0;
        config.ch_mode_vacation = 604800L;
        config.ch_mode_extend = 3600L;
        config.time_zone = 1;
        config.dhw_legion_enabled = 1;
        config.dhw_legion_day = 7;
        config.dhw_legion_time = 420;
        return config;
    }

    /** Directly sets the last-polled ch_schedule.entries, simulating a prior poll. */
    private void seedLastChScheduleEntries(double[][][] entries) throws ReflectiveOperationException {
        Field field = AtagOneHandler.class.getDeclaredField("lastChScheduleEntries");
        field.setAccessible(true);
        field.set(handler, entries);
    }

    /** Directly sets the last-polled dhw_schedule.entries, simulating a prior poll. */
    private void seedLastDhwScheduleEntries(double[][][] entries) throws ReflectiveOperationException {
        Field field = AtagOneHandler.class.getDeclaredField("lastDhwScheduleEntries");
        field.setAccessible(true);
        field.set(handler, entries);
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

    /** Reads a channel's last-published state directly from the handler's private stateMap. */
    @SuppressWarnings("unchecked")
    private State readState(String channelId) throws ReflectiveOperationException {
        Field field = AtagOneHandler.class.getDeclaredField("stateMap");
        field.setAccessible(true);
        Map<String, State> stateMap = (Map<String, State>) Objects.requireNonNull(field.get(handler));
        return Objects.requireNonNull(stateMap.get(channelId));
    }

    @Test
    void deltaTemperatureIsFlowMinusReturn() throws IOException, ReflectiveOperationException {
        RetrieveReplyDTO reply = loadRetrieveReply();

        invokeUpdateChannels(reply);

        QuantityType<?> delta = (QuantityType<?>) readState(CHANNEL_DELTA_TEMPERATURE);
        assertEquals(reply.report.ch_water_temp - reply.report.ch_return_temp, delta.doubleValue(), 0.001);
    }

    @Test
    void chAndDhwActiveDecodeDisjointBoilerStatusBits() throws IOException, ReflectiveOperationException {
        RetrieveReplyDTO reply = loadRetrieveReply();
        // Fixture boiler_status = 268 = 0x10C: CH_SCHEMA (0x100) + FLAME (0x008) + DHW_ACTIVE (0x004),
        // CH_ACTIVE (0x002) not set — a real DHW-heating value, corrected 2026-09-14 (see
        // AtagOneBindingConstants.BOILER_STATUS_*'s field comment for why the old bit assignments were
        // wrong and produced exactly this fixture's value misclassified as CH-active).
        assertEquals(268, reply.report.boiler_status);

        invokeUpdateChannels(reply);

        assertEquals(OnOffType.OFF, readState(CHANNEL_CH_ACTIVE));
        assertEquals(OnOffType.ON, readState(CHANNEL_DHW_ACTIVE));
        assertEquals(OnOffType.ON, readState(CHANNEL_FLAME));
    }

    @Test
    void chActiveDecodesFromItsOwnBitOnly() throws IOException, ReflectiveOperationException {
        RetrieveReplyDTO reply = loadRetrieveReply();
        reply.report.boiler_status = BOILER_STATUS_CH_ACTIVE;

        invokeUpdateChannels(reply);

        assertEquals(OnOffType.ON, readState(CHANNEL_CH_ACTIVE));
        assertEquals(OnOffType.OFF, readState(CHANNEL_DHW_ACTIVE));
        assertEquals(OnOffType.OFF, readState(CHANNEL_FLAME));
    }

    @Test
    void allBoilerStatusBitsClearDecodesToNoneAndOff() throws IOException, ReflectiveOperationException {
        RetrieveReplyDTO reply = loadRetrieveReply();
        reply.report.boiler_status = 0;

        invokeUpdateChannels(reply);

        assertEquals(OnOffType.OFF, readState(CHANNEL_CH_ACTIVE));
        assertEquals(OnOffType.OFF, readState(CHANNEL_DHW_ACTIVE));
        assertEquals(OnOffType.OFF, readState(CHANNEL_FLAME));
    }

    @Test
    void weatherTemperatureIsReadFromControlBlock() throws IOException, ReflectiveOperationException {
        RetrieveReplyDTO reply = loadRetrieveReply();

        invokeUpdateChannels(reply);

        QuantityType<?> weatherTemp = (QuantityType<?>) readState(CHANNEL_WEATHER_TEMPERATURE);
        assertEquals(reply.control.weather_temp, weatherTemp.doubleValue(), 0.001);
    }

    @Test
    void updateDevicePropertiesSetsIdentityFromDevice() throws IOException, ReflectiveOperationException {
        RetrieveReplyDTO reply = loadRetrieveReply();

        invokeUpdateChannels(reply);

        verify(thing).setProperty(PROPERTY_DEVICE_ID, reply.status.device_id);
        verify(thing).setProperty(Thing.PROPERTY_SERIAL_NUMBER, reply.configuration.boiler_id);
        verify(thing).setProperty(Thing.PROPERTY_VENDOR, "ATAG");
        // Fixture has no download_url, so no firmware version can be parsed from it.
        verify(thing, never()).setProperty(eq(Thing.PROPERTY_FIRMWARE_VERSION), anyString());
    }

    @Test
    void nextScheduleChannelsFindTodaysNextEntry() throws ReflectiveOperationException {
        // Monday (entries[0]), one entry starting at 08:00 (480 min) and one at 20:00 (1200 min).
        double[][][] entries = new double[7][][];
        entries[0] = new double[][] { { 480, 600, 21.0 }, { 1200, 1320, 19.0 } };
        for (int i = 1; i < 7; i++) {
            entries[i] = new double[0][];
        }
        ZonedDateTime monday10am = ZonedDateTime.of(2026, 9, 14, 10, 0, 0, 0, ZonedDateTime.now().getZone());
        assertEquals(java.time.DayOfWeek.MONDAY, monday10am.getDayOfWeek());

        handler.updateNextScheduleChannels(entries, monday10am);

        DateTimeType nextTime = (DateTimeType) readState(CHANNEL_NEXT_SCHEDULE_TIME);
        assertEquals(monday10am.withHour(20).withMinute(0).withSecond(0).withNano(0).toInstant(),
                nextTime.getInstant());
        QuantityType<?> nextTemp = (QuantityType<?>) readState(CHANNEL_NEXT_SCHEDULE_TEMPERATURE);
        assertEquals(19.0, nextTemp.doubleValue(), 0.001);
    }

    @Test
    void nextScheduleChannelsFallForwardToNextDayWithEntries() throws ReflectiveOperationException {
        // Monday has no more entries after "now"; Tuesday's first entry is the next change.
        double[][][] entries = new double[7][][];
        entries[0] = new double[][] { { 480, 600, 21.0 } };
        entries[1] = new double[][] { { 420, 540, 20.0 } };
        for (int i = 2; i < 7; i++) {
            entries[i] = new double[0][];
        }
        ZonedDateTime mondayEvening = ZonedDateTime.of(2026, 9, 14, 22, 0, 0, 0, ZonedDateTime.now().getZone());

        handler.updateNextScheduleChannels(entries, mondayEvening);

        DateTimeType nextTime = (DateTimeType) readState(CHANNEL_NEXT_SCHEDULE_TIME);
        assertEquals(mondayEvening.plusDays(1).withHour(7).withMinute(0).withSecond(0).withNano(0).toInstant(),
                nextTime.getInstant());
        QuantityType<?> nextTemp = (QuantityType<?>) readState(CHANNEL_NEXT_SCHEDULE_TEMPERATURE);
        assertEquals(20.0, nextTemp.doubleValue(), 0.001);
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
    void manualPresetModeReusesCurrentTargetTemperature() throws ReflectiveOperationException {
        seedState(CHANNEL_TARGET_TEMPERATURE, new QuantityType<>(21.5, SIUnits.CELSIUS));
        ControlUpdateDTO control = new ControlUpdateDTO();
        DeviceConfigUpdateDTO configUpdate = new DeviceConfigUpdateDTO();

        boolean accepted = handler.buildControlUpdate(CHANNEL_PRESET_MODE, new StringType("manual"), control,
                configUpdate);

        assertTrue(accepted);
        assertEquals(CH_MODE_MANUAL, control.ch_mode);
        assertEquals(21.5, Objects.requireNonNull(control.ch_mode_temp), 0.001);
    }

    @Test
    void manualPresetModeWithNoStoredTargetTemperatureOmitsSetpoint() {
        ControlUpdateDTO control = new ControlUpdateDTO();
        DeviceConfigUpdateDTO configUpdate = new DeviceConfigUpdateDTO();

        boolean accepted = handler.buildControlUpdate(CHANNEL_PRESET_MODE, new StringType("manual"), control,
                configUpdate);

        assertTrue(accepted);
        assertEquals(CH_MODE_MANUAL, control.ch_mode);
        assertNull(control.ch_mode_temp);
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
    void vacationTemperatureWriteBundlesFullConfiguration() throws ReflectiveOperationException {
        seedLastConfiguration(sampleConfiguration());
        ControlUpdateDTO control = new ControlUpdateDTO();
        DeviceConfigUpdateDTO configUpdate = new DeviceConfigUpdateDTO();

        boolean accepted = handler.buildControlUpdate(CHANNEL_VACATION_TEMPERATURE,
                new org.openhab.core.library.types.QuantityType<>(13.0, SIUnits.CELSIUS), control, configUpdate);

        assertTrue(accepted);
        assertEquals(13.0, Objects.requireNonNull(configUpdate.ch_vacation_temp), 0.001);
        // Every configuration write must bundle the other confirmed fields alongside the changed one
        // (see fillConfigBundle()) — this one was previously missing the call.
        assertEquals(5, configUpdate.ch_heating_type);
    }

    @Test
    void vacationTemperatureWriteRejectedWithoutPriorPoll() {
        ControlUpdateDTO control = new ControlUpdateDTO();
        DeviceConfigUpdateDTO configUpdate = new DeviceConfigUpdateDTO();

        boolean accepted = handler.buildControlUpdate(CHANNEL_VACATION_TEMPERATURE,
                new org.openhab.core.library.types.QuantityType<>(13.0, SIUnits.CELSIUS), control, configUpdate);

        assertFalse(accepted);
        assertNull(configUpdate.ch_vacation_temp);
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
    void extendDurationAcceptsFifteenMinuteIncrement() {
        ControlUpdateDTO control = new ControlUpdateDTO();
        DeviceConfigUpdateDTO configUpdate = new DeviceConfigUpdateDTO();

        boolean accepted = handler.buildControlUpdate(CHANNEL_EXTEND_DURATION,
                new org.openhab.core.library.types.QuantityType<>(1800, Units.SECOND), control, configUpdate);

        assertTrue(accepted);
        assertEquals(1800L, control.extend_duration);
    }

    @Test
    void extendDurationRejectsNonFifteenMinuteIncrement() {
        ControlUpdateDTO control = new ControlUpdateDTO();
        DeviceConfigUpdateDTO configUpdate = new DeviceConfigUpdateDTO();

        // A value that isn't a whole 15-minute increment doesn't fail safely on the device — it
        // triggers the same physical-confirmation/reboot pathway as a real cancel.
        boolean accepted = handler.buildControlUpdate(CHANNEL_EXTEND_DURATION,
                new org.openhab.core.library.types.QuantityType<>(1000, Units.SECOND), control, configUpdate);

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
    void chScheduleBaseTemperatureWriteResendsEntriesUnchanged() throws ReflectiveOperationException {
        double[][][] entries = { { { 0, 240, 20.5 }, { 1230, 1440, 20.5 } } };
        seedLastChScheduleEntries(entries);

        ScheduleDTO schedule = handler
                .composeChScheduleUpdate(new org.openhab.core.library.types.QuantityType<>(21.0, SIUnits.CELSIUS));

        assertNotNull(schedule);
        assertEquals(21.0, schedule.base_temp, 0.001);
        assertSame(entries, schedule.entries);
    }

    @Test
    void chScheduleBaseTemperatureWriteRejectedWithoutPriorPoll() {
        ScheduleDTO schedule = handler
                .composeChScheduleUpdate(new org.openhab.core.library.types.QuantityType<>(21.0, SIUnits.CELSIUS));

        assertNull(schedule);
    }

    @Test
    void chScheduleBaseTemperatureWriteRejectsNonQuantityCommand() throws ReflectiveOperationException {
        seedLastChScheduleEntries(new double[][][] { { { 0, 1440, 20.5 } } });

        ScheduleDTO schedule = handler.composeChScheduleUpdate(new StringType("21"));

        assertNull(schedule);
    }

    @Test
    void dhwScheduleBaseTemperatureWriteResendsEntriesUnchanged() throws ReflectiveOperationException {
        double[][][] entries = { { { 0, 360, 45.0 }, { 360, 1260, 50.0 }, { 1260, 1440, 45.0 } } };
        seedLastDhwScheduleEntries(entries);

        ScheduleDTO schedule = handler
                .composeDhwScheduleUpdate(new org.openhab.core.library.types.QuantityType<>(48.0, SIUnits.CELSIUS));

        assertNotNull(schedule);
        assertEquals(48.0, schedule.base_temp, 0.001);
        assertSame(entries, schedule.entries);
    }

    @Test
    void dhwScheduleBaseTemperatureWriteRejectedWithoutPriorPoll() {
        ScheduleDTO schedule = handler
                .composeDhwScheduleUpdate(new org.openhab.core.library.types.QuantityType<>(48.0, SIUnits.CELSIUS));

        assertNull(schedule);
    }

    @Test
    void dhwScheduleBaseTemperatureWriteRejectsNonQuantityCommand() throws ReflectiveOperationException {
        seedLastDhwScheduleEntries(new double[][][] { { { 0, 1440, 50.0 } } });

        ScheduleDTO schedule = handler.composeDhwScheduleUpdate(new StringType("48"));

        assertNull(schedule);
    }

    @Test
    void chSchedulePeriodSetReplacesExistingPeriodAndKeepsOtherDaysUntouched() throws ReflectiveOperationException {
        double[][][] entries = new double[7][][];
        entries[0] = new double[][] { { 270, 1440, 48.5 } };
        entries[1] = new double[][] { { 0, 1440, 22.0 } };
        seedLastChScheduleEntries(entries);
        seedState(CHANNEL_CH_SCHEDULE_BASE_TEMPERATURE, new QuantityType<>(22.5, SIUnits.CELSIUS));

        ScheduleDTO schedule = handler.composeChSchedulePeriodSet("monday", 0, 360, 1200, 20.0);

        assertNotNull(schedule);
        assertEquals(22.5, schedule.base_temp, 0.001);
        assertArrayEquals(new double[] { 360, 1200, 20.0 }, schedule.entries[0][0], 0.001);
        assertArrayEquals(entries[1], schedule.entries[1]);
        // The original array is untouched — sendChScheduleUpdate() must send a genuinely new object.
        assertArrayEquals(new double[] { 270, 1440, 48.5 }, entries[0][0], 0.001);
    }

    @Test
    void chSchedulePeriodSetAppendsWhenIndexEqualsDayLength() throws ReflectiveOperationException {
        double[][][] entries = new double[7][][];
        entries[0] = new double[][] { { 0, 360, 18.0 } };
        for (int i = 1; i < 7; i++) {
            entries[i] = new double[0][];
        }
        seedLastChScheduleEntries(entries);
        seedState(CHANNEL_CH_SCHEDULE_BASE_TEMPERATURE, new QuantityType<>(22.5, SIUnits.CELSIUS));

        ScheduleDTO schedule = handler.composeChSchedulePeriodSet("monday", 1, 600, 1200, 21.0);

        assertNotNull(schedule);
        assertEquals(2, schedule.entries[0].length);
        assertArrayEquals(new double[] { 600, 1200, 21.0 }, schedule.entries[0][1], 0.001);
    }

    @Test
    void chSchedulePeriodSetRejectsIndexBeyondAppendPosition() throws ReflectiveOperationException {
        double[][][] entries = new double[7][][];
        entries[0] = new double[][] { { 0, 360, 18.0 } };
        seedLastChScheduleEntries(entries);
        seedState(CHANNEL_CH_SCHEDULE_BASE_TEMPERATURE, new QuantityType<>(22.5, SIUnits.CELSIUS));

        assertNull(handler.composeChSchedulePeriodSet("monday", 5, 600, 1200, 21.0));
    }

    @Test
    void chSchedulePeriodClearRemovesPeriodAndShiftsLaterOnesDown() throws ReflectiveOperationException {
        double[][][] entries = new double[7][][];
        entries[2] = new double[][] { { 0, 360, 18.0 }, { 360, 720, 20.0 }, { 720, 1440, 18.0 } };
        seedLastChScheduleEntries(entries);
        seedState(CHANNEL_CH_SCHEDULE_BASE_TEMPERATURE, new QuantityType<>(22.5, SIUnits.CELSIUS));

        ScheduleDTO schedule = handler.composeChSchedulePeriodClear("wednesday", 0);

        assertNotNull(schedule);
        assertEquals(2, schedule.entries[2].length);
        assertArrayEquals(new double[] { 360, 720, 20.0 }, schedule.entries[2][0], 0.001);
        assertArrayEquals(new double[] { 720, 1440, 18.0 }, schedule.entries[2][1], 0.001);
    }

    @Test
    void chSchedulePeriodClearRejectsIndexAtOrBeyondDayLength() throws ReflectiveOperationException {
        double[][][] entries = new double[7][][];
        entries[0] = new double[][] { { 0, 1440, 18.0 } };
        seedLastChScheduleEntries(entries);
        seedState(CHANNEL_CH_SCHEDULE_BASE_TEMPERATURE, new QuantityType<>(22.5, SIUnits.CELSIUS));

        assertNull(handler.composeChSchedulePeriodClear("monday", 1));
    }

    @Test
    void chSchedulePeriodChangeRejectsUnknownWeekday() throws ReflectiveOperationException {
        seedLastChScheduleEntries(new double[7][][]);
        seedState(CHANNEL_CH_SCHEDULE_BASE_TEMPERATURE, new QuantityType<>(22.5, SIUnits.CELSIUS));

        assertNull(handler.composeChSchedulePeriodSet("someday", 0, 0, 1440, 18.0));
        assertNull(handler.composeChSchedulePeriodClear("", 0));
    }

    @Test
    void chSchedulePeriodChangeIsCaseInsensitiveOnWeekday() throws ReflectiveOperationException {
        double[][][] entries = new double[7][][];
        entries[6] = new double[][] { { 0, 1440, 18.0 } };
        seedLastChScheduleEntries(entries);
        seedState(CHANNEL_CH_SCHEDULE_BASE_TEMPERATURE, new QuantityType<>(22.5, SIUnits.CELSIUS));

        assertNotNull(handler.composeChSchedulePeriodClear("SUNDAY", 0));
    }

    @Test
    void chSchedulePeriodChangeRejectedWithoutPriorPoll() throws ReflectiveOperationException {
        seedState(CHANNEL_CH_SCHEDULE_BASE_TEMPERATURE, new QuantityType<>(22.5, SIUnits.CELSIUS));

        assertNull(handler.composeChSchedulePeriodSet("monday", 0, 0, 1440, 18.0));
    }

    @Test
    void chSchedulePeriodChangeRejectedWithoutBaseTemperatureState() throws ReflectiveOperationException {
        double[][][] entries = new double[7][][];
        entries[0] = new double[][] { { 0, 1440, 18.0 } };
        seedLastChScheduleEntries(entries);

        assertNull(handler.composeChSchedulePeriodSet("monday", 0, 0, 1440, 18.0));
    }

    @Test
    void chSchedulePeriodChangeRejectsNullDayEntriesWithoutThrowing() throws ReflectiveOperationException {
        // A day with no periods at all may arrive as a null slot in the entries array, not just an
        // empty one — must fail gracefully like any other invalid input, not throw.
        seedLastChScheduleEntries(new double[7][][]);
        seedState(CHANNEL_CH_SCHEDULE_BASE_TEMPERATURE, new QuantityType<>(22.5, SIUnits.CELSIUS));

        assertNull(handler.composeChSchedulePeriodSet("monday", 0, 0, 1440, 18.0));
        assertNull(handler.composeChSchedulePeriodClear("monday", 0));
    }

    @Test
    void dhwSchedulePeriodSetAndClearMirrorChSchedule() throws ReflectiveOperationException {
        double[][][] entries = new double[7][][];
        entries[4] = new double[][] { { 0, 360, 45.0 }, { 360, 1260, 50.0 }, { 1260, 1440, 45.0 } };
        seedLastDhwScheduleEntries(entries);
        seedState(CHANNEL_DHW_SCHEDULE_BASE_TEMPERATURE, new QuantityType<>(48.0, SIUnits.CELSIUS));

        ScheduleDTO setResult = handler.composeDhwSchedulePeriodSet("friday", 1, 400, 1200, 55.0);
        assertNotNull(setResult);
        assertArrayEquals(new double[] { 400, 1200, 55.0 }, setResult.entries[4][1], 0.001);

        ScheduleDTO clearResult = handler.composeDhwSchedulePeriodClear("friday", 2);
        assertNotNull(clearResult);
        assertEquals(2, clearResult.entries[4].length);
    }

    @Test
    void timeToTargetPublishesInMinutes() throws IOException, ReflectiveOperationException {
        RetrieveReplyDTO reply = loadRetrieveReply();
        reply.report.ch_time_to_temp = 1800; // 30 minutes

        invokeUpdateChannels(reply);

        QuantityType<?> state = (QuantityType<?>) readState(CHANNEL_TIME_TO_TARGET);
        assertEquals(Units.MINUTE, state.getUnit());
        assertEquals(30.0, state.doubleValue(), 0.001);
    }

    @Test
    void wifiSignalBucketsRawRssiIntoQualityScale() throws IOException, ReflectiveOperationException {
        RetrieveReplyDTO reply = loadRetrieveReply();

        reply.report.rssi = 45; // negated -> -45 dBm -> excellent
        invokeUpdateChannels(reply);
        assertEquals(new DecimalType(4), readState(CHANNEL_WIFI_SIGNAL));

        reply.report.rssi = 75; // -75 dBm -> weak
        invokeUpdateChannels(reply);
        assertEquals(new DecimalType(1), readState(CHANNEL_WIFI_SIGNAL));

        reply.report.rssi = 90; // -90 dBm -> no signal
        invokeUpdateChannels(reply);
        assertEquals(new DecimalType(0), readState(CHANNEL_WIFI_SIGNAL));
    }

    @Test
    void legionellaProtectionTimeReadsAsClockTime() throws IOException, ReflectiveOperationException {
        RetrieveReplyDTO reply = loadRetrieveReply();
        reply.configuration.dhw_legion_time = 435; // 07:15

        invokeUpdateChannels(reply);

        assertEquals(new StringType("07:15"), readState(CHANNEL_LEGIONELLA_PROTECTION_TIME));
    }

    @Test
    void languageDecodesToName() throws IOException, ReflectiveOperationException {
        RetrieveReplyDTO reply = loadRetrieveReply();
        reply.configuration.language = 4;

        invokeUpdateChannels(reply);

        assertEquals(new StringType("german"), readState(CHANNEL_LANGUAGE));
    }

    @Test
    void languageUnknownValueDecodesToUnknown() throws IOException, ReflectiveOperationException {
        RetrieveReplyDTO reply = loadRetrieveReply();
        reply.configuration.language = 99;

        invokeUpdateChannels(reply);

        assertEquals(new StringType("unknown"), readState(CHANNEL_LANGUAGE));
    }

    @Test
    void timeZoneWriteBundlesFullConfiguration() throws ReflectiveOperationException {
        seedLastConfiguration(sampleConfiguration());
        ControlUpdateDTO control = new ControlUpdateDTO();
        DeviceConfigUpdateDTO configUpdate = new DeviceConfigUpdateDTO();

        boolean accepted = handler.buildControlUpdate(CHANNEL_TIME_ZONE, new StringType("amsterdam"), control,
                configUpdate);

        assertTrue(accepted);
        assertEquals(0, configUpdate.time_zone);
    }

    @Test
    void timeZoneWriteRejectsUnknownCity() throws ReflectiveOperationException {
        seedLastConfiguration(sampleConfiguration());
        ControlUpdateDTO control = new ControlUpdateDTO();
        DeviceConfigUpdateDTO configUpdate = new DeviceConfigUpdateDTO();

        assertFalse(handler.buildControlUpdate(CHANNEL_TIME_ZONE, new StringType("atlantis"), control, configUpdate));
    }

    @Test
    void languageWriteBundlesFullConfiguration() throws ReflectiveOperationException {
        seedLastConfiguration(sampleConfiguration());
        ControlUpdateDTO control = new ControlUpdateDTO();
        DeviceConfigUpdateDTO configUpdate = new DeviceConfigUpdateDTO();

        boolean accepted = handler.buildControlUpdate(CHANNEL_LANGUAGE, new StringType("german"), control,
                configUpdate);

        assertTrue(accepted);
        assertEquals(4, configUpdate.language);
    }

    @Test
    void languageWriteRejectsUnknownValue() throws ReflectiveOperationException {
        seedLastConfiguration(sampleConfiguration());
        ControlUpdateDTO control = new ControlUpdateDTO();
        DeviceConfigUpdateDTO configUpdate = new DeviceConfigUpdateDTO();

        assertFalse(handler.buildControlUpdate(CHANNEL_LANGUAGE, new StringType("klingon"), control, configUpdate));
    }

    @Test
    void unhandledChannelIsRejected() {
        ControlUpdateDTO control = new ControlUpdateDTO();
        DeviceConfigUpdateDTO configUpdate = new DeviceConfigUpdateDTO();

        boolean accepted = handler.buildControlUpdate("not-a-real-channel", new StringType("x"), control, configUpdate);

        assertFalse(accepted);
    }

    @Test
    void chControlModeWriteBundlesFullConfiguration() throws ReflectiveOperationException {
        seedLastConfiguration(sampleConfiguration());
        ControlUpdateDTO control = new ControlUpdateDTO();
        DeviceConfigUpdateDTO configUpdate = new DeviceConfigUpdateDTO();

        boolean accepted = handler.buildControlUpdate(CHANNEL_CH_CONTROL_MODE, new StringType("weather-dependent"),
                control, configUpdate);

        assertTrue(accepted);
        assertEquals(CH_CONTROL_MODE_WEATHER, control.ch_control_mode);
        assertEquals(5, configUpdate.ch_heating_type);
        assertEquals(3, configUpdate.ch_isolation);
        assertEquals(2, configUpdate.ch_building_size);
        assertEquals(2, configUpdate.wdr_temps_influence);
        assertEquals(-10.0, Objects.requireNonNull(configUpdate.climate_zone), 0.001);
        assertEquals(0.0, Objects.requireNonNull(configUpdate.wd_temp_offs), 0.001);
        assertEquals(0, configUpdate.summer_eco_mode);
        assertEquals(18.5, Objects.requireNonNull(configUpdate.summer_eco_temp), 0.001);
        assertEquals(0, configUpdate.frost_prot_enabled);
        assertEquals(4.0, Objects.requireNonNull(configUpdate.frost_prot_temp_room), 0.001);
        assertEquals(0.0, Objects.requireNonNull(configUpdate.frost_prot_temp_outs), 0.001);
        assertEquals(1440, configUpdate.max_preheat);
        assertEquals(14.0, Objects.requireNonNull(configUpdate.ch_vacation_temp), 0.001);
        assertEquals(604800L, configUpdate.ch_mode_vacation);
        assertEquals(3600L, configUpdate.ch_mode_extend);
        assertEquals(1, configUpdate.time_zone);
        assertEquals(1, configUpdate.dhw_legion_enabled);
        assertEquals(7, configUpdate.dhw_legion_day);
        assertEquals(420, configUpdate.dhw_legion_time);
    }

    @Test
    void chControlModeWriteRejectedWithoutPriorPoll() {
        ControlUpdateDTO control = new ControlUpdateDTO();
        DeviceConfigUpdateDTO configUpdate = new DeviceConfigUpdateDTO();

        boolean accepted = handler.buildControlUpdate(CHANNEL_CH_CONTROL_MODE, new StringType("thermostat"), control,
                configUpdate);

        assertFalse(accepted);
        assertNull(control.ch_control_mode);
    }

    @Test
    void chControlModeWriteRejectsUnknownValue() throws ReflectiveOperationException {
        seedLastConfiguration(sampleConfiguration());
        ControlUpdateDTO control = new ControlUpdateDTO();
        DeviceConfigUpdateDTO configUpdate = new DeviceConfigUpdateDTO();

        boolean accepted = handler.buildControlUpdate(CHANNEL_CH_CONTROL_MODE, new StringType("auto"), control,
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

    @Test
    void durationChannelsPublishInTheirDocumentedUnit() throws IOException, ReflectiveOperationException {
        RetrieveReplyDTO reply = loadRetrieveReply();
        reply.control.vacation_duration = 3 * 86400L;
        reply.control.extend_duration = 2 * 3600L;
        reply.control.fireplace_duration = 5 * 3600L;

        invokeUpdateChannels(reply);

        QuantityType<?> vacation = (QuantityType<?>) readState(CHANNEL_VACATION_DURATION);
        assertEquals(Units.DAY, vacation.getUnit());
        assertEquals(3.0, vacation.doubleValue(), 0.001);
        QuantityType<?> extend = (QuantityType<?>) readState(CHANNEL_EXTEND_DURATION);
        assertEquals(Units.MINUTE, extend.getUnit());
        assertEquals(120.0, extend.doubleValue(), 0.001);
        QuantityType<?> fireplace = (QuantityType<?>) readState(CHANNEL_FIREPLACE_DURATION);
        assertEquals(Units.HOUR, fireplace.getUnit());
        assertEquals(5.0, fireplace.doubleValue(), 0.001);
    }

    @Test
    void settingsChannelWriteRejectedWithoutPriorPoll() {
        ControlUpdateDTO control = new ControlUpdateDTO();
        DeviceConfigUpdateDTO configUpdate = new DeviceConfigUpdateDTO();

        boolean accepted = handler.buildControlUpdate(CHANNEL_FROST_PROTECTION, new StringType("inside"), control,
                configUpdate);

        assertFalse(accepted);
    }

    @Test
    void frostProtectionWriteBundlesFullConfiguration() throws ReflectiveOperationException {
        seedLastConfiguration(sampleConfiguration());
        ControlUpdateDTO control = new ControlUpdateDTO();
        DeviceConfigUpdateDTO configUpdate = new DeviceConfigUpdateDTO();

        boolean accepted = handler.buildControlUpdate(CHANNEL_FROST_PROTECTION, new StringType("inside"), control,
                configUpdate);

        assertTrue(accepted);
        assertEquals(2, configUpdate.frost_prot_enabled);
        assertEquals(5, configUpdate.ch_heating_type);
    }

    @Test
    void frostProtectionWriteRejectsUnknownValue() throws ReflectiveOperationException {
        seedLastConfiguration(sampleConfiguration());
        ControlUpdateDTO control = new ControlUpdateDTO();
        DeviceConfigUpdateDTO configUpdate = new DeviceConfigUpdateDTO();

        boolean accepted = handler.buildControlUpdate(CHANNEL_FROST_PROTECTION, new StringType("nonsense"), control,
                configUpdate);

        assertFalse(accepted);
    }

    @Test
    void frostProtectionTemperatureRoomWriteConvertsToCelsius() throws ReflectiveOperationException {
        seedLastConfiguration(sampleConfiguration());
        ControlUpdateDTO control = new ControlUpdateDTO();
        DeviceConfigUpdateDTO configUpdate = new DeviceConfigUpdateDTO();

        boolean accepted = handler.buildControlUpdate(CHANNEL_FROST_PROTECTION_TEMPERATURE_ROOM,
                new QuantityType<>(6.0, SIUnits.CELSIUS), control, configUpdate);

        assertTrue(accepted);
        assertEquals(6.0, Objects.requireNonNull(configUpdate.frost_prot_temp_room), 0.001);
    }

    @Test
    void frostProtectionTemperatureOutsideWriteConvertsToCelsius() throws ReflectiveOperationException {
        seedLastConfiguration(sampleConfiguration());
        ControlUpdateDTO control = new ControlUpdateDTO();
        DeviceConfigUpdateDTO configUpdate = new DeviceConfigUpdateDTO();

        boolean accepted = handler.buildControlUpdate(CHANNEL_FROST_PROTECTION_TEMPERATURE_OUTSIDE,
                new QuantityType<>(-2.0, SIUnits.CELSIUS), control, configUpdate);

        assertTrue(accepted);
        assertEquals(-2.0, Objects.requireNonNull(configUpdate.frost_prot_temp_outs), 0.001);
    }

    @Test
    void summerEcoModeWriteSetsIntegerFlag() throws ReflectiveOperationException {
        seedLastConfiguration(sampleConfiguration());
        ControlUpdateDTO control = new ControlUpdateDTO();
        DeviceConfigUpdateDTO configUpdate = new DeviceConfigUpdateDTO();

        boolean accepted = handler.buildControlUpdate(CHANNEL_SUMMER_ECO_MODE, OnOffType.ON, control, configUpdate);

        assertTrue(accepted);
        assertEquals(1, configUpdate.summer_eco_mode);
    }

    @Test
    void summerEcoTemperatureWriteConvertsToCelsius() throws ReflectiveOperationException {
        seedLastConfiguration(sampleConfiguration());
        ControlUpdateDTO control = new ControlUpdateDTO();
        DeviceConfigUpdateDTO configUpdate = new DeviceConfigUpdateDTO();

        boolean accepted = handler.buildControlUpdate(CHANNEL_SUMMER_ECO_TEMPERATURE,
                new QuantityType<>(20.0, SIUnits.CELSIUS), control, configUpdate);

        assertTrue(accepted);
        assertEquals(20.0, Objects.requireNonNull(configUpdate.summer_eco_temp), 0.001);
    }

    @Test
    void heatingTypeWriteMapsNameToDeviceValue() throws ReflectiveOperationException {
        seedLastConfiguration(sampleConfiguration());
        ControlUpdateDTO control = new ControlUpdateDTO();
        DeviceConfigUpdateDTO configUpdate = new DeviceConfigUpdateDTO();

        boolean accepted = handler.buildControlUpdate(CHANNEL_HEATING_TYPE, new StringType("radiator"), control,
                configUpdate);

        assertTrue(accepted);
        assertEquals(3, configUpdate.ch_heating_type);
    }

    @Test
    void insulationWriteMapsNameToDeviceValue() throws ReflectiveOperationException {
        seedLastConfiguration(sampleConfiguration());
        ControlUpdateDTO control = new ControlUpdateDTO();
        DeviceConfigUpdateDTO configUpdate = new DeviceConfigUpdateDTO();

        boolean accepted = handler.buildControlUpdate(CHANNEL_INSULATION, new StringType("good"), control,
                configUpdate);

        assertTrue(accepted);
        assertEquals(3, configUpdate.ch_isolation);
    }

    @Test
    void buildingSizeWriteMapsNameToDeviceValue() throws ReflectiveOperationException {
        seedLastConfiguration(sampleConfiguration());
        ControlUpdateDTO control = new ControlUpdateDTO();
        DeviceConfigUpdateDTO configUpdate = new DeviceConfigUpdateDTO();

        boolean accepted = handler.buildControlUpdate(CHANNEL_BUILDING_SIZE, new StringType("large"), control,
                configUpdate);

        assertTrue(accepted);
        assertEquals(3, configUpdate.ch_building_size);
    }

    @Test
    void wdrTemperatureInfluenceWriteMapsNameToDeviceValue() throws ReflectiveOperationException {
        seedLastConfiguration(sampleConfiguration());
        ControlUpdateDTO control = new ControlUpdateDTO();
        DeviceConfigUpdateDTO configUpdate = new DeviceConfigUpdateDTO();

        boolean accepted = handler.buildControlUpdate(CHANNEL_WDR_TEMPERATURE_INFLUENCE, new StringType("room-control"),
                control, configUpdate);

        assertTrue(accepted);
        assertEquals(4, configUpdate.wdr_temps_influence);
    }

    @Test
    void climateZoneWriteConvertsToCelsius() throws ReflectiveOperationException {
        seedLastConfiguration(sampleConfiguration());
        ControlUpdateDTO control = new ControlUpdateDTO();
        DeviceConfigUpdateDTO configUpdate = new DeviceConfigUpdateDTO();

        boolean accepted = handler.buildControlUpdate(CHANNEL_CLIMATE_ZONE, new QuantityType<>(-12.5, SIUnits.CELSIUS),
                control, configUpdate);

        assertTrue(accepted);
        assertEquals(-12.5, Objects.requireNonNull(configUpdate.climate_zone), 0.001);
    }

    @Test
    void maxPreheatWriteMapsNameToDeviceValue() throws ReflectiveOperationException {
        seedLastConfiguration(sampleConfiguration());
        ControlUpdateDTO control = new ControlUpdateDTO();
        DeviceConfigUpdateDTO configUpdate = new DeviceConfigUpdateDTO();

        boolean accepted = handler.buildControlUpdate(CHANNEL_MAX_PREHEAT, new StringType("2h"), control, configUpdate);

        assertTrue(accepted);
        assertEquals(120, configUpdate.max_preheat);
    }

    @Test
    void legionellaProtectionWriteSetsIntegerFlag() throws ReflectiveOperationException {
        seedLastConfiguration(sampleConfiguration());
        ControlUpdateDTO control = new ControlUpdateDTO();
        DeviceConfigUpdateDTO configUpdate = new DeviceConfigUpdateDTO();

        boolean accepted = handler.buildControlUpdate(CHANNEL_LEGIONELLA_PROTECTION, OnOffType.OFF, control,
                configUpdate);

        assertTrue(accepted);
        assertEquals(0, configUpdate.dhw_legion_enabled);
    }

    @Test
    void legionellaProtectionDayWriteMapsNameToDeviceValue() throws ReflectiveOperationException {
        seedLastConfiguration(sampleConfiguration());
        ControlUpdateDTO control = new ControlUpdateDTO();
        DeviceConfigUpdateDTO configUpdate = new DeviceConfigUpdateDTO();

        boolean accepted = handler.buildControlUpdate(CHANNEL_LEGIONELLA_PROTECTION_DAY, new StringType("wednesday"),
                control, configUpdate);

        assertTrue(accepted);
        assertEquals(3, configUpdate.dhw_legion_day);
    }

    @Test
    void legionellaProtectionTimeWriteParsesHhMm() throws ReflectiveOperationException {
        seedLastConfiguration(sampleConfiguration());
        ControlUpdateDTO control = new ControlUpdateDTO();
        DeviceConfigUpdateDTO configUpdate = new DeviceConfigUpdateDTO();

        boolean accepted = handler.buildControlUpdate(CHANNEL_LEGIONELLA_PROTECTION_TIME, new StringType("06:30"),
                control, configUpdate);

        assertTrue(accepted);
        assertEquals(390, configUpdate.dhw_legion_time);
    }

    @Test
    void legionellaProtectionTimeWriteRejectsInvalidFormat() throws ReflectiveOperationException {
        seedLastConfiguration(sampleConfiguration());
        ControlUpdateDTO control = new ControlUpdateDTO();
        DeviceConfigUpdateDTO configUpdate = new DeviceConfigUpdateDTO();

        assertFalse(handler.buildControlUpdate(CHANNEL_LEGIONELLA_PROTECTION_TIME, new StringType("not-a-time"),
                control, configUpdate));
        assertFalse(handler.buildControlUpdate(CHANNEL_LEGIONELLA_PROTECTION_TIME, new StringType("25:00"), control,
                configUpdate));
    }

    @Test
    void displayBrightnessWriteConvertsToPercent() throws ReflectiveOperationException {
        seedLastConfiguration(sampleConfiguration());
        ControlUpdateDTO control = new ControlUpdateDTO();
        DeviceConfigUpdateDTO configUpdate = new DeviceConfigUpdateDTO();

        boolean accepted = handler.buildControlUpdate(CHANNEL_DISPLAY_BRIGHTNESS, new QuantityType<>(75, Units.PERCENT),
                control, configUpdate);

        assertTrue(accepted);
        assertEquals(75, configUpdate.disp_brightness);
    }

    @Test
    void chSchedulePublishesJsonMatchingPolledEntries() throws IOException, ReflectiveOperationException {
        RetrieveReplyDTO reply = loadRetrieveReply();

        invokeUpdateChannels(reply);

        JsonObject published = JsonParser.parseString(((StringType) readState(CHANNEL_CH_SCHEDULE)).toString())
                .getAsJsonObject();
        assertEquals(reply.schedules.ch_schedule.base_temp, published.get("baseTemp").getAsDouble(), 0.001);
        JsonObject days = published.getAsJsonObject("days");
        assertEquals(7, days.size());
        // entries[0] = Monday — array order must match periodIndex order exactly (see ScheduleJson).
        assertEquals(reply.schedules.ch_schedule.entries[0].length, days.getAsJsonArray("monday").size());
        JsonObject firstPeriod = days.getAsJsonArray("monday").get(0).getAsJsonObject();
        assertEquals((long) reply.schedules.ch_schedule.entries[0][0][0], firstPeriod.get("start").getAsLong());
        assertEquals((long) reply.schedules.ch_schedule.entries[0][0][1], firstPeriod.get("end").getAsLong());
        assertEquals(reply.schedules.ch_schedule.entries[0][0][2], firstPeriod.get("temp").getAsDouble(), 0.001);
    }

    @Test
    void dhwSchedulePublishesJsonMatchingPolledEntries() throws IOException, ReflectiveOperationException {
        RetrieveReplyDTO reply = loadRetrieveReply();

        invokeUpdateChannels(reply);

        JsonObject published = JsonParser.parseString(((StringType) readState(CHANNEL_DHW_SCHEDULE)).toString())
                .getAsJsonObject();
        assertEquals(reply.schedules.dhw_schedule.base_temp, published.get("baseTemp").getAsDouble(), 0.001);
        assertEquals(reply.schedules.dhw_schedule.entries[0].length,
                published.getAsJsonObject("days").getAsJsonArray("monday").size());
    }

    @Test
    void chScheduleIsUndefWithoutAPriorPoll() throws ReflectiveOperationException {
        Method method = AtagOneHandler.class.getDeclaredMethod("publishSchedule", String.class, double.class,
                double[][][].class);
        method.setAccessible(true);
        method.invoke(handler, CHANNEL_CH_SCHEDULE, 22.5, (Object) null);

        assertEquals(org.openhab.core.types.UnDefType.UNDEF, readState(CHANNEL_CH_SCHEDULE));
    }

    /**
     * The contract that matters most for a UI editor: a period's position in the published JSON array
     * must be exactly the {@code periodIndex} the per-period actions expect for that same period — see
     * {@link ScheduleJson}. Picks a non-trivial position (not just index 0) and round-trips it.
     */
    @Test
    void publishedScheduleArrayOrderMatchesPeriodIndexOrder() throws IOException, ReflectiveOperationException {
        RetrieveReplyDTO reply = loadRetrieveReply();
        reply.schedules.ch_schedule.entries[0] = new double[][] { { 0, 240, 18.0 }, { 240, 480, 19.0 },
                { 480, 1440, 18.0 } };
        invokeUpdateChannels(reply);
        seedState(CHANNEL_CH_SCHEDULE_BASE_TEMPERATURE,
                new QuantityType<>(reply.schedules.ch_schedule.base_temp, SIUnits.CELSIUS));

        JsonObject published = JsonParser.parseString(((StringType) readState(CHANNEL_CH_SCHEDULE)).toString())
                .getAsJsonObject();
        int position = 1;
        JsonObject periodAtPosition = published.getAsJsonObject("days").getAsJsonArray("monday").get(position)
                .getAsJsonObject();

        // Replacement stays inside the original [240,480) slot so it can't overlap its neighbors —
        // the point of this test is array-order/index agreement, not overlap rejection.
        ScheduleDTO composed = handler.composeChSchedulePeriodSet("monday", position, 300, 400, 30.0);

        assertNotNull(composed);
        // Replacing at `position` must have overwritten exactly the period that sat at `position` in
        // the published JSON, not some other one — confirming array order and periodIndex agree.
        assertEquals(240, periodAtPosition.get("start").getAsLong());
        assertArrayEquals(new double[] { 300, 400, 30.0 }, composed.entries[0][position], 0.001);
        assertArrayEquals(reply.schedules.ch_schedule.entries[0][0], composed.entries[0][0], 0.001);
        assertArrayEquals(reply.schedules.ch_schedule.entries[0][2], composed.entries[0][2], 0.001);
    }

    @Test
    void chSchedulePeriodSetRejectsEndAtOrBeforeStart() throws ReflectiveOperationException {
        double[][][] entries = new double[7][][];
        entries[0] = new double[][] { { 0, 360, 18.0 } };
        seedLastChScheduleEntries(entries);
        seedState(CHANNEL_CH_SCHEDULE_BASE_TEMPERATURE, new QuantityType<>(22.5, SIUnits.CELSIUS));

        assertNull(handler.composeChSchedulePeriodSet("monday", 0, 600, 600, 20.0));
        assertNull(handler.composeChSchedulePeriodSet("monday", 0, 600, 500, 20.0));
    }

    @Test
    void chSchedulePeriodSetRejectsOverlapWithAnotherPeriodOnTheSameDay() throws ReflectiveOperationException {
        double[][][] entries = new double[7][][];
        // Two existing periods; appending a third that overlaps the second must be rejected.
        entries[0] = new double[][] { { 0, 240, 18.0 }, { 600, 1200, 20.0 } };
        seedLastChScheduleEntries(entries);
        seedState(CHANNEL_CH_SCHEDULE_BASE_TEMPERATURE, new QuantityType<>(22.5, SIUnits.CELSIUS));

        assertNull(handler.composeChSchedulePeriodSet("monday", 2, 900, 1300, 21.0));
    }

    @Test
    void chSchedulePeriodSetAllowsReplacingAPeriodWithSomethingOverlappingItsOwnOldSlot()
            throws ReflectiveOperationException {
        // The period being replaced must be excluded from the overlap comparison — otherwise editing
        // a period's own time range even slightly would always "overlap" its own prior value.
        double[][][] entries = new double[7][][];
        entries[0] = new double[][] { { 0, 600, 18.0 } };
        seedLastChScheduleEntries(entries);
        seedState(CHANNEL_CH_SCHEDULE_BASE_TEMPERATURE, new QuantityType<>(22.5, SIUnits.CELSIUS));

        ScheduleDTO schedule = handler.composeChSchedulePeriodSet("monday", 0, 100, 700, 19.0);

        assertNotNull(schedule);
        assertArrayEquals(new double[] { 100, 700, 19.0 }, schedule.entries[0][0], 0.001);
    }

    @Test
    void chSchedulePeriodSetAllowsBackToBackNonOverlappingPeriods() throws ReflectiveOperationException {
        double[][][] entries = new double[7][][];
        entries[0] = new double[][] { { 0, 600, 18.0 } };
        seedLastChScheduleEntries(entries);
        seedState(CHANNEL_CH_SCHEDULE_BASE_TEMPERATURE, new QuantityType<>(22.5, SIUnits.CELSIUS));

        // Appended period starts exactly when the existing one ends — half-open, not an overlap.
        ScheduleDTO schedule = handler.composeChSchedulePeriodSet("monday", 1, 600, 900, 20.0);

        assertNotNull(schedule);
        assertEquals(2, schedule.entries[0].length);
    }

    @Test
    void dhwSchedulePeriodSetRejectsOverlapWithAnotherPeriodOnTheSameDay() throws ReflectiveOperationException {
        double[][][] entries = new double[7][][];
        entries[4] = new double[][] { { 0, 360, 45.0 }, { 360, 1260, 50.0 } };
        seedLastDhwScheduleEntries(entries);
        seedState(CHANNEL_DHW_SCHEDULE_BASE_TEMPERATURE, new QuantityType<>(48.0, SIUnits.CELSIUS));

        assertNull(handler.composeDhwSchedulePeriodSet("friday", 0, 100, 500, 55.0));
    }

    @Test
    void composeChScheduleFromJsonMergesPartialDaysFromLastPoll() throws ReflectiveOperationException {
        double[][][] entries = new double[7][][];
        entries[0] = new double[][] { { 0, 1440, 18.0 } };
        entries[1] = new double[][] { { 0, 720, 19.0 }, { 720, 1440, 18.0 } };
        seedLastChScheduleEntries(entries);
        seedState(CHANNEL_CH_SCHEDULE_BASE_TEMPERATURE, new QuantityType<>(22.5, SIUnits.CELSIUS));

        ScheduleDTO schedule = handler
                .composeChScheduleFromJson("{\"days\":{\"monday\":[{\"start\":300,\"end\":900,\"temp\":21.0}]}}");

        assertNotNull(schedule);
        assertEquals(22.5, schedule.base_temp, 0.001); // baseTemp omitted -> falls back to current state
        assertEquals(1, schedule.entries[0].length);
        assertArrayEquals(new double[] { 300, 900, 21.0 }, schedule.entries[0][0], 0.001);
        // Tuesday wasn't named in the JSON -> resent byte-for-byte from the last poll.
        assertArrayEquals(entries[1], schedule.entries[1]);
    }

    @Test
    void composeChScheduleFromJsonAppliesExplicitBaseTemp() throws ReflectiveOperationException {
        seedLastChScheduleEntries(new double[7][][]);
        seedState(CHANNEL_CH_SCHEDULE_BASE_TEMPERATURE, new QuantityType<>(22.5, SIUnits.CELSIUS));

        ScheduleDTO schedule = handler.composeChScheduleFromJson("{\"baseTemp\":19.5,\"days\":{}}");

        assertNotNull(schedule);
        assertEquals(19.5, schedule.base_temp, 0.001);
    }

    @Test
    void composeChScheduleFromJsonRejectsMalformedInput() throws ReflectiveOperationException {
        seedLastChScheduleEntries(new double[7][][]);
        seedState(CHANNEL_CH_SCHEDULE_BASE_TEMPERATURE, new QuantityType<>(22.5, SIUnits.CELSIUS));

        assertNull(handler.composeChScheduleFromJson("not json"));
        assertNull(handler.composeChScheduleFromJson("[]"));
        assertNull(handler.composeChScheduleFromJson("{\"days\":{\"someday\":[]}}"));
        assertNull(handler
                .composeChScheduleFromJson("{\"days\":{\"monday\":[{\"start\":600,\"end\":500,\"temp\":20.0}]}}"));
    }

    @Test
    void composeChScheduleFromJsonRejectedWithoutPriorPoll() {
        assertNull(handler.composeChScheduleFromJson("{}"));
    }

    @Test
    void composeDhwScheduleFromJsonMirrorsChSchedule() throws ReflectiveOperationException {
        double[][][] entries = new double[7][][];
        entries[5] = new double[][] { { 0, 1440, 45.0 } };
        seedLastDhwScheduleEntries(entries);
        seedState(CHANNEL_DHW_SCHEDULE_BASE_TEMPERATURE, new QuantityType<>(48.0, SIUnits.CELSIUS));

        ScheduleDTO schedule = handler
                .composeDhwScheduleFromJson("{\"days\":{\"saturday\":[{\"start\":600,\"end\":1200,\"temp\":55.0}]}}");

        assertNotNull(schedule);
        assertArrayEquals(new double[] { 600, 1200, 55.0 }, schedule.entries[5][0], 0.001);
    }
}

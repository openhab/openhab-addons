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
package org.openhab.binding.atagone.internal.action;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.openhab.binding.atagone.internal.AtagOneBindingConstants.*;

import java.lang.reflect.Field;
import java.time.LocalTime;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.atagone.internal.AtagOneHandler;
import org.openhab.binding.atagone.internal.AtagOneStateDescriptionProvider;
import org.openhab.binding.atagone.internal.api.AtagOneApiClient;
import org.openhab.binding.atagone.internal.dto.ControlUpdateDTO;
import org.openhab.binding.atagone.internal.dto.DeviceConfigUpdateDTO;
import org.openhab.binding.atagone.internal.dto.ScheduleDTO;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.State;

/**
 * Unit tests for {@link AtagOneActions}. The handler is a real {@link AtagOneHandler} with a mocked
 * {@link AtagOneApiClient} seeded directly (bypassing pairing/connect), so actions exercise the real
 * compose-and-send path; sends run on the handler's scheduler, so assertions on the mocked API client
 * use {@code timeout()}.
 *
 * @author Florian Lettner - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@NonNullByDefault
class AtagOneActionsTest {

    private static final long VERIFY_TIMEOUT_MS = 2000L;

    private @Mock @NonNullByDefault({}) Thing thing;
    private @Mock @NonNullByDefault({}) HttpClient httpClient;
    private @Mock @NonNullByDefault({}) AtagOneApiClient apiClient;
    private @NonNullByDefault({}) AtagOneHandler handler;
    private @NonNullByDefault({}) AtagOneActions actions;

    @BeforeEach
    void setUp() throws ReflectiveOperationException {
        lenient().when(thing.getUID()).thenReturn(new ThingUID(THING_TYPE_THERMOSTAT, "test"));
        lenient().when(thing.getStatus()).thenReturn(ThingStatus.ONLINE);
        handler = new AtagOneHandler(thing, httpClient, new AtagOneStateDescriptionProvider());
        seedApiClient(apiClient);
        actions = new AtagOneActions();
        actions.setThingHandler(handler);
    }

    @AfterEach
    void tearDown() {
        // A successful send schedules a real poll job on the shared thread pool; cancel it so it
        // doesn't keep firing against this test's mocks after the test method returns.
        handler.dispose();
    }

    private void seedApiClient(AtagOneApiClient client) throws ReflectiveOperationException {
        Field field = AtagOneHandler.class.getDeclaredField("apiClient");
        field.setAccessible(true);
        field.set(handler, client);
    }

    private void seedLastChScheduleEntries(double[][][] entries) throws ReflectiveOperationException {
        Field field = AtagOneHandler.class.getDeclaredField("lastChScheduleEntries");
        field.setAccessible(true);
        field.set(handler, entries);
    }

    private void seedLastDhwScheduleEntries(double[][][] entries) throws ReflectiveOperationException {
        Field field = AtagOneHandler.class.getDeclaredField("lastDhwScheduleEntries");
        field.setAccessible(true);
        field.set(handler, entries);
    }

    @SuppressWarnings("unchecked")
    private void seedState(String channelId, State state) throws ReflectiveOperationException {
        Field field = AtagOneHandler.class.getDeclaredField("stateMap");
        field.setAccessible(true);
        Object fieldValue = Objects.requireNonNull(field.get(handler));
        ((Map<String, State>) fieldValue).put(channelId, state);
    }

    private void seedArmedStartVacation(long epochOffset) throws ReflectiveOperationException {
        Field field = AtagOneHandler.class.getDeclaredField("armedStartVacation");
        field.setAccessible(true);
        field.set(handler, epochOffset);
    }

    @Test
    void setThingHandlerIgnoresUnrelatedHandlerType() {
        AtagOneActions freshActions = new AtagOneActions();
        ThingHandler other = mock(ThingHandler.class);

        freshActions.setThingHandler(other);

        assertNull(freshActions.getThingHandler());
    }

    @Test
    void getThingHandlerReturnsBoundHandler() {
        assertEquals(handler, actions.getThingHandler());
    }

    @Test
    void activateVacationWithNoBoundHandlerDoesNotThrow() {
        AtagOneActions freshActions = new AtagOneActions();

        assertDoesNotThrow(() -> freshActions.activateVacation(1));
    }

    @Test
    void activateVacationRejectsNonPositiveDuration() throws Exception {
        actions.activateVacation(0);
        actions.activateVacation(-10);

        verify(apiClient, never()).updateControl(any(), any());
    }

    @Test
    void activateVacationComposesHolidayActivationWithExplicitDuration() throws Exception {
        actions.activateVacation(2);

        ArgumentCaptor<ControlUpdateDTO> control = ArgumentCaptor.forClass(ControlUpdateDTO.class);
        verify(apiClient, timeout(VERIFY_TIMEOUT_MS)).updateControl(control.capture(), any());

        assertEquals(CH_MODE_HOLIDAY, control.getValue().ch_mode);
        assertEquals(2 * 86400L, control.getValue().ch_mode_duration);
        assertEquals(2 * 86400L, control.getValue().vacation_duration);
    }

    @Test
    void activateExtendRejectsNonPositiveDuration() throws Exception {
        actions.activateExtend(0);

        verify(apiClient, never()).updateControl(any(), any());
    }

    @Test
    void activateExtendComposesExtendActivationWithExplicitDuration() throws Exception {
        actions.activateExtend(120);

        ArgumentCaptor<ControlUpdateDTO> control = ArgumentCaptor.forClass(ControlUpdateDTO.class);
        verify(apiClient, timeout(VERIFY_TIMEOUT_MS)).updateControl(control.capture(), any());

        assertEquals(CH_MODE_EXTEND, control.getValue().ch_mode);
        assertEquals(2 * 3600L, control.getValue().extend_duration);
        // extend_duration is additive to the schedule-boundary time — ch_mode_duration must stay unset.
        assertNull(control.getValue().ch_mode_duration);
    }

    @Test
    void activateExtendAcceptsFifteenMinuteIncrement() throws Exception {
        actions.activateExtend(30);

        verify(apiClient, timeout(VERIFY_TIMEOUT_MS)).updateControl(any(), any());
    }

    @Test
    void activateExtendRejectsNonFifteenMinuteIncrement() throws Exception {
        actions.activateExtend(10);

        verify(apiClient, never()).updateControl(any(), any());
    }

    @Test
    void activateFireplaceRejectsNonPositiveDuration() throws Exception {
        actions.activateFireplace(0);

        verify(apiClient, never()).updateControl(any(), any());
    }

    @Test
    void activateFireplaceComposesFireplaceActivationWithExplicitDuration() throws Exception {
        actions.activateFireplace(1);

        ArgumentCaptor<ControlUpdateDTO> control = ArgumentCaptor.forClass(ControlUpdateDTO.class);
        verify(apiClient, timeout(VERIFY_TIMEOUT_MS)).updateControl(control.capture(), any());

        assertEquals(CH_MODE_FIREPLACE, control.getValue().ch_mode);
        assertEquals(3600L, control.getValue().fireplace_duration);
        assertEquals(3600L, control.getValue().ch_mode_duration);
    }

    @Test
    void activateFireplaceRejectsNegativeHours() throws Exception {
        actions.activateFireplace(-1);

        verify(apiClient, never()).updateControl(any(), any());
    }

    @Test
    void cancelModeFromPendingVacationClearsSchedule() throws Exception {
        // A pending (future-scheduled, not-yet-active) vacation reports preset-mode=auto. Keying
        // cancellation only on reported preset-mode would silently leave the schedule fully armed
        // while returning "nothing to cancel".
        seedState(CHANNEL_PRESET_MODE, new StringType("auto"));
        seedArmedStartVacation(841477166L);

        boolean requiresPhysicalConfirmation = actions.cancelMode();

        assertFalse(requiresPhysicalConfirmation);
        ArgumentCaptor<ControlUpdateDTO> control = ArgumentCaptor.forClass(ControlUpdateDTO.class);
        ArgumentCaptor<DeviceConfigUpdateDTO> configUpdate = ArgumentCaptor.forClass(DeviceConfigUpdateDTO.class);
        verify(apiClient, timeout(VERIFY_TIMEOUT_MS)).updateControl(control.capture(), configUpdate.capture());
        assertEquals(0L, control.getValue().vacation_duration);
        assertEquals(0L, configUpdate.getValue().start_vacation);
    }

    @Test
    void cancelModeFromAutoDoesNotRequirePhysicalConfirmation() throws Exception {
        boolean requiresPhysicalConfirmation = actions.cancelMode();

        assertFalse(requiresPhysicalConfirmation);
        ArgumentCaptor<ControlUpdateDTO> control = ArgumentCaptor.forClass(ControlUpdateDTO.class);
        verify(apiClient, timeout(VERIFY_TIMEOUT_MS)).updateControl(control.capture(), any());
        assertEquals(CH_MODE_AUTO, control.getValue().ch_mode);
        assertEquals(0L, control.getValue().ch_mode_duration);
    }

    @Test
    void cancelModeFromFireplaceRequiresPhysicalConfirmation() throws ReflectiveOperationException {
        seedState(CHANNEL_PRESET_MODE, new StringType("fireplace"));

        boolean requiresPhysicalConfirmation = actions.cancelMode();

        assertTrue(requiresPhysicalConfirmation);
    }

    @Test
    void cancelModeFromHolidayClearsVacationSchedule() throws Exception {
        seedState(CHANNEL_PRESET_MODE, new StringType("holiday"));

        actions.cancelMode();

        ArgumentCaptor<ControlUpdateDTO> control = ArgumentCaptor.forClass(ControlUpdateDTO.class);
        ArgumentCaptor<DeviceConfigUpdateDTO> configUpdate = ArgumentCaptor.forClass(DeviceConfigUpdateDTO.class);
        verify(apiClient, timeout(VERIFY_TIMEOUT_MS)).updateControl(control.capture(), configUpdate.capture());
        assertEquals(0L, control.getValue().vacation_duration);
        assertEquals(0L, configUpdate.getValue().start_vacation);
    }

    @Test
    void staticDelegatesCallThroughToInstanceMethods() throws Exception {
        AtagOneActions.activateFireplace(actions, 1);

        verify(apiClient, timeout(VERIFY_TIMEOUT_MS)).updateControl(any(), any());
    }

    @Test
    void setChSchedulePeriodComposesAndSendsOnSuccess() throws Exception {
        double[][][] entries = new double[7][][];
        entries[0] = new double[][] { { 0, 360, 18.0 } };
        seedLastChScheduleEntries(entries);
        seedState(CHANNEL_CH_SCHEDULE_BASE_TEMPERATURE, new QuantityType<>(22.5, SIUnits.CELSIUS));

        boolean accepted = actions.setChSchedulePeriod("monday", LocalTime.of(10, 0), LocalTime.of(20, 0), 20.0);

        assertTrue(accepted);
        ArgumentCaptor<ScheduleDTO> schedule = ArgumentCaptor.forClass(ScheduleDTO.class);
        verify(apiClient, timeout(VERIFY_TIMEOUT_MS)).updateChSchedule(schedule.capture());
        assertEquals(2, schedule.getValue().entries[0].length);
    }

    @Test
    void setChSchedulePeriodTreatsMidnightEndAsEndOfDay() throws Exception {
        double[][][] entries = new double[7][][];
        entries[0] = new double[][] {};
        seedLastChScheduleEntries(entries);
        seedState(CHANNEL_CH_SCHEDULE_BASE_TEMPERATURE, new QuantityType<>(22.5, SIUnits.CELSIUS));

        boolean accepted = actions.setChSchedulePeriod("monday", LocalTime.of(22, 0), LocalTime.MIDNIGHT, 20.0);

        assertTrue(accepted);
        ArgumentCaptor<ScheduleDTO> schedule = ArgumentCaptor.forClass(ScheduleDTO.class);
        verify(apiClient, timeout(VERIFY_TIMEOUT_MS)).updateChSchedule(schedule.capture());
        assertEquals(1320, schedule.getValue().entries[0][0][0]);
        assertEquals(1440, schedule.getValue().entries[0][0][1]);
    }

    @Test
    void setChSchedulePeriodAcceptsTimeStrings() throws Exception {
        double[][][] entries = new double[7][][];
        entries[0] = new double[][] {};
        seedLastChScheduleEntries(entries);
        seedState(CHANNEL_CH_SCHEDULE_BASE_TEMPERATURE, new QuantityType<>(22.5, SIUnits.CELSIUS));

        boolean accepted = actions.setChSchedulePeriod("monday", "06:00", "22:00", 20.0);

        assertTrue(accepted);
        ArgumentCaptor<ScheduleDTO> schedule = ArgumentCaptor.forClass(ScheduleDTO.class);
        verify(apiClient, timeout(VERIFY_TIMEOUT_MS)).updateChSchedule(schedule.capture());
        assertEquals(360, schedule.getValue().entries[0][0][0]);
        assertEquals(1320, schedule.getValue().entries[0][0][1]);
    }

    @Test
    void setChSchedulePeriodRejectsMalformedTimeStringWithoutSending() throws Exception {
        double[][][] entries = new double[7][][];
        entries[0] = new double[][] {};
        seedLastChScheduleEntries(entries);
        seedState(CHANNEL_CH_SCHEDULE_BASE_TEMPERATURE, new QuantityType<>(22.5, SIUnits.CELSIUS));

        boolean accepted = actions.setChSchedulePeriod("monday", "6am", "22:00", 20.0);

        assertFalse(accepted);
        verify(apiClient, never()).updateChSchedule(any());
    }

    @Test
    void clearChSchedulePeriodAcceptsTimeString() throws Exception {
        double[][][] entries = new double[7][][];
        entries[0] = new double[][] { { 0, 360, 18.0 }, { 360, 1440, 20.0 } };
        seedLastChScheduleEntries(entries);
        seedState(CHANNEL_CH_SCHEDULE_BASE_TEMPERATURE, new QuantityType<>(22.5, SIUnits.CELSIUS));

        assertTrue(actions.clearChSchedulePeriod("monday", "06:00"));
        assertFalse(actions.clearChSchedulePeriod("monday", "6am"));
        verify(apiClient, timeout(VERIFY_TIMEOUT_MS)).updateChSchedule(any());
    }

    @Test
    void setChSchedulePeriodTrimsEarlierPeriodThatOverlapsTheNewStart() throws Exception {
        double[][][] entries = new double[7][][];
        entries[0] = new double[][] { { 0, 600, 18.0 } };
        seedLastChScheduleEntries(entries);
        seedState(CHANNEL_CH_SCHEDULE_BASE_TEMPERATURE, new QuantityType<>(22.5, SIUnits.CELSIUS));

        boolean accepted = actions.setChSchedulePeriod("monday", LocalTime.of(5, 0), LocalTime.of(15, 0), 20.0);

        assertTrue(accepted);
        ArgumentCaptor<ScheduleDTO> schedule = ArgumentCaptor.forClass(ScheduleDTO.class);
        verify(apiClient, timeout(VERIFY_TIMEOUT_MS)).updateChSchedule(schedule.capture());
        assertEquals(300, schedule.getValue().entries[0][0][1]);
    }

    @Test
    void setChSchedulePeriodRejectsInvalidWeekdayWithoutSending() throws ReflectiveOperationException {
        seedLastChScheduleEntries(new double[7][][]);
        seedState(CHANNEL_CH_SCHEDULE_BASE_TEMPERATURE, new QuantityType<>(22.5, SIUnits.CELSIUS));

        boolean accepted = actions.setChSchedulePeriod("someday", LocalTime.MIDNIGHT, LocalTime.MIDNIGHT, 18.0);

        assertFalse(accepted);
    }

    @Test
    void setChSchedulePeriodRejectsOverlapWithALaterPeriodWithoutSending() throws ReflectiveOperationException {
        double[][][] entries = new double[7][][];
        entries[0] = new double[][] { { 600, 900, 18.0 } };
        seedLastChScheduleEntries(entries);
        seedState(CHANNEL_CH_SCHEDULE_BASE_TEMPERATURE, new QuantityType<>(22.5, SIUnits.CELSIUS));

        boolean accepted = actions.setChSchedulePeriod("monday", LocalTime.of(5, 0), LocalTime.of(12, 0), 20.0);

        assertFalse(accepted);
    }

    @Test
    void setChScheduleRejectsOverlappingPeriodsWithoutSending() throws ReflectiveOperationException {
        seedLastChScheduleEntries(new double[7][][]);
        seedState(CHANNEL_CH_SCHEDULE_BASE_TEMPERATURE, new QuantityType<>(22.5, SIUnits.CELSIUS));

        boolean accepted = actions.setChSchedule(
                "{\"days\":{\"monday\":[{\"start\":0,\"end\":600,\"temp\":18},{\"start\":300,\"end\":900,\"temp\":20}]}}");

        assertFalse(accepted);
    }

    @Test
    void setChSchedulePeriodWithNoBoundHandlerDoesNotThrow() {
        AtagOneActions freshActions = new AtagOneActions();

        assertDoesNotThrow(
                () -> freshActions.setChSchedulePeriod("monday", LocalTime.MIDNIGHT, LocalTime.MIDNIGHT, 18.0));
    }

    @Test
    void clearChSchedulePeriodComposesAndSendsOnSuccess() throws Exception {
        double[][][] entries = new double[7][][];
        entries[2] = new double[][] { { 0, 720, 18.0 }, { 720, 1440, 20.0 } };
        seedLastChScheduleEntries(entries);
        seedState(CHANNEL_CH_SCHEDULE_BASE_TEMPERATURE, new QuantityType<>(22.5, SIUnits.CELSIUS));

        boolean accepted = actions.clearChSchedulePeriod("wednesday", LocalTime.MIDNIGHT);

        assertTrue(accepted);
        ArgumentCaptor<ScheduleDTO> schedule = ArgumentCaptor.forClass(ScheduleDTO.class);
        verify(apiClient, timeout(VERIFY_TIMEOUT_MS)).updateChSchedule(schedule.capture());
        assertEquals(1, schedule.getValue().entries[2].length);
    }

    @Test
    void clearChSchedulePeriodRejectsStartThatMatchesNoPeriodWithoutSending() throws ReflectiveOperationException {
        seedLastChScheduleEntries(new double[7][][]);
        seedState(CHANNEL_CH_SCHEDULE_BASE_TEMPERATURE, new QuantityType<>(22.5, SIUnits.CELSIUS));

        boolean accepted = actions.clearChSchedulePeriod("monday", LocalTime.NOON);

        assertFalse(accepted);
    }

    @Test
    void setDhwSchedulePeriodComposesAndSendsOnSuccess() throws Exception {
        double[][][] entries = new double[7][][];
        entries[5] = new double[][] { { 0, 360, 45.0 } };
        seedLastDhwScheduleEntries(entries);
        seedState(CHANNEL_DHW_SCHEDULE_BASE_TEMPERATURE, new QuantityType<>(48.0, SIUnits.CELSIUS));

        boolean accepted = actions.setDhwSchedulePeriod("saturday", LocalTime.of(10, 0), LocalTime.of(20, 0), 55.0);

        assertTrue(accepted);
        verify(apiClient, timeout(VERIFY_TIMEOUT_MS)).updateDhwSchedule(any());
    }

    @Test
    void clearDhwSchedulePeriodRejectsInvalidWeekdayWithoutSending() throws ReflectiveOperationException {
        seedLastDhwScheduleEntries(new double[7][][]);
        seedState(CHANNEL_DHW_SCHEDULE_BASE_TEMPERATURE, new QuantityType<>(48.0, SIUnits.CELSIUS));

        boolean accepted = actions.clearDhwSchedulePeriod("someday", LocalTime.MIDNIGHT);

        assertFalse(accepted);
    }

    @Test
    void staticScheduleDelegatesCallThroughToInstanceMethods() throws Exception {
        double[][][] entries = new double[7][][];
        entries[0] = new double[][] { { 0, 1440, 18.0 } };
        seedLastChScheduleEntries(entries);
        seedState(CHANNEL_CH_SCHEDULE_BASE_TEMPERATURE, new QuantityType<>(22.5, SIUnits.CELSIUS));

        boolean accepted = AtagOneActions.clearChSchedulePeriod(actions, "monday", LocalTime.MIDNIGHT);

        assertTrue(accepted);
        verify(apiClient, timeout(VERIFY_TIMEOUT_MS)).updateChSchedule(any());
    }

    @Test
    void setChScheduleComposesAndSendsOnSuccess() throws Exception {
        double[][][] entries = new double[7][][];
        entries[0] = new double[][] { { 0, 1440, 18.0 } };
        seedLastChScheduleEntries(entries);
        seedState(CHANNEL_CH_SCHEDULE_BASE_TEMPERATURE, new QuantityType<>(22.5, SIUnits.CELSIUS));

        boolean accepted = actions
                .setChSchedule("{\"days\":{\"monday\":[{\"start\":360,\"end\":1200,\"temp\":20.0}]}}");

        assertTrue(accepted);
        ArgumentCaptor<ScheduleDTO> schedule = ArgumentCaptor.forClass(ScheduleDTO.class);
        verify(apiClient, timeout(VERIFY_TIMEOUT_MS)).updateChSchedule(schedule.capture());
        assertArrayEquals(new double[] { 360, 1200, 20.0 }, schedule.getValue().entries[0][0], 0.001);
    }

    @Test
    void setChScheduleRejectsMalformedJsonWithoutSending() throws ReflectiveOperationException {
        seedLastChScheduleEntries(new double[7][][]);
        seedState(CHANNEL_CH_SCHEDULE_BASE_TEMPERATURE, new QuantityType<>(22.5, SIUnits.CELSIUS));

        boolean accepted = actions.setChSchedule("not json");

        assertFalse(accepted);
    }

    @Test
    void setChScheduleWithNoBoundHandlerDoesNotThrow() {
        AtagOneActions freshActions = new AtagOneActions();

        assertDoesNotThrow(() -> freshActions.setChSchedule("{}"));
    }

    @Test
    void setDhwScheduleComposesAndSendsOnSuccess() throws Exception {
        double[][][] entries = new double[7][][];
        entries[5] = new double[][] { { 0, 1440, 45.0 } };
        seedLastDhwScheduleEntries(entries);
        seedState(CHANNEL_DHW_SCHEDULE_BASE_TEMPERATURE, new QuantityType<>(48.0, SIUnits.CELSIUS));

        boolean accepted = actions.setDhwSchedule(
                "{\"baseTemp\":50.0,\"days\":{\"saturday\":[{\"start\":600,\"end\":1200,\"temp\":55.0}]}}");

        assertTrue(accepted);
        ArgumentCaptor<ScheduleDTO> schedule = ArgumentCaptor.forClass(ScheduleDTO.class);
        verify(apiClient, timeout(VERIFY_TIMEOUT_MS)).updateDhwSchedule(schedule.capture());
        assertEquals(50.0, schedule.getValue().base_temp, 0.001);
        assertArrayEquals(new double[] { 600, 1200, 55.0 }, schedule.getValue().entries[5][0], 0.001);
    }

    @Test
    void setDhwScheduleRejectsWithoutPriorPollWithoutSending() {
        boolean accepted = actions.setDhwSchedule("{}");

        assertFalse(accepted);
    }

    @Test
    void staticSetChScheduleDelegateCallsThroughToInstanceMethod() throws Exception {
        double[][][] entries = new double[7][][];
        entries[0] = new double[][] { { 0, 1440, 18.0 } };
        seedLastChScheduleEntries(entries);
        seedState(CHANNEL_CH_SCHEDULE_BASE_TEMPERATURE, new QuantityType<>(22.5, SIUnits.CELSIUS));

        boolean accepted = AtagOneActions.setChSchedule(actions, "{}");

        assertTrue(accepted);
        verify(apiClient, timeout(VERIFY_TIMEOUT_MS)).updateChSchedule(any());
    }
}

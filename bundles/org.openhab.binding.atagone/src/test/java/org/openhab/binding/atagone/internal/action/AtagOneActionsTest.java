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
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.atagone.internal.AtagOneHandler;
import org.openhab.binding.atagone.internal.dto.ControlUpdateDTO;
import org.openhab.binding.atagone.internal.dto.DeviceConfigUpdateDTO;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.State;

/**
 * Unit tests for {@link AtagOneActions}. The handler is a Mockito spy wrapping a real
 * {@link AtagOneHandler} with {@code sendComposedUpdate} stubbed out — this exercises the handler's
 * real {@code composeXxx} composition logic (the same logic {@code buildControlUpdate} uses for the
 * channel path) while capturing what would have been sent, without needing a live device connection.
 *
 * @author Florian Lettner - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@NonNullByDefault
class AtagOneActionsTest {

    private @Mock @NonNullByDefault({}) Thing thing;
    private @Mock @NonNullByDefault({}) HttpClient httpClient;
    private @NonNullByDefault({}) AtagOneHandler handler;
    private @NonNullByDefault({}) AtagOneActions actions;

    @BeforeEach
    void setUp() {
        handler = spy(new AtagOneHandler(thing, httpClient));
        lenient().doNothing().when(handler).sendComposedUpdate(anyString(), any(), any());
        actions = new AtagOneActions();
        actions.setThingHandler(handler);
    }

    /** Directly seeds the handler's private stateMap, simulating a previously-polled channel value. */
    @SuppressWarnings("unchecked")
    private void seedState(String channelId, State state) throws ReflectiveOperationException {
        Field field = AtagOneHandler.class.getDeclaredField("stateMap");
        field.setAccessible(true);
        Object fieldValue = Objects.requireNonNull(field.get(handler));
        ((Map<String, State>) fieldValue).put(channelId, state);
    }

    /** Directly sets the handler's armed (possibly pending) vacation start, simulating a prior poll. */
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

        assertDoesNotThrow(() -> freshActions.activateVacation(3600));
    }

    @Test
    void activateVacationRejectsNonPositiveDuration() {
        actions.activateVacation(0);
        actions.activateVacation(-10);

        verify(handler, never()).sendComposedUpdate(anyString(), any(), any());
    }

    @Test
    void activateVacationComposesHolidayActivationWithExplicitDuration() {
        actions.activateVacation(2 * 86400); // 2 whole days

        ArgumentCaptor<ControlUpdateDTO> control = ArgumentCaptor.forClass(ControlUpdateDTO.class);
        ArgumentCaptor<DeviceConfigUpdateDTO> configUpdate = ArgumentCaptor.forClass(DeviceConfigUpdateDTO.class);
        verify(handler).sendComposedUpdate(eq("action:activateVacation"), control.capture(), configUpdate.capture());

        assertEquals(CH_MODE_HOLIDAY, control.getValue().ch_mode);
        assertEquals(2 * 86400L, control.getValue().ch_mode_duration);
        assertEquals(2 * 86400L, control.getValue().vacation_duration);
        assertNotNull(configUpdate.getValue().start_vacation);
    }

    @Test
    void activateVacationRejectsNonWholeDay() {
        actions.activateVacation(12 * 3600); // 12 hours — not a whole day

        verify(handler, never()).sendComposedUpdate(anyString(), any(), any());
    }

    @Test
    void activateExtendRejectsNonPositiveDuration() {
        actions.activateExtend(0);

        verify(handler, never()).sendComposedUpdate(anyString(), any(), any());
    }

    @Test
    void activateExtendComposesExtendActivationWithExplicitDuration() {
        actions.activateExtend(2 * 3600); // 2 whole hours

        ArgumentCaptor<ControlUpdateDTO> control = ArgumentCaptor.forClass(ControlUpdateDTO.class);
        verify(handler).sendComposedUpdate(eq("action:activateExtend"), control.capture(), any());

        assertEquals(CH_MODE_EXTEND, control.getValue().ch_mode);
        assertEquals(2 * 3600L, control.getValue().extend_duration);
        // extend_duration is additive to the schedule-boundary time — ch_mode_duration must stay unset.
        assertNull(control.getValue().ch_mode_duration);
    }

    @Test
    void activateExtendRejectsNonWholeHour() {
        actions.activateExtend(1800); // 30 minutes — not a whole hour

        verify(handler, never()).sendComposedUpdate(anyString(), any(), any());
    }

    @Test
    void activateFireplaceRejectsNonPositiveDuration() {
        actions.activateFireplace(0);

        verify(handler, never()).sendComposedUpdate(anyString(), any(), any());
    }

    @Test
    void activateFireplaceComposesFireplaceActivationWithExplicitDuration() {
        actions.activateFireplace(3600);

        ArgumentCaptor<ControlUpdateDTO> control = ArgumentCaptor.forClass(ControlUpdateDTO.class);
        verify(handler).sendComposedUpdate(eq("action:activateFireplace"), control.capture(), any());

        assertEquals(CH_MODE_FIREPLACE, control.getValue().ch_mode);
        assertEquals(3600L, control.getValue().fireplace_duration);
        assertEquals(3600L, control.getValue().ch_mode_duration);
    }

    @Test
    void activateFireplaceRejectsNonWholeHour() {
        // 2400s (40 minutes) is the exact value confirmed to trigger a real device reboot — must be
        // rejected before it ever reaches the device.
        actions.activateFireplace(2400);

        verify(handler, never()).sendComposedUpdate(anyString(), any(), any());
    }

    @Test
    void cancelModeFromPendingVacationClearsSchedule() throws ReflectiveOperationException {
        // A pending (future-scheduled, not-yet-active) vacation reports preset-mode=auto. Keying
        // cancellation only on reported preset-mode would silently leave the schedule fully armed
        // while returning "nothing to cancel".
        seedState(CHANNEL_PRESET_MODE, new StringType("auto"));
        seedArmedStartVacation(841477166L);

        boolean requiresPhysicalConfirmation = actions.cancelMode();

        assertFalse(requiresPhysicalConfirmation);
        ArgumentCaptor<ControlUpdateDTO> control = ArgumentCaptor.forClass(ControlUpdateDTO.class);
        ArgumentCaptor<DeviceConfigUpdateDTO> configUpdate = ArgumentCaptor.forClass(DeviceConfigUpdateDTO.class);
        verify(handler).sendComposedUpdate(eq("action:cancelMode"), control.capture(), configUpdate.capture());
        assertEquals(0L, control.getValue().vacation_duration);
        assertEquals(0L, configUpdate.getValue().start_vacation);
    }

    @Test
    void cancelModeFromAutoDoesNotRequirePhysicalConfirmation() {
        boolean requiresPhysicalConfirmation = actions.cancelMode();

        assertFalse(requiresPhysicalConfirmation);
        ArgumentCaptor<ControlUpdateDTO> control = ArgumentCaptor.forClass(ControlUpdateDTO.class);
        verify(handler).sendComposedUpdate(eq("action:cancelMode"), control.capture(), any());
        assertEquals(CH_MODE_AUTO, control.getValue().ch_mode);
        assertEquals(0L, control.getValue().ch_mode_duration);
    }

    @Test
    void cancelModeFromFireplaceRequiresPhysicalConfirmation() throws ReflectiveOperationException {
        seedState(CHANNEL_PRESET_MODE, new StringType("fireplace"));

        boolean requiresPhysicalConfirmation = actions.cancelMode();

        assertTrue(requiresPhysicalConfirmation);
        verify(handler).sendComposedUpdate(eq("action:cancelMode"), any(), any());
    }

    @Test
    void cancelModeFromHolidayClearsVacationSchedule() throws ReflectiveOperationException {
        seedState(CHANNEL_PRESET_MODE, new StringType("holiday"));

        actions.cancelMode();

        ArgumentCaptor<ControlUpdateDTO> control = ArgumentCaptor.forClass(ControlUpdateDTO.class);
        ArgumentCaptor<DeviceConfigUpdateDTO> configUpdate = ArgumentCaptor.forClass(DeviceConfigUpdateDTO.class);
        verify(handler).sendComposedUpdate(eq("action:cancelMode"), control.capture(), configUpdate.capture());
        assertEquals(0L, control.getValue().vacation_duration);
        assertEquals(0L, configUpdate.getValue().start_vacation);
    }

    @Test
    void staticDelegatesCallThroughToInstanceMethods() {
        AtagOneActions.activateFireplace(actions, 3600);

        verify(handler).sendComposedUpdate(eq("action:activateFireplace"), any(), any());
    }
}

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
package org.openhab.binding.evohome.internal.action;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.openhab.binding.evohome.internal.handler.EvohomeTemperatureControlSystemHandler;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingHandler;

/**
 * Tests system action expiry conversion, command acceptance and handler detachment.
 *
 * @author Kovacs Istvan - Initial contribution
 */
@NonNullByDefault
class EvohomeSystemActionsTest {

    private final EvohomeTemperatureControlSystemHandler handler = mock(EvohomeTemperatureControlSystemHandler.class);
    private final EvohomeSystemActions actions = new EvohomeSystemActions();

    @Test
    void explicitExpiryPreservesOffsetAndExactInstant() {
        actions.setThingHandler(handler);
        ZonedDateTime until = ZonedDateTime.parse("2030-01-17T20:15:42+05:30");
        Instant expected = Instant.parse("2030-01-17T14:45:42Z");
        when(handler.setTemporarySystemMode("Away", expected)).thenReturn(true);

        assertTrue(EvohomeSystemActions.setTemporarySystemMode(actions, "Away", until));

        verify(handler).setTemporarySystemMode("Away", expected);
        verifyNoMoreInteractions(handler);
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void actionsReturnActualHandlerAcceptance(boolean accepted) {
        actions.setThingHandler(handler);
        when(handler.setSystemMode("Auto")).thenReturn(accepted);
        when(handler.setTemporarySystemMode(eq("Away"), any(Instant.class))).thenReturn(accepted);

        assertEquals(accepted, EvohomeSystemActions.setSystemMode(actions, "Auto"));
        assertEquals(accepted,
                actions.setTemporarySystemMode("Away", ZonedDateTime.parse("2030-01-17T20:15:00+01:00")));
        assertEquals(accepted, EvohomeSystemActions.setTemporarySystemModeFor(actions, "Away", 90));
    }

    @ParameterizedTest
    @ValueSource(ints = { 1, 1441, 40320, Integer.MAX_VALUE })
    void durationUsesElapsedMinutesWithoutZoneOverrideLimitOrIntegerOverflow(int minutes) {
        actions.setThingHandler(handler);
        when(handler.setTemporarySystemMode(eq("Away"), any(Instant.class))).thenReturn(true);
        Instant earliest = Instant.now().plusSeconds(minutes * 60L);

        assertTrue(actions.setTemporarySystemModeFor("Away", minutes));

        Instant latest = Instant.now().plusSeconds(minutes * 60L);
        ArgumentCaptor<Instant> until = ArgumentCaptor.forClass(Instant.class);
        verify(handler).setTemporarySystemMode(eq("Away"), until.capture());
        Instant sentUntil = Objects.requireNonNull(until.getValue());
        assertFalse(sentUntil.isBefore(earliest));
        assertFalse(sentUntil.isAfter(latest));
    }

    @ParameterizedTest
    @ValueSource(ints = { Integer.MIN_VALUE, -1, 0 })
    void nonPositiveDurationDoesNotSendCommand(int minutes) {
        actions.setThingHandler(handler);
        assertFalse(actions.setTemporarySystemModeFor("Away", minutes));
        verifyNoInteractions(handler);
    }

    @Test
    void missingRequiredInputsDoNotSendCommand() {
        actions.setThingHandler(handler);
        ZonedDateTime until = ZonedDateTime.parse("2030-01-17T20:15:00+01:00");
        assertFalse(actions.setSystemMode(null));
        assertFalse(actions.setTemporarySystemMode(null, until));
        assertFalse(actions.setTemporarySystemMode("Away", null));
        assertFalse(actions.setTemporarySystemModeFor(null, 90));
        assertFalse(actions.setTemporarySystemModeFor("Away", null));
        verifyNoInteractions(handler);
    }

    @ParameterizedTest
    @ValueSource(strings = { "", " ", "\t" })
    void blankModeDoesNotSendCommand(String mode) {
        actions.setThingHandler(handler);
        assertFalse(actions.setSystemMode(mode));
        assertFalse(actions.setTemporarySystemMode(mode, ZonedDateTime.parse("2030-01-17T20:15:00+01:00")));
        assertFalse(actions.setTemporarySystemModeFor(mode, 90));
        verifyNoInteractions(handler);
    }

    @Test
    void unattachedActionsReturnFalse() {
        assertCommandsFail();
        assertNull(actions.getThingHandler());
    }

    @Test
    void detachedActionsDoNotCallPreviousHandler() {
        actions.setThingHandler(handler);
        assertSame(handler, actions.getThingHandler());
        actions.setThingHandler(null);
        assertCommandsFail();
        assertNull(actions.getThingHandler());
        verifyNoInteractions(handler);
    }

    @Test
    void wrongHandlerDoesNotRetainPreviousControllerHandler() {
        actions.setThingHandler(handler);
        actions.setThingHandler(mock(ThingHandler.class));
        assertCommandsFail();
        assertNull(actions.getThingHandler());
        verifyNoInteractions(handler);
    }

    @Test
    void staticActionRejectsWrongThingActions() {
        ThingActions wrongActions = mock(ThingActions.class);
        assertThrows(IllegalArgumentException.class, () -> EvohomeSystemActions.setSystemMode(wrongActions, "Auto"));
        assertThrows(IllegalArgumentException.class,
                () -> EvohomeSystemActions.setTemporarySystemMode(wrongActions, "Away", ZonedDateTime.now()));
        assertThrows(IllegalArgumentException.class,
                () -> EvohomeSystemActions.setTemporarySystemModeFor(wrongActions, "Away", 90));
    }

    private void assertCommandsFail() {
        assertFalse(actions.setSystemMode("Auto"));
        assertFalse(actions.setTemporarySystemMode("Away", ZonedDateTime.parse("2030-01-17T20:15:00+01:00")));
        assertFalse(actions.setTemporarySystemModeFor("Away", 90));
    }
}

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
import org.openhab.binding.evohome.internal.handler.EvohomeHeatingZoneHandler;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingHandler;

/**
 * Tests rule action expiry conversion, command results and handler detachment.
 *
 * @author Kovacs Istvan - Initial contribution
 */
@NonNullByDefault
class EvohomeZoneActionsTest {

    private final EvohomeHeatingZoneHandler handler = mock(EvohomeHeatingZoneHandler.class);
    private final EvohomeZoneActions actions = new EvohomeZoneActions();

    @Test
    void explicitExpiryPreservesOffsetAndExactInstant() {
        actions.setThingHandler(handler);
        ZonedDateTime until = ZonedDateTime.parse("2030-01-17T20:15:42+05:30");
        Instant expected = Instant.parse("2030-01-17T14:45:42Z");
        when(handler.setTemporarySetPoint(21.5, expected)).thenReturn(true);

        assertTrue(EvohomeZoneActions.setTemporarySetPoint(actions, 21.5, until));

        verify(handler).setTemporarySetPoint(21.5, expected);
        verifyNoMoreInteractions(handler);
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void actionsReturnActualHandlerAcceptance(boolean accepted) {
        actions.setThingHandler(handler);
        ZonedDateTime until = ZonedDateTime.parse("2030-01-17T20:15:00+01:00");
        when(handler.setPermanentSetPoint(20)).thenReturn(accepted);
        when(handler.setTemporarySetPoint(eq(21.0), any(Instant.class))).thenReturn(accepted);
        when(handler.setTemporarySetPointUntilNextSwitchpoint(22)).thenReturn(accepted);
        when(handler.cancelSetPointOverride()).thenReturn(accepted);

        assertEquals(accepted, EvohomeZoneActions.setPermanentSetPoint(actions, 20.0));
        assertEquals(accepted, actions.setTemporarySetPoint(21.0, until));
        assertEquals(accepted, EvohomeZoneActions.setTemporarySetPointFor(actions, 21.0, 90));
        assertEquals(accepted, EvohomeZoneActions.setTemporarySetPointUntilNextSwitchpoint(actions, 22.0));
        assertEquals(accepted, EvohomeZoneActions.cancelSetPointOverride(actions));
    }

    @ParameterizedTest
    @ValueSource(ints = { 1, 1440 })
    void fixedDurationIsMeasuredFromCommandTime(int minutes) {
        actions.setThingHandler(handler);
        when(handler.setTemporarySetPoint(eq(21.0), any(Instant.class))).thenReturn(true);
        Instant earliest = Instant.now().plusSeconds(minutes * 60L);

        assertTrue(actions.setTemporarySetPointFor(21.0, minutes));

        Instant latest = Instant.now().plusSeconds(minutes * 60L);
        ArgumentCaptor<Instant> until = ArgumentCaptor.forClass(Instant.class);
        verify(handler).setTemporarySetPoint(eq(21.0), until.capture());
        Instant sentUntil = Objects.requireNonNull(until.getValue());
        assertFalse(sentUntil.isBefore(earliest));
        assertFalse(sentUntil.isAfter(latest));
    }

    @ParameterizedTest
    @ValueSource(ints = { Integer.MIN_VALUE, -1, 0, 1441, Integer.MAX_VALUE })
    void invalidDurationDoesNotSendCommand(int minutes) {
        actions.setThingHandler(handler);

        assertFalse(actions.setTemporarySetPointFor(21.0, minutes));

        verifyNoInteractions(handler);
    }

    @Test
    void missingRequiredInputsDoNotSendCommand() {
        actions.setThingHandler(handler);
        ZonedDateTime until = ZonedDateTime.parse("2030-01-17T20:15:00+01:00");

        assertFalse(actions.setPermanentSetPoint(null));
        assertFalse(actions.setTemporarySetPoint(null, until));
        assertFalse(actions.setTemporarySetPoint(21.0, null));
        assertFalse(actions.setTemporarySetPointFor(null, 90));
        assertFalse(actions.setTemporarySetPointFor(21.0, null));
        assertFalse(actions.setTemporarySetPointUntilNextSwitchpoint(null));

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
    void wrongHandlerDoesNotRetainPreviousZoneHandler() {
        actions.setThingHandler(handler);

        actions.setThingHandler(mock(ThingHandler.class));

        assertCommandsFail();
        assertNull(actions.getThingHandler());
        verifyNoInteractions(handler);
    }

    @Test
    void staticActionRejectsWrongThingActions() {
        ThingActions wrongActions = mock(ThingActions.class);

        assertThrows(IllegalArgumentException.class, () -> EvohomeZoneActions.cancelSetPointOverride(wrongActions));
    }

    private void assertCommandsFail() {
        assertFalse(actions.setPermanentSetPoint(21.0));
        assertFalse(actions.setTemporarySetPoint(21.0, ZonedDateTime.parse("2030-01-17T20:15:00+01:00")));
        assertFalse(actions.setTemporarySetPointFor(21.0, 90));
        assertFalse(actions.setTemporarySetPointUntilNextSwitchpoint(21.0));
        assertFalse(actions.cancelSetPointOverride());
    }
}

/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.satel.internal.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.openhab.binding.satel.internal.SatelBindingConstants.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.satel.internal.action.SatelEventLogActions;
import org.openhab.binding.satel.internal.event.ConnectionStatusEvent;
import org.openhab.binding.satel.internal.util.EventLogReader;
import org.openhab.binding.satel.internal.util.EventLogReader.EventDescription;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.thing.internal.ThingImpl;

/**
 * @author Krzysztof Goworek - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
class SatelEventLogHandlerTest {

    @Mock
    private ThingHandlerCallback callback;

    @Mock
    private SatelBridgeHandler bridgeHandler;

    @Mock
    private EventLogReader eventLogReader;

    private final Thing thing = new ThingImpl(THING_TYPE_EVENTLOG, "thingId");

    @InjectMocks
    private final SatelEventLogHandler testSubject = new SatelEventLogHandler(thing);

    @Test
    void handleCommandShouldNotUpdateStateWhenOtherChannelIsGiven() {
        testSubject.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_DESCRIPTION), new DecimalType(0));

        verify(eventLogReader, never()).readEvent(0);
        verify(callback, never()).stateUpdated(any(), any());
    }

    @Test
    void handleCommandShouldNotUpdateStateWhenOtherCommandIsGiven() {
        testSubject.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_INDEX), new StringType(""));

        verify(eventLogReader, never()).readEvent(0);
        verify(callback, never()).stateUpdated(any(), any());
    }

    @Test
    void handleCommandShouldNotUpdateStateWhenEventLogReaderIsNotPresent() {
        testSubject.dispose();

        testSubject.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_INDEX), new DecimalType(0));

        verify(eventLogReader, never()).readEvent(0);
        verify(callback, never()).stateUpdated(any(), any());
    }

    @Test
    void handleCommandShouldNotUpdateStateWhenReadEventFailed() {
        testSubject.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_INDEX), new DecimalType(0));

        verify(eventLogReader).readEvent(0);
        verify(callback, never()).stateUpdated(any(), any());
    }

    @Test
    void handleCommandShouldUpdateStateWhenSendCommandSucceeded() {
        LocalDateTime timestamp = LocalDateTime.parse("2020-03-12T12:34:56");
        EventDescription eventDescription = mock(EventDescription.class);
        when(eventDescription.getCurrentIndex()).thenReturn(1);
        when(eventDescription.getNextIndex()).thenReturn(2);
        when(eventDescription.getTimestamp()).thenReturn(timestamp);
        when(eventDescription.getText()).thenReturn("description");
        when(eventLogReader.readEvent(0)).thenReturn(Optional.of(eventDescription));
        when(eventLogReader.buildDetails(same(eventDescription))).thenReturn("details");
        when(bridgeHandler.getZoneId()).thenReturn(ZoneId.systemDefault());

        testSubject.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_INDEX), new DecimalType(0));

        verify(callback).stateUpdated(new ChannelUID(thing.getUID(), CHANNEL_INDEX), new DecimalType(1));
        verify(callback).stateUpdated(new ChannelUID(thing.getUID(), CHANNEL_PREV_INDEX), new DecimalType(2));
        verify(callback).stateUpdated(new ChannelUID(thing.getUID(), CHANNEL_TIMESTAMP),
                new DateTimeType(timestamp.atZone(ZoneId.systemDefault())));
        verify(callback).stateUpdated(new ChannelUID(thing.getUID(), CHANNEL_DESCRIPTION),
                new StringType("description"));
        verify(callback).stateUpdated(new ChannelUID(thing.getUID(), CHANNEL_DETAILS), new StringType("details"));
    }

    @Test
    void incomingEventShouldUpdateStatusIfConnected() {
        testSubject.incomingEvent(new ConnectionStatusEvent(true));

        ArgumentCaptor<ThingStatusInfo> statusCaptor = ArgumentCaptor.forClass(ThingStatusInfo.class);
        verify(callback).statusUpdated(eq(thing), statusCaptor.capture());
        assertEquals(ThingStatus.ONLINE, statusCaptor.getValue().getStatus());
    }

    @Test
    void incomingEventShouldNotUpdateStatusIfNotConnected() {
        testSubject.incomingEvent(new ConnectionStatusEvent(false));

        verify(callback, never()).statusUpdated(eq(thing), any());
    }

    @Test
    void getServicesShouldReturnEventLogActions() {
        Collection<Class<? extends ThingHandlerService>> result = testSubject.getServices();

        assertEquals(1, result.size());
        assertTrue(result.contains(SatelEventLogActions.class));
    }
}

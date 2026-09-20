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
package org.openhab.binding.chromecast.internal;

import static org.digitalmediaserver.cast.event.CastEvent.CastEventType.CONNECTED;
import static org.digitalmediaserver.cast.event.CastEvent.CastEventType.MEDIA_STATUS;
import static org.digitalmediaserver.cast.event.CastEvent.CastEventType.RECEIVER_STATUS;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.digitalmediaserver.cast.event.CastEvent;
import org.digitalmediaserver.cast.message.entity.MediaStatus;
import org.digitalmediaserver.cast.message.response.MediaStatusResponse;
import org.digitalmediaserver.cast.message.response.ReceiverStatusResponse;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.openhab.core.thing.ThingStatus;

/**
 * Tests for {@link ChromecastEventReceiver}.
 *
 * @author Leo Siepel - Initial contribution
 */
@NonNullByDefault
class ChromecastEventReceiverTest {

    private final ChromecastScheduler scheduler = mock(ChromecastScheduler.class);
    private final ChromecastStatusUpdater statusUpdater = mock(ChromecastStatusUpdater.class);
    private final ChromecastEventReceiver eventReceiver = new ChromecastEventReceiver(scheduler, statusUpdater);

    @Test
    void connectedEventUpdatesThingStatus() {
        CastEvent<Object> event = createEvent(CONNECTED);
        when(event.getData()).thenReturn(Boolean.TRUE);

        eventReceiver.onEvent(event);

        verify(statusUpdater).updateStatus(ThingStatus.ONLINE);
        verify(scheduler, never()).scheduleConnect();
    }

    @Test
    void disconnectedEventSchedulesReconnect() {
        CastEvent<Object> event = createEvent(CONNECTED);
        when(event.getData()).thenReturn(Boolean.FALSE);

        eventReceiver.onEvent(event);

        verify(statusUpdater).updateStatus(ThingStatus.OFFLINE);
        verify(scheduler).scheduleConnect();
    }

    @Test
    void mediaStatusesAreAppliedInOrder() {
        CastEvent<Object> event = createEvent(MEDIA_STATUS);
        MediaStatusResponse response = mock(MediaStatusResponse.class);
        MediaStatus firstStatus = mock(MediaStatus.class);
        MediaStatus secondStatus = mock(MediaStatus.class);
        when(event.getData(MediaStatusResponse.class)).thenReturn(response);
        when(response.getStatuses()).thenReturn(List.of(firstStatus, secondStatus));

        eventReceiver.onEvent(event);

        InOrder inOrder = inOrder(statusUpdater);
        inOrder.verify(statusUpdater).updateMediaStatus(firstStatus);
        inOrder.verify(statusUpdater).updateMediaStatus(secondStatus);
    }

    @Test
    void missingReceiverStatusResponseClearsReceiverState() {
        CastEvent<Object> event = createEvent(RECEIVER_STATUS);
        when(event.getData(ReceiverStatusResponse.class)).thenReturn(null);

        eventReceiver.onEvent(event);

        verify(statusUpdater).processStatusUpdate(null);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private CastEvent<Object> createEvent(CastEvent.CastEventType eventType) {
        CastEvent event = mock(CastEvent.class);
        when(event.getEventType()).thenReturn(eventType);
        return event;
    }
}

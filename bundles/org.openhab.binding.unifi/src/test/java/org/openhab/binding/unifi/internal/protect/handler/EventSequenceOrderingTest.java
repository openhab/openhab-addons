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
package org.openhab.binding.unifi.internal.protect.handler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.unifi.internal.protect.api.pub.dto.events.BaseEvent;
import org.openhab.binding.unifi.internal.protect.api.pub.dto.events.CameraMotionEvent;
import org.openhab.binding.unifi.internal.protect.api.pub.dto.events.EventType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.type.ThingTypeRegistry;

/**
 * Event snapshots are stamped in WebSocket order but dispatched through a multi-threaded pool, so a
 * task can run after a newer one. These tests pin that an older snapshot never wins -- including
 * across a delivery, which discards the pending state the guard would otherwise live in.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
public class EventSequenceOrderingTest {

    private static final String EVENT_ID = "4b61ed79-7789-4ac1-8cce-927ded49c44e";

    private @NonNullByDefault({}) UnifiProtectNVRHandler handler;

    private static BaseEvent event(long end) {
        BaseEvent e = new CameraMotionEvent();
        e.type = EventType.CAMERA_MOTION;
        e.id = EVENT_ID;
        e.device = "62e3a86301824803e700041d";
        e.end = end;
        return e;
    }

    @BeforeEach
    public void setUp() {
        handler = new UnifiProtectNVRHandler(mock(Bridge.class), mock(ThingTypeRegistry.class));
    }

    private long pendingSequence() {
        UnifiProtectNVRHandler.PendingUpdate state = handler.pendingEventUpdates.get(EVENT_ID);
        return state == null ? Long.MIN_VALUE : state.lastSequence;
    }

    @Test
    public void olderSnapshotIsIgnoredWithinABurst() {
        handler.handleUpdateEvent(event(200), 11);
        handler.handleUpdateEvent(event(100), 10);

        assertEquals(11, pendingSequence(), "sequence 10 must not overwrite the newer 11");
        BaseEvent kept = handler.pendingEventUpdates.get(EVENT_ID).lastEvent;
        assertNotNull(kept);
        assertEquals(200L, kept.end);
    }

    @Test
    public void olderSnapshotIsIgnoredAfterTheNewerOneWasDelivered() {
        // 11 arrives and its burst is delivered, which discards the pending state.
        handler.handleUpdateEvent(event(200), 11);
        UnifiProtectNVRHandler.PendingUpdate delivered = handler.pendingEventUpdates.get(EVENT_ID);
        assertNotNull(delivered);
        handler.deliverDebouncedUpdate(EVENT_ID, delivered);

        // 10 was stuck in the scheduler all along and only now gets to run.
        handler.handleUpdateEvent(event(100), 10);

        assertNull(handler.pendingEventUpdates.get(EVENT_ID),
                "a snapshot older than the delivered one must not start a new burst");
    }

    @Test
    public void aGenuinelyNewerSnapshotAfterDeliveryIsStillAccepted() {
        handler.handleUpdateEvent(event(200), 11);
        UnifiProtectNVRHandler.PendingUpdate delivered = handler.pendingEventUpdates.get(EVENT_ID);
        assertNotNull(delivered);
        handler.deliverDebouncedUpdate(EVENT_ID, delivered);

        handler.handleUpdateEvent(event(300), 12);

        assertEquals(12, pendingSequence(), "the guard must not block later updates of the same event");
    }
}

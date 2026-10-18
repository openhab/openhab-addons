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
package org.openhab.io.mcp.internal.util;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.automation.events.RuleStatusInfoEvent;
import org.openhab.core.items.events.ItemAddedEvent;
import org.openhab.core.items.events.ItemRemovedEvent;
import org.openhab.core.items.events.ItemStateChangedEvent;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.events.ThingStatusInfoChangedEvent;

import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.spec.McpSchema.ResourcesUpdatedNotification;

/**
 * Tests for {@link McpEventBridge}.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
@ExtendWith(MockitoExtension.class)
class McpEventBridgeTest {

    @Mock
    @Nullable
    McpSyncServer server;

    private @Nullable SubscriptionManager subscriptions;
    private @Nullable McpEventBridge bridge;

    @BeforeEach
    void setUp() {
        subscriptions = new SubscriptionManager();
        bridge = new McpEventBridge(Objects.requireNonNull(server), Objects.requireNonNull(subscriptions), 0);
    }

    private McpEventBridge br() {
        McpEventBridge b = bridge;
        assertNotNull(b);
        return b;
    }

    private SubscriptionManager subs() {
        SubscriptionManager s = subscriptions;
        assertNotNull(s);
        return s;
    }

    @Test
    void testSubscribedEventTypes() {
        Set<String> types = br().getSubscribedEventTypes();
        assertEquals(5, types.size());
        assertTrue(types.contains(ItemStateChangedEvent.TYPE));
        assertTrue(types.contains(ItemAddedEvent.TYPE));
        assertTrue(types.contains(ItemRemovedEvent.TYPE));
        assertTrue(types.contains(ThingStatusInfoChangedEvent.TYPE));
        assertTrue(types.contains(RuleStatusInfoEvent.TYPE));
    }

    @Test
    void testEventFilterNull() {
        assertNull(br().getEventFilter());
    }

    @Test
    void testItemStateChangedNotifiesResource() {
        ItemStateChangedEvent event = mock(ItemStateChangedEvent.class);
        when(event.getItemName()).thenReturn("Kitchen_Light");
        when(event.getItemState()).thenReturn(OnOffType.ON);
        when(event.getOldItemState()).thenReturn(OnOffType.OFF);

        br().receive(event);

        ArgumentCaptor<ResourcesUpdatedNotification> captor = ArgumentCaptor
                .forClass(ResourcesUpdatedNotification.class);
        verify(server).notifyResourcesUpdated(captor.capture());
        assertEquals("openhab://item/Kitchen_Light", captor.getValue().uri());
    }

    @Test
    void testItemStateChangedRecordsInSubscriptions() {
        subs().watch("s1", List.of("Kitchen_Light"));

        ItemStateChangedEvent event = mock(ItemStateChangedEvent.class);
        when(event.getItemName()).thenReturn("Kitchen_Light");
        when(event.getItemState()).thenReturn(OnOffType.ON);
        when(event.getOldItemState()).thenReturn(OnOffType.OFF);

        br().receive(event);

        assertEquals(1, subs().drainEvents("s1").size());
    }

    @Test
    void testItemAddedNotifiesListChanged() {
        ItemAddedEvent event = mock(ItemAddedEvent.class);
        br().receive(event);
        verify(server).notifyResourcesListChanged();
    }

    @Test
    void testItemRemovedNotifiesListChanged() {
        ItemRemovedEvent event = mock(ItemRemovedEvent.class);
        br().receive(event);
        verify(server).notifyResourcesListChanged();
    }

    @Test
    void testThingStatusChangedNotifies() {
        ThingStatusInfoChangedEvent event = mock(ThingStatusInfoChangedEvent.class);
        when(event.getThingUID()).thenReturn(new ThingUID("binding:type:myThing"));

        br().receive(event);

        ArgumentCaptor<ResourcesUpdatedNotification> captor = ArgumentCaptor
                .forClass(ResourcesUpdatedNotification.class);
        verify(server).notifyResourcesUpdated(captor.capture());
        assertEquals("openhab://thing/binding:type:myThing", captor.getValue().uri());
    }

    @Test
    void testRuleStatusNotifies() {
        RuleStatusInfoEvent event = mock(RuleStatusInfoEvent.class);
        when(event.getRuleId()).thenReturn("rule-123");

        br().receive(event);

        ArgumentCaptor<ResourcesUpdatedNotification> captor = ArgumentCaptor
                .forClass(ResourcesUpdatedNotification.class);
        verify(server).notifyResourcesUpdated(captor.capture());
        assertEquals("openhab://rule/rule-123", captor.getValue().uri());
    }

    @Test
    void testCoalescingSuppressesDuplicate() throws Exception {
        McpEventBridge coalescingBridge = new McpEventBridge(Objects.requireNonNull(server), subs(), 5000);

        ItemStateChangedEvent event = mock(ItemStateChangedEvent.class);
        when(event.getItemName()).thenReturn("Item1");
        when(event.getItemState()).thenReturn(OnOffType.ON);
        when(event.getOldItemState()).thenReturn(OnOffType.OFF);

        coalescingBridge.receive(event);
        Thread.sleep(5);
        coalescingBridge.receive(event);

        verify(server, times(1)).notifyResourcesUpdated(any());
    }

    @Test
    void testCoalesceDisabledWhenZero() {
        ItemStateChangedEvent event = mock(ItemStateChangedEvent.class);
        when(event.getItemName()).thenReturn("Item1");
        when(event.getItemState()).thenReturn(OnOffType.ON);
        when(event.getOldItemState()).thenReturn(OnOffType.OFF);

        br().receive(event);
        br().receive(event);

        verify(server, times(2)).notifyResourcesUpdated(any());
    }

    @Test
    void testReceiveCatchesRuntimeException() {
        doThrow(new RuntimeException("boom")).when(server).notifyResourcesUpdated(any());

        ItemStateChangedEvent event = mock(ItemStateChangedEvent.class);
        when(event.getItemName()).thenReturn("Item1");
        when(event.getItemState()).thenReturn(OnOffType.ON);
        when(event.getOldItemState()).thenReturn(OnOffType.OFF);

        assertDoesNotThrow(() -> br().receive(event));
    }
}

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
package org.openhab.binding.matter.internal.bridge;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.after;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.openhab.binding.matter.internal.client.MatterRequestException;
import org.openhab.binding.matter.internal.client.MatterWebsocketService;
import org.openhab.core.items.GenericItem;
import org.openhab.core.items.ItemNotFoundException;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.items.Metadata;
import org.openhab.core.items.MetadataKey;
import org.openhab.core.items.MetadataRegistry;
import org.openhab.core.items.events.ItemEventFactory;
import org.openhab.core.library.items.SwitchItem;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.link.ItemChannelLinkRegistry;
import org.openhab.core.types.UnDefType;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

/**
 * Tests how {@link MatterBridge} keeps the bridge endpoints in line with the items.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
@SuppressWarnings("null")
class MatterBridgeTest {
    private static final String CLUSTER = "bridgedDeviceBasicInformation";

    private final Map<String, GenericItem> items = new HashMap<>();
    private final Map<String, Metadata> metadata = new HashMap<>();
    private final Map<String, Set<Thing>> boundThings = new HashMap<>();

    private final MatterBridgeClient client = mock(MatterBridgeClient.class);
    private final ItemChannelLinkRegistry linkRegistry = mock(ItemChannelLinkRegistry.class);
    private final MatterBridge bridge;

    MatterBridgeTest() throws IOException, ItemNotFoundException {
        ItemRegistry itemRegistry = mock(ItemRegistry.class);
        when(itemRegistry.getItem(anyString())).thenAnswer(invocation -> {
            GenericItem item = items.get(invocation.getArgument(0));
            if (item == null) {
                throw new ItemNotFoundException(invocation.getArgument(0));
            }
            return item;
        });
        MetadataRegistry metadataRegistry = mock(MetadataRegistry.class);
        when(metadataRegistry.getAll()).thenAnswer(invocation -> List.copyOf(metadata.values()));
        when(metadataRegistry.get(any(MetadataKey.class)))
                .thenAnswer(invocation -> metadata.get(((MetadataKey) invocation.getArgument(0)).getItemName()));
        when(linkRegistry.getBoundThings(anyString()))
                .thenAnswer(invocation -> boundThings.getOrDefault(invocation.getArgument(0), Set.of()));
        ConfigurationAdmin configAdmin = mock(ConfigurationAdmin.class);
        when(configAdmin.getConfiguration(anyString())).thenReturn(mock(Configuration.class));

        bridge = new MatterBridge(itemRegistry, metadataRegistry, mock(MatterWebsocketService.class), configAdmin,
                linkRegistry, client);
    }

    @BeforeEach
    void setUp() {
        CompletableFuture<Void> done = CompletableFuture.completedFuture(null);
        when(client.isConnected()).thenReturn(true);
        when(client.initializeBridge(anyBoolean())).thenReturn(done);
        when(client.startBridge()).thenReturn(done);
        when(client.addEndpoint(any())).thenReturn(CompletableFuture.completedFuture("null"));
        when(client.updateEndpoint(any())).thenReturn(CompletableFuture.completedFuture("null"));
        when(client.removeEndpoint(anyString())).thenReturn(done);
        when(client.setEndpointState(anyString(), anyString(), anyString(), any())).thenReturn(done);
        when(client.getCommissioningState()).thenReturn(CompletableFuture.failedFuture(new IOException("none")));
    }

    private SwitchItem addLight(String name, Map<String, Object> config) {
        SwitchItem item = new SwitchItem(name);
        item.setState(OnOffType.OFF);
        items.put(name, item);
        metadata.put(name, new Metadata(new MetadataKey("matter", name), "OnOffLight", config));
        return item;
    }

    private void startWithLight() {
        addLight("light1", Map.of());
        bridge.registerItems();
    }

    private static CompletableFuture<String> itemError() {
        return CompletableFuture.failedFuture(new MatterRequestException("Behaviors have errors", null, true));
    }

    @Test
    void testStartAddsEndpoints() {
        startWithLight();
        verify(client).initializeBridge(false);
        verify(client).addEndpoint(argThat(endpoint -> "light1".equals(endpoint.id)));
        verify(client).startBridge();
    }

    @Test
    void testAddedItemDoesNotRestart() {
        startWithLight();
        addLight("light2", Map.of());
        bridge.registerItems();
        verify(client).addEndpoint(argThat(endpoint -> "light2".equals(endpoint.id)));
        verify(client, times(1)).initializeBridge(anyBoolean());
    }

    @Test
    void testRemovedItemRemovesEndpoint() {
        startWithLight();
        addLight("light2", Map.of());
        bridge.registerItems();
        metadata.remove("light2");
        bridge.registerItems();
        verify(client).removeEndpoint("light2");
        verify(client, times(1)).initializeBridge(anyBoolean());
    }

    @Test
    void testChangedOptionsReplaceEndpoint() {
        startWithLight();
        metadata.put("light1",
                new Metadata(new MetadataKey("matter", "light1"), "OnOffLight", Map.of("fixedLabels", "room=Office")));
        bridge.registerItems();
        verify(client).updateEndpoint(argThat(endpoint -> "light1".equals(endpoint.id)));
        verify(client, times(1)).initializeBridge(anyBoolean());
    }

    @Test
    void testChangedLabelRenamesInPlace() {
        startWithLight();
        metadata.put("light1",
                new Metadata(new MetadataKey("matter", "light1"), "OnOffLight", Map.of("label", "Desk Lamp")));
        bridge.registerItems();
        verify(client).setEndpointState("light1", CLUSTER, "nodeLabel", "Desk Lamp");
        verify(client, never()).updateEndpoint(any());
    }

    @Test
    void testUnchangedItemsSendNothing() {
        startWithLight();
        bridge.registerItems();
        verify(client, times(1)).addEndpoint(any());
        verify(client, never()).updateEndpoint(any());
        verify(client, never()).removeEndpoint(anyString());
        verify(client, never()).setEndpointState(anyString(), anyString(), anyString(), any());
    }

    @Test
    void testEditedItemMovesToTheNewItem() {
        startWithLight();
        SwitchItem oldItem = (SwitchItem) items.get("light1");
        // editing an item replaces its object in the registry
        SwitchItem newItem = new SwitchItem("light1");
        newItem.setState(OnOffType.OFF);
        items.put("light1", newItem);
        bridge.registerItems();

        verify(client, times(1)).addEndpoint(any());
        verify(client, never()).updateEndpoint(any());
        // item listeners are notified on a core thread pool
        newItem.setState(OnOffType.ON);
        verify(client, timeout(5000)).setEndpointState("light1", "onOff", "onOff", true);
        // the old item no longer reaches the endpoint
        oldItem.setState(OnOffType.ON);
        verify(client, after(500).times(1)).setEndpointState("light1", "onOff", "onOff", true);
    }

    @Test
    void testFailedItemIsLeftOutUntilItsMetadataChanges() {
        startWithLight();
        addLight("bad", Map.of());
        when(client.addEndpoint(argThat(endpoint -> endpoint != null && "bad".equals(endpoint.id))))
                .thenReturn(itemError());
        bridge.registerItems();
        // the bridge restarts without the failed item
        verify(client, times(2)).initializeBridge(anyBoolean());
        verify(client, times(2)).addEndpoint(argThat(endpoint -> endpoint != null && "light1".equals(endpoint.id)));

        bridge.registerItems();
        verify(client, times(1)).addEndpoint(argThat(endpoint -> endpoint != null && "bad".equals(endpoint.id)));

        metadata.put("bad",
                new Metadata(new MetadataKey("matter", "bad"), "OnOffLight", Map.of("fixedLabels", "room=Office")));
        bridge.registerItems();
        verify(client, times(2)).addEndpoint(argThat(endpoint -> endpoint != null && "bad".equals(endpoint.id)));
    }

    @Test
    void testConnectionErrorDoesNotLeaveItemOut() {
        startWithLight();
        addLight("light2", Map.of());
        when(client.addEndpoint(argThat(endpoint -> endpoint != null && "light2".equals(endpoint.id))))
                .thenReturn(CompletableFuture.failedFuture(new MatterRequestException("No valid session", null, false)))
                .thenReturn(CompletableFuture.failedFuture(new MatterRequestException("No valid session", null, false)))
                .thenReturn(CompletableFuture.completedFuture("null"));
        bridge.registerItems();
        bridge.registerItems();
        verify(client, times(3)).addEndpoint(argThat(endpoint -> endpoint != null && "light2".equals(endpoint.id)));
    }

    @Test
    void testUndefinedItemIsUnreachable() {
        addLight("light1", Map.of()).setState(UnDefType.UNDEF);
        bridge.registerItems();
        ArgumentCaptor<BridgedEndpoint> endpoint = ArgumentCaptor.forClass(BridgedEndpoint.class);
        verify(client).addEndpoint(endpoint.capture());
        assertFalse(endpoint.getValue().reachable);
    }

    @Test
    void testOfflineThingIsUnreachable() {
        addLight("light1", Map.of());
        addLight("light2", Map.of());
        addLight("light3", Map.of());
        Thing offline = mock(Thing.class);
        when(offline.getStatus()).thenReturn(ThingStatus.OFFLINE);
        Thing online = mock(Thing.class);
        when(online.getStatus()).thenReturn(ThingStatus.ONLINE);
        boundThings.put("light1", Set.of(offline));
        boundThings.put("light2", Set.of(offline, online));
        bridge.registerItems();

        ArgumentCaptor<BridgedEndpoint> captor = ArgumentCaptor.forClass(BridgedEndpoint.class);
        verify(client, times(3)).addEndpoint(captor.capture());
        Map<String, Boolean> reachable = new HashMap<>();
        captor.getAllValues().forEach(endpoint -> reachable.put(endpoint.id, endpoint.reachable));
        assertEquals(Map.of("light1", false, "light2", true, "light3", true), reachable);
    }

    @Test
    void testItemStateChangeUpdatesReachable() {
        SwitchItem item = addLight("light1", Map.of());
        bridge.registerItems();
        item.setState(UnDefType.NULL);
        bridge.receive(ItemEventFactory.createStateChangedEvent("light1", UnDefType.NULL, OnOffType.OFF,
                ZonedDateTime.now(), ZonedDateTime.now()));
        verify(client).setEndpointState("light1", CLUSTER, "reachable", false);

        // changes between defined states do not touch the flag
        bridge.receive(ItemEventFactory.createStateChangedEvent("light1", OnOffType.ON, OnOffType.OFF,
                ZonedDateTime.now(), ZonedDateTime.now()));
        verify(client, times(1)).setEndpointState(eq("light1"), eq(CLUSTER), eq("reachable"), any());
    }
}

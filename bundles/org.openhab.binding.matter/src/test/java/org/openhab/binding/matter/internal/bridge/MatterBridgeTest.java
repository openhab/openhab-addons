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
import static org.junit.jupiter.api.Assertions.assertTrue;
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
import org.openhab.core.library.items.SwitchItem;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.builder.ThingStatusInfoBuilder;
import org.openhab.core.thing.events.ThingEventFactory;
import org.openhab.core.thing.events.ThingStatusInfoChangedEvent;
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
        when(client.increaseConfigurationVersion()).thenReturn(done);
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
        verify(client, times(1)).increaseConfigurationVersion();
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
        verify(client, times(2)).increaseConfigurationVersion();
    }

    @Test
    void testChangedOptionsReplaceEndpoint() {
        startWithLight();
        metadata.put("light1",
                new Metadata(new MetadataKey("matter", "light1"), "OnOffLight", Map.of("fixedLabels", "room=Office")));
        bridge.registerItems();
        verify(client).updateEndpoint(argThat(endpoint -> "light1".equals(endpoint.id)));
        verify(client, times(1)).initializeBridge(anyBoolean());
        verify(client).increaseConfigurationVersion();
    }

    @Test
    void testChangedLabelRenamesInPlace() {
        startWithLight();
        metadata.put("light1",
                new Metadata(new MetadataKey("matter", "light1"), "OnOffLight", Map.of("label", "Desk Lamp")));
        bridge.registerItems();
        verify(client).setEndpointState("light1", CLUSTER, "nodeLabel", "Desk Lamp");
        verify(client, never()).updateEndpoint(any());
        verify(client, never()).increaseConfigurationVersion();
    }

    @Test
    void testUnchangedItemsSendNothing() {
        startWithLight();
        bridge.registerItems();
        verify(client, times(1)).addEndpoint(any());
        verify(client, never()).updateEndpoint(any());
        verify(client, never()).removeEndpoint(anyString());
        verify(client, never()).setEndpointState(anyString(), anyString(), anyString(), any());
        verify(client, never()).increaseConfigurationVersion();
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
    void testUnlinkedItemIsReachable() {
        // items driven by rules have no Thing and may never get a state
        addLight("light1", Map.of()).setState(UnDefType.NULL);
        bridge.registerItems();
        ArgumentCaptor<BridgedEndpoint> endpoint = ArgumentCaptor.forClass(BridgedEndpoint.class);
        verify(client).addEndpoint(endpoint.capture());
        assertTrue(endpoint.getValue().reachable);
    }

    @Test
    void testOnlyOnlineThingsAreReachable() {
        addLight("offline", Map.of());
        addLight("someOnline", Map.of());
        addLight("unknown", Map.of());
        addLight("unlinked", Map.of());
        boundThings.put("offline", Set.of(thing(ThingStatus.OFFLINE)));
        boundThings.put("someOnline", Set.of(thing(ThingStatus.OFFLINE), thing(ThingStatus.ONLINE)));
        boundThings.put("unknown", Set.of(thing(ThingStatus.UNKNOWN)));
        bridge.registerItems();

        ArgumentCaptor<BridgedEndpoint> captor = ArgumentCaptor.forClass(BridgedEndpoint.class);
        verify(client, times(4)).addEndpoint(captor.capture());
        Map<String, Boolean> reachable = new HashMap<>();
        captor.getAllValues().forEach(endpoint -> reachable.put(endpoint.id, endpoint.reachable));
        assertEquals(Map.of("offline", false, "someOnline", true, "unknown", false, "unlinked", true), reachable);
    }

    @Test
    void testThingStatusChangeUpdatesReachable() {
        addLight("light1", Map.of());
        Thing thing = thing(ThingStatus.ONLINE);
        boundThings.put("light1", Set.of(thing));
        bridge.registerItems();

        when(thing.getStatus()).thenReturn(ThingStatus.OFFLINE);
        bridge.receive(statusChanged(ThingStatus.ONLINE, ThingStatus.OFFLINE));
        verify(client).setEndpointState("light1", CLUSTER, "reachable", false);

        // changes that do not involve ONLINE do not touch the flag
        when(thing.getStatus()).thenReturn(ThingStatus.UNKNOWN);
        bridge.receive(statusChanged(ThingStatus.OFFLINE, ThingStatus.UNKNOWN));
        verify(client, times(1)).setEndpointState(eq("light1"), eq(CLUSTER), eq("reachable"), any());

        when(thing.getStatus()).thenReturn(ThingStatus.ONLINE);
        bridge.receive(statusChanged(ThingStatus.UNKNOWN, ThingStatus.ONLINE));
        verify(client).setEndpointState("light1", CLUSTER, "reachable", true);
    }

    private static Thing thing(ThingStatus status) {
        Thing thing = mock(Thing.class);
        when(thing.getStatus()).thenReturn(status);
        return thing;
    }

    private static ThingStatusInfoChangedEvent statusChanged(ThingStatus from, ThingStatus to) {
        return ThingEventFactory.createStatusInfoChangedEvent(new ThingUID("test:thing:1"),
                ThingStatusInfoBuilder.create(to).build(), ThingStatusInfoBuilder.create(from).build());
    }
}

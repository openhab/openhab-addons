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
package org.openhab.binding.modbus.migration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.link.ItemChannelLinkRegistry;

class ManagedModbusMigrationAdapterTest {

    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.ValueSource(strings = { "Number", "Switch", "Contact", "DateTime", "Dimmer",
            "String", "Rollershutter" })
    void snapshotPreservesSourceChannelItemType(String itemType) {
        ThingUID pollerUid = new ThingUID("modbus:poller:tcp:plant");
        Thing data = org.openhab.core.thing.binding.builder.ThingBuilder
                .create(new ThingTypeUID("modbus", "data"), new ThingUID("modbus:data:tcp:plant:value"))
                .withBridge(pollerUid)
                .withChannel(org.openhab.core.thing.binding.builder.ChannelBuilder
                        .create(new org.openhab.core.thing.ChannelUID("modbus:data:tcp:plant:value:typed"))
                        .withAcceptedItemType(itemType).build())
                .build();
        ThingRegistry registry = mock(ThingRegistry.class);
        when(registry.getAll()).thenReturn(List.of(data));
        org.openhab.core.thing.ManagedThingProvider provider = mock(org.openhab.core.thing.ManagedThingProvider.class);
        when(provider.get(data.getUID())).thenReturn(data);
        Thing poller = mock(Thing.class);
        when(poller.getThingTypeUID()).thenReturn(new ThingTypeUID("modbus", "poller"));
        when(registry.get(pollerUid)).thenReturn(poller);
        ItemChannelLinkRegistry links = mock(ItemChannelLinkRegistry.class);
        when(links.getAll()).thenReturn(List.of(new org.openhab.core.thing.link.ItemChannelLink("Value",
                new org.openhab.core.thing.ChannelUID("modbus:data:tcp:plant:value:typed"))));

        assertEquals(itemType, new ManagedModbusMigrationAdapter(registry, links, provider).snapshot().data().getFirst()
                .links().getFirst().itemType());
    }

    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.ValueSource(strings = { "tcp", "serial" })
    void snapshotReportsEndpointWriteOnlyChildrenAsManualWork(String endpointType) {
        ThingUID endpointUid = new ThingUID("modbus:" + endpointType + ":plant");
        Thing endpoint = org.openhab.core.thing.binding.builder.BridgeBuilder
                .create(new ThingTypeUID("modbus", endpointType), endpointUid).build();
        Thing data = org.openhab.core.thing.binding.builder.ThingBuilder
                .create(new ThingTypeUID("modbus", "data"), new ThingUID("modbus:data:plant:writer"))
                .withBridge(endpointUid)
                .withConfiguration(new org.openhab.core.config.core.Configuration(
                        java.util.Map.of("writeStart", "100", "writeType", "holding", "writeValueType", "int16")))
                .build();
        ThingRegistry registry = mock(ThingRegistry.class);
        when(registry.getAll()).thenReturn(List.of(data, endpoint));
        when(registry.get(endpointUid)).thenReturn(endpoint);
        org.openhab.core.thing.ManagedThingProvider provider = mock(org.openhab.core.thing.ManagedThingProvider.class);
        when(provider.get(data.getUID())).thenReturn(data);
        ItemChannelLinkRegistry links = mock(ItemChannelLinkRegistry.class);
        when(links.getAll()).thenReturn(List.of());

        ManagedModbusMigrationAdapter.Snapshot snapshot = new ManagedModbusMigrationAdapter(registry, links, provider)
                .snapshot();

        assertEquals(List.of(), snapshot.data());
        assertEquals(List.of(new ManualMigrationWork(data.getUID().toString(),
                "legacy data parent is not a poller; writes require manual migration")), snapshot.manualWork());
    }

    @Test
    void previewRefusesAnUnmanagedLegacyPoller() {
        Thing legacyPoller = mock(Thing.class);
        ThingUID pollerUid = new ThingUID("modbus:poller:tcp:plant");
        when(legacyPoller.getUID()).thenReturn(pollerUid);
        when(legacyPoller.getThingTypeUID()).thenReturn(new ThingTypeUID("modbus", "poller"));
        ThingRegistry registry = mock(ThingRegistry.class);
        when(registry.getAll()).thenReturn(List.of(legacyPoller));

        ManagedModbusMigrationAdapter adapter = new ManagedModbusMigrationAdapter(registry,
                mock(ItemChannelLinkRegistry.class), mock(org.openhab.core.thing.ManagedThingProvider.class));

        assertEquals(List.of(new ManualMigrationWork(pollerUid.toString(), "legacy Thing is not managed")),
                adapter.snapshot().manualWork());
        assertEquals(List.of(), adapter.snapshot().pollers());
    }
}

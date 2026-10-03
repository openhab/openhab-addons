/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
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

/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.modbus.migration;

import static org.openhab.binding.modbus.internal.ModbusBindingConstantsInternal.THING_TYPE_MODBUS_DATA;
import static org.openhab.binding.modbus.internal.ModbusBindingConstantsInternal.THING_TYPE_MODBUS_POLLER;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ManagedThingProvider;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.link.ItemChannelLink;
import org.openhab.core.thing.link.ItemChannelLinkRegistry;

/** Reads only managed, unprofiled legacy topology from the registries. */
@NonNullByDefault
public final class ManagedModbusMigrationAdapter {
    private final ThingRegistry thingRegistry;
    private final ItemChannelLinkRegistry linkRegistry;
    private final ManagedThingProvider managedThingProvider;

    public ManagedModbusMigrationAdapter(ThingRegistry thingRegistry, ItemChannelLinkRegistry linkRegistry,
            ManagedThingProvider managedThingProvider) {
        this.thingRegistry = thingRegistry;
        this.linkRegistry = linkRegistry;
        this.managedThingProvider = managedThingProvider;
    }

    public Snapshot snapshot() {
        List<LegacyPoller> pollers = new ArrayList<>();
        List<LegacyData> data = new ArrayList<>();
        List<ManualMigrationWork> manual = new ArrayList<>();
        for (Thing thing : thingRegistry.getAll()) {
            if (THING_TYPE_MODBUS_POLLER.equals(thing.getThingTypeUID())) {
                if (!managed(thing)) {
                    manual.add(new ManualMigrationWork(thing.getUID().toString(), "legacy Thing is not managed"));
                } else if (targetExists(thing.getUID())) {
                    manual.add(
                            new ManualMigrationWork(thing.getUID().toString(), "target poller2 Thing already exists"));
                } else if (thing.getBridgeUID() == null) {
                    manual.add(new ManualMigrationWork(thing.getUID().toString(), "legacy poller has no bridge"));
                } else {
                    pollers.add(new LegacyPoller(thing.getUID().toString(), thing.getBridgeUID().toString(),
                            thing.getConfiguration().getProperties()));
                }
            } else if (THING_TYPE_MODBUS_DATA.equals(thing.getThingTypeUID())) {
                if (!managed(thing)) {
                    manual.add(new ManualMigrationWork(thing.getUID().toString(), "legacy Thing is not managed"));
                } else if (thing.getBridgeUID() == null) {
                    manual.add(new ManualMigrationWork(thing.getUID().toString(), "legacy data Thing has no poller"));
                } else {
                    List<LegacyLink> links = links(thing, manual);
                    data.add(new LegacyData(thing.getUID().toString(), thing.getBridgeUID().toString(),
                            thing.getConfiguration().getProperties(), links));
                }
            }
        }
        return new Snapshot(pollers, data, manual);
    }

    private boolean managed(Thing thing) {
        return managedThingProvider.get(thing.getUID()) != null;
    }

    private boolean targetExists(ThingUID sourceUid) {
        String target = sourceUid.toString().replace(":poller:", ":poller2:");
        return thingRegistry.get(new ThingUID(target)) != null;
    }

    private List<LegacyLink> links(Thing thing, List<ManualMigrationWork> manual) {
        List<LegacyLink> result = new ArrayList<>();
        for (ItemChannelLink link : linkRegistry.getAll()) {
            if (thing.getUID().equals(link.getLinkedUID().getThingUID())) {
                if (!link.getConfiguration().getProperties().isEmpty()) {
                    manual.add(new ManualMigrationWork(thing.getUID().toString(),
                            "configured/profile Item link requires manual migration"));
                } else {
                    result.add(new LegacyLink(link.getItemName(), link.getLinkedUID().toString(), "Number"));
                }
            }
        }
        return result;
    }

    /** Immutable safe registry input plus explicitly refused work. */
    public record Snapshot(List<LegacyPoller> pollers, List<LegacyData> data, List<ManualMigrationWork> manualWork) {
        public Snapshot {
            pollers = List.copyOf(pollers);
            data = List.copyOf(data);
            manualWork = List.copyOf(manualWork);
        }
    }
}

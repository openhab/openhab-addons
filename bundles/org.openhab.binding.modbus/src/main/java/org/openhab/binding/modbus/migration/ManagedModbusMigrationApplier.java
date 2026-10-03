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

import static org.openhab.binding.modbus.internal.ModbusBindingConstantsInternal.THING_TYPE_MODBUS_POLLER2;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ManagedThingProvider;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.builder.BridgeBuilder;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.link.ItemChannelLink;
import org.openhab.core.thing.link.ItemChannelLinkRegistry;

/** Applies managed targets only and compensates every object created by a failed apply. */
@NonNullByDefault
public final class ManagedModbusMigrationApplier implements MigrationApplier {
    private final ManagedThingProvider thingProvider;
    private final ItemChannelLinkRegistry linkRegistry;

    public ManagedModbusMigrationApplier(ManagedThingProvider thingProvider, ItemChannelLinkRegistry linkRegistry) {
        this.thingProvider = thingProvider;
        this.linkRegistry = linkRegistry;
    }

    @Override
    public void createTargets(MigrationPreview preview) {
        List<ThingUID> createdThings = new ArrayList<>();
        List<ItemChannelLink> createdLinks = new ArrayList<>();
        try {
            for (MigrationGroup group : preview.groups()) {
                ThingUID targetUid = new ThingUID(group.targetPollerUid());
                if (thingProvider.get(targetUid) != null) {
                    throw new IllegalStateException("refusing to overwrite existing target " + targetUid);
                }
                BridgeBuilder builder = BridgeBuilder.create(THING_TYPE_MODBUS_POLLER2, targetUid)
                        .withBridge(new ThingUID(group.targetBridgeUid()))
                        .withConfiguration(new Configuration(group.configuration()));
                group.channels().forEach(channel -> builder.withChannel(ChannelBuilder
                        .create(new ChannelUID(targetUid, channel.id())).withAcceptedItemType(channel.itemType())
                        .withConfiguration(new Configuration(channel.configuration())).build()));
                thingProvider.add(builder.build());
                createdThings.add(targetUid);
                for (MigrationLink link : group.links()) {
                    ItemChannelLink targetLink = new ItemChannelLink(link.itemName(),
                            new ChannelUID(link.targetChannelUid()));
                    linkRegistry.add(targetLink);
                    createdLinks.add(targetLink);
                }
            }
        } catch (RuntimeException e) {
            remove(createdLinks, createdThings);
            throw e;
        }
    }

    @Override
    public void removeTargets(MigrationPreview preview) {
        List<ItemChannelLink> links = new ArrayList<>();
        List<ThingUID> things = new ArrayList<>();
        for (MigrationGroup group : preview.groups()) {
            ThingUID targetUid = new ThingUID(group.targetPollerUid());
            things.add(targetUid);
            for (MigrationLink link : group.links()) {
                links.add(new ItemChannelLink(link.itemName(), new ChannelUID(link.targetChannelUid())));
            }
        }
        remove(links, things);
    }

    private void remove(List<ItemChannelLink> links, List<ThingUID> things) {
        for (ItemChannelLink link : links) {
            try {
                linkRegistry.remove(link.getUID());
            } catch (RuntimeException e) {
                // Continue compensation; callers retain the error that caused rollback.
            }
        }
        for (ThingUID thing : things.reversed()) {
            try {
                thingProvider.remove(thing);
            } catch (RuntimeException e) {
                // Continue compensation; callers retain the error that caused rollback.
            }
        }
    }

    @Override
    public void removeLegacy(MigrationPreview preview) {
        throw new UnsupportedOperationException("legacy Things are never removed by this migration console");
    }
}

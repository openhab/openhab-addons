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
package org.openhab.binding.homekit.internal.discovery;

import static org.openhab.binding.homekit.internal.HomekitBindingConstants.THING_TYPE_DEVICE;

import java.util.List;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.homekit.internal.dto.Accessory;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;

/**
 * Discovery service component that publishes newly discovered child accessories of a HomeKit bridge accessory.
 * No active scanning is performed; it relies on being informed of new accessories by the bridge handler.
 * Discovered accessories are published with a ThingUID based on their accessory ID (aid) and service ID (iid).
 *
 * @author Andrew Fiddian-Green - Initial Contribution
 */
@NonNullByDefault
@Component(service = DiscoveryService.class)
public class AccessoryDiscoveryService extends AbstractDiscoveryService {

    public AccessoryDiscoveryService() {
        super(Set.of(THING_TYPE_DEVICE), 10, false);
    }

    @Override
    protected void startScan() {
        // no scanning is done; we rely on being informed of new accessories
    }

    public void devicesDiscovered(Thing bridge, List<Accessory> accessories) {
        accessories.forEach(accessory -> {
            if (accessory.accessoryId != null && accessory.services != null) {
                accessory.services.forEach(service -> {
                    if (service.instanceId != null) {
                        String id = "%d-%d".formatted(accessory.accessoryId, service.instanceId);
                        ThingUID uid = new ThingUID(THING_TYPE_DEVICE, bridge.getUID(), id);
                        thingDiscovered(DiscoveryResultBuilder.create(uid) //
                                .withBridge(bridge.getUID()) //
                                .withLabel(service.toString()) //
                                .withProperty("uid", uid.toString()) //
                                .withRepresentationProperty("uid").build());
                    }
                });
            }
        });
    }
}

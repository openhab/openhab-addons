/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.warmup.internal.discovery;

import static org.openhab.binding.warmup.internal.WarmupBindingConstants.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.warmup.internal.handler.MyWarmupAccountHandler;
import org.openhab.binding.warmup.internal.model.query.LocationDTO;
import org.openhab.binding.warmup.internal.model.query.QueryResponseDTO;
import org.openhab.binding.warmup.internal.model.query.RoomDTO;

/**
 * The {@link WarmupDiscoveryService} is used to discover devices that are connected to a My Warmup account.
 *
 * @author James Melville - Initial contribution
 */
@NonNullByDefault
public class WarmupDiscoveryService extends AbstractDiscoveryService {

    private MyWarmupAccountHandler bridgeHandler;
    private ThingUID bridgeUID;
    private HashSet<ThingUID> previousThings;

    public WarmupDiscoveryService(MyWarmupAccountHandler handler) {
        super(DISCOVERABLE_THING_TYPES_UIDS, 5, false);
        this.bridgeHandler = handler;
        this.bridgeUID = handler.getThing().getUID();
        this.previousThings = new HashSet<ThingUID>();
    }

    @Override
    public void startScan() {
        bridgeHandler.scanDevices();
    }

    /**
     * Process device list and populate discovery list with things
     *
     * @param domain Data model representing all devices
     */
    public void onRefresh(@Nullable QueryResponseDTO domain) {
        if (domain != null) {
            HashSet<ThingUID> discoveredThings = new HashSet<ThingUID>();
            for (LocationDTO location : domain.getData().getUser().getLocations()) {
                for (RoomDTO room : location.getRooms()) {
                    discoverRoom(location, room, discoveredThings);
                }
            }

            previousThings.removeAll(discoveredThings);
            for (ThingUID missingThing : previousThings) {
                thingRemoved(missingThing);
            }
            previousThings = discoveredThings;
        }
    }

    private void discoverRoom(LocationDTO location, RoomDTO room, HashSet<ThingUID> discoveredThings) {
        if (room.getThermostat4ies() != null && !room.getThermostat4ies().isEmpty()) {
            final String deviceSN = room.getThermostat4ies().get(0).getDeviceSN();
            if (deviceSN != null) {
                final Map<String, Object> roomProperties = new HashMap<>();
                roomProperties.put("serialNumber", deviceSN);
                roomProperties.put("Serial Number", deviceSN);

                roomProperties.put("Id", room.getId());
                roomProperties.put("Name", room.getName());
                roomProperties.put("Location Id", location.getId());
                roomProperties.put("Location", location.getName());

                ThingUID roomThingUID = new ThingUID(THING_TYPE_ROOM, bridgeUID, deviceSN);
                thingDiscovered(DiscoveryResultBuilder.create(roomThingUID).withBridge(bridgeUID)
                        .withProperties(roomProperties).withLabel(location.getName() + " - " + room.getName())
                        .withRepresentationProperty("serialNumber").build());

                discoveredThings.add(roomThingUID);
            }
        }
    }
}

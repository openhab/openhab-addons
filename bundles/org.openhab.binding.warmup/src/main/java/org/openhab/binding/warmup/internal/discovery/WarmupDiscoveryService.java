/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
import org.openhab.binding.warmup.internal.handler.MyWarmupAccountHandler;
import org.openhab.binding.warmup.internal.handler.WarmupRefreshListener;
import org.openhab.binding.warmup.internal.model.query.LocationDTO;
import org.openhab.binding.warmup.internal.model.query.QueryResponseDTO;
import org.openhab.binding.warmup.internal.model.query.RoomDTO;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;

/**
 * The {@link WarmupDiscoveryService} is used to discover devices that are connected to a My Warmup account.
 *
 * @author James Melville - Initial contribution
 */
@NonNullByDefault
public class WarmupDiscoveryService extends AbstractDiscoveryService
        implements DiscoveryService, ThingHandlerService, WarmupRefreshListener {

    private @Nullable MyWarmupAccountHandler bridgeHandler;
    private @Nullable ThingUID bridgeUID;

    public WarmupDiscoveryService() {
        super(DISCOVERABLE_THING_TYPES_UIDS, 5, false);
    }

    @Override
    public void deactivate() {
    }

    @Override
    public void startScan() {
        final MyWarmupAccountHandler handler = bridgeHandler;
        if (handler != null) {
            removeOlderResults(getTimestampOfLastScan());
            handler.setDiscoveryService(this);
        }
    }

    /**
     * Process device list and populate discovery list with things
     *
     * @param domain Data model representing all devices
     */
    @Override
    public void refresh(@Nullable QueryResponseDTO domain) {
        if (domain != null) {
            HashSet<ThingUID> discoveredThings = new HashSet<ThingUID>();
            for (LocationDTO location : domain.getData().getUser().getLocations()) {
                for (RoomDTO room : location.getRooms()) {
                    discoverRoom(location, room, discoveredThings);
                }
            }
        }
    }

    private void discoverRoom(LocationDTO location, RoomDTO room, HashSet<ThingUID> discoveredThings) {
        if (room.getThermostat4ies() != null && !room.getThermostat4ies().isEmpty()) {
            final String deviceSN = room.getThermostat4ies().get(0).getDeviceSN();
            ThingUID localBridgeUID = this.bridgeUID;
            if (localBridgeUID != null && deviceSN != null) {
                final Map<String, Object> roomProperties = new HashMap<>();
                roomProperties.put(Thing.PROPERTY_SERIAL_NUMBER, deviceSN);
                roomProperties.put(PROPERTY_ROOM_ID, room.getId());
                roomProperties.put(PROPERTY_ROOM_NAME, room.getName());
                roomProperties.put(PROPERTY_LOCATION_ID, location.getId());
                roomProperties.put(PROPERTY_LOCATION_NAME, location.getName());

                ThingUID roomThingUID = new ThingUID(THING_TYPE_ROOM, localBridgeUID, deviceSN);
                thingDiscovered(DiscoveryResultBuilder.create(roomThingUID).withBridge(localBridgeUID)
                        .withProperties(roomProperties).withLabel(location.getName() + " - " + room.getName())
                        .withRepresentationProperty(Thing.PROPERTY_SERIAL_NUMBER).build());

                discoveredThings.add(roomThingUID);
            }
        }
    }

    @Override
    public void setThingHandler(@Nullable final ThingHandler handler) {
        if (handler instanceof MyWarmupAccountHandler accountHandler) {
            bridgeHandler = accountHandler;
            bridgeUID = handler.getThing().getUID();
        } else {
            bridgeHandler = null;
            bridgeUID = null;
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return bridgeHandler;
    }
}

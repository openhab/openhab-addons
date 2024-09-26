/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
import org.openhab.core.config.discovery.AbstractThingHandlerDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * The {@link WarmupDiscoveryService} is used to discover devices that are connected to a My Warmup account.
 *
 * @author James Melville - Initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = WarmupDiscoveryService.class)
@NonNullByDefault
public class WarmupDiscoveryService extends AbstractThingHandlerDiscoveryService<MyWarmupAccountHandler>
        implements WarmupRefreshListener {

    private @Nullable ThingUID bridgeUID;

    public WarmupDiscoveryService() {
        super(MyWarmupAccountHandler.class, DISCOVERABLE_THING_TYPES_UIDS, 5, false);
    }

    @Override
    public void deactivate() {
    }

    @Override
    public void startScan() {
        removeOlderResults(getTimestampOfLastScan());
        thingHandler.setDiscoveryService(this);
    }

    /**
     * Process device list and populate discovery list with things
     *
     * @param domain Data model representing all devices
     */
    @Override
    public void refresh(@Nullable QueryResponseDTO domain) {
        if (domain != null) {
            HashSet<ThingUID> discoveredThings = new HashSet<>();
            for (LocationDTO location : domain.data().user().locations()) {
                for (RoomDTO room : location.rooms()) {
                    discoverRoom(location, room, discoveredThings);
                }
            }
        }
    }

    private void discoverRoom(LocationDTO location, RoomDTO room, HashSet<ThingUID> discoveredThings) {
        if (room.thermostat4ies() != null && !room.thermostat4ies().isEmpty()) {
            final String deviceSN = room.thermostat4ies().get(0).deviceSN();
            ThingUID localBridgeUID = this.bridgeUID;
            if (localBridgeUID != null && deviceSN != null) {
                final Map<String, Object> roomProperties = new HashMap<>();
                roomProperties.put(Thing.PROPERTY_SERIAL_NUMBER, deviceSN);
                roomProperties.put(PROPERTY_ROOM_ID, room.getId());
                roomProperties.put(PROPERTY_ROOM_NAME, room.roomName());
                roomProperties.put(PROPERTY_LOCATION_ID, location.getId());
                roomProperties.put(PROPERTY_LOCATION_NAME, location.name());

                ThingUID roomThingUID = new ThingUID(THING_TYPE_ROOM, localBridgeUID, deviceSN);
                thingDiscovered(DiscoveryResultBuilder.create(roomThingUID).withBridge(localBridgeUID)
                        .withProperties(roomProperties).withLabel(location.name() + " - " + room.roomName())
                        .withRepresentationProperty(Thing.PROPERTY_SERIAL_NUMBER).build());

                discoveredThings.add(roomThingUID);
            }
        }
    }
}

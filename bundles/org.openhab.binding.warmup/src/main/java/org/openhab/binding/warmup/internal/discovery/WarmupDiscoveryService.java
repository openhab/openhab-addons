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
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
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

    public WarmupDiscoveryService(MyWarmupAccountHandler handler) {
        super(SUPPORTED_THING_TYPES_UIDS, 60, true);
        this.bridgeHandler = handler;
        this.bridgeUID = handler.getThing().getUID();
    }

    @Override
    protected void startScan() {
        bridgeHandler.setDiscoveryService(this);
    }

    @Override
    public synchronized void stopScan() {
        bridgeHandler.unsetDiscoveryService();
        super.stopScan();
        removeOlderResults(getTimestampOfLastScan());
    }

    /**
     * Process device list and populate discovery list with things
     *
     * @param domain Data model representing all devices
     */
    public void onRefresh(final QueryResponseDTO domain) {
        for (LocationDTO location : domain.getData().getUser().getLocations()) {
            for (RoomDTO room : location.getRooms()) {
                String deviceSN = room.getThermostat4ies().get(0).getDeviceSN();
                final Map<String, Object> roomProperties = new HashMap<>();

                roomProperties.put("Id", room.getId().toString());
                roomProperties.put("Name", room.getName());
                roomProperties.put("Location", location.getName());
                // serial number displayed in UI
                roomProperties.put("Serial Number", deviceSN);
                // serial number used on configuration
                roomProperties.put("serialNumber", deviceSN);

                thingDiscovered(DiscoveryResultBuilder.create(new ThingUID(THING_TYPE_ROOM, bridgeUID, deviceSN))
                        .withBridge(bridgeUID).withProperties(roomProperties)
                        .withLabel(location.getName() + " - " + room.getName())
                        .withRepresentationProperty(room.getId().toString()).build());
            }
        }
    }
}

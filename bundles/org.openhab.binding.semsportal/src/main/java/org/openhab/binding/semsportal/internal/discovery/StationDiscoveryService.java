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
package org.openhab.binding.semsportal.internal.discovery;

import static org.openhab.binding.semsportal.internal.SEMSPortalBindingConstants.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.semsportal.internal.PortalHandler;
import org.openhab.binding.semsportal.internal.dto.Station;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;

/**
 * The discovery service can discover the power stations that are registered to the portal that it belongs to. It will
 * find unique power stations and add them as a discovery result;
 *
 * @author Iwan Bron - Initial contribution
 *
 */
@NonNullByDefault
public class StationDiscoveryService extends AbstractDiscoveryService {

    private static final int DISCOVERY_TIME = 10;
    private PortalHandler portal;
    private ThingUID bridgeUID;

    public StationDiscoveryService(PortalHandler bridgeHandler) {
        super(Set.of(THING_TYPE_STATION), DISCOVERY_TIME);
        this.portal = bridgeHandler;
        this.bridgeUID = bridgeHandler.getThing().getUID();
    }

    @Override
    protected void startScan() {
        for (Station station : portal.getAllStations()) {
            DiscoveryResult discovery = DiscoveryResultBuilder.create(createThingUUID(station)).withBridge(bridgeUID)
                    .withProperties(buildProperties(station))
                    .withRepresentationProperty(STATION_REPRESENTATION_PROPERTY)
                    .withLabel(String.format(STATION_LABEL_FORMAT, station.getName())).withThingType(THING_TYPE_STATION)
                    .build();
            thingDiscovered(discovery);
        }
        stopScan();
    }

    private ThingUID createThingUUID(Station station) {
        return new ThingUID(THING_TYPE_STATION, station.getStationId(), bridgeUID.getId());
    }

    private @Nullable Map<String, Object> buildProperties(Station station) {
        Map<String, Object> properties = new HashMap<>();
        properties.put(Thing.PROPERTY_MODEL_ID, station.getType());
        properties.put(Thing.PROPERTY_SERIAL_NUMBER, station.getSerialNumber());
        properties.put(STATION_NAME, station.getName());
        properties.put(STATION_CAPACITY, station.getCapacity());
        properties.put(STATION_UUID, station.getStationId());
        properties.put(STATION_CAPACITY, station.getCapacity());
        return properties;
    }
}

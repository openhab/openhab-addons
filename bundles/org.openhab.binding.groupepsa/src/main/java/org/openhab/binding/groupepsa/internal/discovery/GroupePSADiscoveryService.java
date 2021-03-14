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
package org.openhab.binding.groupepsa.internal.discovery;

import static org.openhab.binding.groupepsa.internal.GroupePSABindingConstants.THING_TYPE_VEHICLE;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.groupepsa.internal.GroupePSABindingConstants;
import org.openhab.binding.groupepsa.internal.bridge.GroupePSABridgeHandler;
import org.openhab.binding.groupepsa.internal.rest.api.dto.Vehicle;
import org.openhab.binding.groupepsa.internal.rest.exceptions.GroupePSACommunicationException;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link GroupePSADiscoveryService} is responsible for discovering new
 * vehicles available for the configured app key.
 *
 * @author Arjan Mels - Initial contribution
 */
@NonNullByDefault
public class GroupePSADiscoveryService extends AbstractDiscoveryService {
    private final Logger logger = LoggerFactory.getLogger(GroupePSADiscoveryService.class);

    private final GroupePSABridgeHandler bridgeHandler;

    public GroupePSADiscoveryService(GroupePSABridgeHandler bridgeHandler) {
        super(Collections.singleton(THING_TYPE_VEHICLE), 10, false);
        this.bridgeHandler = bridgeHandler;
    }

    @Override
    protected void startScan() {
        try {
            List<Vehicle> vehicles = bridgeHandler.getVehicles();
            if (vehicles == null || vehicles.size() == 0) {
                logger.warn("No vehicles found");
                return;
            }
            for (Vehicle vehicle : vehicles) {
                ThingUID bridgeUID = bridgeHandler.getThing().getUID();
                ThingTypeUID thingTypeUID = THING_TYPE_VEHICLE;
                String vin = vehicle.getId();
                if (vin != null) {
                    ThingUID VehicleThingUid = new ThingUID(THING_TYPE_VEHICLE, bridgeUID, vin);

                    Map<String, Object> properties = new HashMap<>();
                    properties.put(GroupePSABindingConstants.VEHICLE_VIN, vin);
                    String brand = vehicle.getBrand();
                    if (brand == null)
                        brand = "Unknown";
                    properties.put(GroupePSABindingConstants.VEHICLE_VENDOR, brand);
                    String label = vehicle.getLabel();
                    if (label == null)
                        label = "Unknown";
                    properties.put(GroupePSABindingConstants.VEHICLE_MODEL, label);

                    DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(VehicleThingUid)
                            .withThingType(thingTypeUID).withProperties(properties).withBridge(bridgeUID)
                            .withRepresentationProperty(GroupePSABindingConstants.VEHICLE_VIN)
                            .withLabel(vehicle.getBrand() + "  " + vehicle.getLabel()).build();

                    thingDiscovered(discoveryResult);
                }
            }
        } catch (GroupePSACommunicationException e) {
            logger.warn("No vehicles found", e);
            return;
        }
    }
}

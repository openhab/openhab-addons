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
package org.openhab.binding.groupepsa.internal.discovery;

import static org.openhab.binding.groupepsa.internal.GroupePSABindingConstants.THING_TYPE_VEHICLE;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.groupepsa.internal.GroupePSABindingConstants;
import org.openhab.binding.groupepsa.internal.bridge.GroupePSABridgeHandler;
import org.openhab.binding.groupepsa.internal.rest.api.dto.Vehicle;
import org.openhab.binding.groupepsa.internal.rest.exceptions.GroupePSACommunicationException;
import org.openhab.core.config.discovery.AbstractThingHandlerDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link GroupePSADiscoveryService} is responsible for discovering new
 * vehicles available for the configured app key.
 *
 * @author Arjan Mels - Initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = GroupePSADiscoveryService.class)
@NonNullByDefault
public class GroupePSADiscoveryService extends AbstractThingHandlerDiscoveryService<GroupePSABridgeHandler> {
    private final Logger logger = LoggerFactory.getLogger(GroupePSADiscoveryService.class);

    public GroupePSADiscoveryService() {
        super(GroupePSABridgeHandler.class, Set.of(THING_TYPE_VEHICLE), 10, false);
    }

    @Override
    protected void startScan() {
        try {
            List<Vehicle> vehicles = thingHandler.getVehicles();
            if (vehicles == null || vehicles.isEmpty()) {
                logger.warn("No vehicles found");
                return;
            }
            for (Vehicle vehicle : vehicles) {
                ThingUID bridgeUID = thingHandler.getThing().getUID();
                ThingTypeUID thingTypeUID = THING_TYPE_VEHICLE;
                String id = vehicle.getId();
                if (id != null) {
                    ThingUID vehicleThingUid = new ThingUID(THING_TYPE_VEHICLE, bridgeUID, id);

                    Map<String, Object> properties = new HashMap<>();
                    putProperty(properties, GroupePSABindingConstants.VEHICLE_ID, id);
                    putProperty(properties, GroupePSABindingConstants.VEHICLE_VIN, vehicle.getVin());
                    putProperty(properties, GroupePSABindingConstants.VEHICLE_VENDOR, vehicle.getBrand());
                    putProperty(properties, GroupePSABindingConstants.VEHICLE_MODEL, vehicle.getLabel());

                    DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(vehicleThingUid)
                            .withThingType(thingTypeUID).withProperties(properties).withBridge(bridgeUID)
                            .withRepresentationProperty(GroupePSABindingConstants.VEHICLE_VIN)
                            .withLabel(vehicle.getBrand() + "  (" + vehicle.getVin() + ")").build();

                    thingDiscovered(discoveryResult);
                }
            }
        } catch (GroupePSACommunicationException e) {
            logger.warn("No vehicles found", e);
            return;
        }
    }

    private void putProperty(Map<String, Object> properties, String key, @Nullable String value) {
        if (value == null) {
            value = "Unknown";
        }
        properties.put(key, value);
    }
}

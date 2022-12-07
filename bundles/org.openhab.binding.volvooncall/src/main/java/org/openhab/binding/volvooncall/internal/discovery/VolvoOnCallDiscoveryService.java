/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.volvooncall.internal.discovery;

import static org.openhab.binding.volvooncall.internal.VolvoOnCallBindingConstants.*;

import java.io.IOException;
import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.volvooncall.internal.dto.AccountVehicleRelation;
import org.openhab.binding.volvooncall.internal.dto.Attributes;
import org.openhab.binding.volvooncall.internal.dto.Vehicles;
import org.openhab.binding.volvooncall.internal.handler.VolvoOnCallBridgeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link VolvoOnCallDiscoveryService} searches for available
 * cars discoverable through VocAPI
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class VolvoOnCallDiscoveryService extends AbstractDiscoveryService {
    private static final int SEARCH_TIME = 2;
    private final Logger logger = LoggerFactory.getLogger(VolvoOnCallDiscoveryService.class);
    private final VolvoOnCallBridgeHandler bridgeHandler;

    public VolvoOnCallDiscoveryService(VolvoOnCallBridgeHandler bridgeHandler) {
        super(SUPPORTED_THING_TYPES_UIDS, SEARCH_TIME);
        this.bridgeHandler = bridgeHandler;
    }

    @Override
    public void startScan() {
        String[] relations = bridgeHandler.getVehiclesRelationsURL();
        Arrays.stream(relations).forEach(relationURL -> {
            try {
                AccountVehicleRelation accountVehicle = bridgeHandler.getURL(relationURL, AccountVehicleRelation.class);
                logger.debug("Found vehicle : {}", accountVehicle.vehicleId);

                Vehicles vehicle = bridgeHandler.getURL(accountVehicle.vehicleURL, Vehicles.class);
                Attributes attributes = bridgeHandler.getURL(Attributes.class, vehicle.vehicleId);

                thingDiscovered(
                        DiscoveryResultBuilder.create(new ThingUID(VEHICLE_THING_TYPE, accountVehicle.vehicleId))
                                .withLabel(attributes.vehicleType + " " + attributes.registrationNumber)
                                .withBridge(bridgeHandler.getThing().getUID()).withProperty(VIN, attributes.vin)
                                .withRepresentationProperty(accountVehicle.vehicleId).build());

            } catch (IOException e) {
                logger.warn("Error while discovering vehicle: {}", e.getMessage());
            }
        });

        stopScan();
    }

}

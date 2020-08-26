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
package org.openhab.binding.bmwconnecteddrive.internal.discovery;

import static org.openhab.binding.bmwconnecteddrive.internal.ConnectedDriveConstants.SUPPORTED_THING_SET;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.bmwconnecteddrive.internal.dto.ConnectedDriveUserInfo;
import org.openhab.binding.bmwconnecteddrive.internal.dto.Vehicle;
import org.openhab.binding.bmwconnecteddrive.internal.handler.ConnectedDriveBridgeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ConnectedCarDiscovery} decodes the initial query from ConnectedDrive and is creating
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
// @Component(service = DiscoveryService.class, immediate = true, configurationPid = "discovery.bmwconnecteddrive")
public class ConnectedCarDiscovery extends AbstractDiscoveryService {
    private final Logger logger = LoggerFactory.getLogger(ConnectedCarDiscovery.class);
    private static final int DISCOVERY_TIMEOUT = 10;

    private ConnectedDriveBridgeHandler bridgeHandler;

    public ConnectedCarDiscovery(ConnectedDriveBridgeHandler bh) {
        super(SUPPORTED_THING_SET, DISCOVERY_TIMEOUT, false);
        bridgeHandler = bh;
        logger.info("Created ConnectedCarDiscovery");
    }

    @Override
    protected void startScan() {
        // nothing to start - wait until first results of ConnectedDriveBridgeHandler results
    }

    public void scan(ConnectedDriveUserInfo cdui) {
        List<Vehicle> vehicles = cdui.getVehicles();
        logger.info("Discovered {} Vehicles", vehicles.size());
        vehicles.forEach(vehicle -> {
            ThingUID bridgeUID = bridgeHandler.getThing().getUID();
            // the DriveTrain field in the delivered json is defining the Vehicle Type
            String vehicleType = vehicle.driveTrain;
            SUPPORTED_THING_SET.forEach(entry -> {
                if (entry.getId().equals(vehicleType)) {
                    ThingUID uid = new ThingUID(entry, vehicle.vin);
                    Map<String, Object> properties = new HashMap<>();
                    properties.put("vin", vehicle.vin);
                    properties.put("refreshInterval", 5);
                    properties.put("model", vehicle.model);
                    properties.put("statisticsCommunityEnabled", vehicle.color);
                    properties.put("driveTrain", vehicle.driveTrain);
                    properties.put("brand", vehicle.brand);
                    properties.put("yearOfConstruction", vehicle.yearOfConstruction);
                    properties.put("bodytype", vehicle.bodytype);
                    properties.put("statisticsCommunityEnabled", vehicle.color);
                    properties.put("dealerName", vehicle.dealer.name);
                    properties.put("dealerAddress", vehicle.dealer.street + " " + vehicle.dealer.country + " "
                            + vehicle.dealer.postalCode + " " + vehicle.dealer.city);
                    properties.put("dealerPhone", vehicle.dealer.phone);
                    properties.put("breakdownNumber", vehicle.breakdownNumber);
                    StringBuffer chargingModes = new StringBuffer();
                    if (vehicle.supportedChargingModes != null) {
                        vehicle.supportedChargingModes.forEach(e -> {
                            chargingModes.append(e + " ");
                        });
                    }

                    properties.put("activatedServcies", getObject(vehicle, Vehicle.ACTIVATED));
                    properties.put("supportedServices", getObject(vehicle, Vehicle.SUPPORTED));
                    properties.put("activatedServcies", getObject(vehicle, Vehicle.NOT_SUPPORTED));
                    String carLabel = vehicle.brand + " " + vehicle.model;
                    logger.info("Thing {} discovered", carLabel);
                    thingDiscovered(DiscoveryResultBuilder.create(uid).withBridge(bridgeUID)
                            .withRepresentationProperty("vin").withLabel(carLabel).withProperties(properties).build());
                }
            });

        });
    }

    public String getObject(Object obj, String compare) {
        StringBuffer buf = new StringBuffer();
        for (Field field : obj.getClass().getDeclaredFields()) {
            try {
                if (field.get(obj).equals(compare)) {
                    buf.append(field.getName());
                }
            } catch (IllegalArgumentException e) {
                logger.warn("Field {} not found {}", compare, e.getMessage());
            } catch (IllegalAccessException e) {
                logger.warn("Field {} not found {}", compare, e.getMessage());
            }
        }
        return buf.toString();
    }
}

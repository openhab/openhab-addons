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
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.bmwconnecteddrive.internal.ConnectedDriveHandlerFactory;
import org.openhab.binding.bmwconnecteddrive.internal.dto.discovery.Vehicle;
import org.openhab.binding.bmwconnecteddrive.internal.dto.discovery.VehiclesContainer;
import org.openhab.binding.bmwconnecteddrive.internal.handler.ConnectedDriveBridgeHandler;
import org.openhab.binding.bmwconnecteddrive.internal.handler.VehicleHandler;
import org.openhab.binding.bmwconnecteddrive.internal.utils.Constants;
import org.openhab.binding.bmwconnecteddrive.internal.utils.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link VehicleDiscovery} decodes the initial query from ConnectedDrive and is creating
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
// @Component(service = DiscoveryService.class, immediate = true, configurationPid = "discovery.bmwconnecteddrive")
public class VehicleDiscovery extends AbstractDiscoveryService {
    private final Logger logger = LoggerFactory.getLogger(VehicleDiscovery.class);
    private static final int DISCOVERY_TIMEOUT = 10;

    private ConnectedDriveBridgeHandler bridgeHandler;

    public VehicleDiscovery(ConnectedDriveBridgeHandler bh) {
        super(SUPPORTED_THING_SET, DISCOVERY_TIMEOUT, false);
        bridgeHandler = bh;
    }

    @Override
    protected void startScan() {
        bridgeHandler.requestVehicles();
    }

    public void onResponse(VehiclesContainer container) {
        List<Vehicle> vehicles = container.vehicles;
        vehicles.forEach(vehicle -> {
            ThingUID bridgeUID = bridgeHandler.getThing().getUID();
            // the DriveTrain field in the delivered json is defining the Vehicle Type
            String vehicleType = vehicle.driveTrain;
            SUPPORTED_THING_SET.forEach(entry -> {
                if (entry.getId().equals(vehicleType)) {
                    ThingUID uid = new ThingUID(entry, vehicle.vin);
                    Map<String, String> properties = new HashMap<>();
                    // Dealer
                    if (vehicle.dealer != null) {
                        properties.put("Dealer", vehicle.dealer.name);
                        properties.put("Dealer Address", vehicle.dealer.street + " " + vehicle.dealer.country + " "
                                + vehicle.dealer.postalCode + " " + vehicle.dealer.city);
                        properties.put("Dealer Phone", vehicle.dealer.phone);
                    }

                    // Services & Support
                    properties.put("Services Activated", getObject(vehicle, Constants.ACTIVATED));
                    String servicesSupported = getObject(vehicle, Constants.SUPPORTED);
                    String servicesNotSupported = getObject(vehicle, Constants.NOT_SUPPORTED);
                    if (vehicle.statisticsAvailable) {
                        servicesSupported += Constants.STATISTICS;
                    } else {
                        servicesNotSupported += Constants.STATISTICS;
                    }
                    properties.put(Constants.SERVICES_SUPPORTED, servicesSupported);
                    properties.put("Services Not Supported", servicesNotSupported);
                    properties.put("Support Breakdown Number", vehicle.breakdownNumber);

                    // Vehicle Properties
                    if (vehicle.supportedChargingModes != null) {
                        StringBuffer chargingModes = new StringBuffer();
                        vehicle.supportedChargingModes.forEach(e -> {
                            chargingModes.append(e).append(Constants.SPACE);
                        });
                        properties.put("Vehicle Charge Modes", chargingModes.toString());
                    }
                    if (vehicle.hasAlarmSystem) {
                        properties.put("Vehicle Alarm System", "Available");
                    } else {
                        properties.put("Vehicle Alarm System", "Not Available");
                    }
                    properties.put("Vehicle Brand", vehicle.brand);
                    properties.put("Vehicle Bodytype", vehicle.bodytype);
                    properties.put("Vehicle Color", vehicle.color);
                    properties.put("Vehicle Construction Year", Short.toString(vehicle.yearOfConstruction));
                    properties.put("Vehicle Drive Train", vehicle.driveTrain);
                    properties.put("Vehicle Model", vehicle.model);
                    if (vehicle.chargingControl != null) {
                        properties.put("Vehicle Charge Control", Converter.toTitleCase(vehicle.model));
                    }

                    // Check now if a thing with the same VIN exists
                    final AtomicBoolean foundVehicle = new AtomicBoolean(false);
                    List<VehicleHandler> l = ConnectedDriveHandlerFactory.getHandlerRegistry();
                    logger.debug("Handler regsitry has {} entries", l.size());
                    l.forEach(handler -> {
                        Thing vehicleThing = handler.getThing();
                        Configuration c = vehicleThing.getConfiguration();
                        if (c.containsKey("vin")) {
                            String thingVIN = c.get("vin").toString();
                            logger.debug("Found VIN {} ", thingVIN);
                            if (vehicle.vin.equals(thingVIN)) {
                                vehicleThing.setProperties(properties);
                                logger.debug("Vehicle {} updated", thingVIN);
                                foundVehicle.set(true);
                            }
                        }
                    });

                    // Vehicle not found -> trigger discovery
                    if (!foundVehicle.get()) {
                        // Properties needed for functional THing
                        properties.put("vin", vehicle.vin);
                        properties.put("refreshInterval", Integer.toString(15));
                        properties.put("units", "AUTODETECT");
                        properties.put("imageSize", Integer.toString(500));
                        properties.put("imageViewport", "FRONT");

                        String vehicleLabel = vehicle.brand + " " + vehicle.model;
                        logger.debug("Thing {} discovered", vehicleLabel);
                        Map<String, Object> convertedProperties = new HashMap<String, Object>(properties);
                        thingDiscovered(DiscoveryResultBuilder.create(uid).withBridge(bridgeUID)
                                .withRepresentationProperty("vin").withLabel(vehicleLabel)
                                .withProperties(convertedProperties).build());
                    }
                }
            });

        });
    }

    public String getObject(Object obj, String compare) {
        StringBuffer buf = new StringBuffer();
        for (Field field : obj.getClass().getDeclaredFields()) {
            try {
                if (field.get(obj) != null) {
                    if (field.get(obj).equals(compare)) {
                        buf.append(Converter.capitalizeFirst(field.getName()) + Constants.SPACE);
                    }
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

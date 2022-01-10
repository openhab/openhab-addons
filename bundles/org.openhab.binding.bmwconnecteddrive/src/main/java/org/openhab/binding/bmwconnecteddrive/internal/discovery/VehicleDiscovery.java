/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bmwconnecteddrive.internal.ConnectedDriveConstants;
import org.openhab.binding.bmwconnecteddrive.internal.dto.discovery.VehiclesContainer;
import org.openhab.binding.bmwconnecteddrive.internal.handler.ConnectedDriveBridgeHandler;
import org.openhab.binding.bmwconnecteddrive.internal.utils.Constants;
import org.openhab.binding.bmwconnecteddrive.internal.utils.Converter;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link VehicleDiscovery} requests data from ConnectedDrive and is identifying the Vehicles after response
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class VehicleDiscovery extends AbstractDiscoveryService implements DiscoveryService, ThingHandlerService {

    private final Logger logger = LoggerFactory.getLogger(VehicleDiscovery.class);
    private static final int DISCOVERY_TIMEOUT = 10;
    private Optional<ConnectedDriveBridgeHandler> bridgeHandler = Optional.empty();

    public VehicleDiscovery() {
        super(SUPPORTED_THING_SET, DISCOVERY_TIMEOUT, false);
    }

    public void onResponse(VehiclesContainer container) {
        bridgeHandler.ifPresent(bridge -> {
            final ThingUID bridgeUID = bridge.getThing().getUID();
            container.vehicles.forEach(vehicle -> {
                // the DriveTrain field in the delivered json is defining the Vehicle Type
                String vehicleType = vehicle.driveTrain.toLowerCase();
                SUPPORTED_THING_SET.forEach(entry -> {
                    if (entry.getId().equals(vehicleType)) {
                        ThingUID uid = new ThingUID(entry, vehicle.vin, bridgeUID.getId());
                        Map<String, String> properties = new HashMap<>();
                        // Dealer
                        if (vehicle.dealer != null) {
                            properties.put("dealer", vehicle.dealer.name);
                            properties.put("dealerAddress", vehicle.dealer.street + " " + vehicle.dealer.country + " "
                                    + vehicle.dealer.postalCode + " " + vehicle.dealer.city);
                            properties.put("dealerPhone", vehicle.dealer.phone);
                        }

                        // Services & Support
                        properties.put("servicesActivated", getObject(vehicle, Constants.ACTIVATED));
                        String servicesSupported = getObject(vehicle, Constants.SUPPORTED);
                        String servicesNotSupported = getObject(vehicle, Constants.NOT_SUPPORTED);
                        if (vehicle.statisticsAvailable) {
                            servicesSupported += Constants.STATISTICS;
                        } else {
                            servicesNotSupported += Constants.STATISTICS;
                        }
                        properties.put(Constants.SERVICES_SUPPORTED, servicesSupported);
                        properties.put("servicesNotSupported", servicesNotSupported);
                        properties.put("supportBreakdownNumber", vehicle.breakdownNumber);

                        // Vehicle Properties
                        if (vehicle.supportedChargingModes != null) {
                            properties.put("vehicleChargeModes",
                                    String.join(Constants.SPACE, vehicle.supportedChargingModes));
                        }
                        if (vehicle.hasAlarmSystem) {
                            properties.put("vehicleAlarmSystem", "Available");
                        } else {
                            properties.put("vehicleAlarmSystem", "Not Available");
                        }
                        properties.put("vehicleBrand", vehicle.brand);
                        properties.put("vehicleBodytype", vehicle.bodytype);
                        properties.put("vehicleColor", vehicle.color);
                        properties.put("vehicleConstructionYear", Short.toString(vehicle.yearOfConstruction));
                        properties.put("vehicleDriveTrain", vehicle.driveTrain);
                        properties.put("vehicleModel", vehicle.model);
                        if (vehicle.chargingControl != null) {
                            properties.put("vehicleChargeControl", Converter.toTitleCase(vehicle.model));
                        }

                        // Update Properties for already created Things
                        bridge.getThing().getThings().forEach(vehicleThing -> {
                            Configuration c = vehicleThing.getConfiguration();
                            if (c.containsKey(ConnectedDriveConstants.VIN)) {
                                String thingVIN = c.get(ConnectedDriveConstants.VIN).toString();
                                if (vehicle.vin.equals(thingVIN)) {
                                    vehicleThing.setProperties(properties);
                                }
                            }
                        });

                        // Properties needed for functional THing
                        properties.put(ConnectedDriveConstants.VIN, vehicle.vin);
                        properties.put("refreshInterval",
                                Integer.toString(ConnectedDriveConstants.DEFAULT_REFRESH_INTERVAL_MINUTES));
                        properties.put("units", ConnectedDriveConstants.UNITS_AUTODETECT);
                        properties.put("imageSize", Integer.toString(ConnectedDriveConstants.DEFAULT_IMAGE_SIZE_PX));
                        properties.put("imageViewport", ConnectedDriveConstants.DEFAULT_IMAGE_VIEWPORT);

                        String vehicleLabel = vehicle.brand + " " + vehicle.model;
                        Map<String, Object> convertedProperties = new HashMap<String, Object>(properties);
                        thingDiscovered(DiscoveryResultBuilder.create(uid).withBridge(bridgeUID)
                                .withRepresentationProperty(ConnectedDriveConstants.VIN).withLabel(vehicleLabel)
                                .withProperties(convertedProperties).build());
                    }
                });
            });
        });
    };

    /**
     * Get all field names from a DTO with a specific value
     * Used to get e.g. all services which are "ACTIVATED"
     *
     * @param DTO Object
     * @param compare String which needs to map with the value
     * @return String with all field names matching this value separated with Spaces
     */
    public String getObject(Object dto, String compare) {
        StringBuilder buf = new StringBuilder();
        for (Field field : dto.getClass().getDeclaredFields()) {
            try {
                Object value = field.get(dto);
                if (compare.equals(value)) {
                    buf.append(Converter.capitalizeFirst(field.getName()) + Constants.SPACE);
                }
            } catch (IllegalArgumentException | IllegalAccessException e) {
                logger.debug("Field {} not found {}", compare, e.getMessage());
            }
        }
        return buf.toString();
    }

    @Override
    public void setThingHandler(ThingHandler handler) {
        if (handler instanceof ConnectedDriveBridgeHandler) {
            bridgeHandler = Optional.of((ConnectedDriveBridgeHandler) handler);
            bridgeHandler.get().setDiscoveryService(this);
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return bridgeHandler.orElse(null);
    }

    @Override
    protected void startScan() {
        bridgeHandler.ifPresent(ConnectedDriveBridgeHandler::requestVehicles);
    }

    @Override
    public void deactivate() {
        super.deactivate();
    }
}

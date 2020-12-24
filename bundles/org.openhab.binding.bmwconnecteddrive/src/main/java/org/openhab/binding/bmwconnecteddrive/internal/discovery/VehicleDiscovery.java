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
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bmwconnecteddrive.internal.ConnectedDriveConstants;
import org.openhab.binding.bmwconnecteddrive.internal.ConnectedDriveHandlerFactory;
import org.openhab.binding.bmwconnecteddrive.internal.dto.discovery.Vehicle;
import org.openhab.binding.bmwconnecteddrive.internal.dto.discovery.VehiclesContainer;
import org.openhab.binding.bmwconnecteddrive.internal.handler.ConnectedDriveBridgeHandler;
import org.openhab.binding.bmwconnecteddrive.internal.handler.VehicleHandler;
import org.openhab.binding.bmwconnecteddrive.internal.utils.Constants;
import org.openhab.binding.bmwconnecteddrive.internal.utils.Converter;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.Thing;
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

    private @Nullable ConnectedDriveBridgeHandler bridgeHandler;

    public VehicleDiscovery() {
        super(SUPPORTED_THING_SET, DISCOVERY_TIMEOUT, false);
    }

    public void onResponse(VehiclesContainer container) {
        List<Vehicle> vehicles = container.vehicles;
        vehicles.forEach(vehicle -> {
            ThingUID bridgeUID = bridgeHandler.getThing().getUID();
            // the DriveTrain field in the delivered json is defining the Vehicle Type
            String vehicleType = vehicle.driveTrain.toLowerCase();
            SUPPORTED_THING_SET.forEach(entry -> {
                if (entry.getId().equals(vehicleType)) {
                    ThingUID uid = new ThingUID(entry, vehicle.vin, bridgeUID.getId());
                    Map<String, String> properties = new HashMap<>();
                    // Dealer
                    if (vehicle.dealer != null) {
                        properties.put("Dealer", vehicle.dealer.name);
                        properties.put("DealerAddress", vehicle.dealer.street + " " + vehicle.dealer.country + " "
                                + vehicle.dealer.postalCode + " " + vehicle.dealer.city);
                        properties.put("DealerPhone", vehicle.dealer.phone);
                    }

                    // Services & Support
                    properties.put("ServicesActivated", getObject(vehicle, Constants.ACTIVATED));
                    String servicesSupported = getObject(vehicle, Constants.SUPPORTED);
                    String servicesNotSupported = getObject(vehicle, Constants.NOT_SUPPORTED);
                    if (vehicle.statisticsAvailable) {
                        servicesSupported += Constants.STATISTICS;
                    } else {
                        servicesNotSupported += Constants.STATISTICS;
                    }
                    properties.put(Constants.SERVICES_SUPPORTED, servicesSupported);
                    properties.put("ServicesNotSupported", servicesNotSupported);
                    properties.put("SupportBreakdownNumber", vehicle.breakdownNumber);

                    // Vehicle Properties
                    if (vehicle.supportedChargingModes != null) {
                        StringBuilder chargingModes = new StringBuilder();
                        vehicle.supportedChargingModes.forEach(e -> {
                            chargingModes.append(e).append(Constants.SPACE);
                        });
                        properties.put("VehicleChargeModes", chargingModes.toString());
                    }
                    if (vehicle.hasAlarmSystem) {
                        properties.put("VehicleAlarmSystem", "Available");
                    } else {
                        properties.put("VehicleAlarmSystem", "Not Available");
                    }
                    properties.put("VehicleBrand", vehicle.brand);
                    properties.put("VehicleBodytype", vehicle.bodytype);
                    properties.put("VehicleColor", vehicle.color);
                    properties.put("VehicleConstructionYear", Short.toString(vehicle.yearOfConstruction));
                    properties.put("VehicleDriveTrain", vehicle.driveTrain);
                    properties.put("VehicleModel", vehicle.model);
                    if (vehicle.chargingControl != null) {
                        properties.put("VehicleChargeControl", Converter.toTitleCase(vehicle.model));
                    }

                    // Check now if a thing with the same VIN exists
                    final AtomicBoolean foundVehicle = new AtomicBoolean(false);
                    List<VehicleHandler> l = ConnectedDriveHandlerFactory.getHandlerRegistry();
                    l.forEach(handler -> {
                        Thing vehicleThing = handler.getThing();
                        Configuration c = vehicleThing.getConfiguration();
                        if (c.containsKey("vin")) {
                            String thingVIN = c.get("vin").toString();
                            if (vehicle.vin.equals(thingVIN)) {
                                vehicleThing.setProperties(properties);
                                foundVehicle.set(true);
                            }
                        }
                    });

                    // Vehicle not found -> trigger discovery
                    if (!foundVehicle.get()) {
                        // Properties needed for functional THing
                        properties.put("vin", vehicle.vin);
                        properties.put("refreshInterval",
                                Integer.toString(ConnectedDriveConstants.DEFAULT_REFRESH_INTERVAL));
                        properties.put("units", ConnectedDriveConstants.UNITS_AUTODETECT);
                        properties.put("imageSize", Integer.toString(ConnectedDriveConstants.DEFAULT_IMAGE_SIZE));
                        properties.put("imageViewport", ConnectedDriveConstants.DEFAULT_IMAGE_VIEWPORT);

                        String vehicleLabel = vehicle.brand + " " + vehicle.model;
                        Map<String, Object> convertedProperties = new HashMap<String, Object>(properties);
                        thingDiscovered(DiscoveryResultBuilder.create(uid).withBridge(bridgeUID)
                                .withRepresentationProperty("vin").withLabel(vehicleLabel)
                                .withProperties(convertedProperties).build());
                    }
                }
            });
        });
    }

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
                if (field.get(dto) != null) {
                    if (field.get(dto).equals(compare)) {
                        buf.append(Converter.capitalizeFirst(field.getName()) + Constants.SPACE);
                    }
                }
            } catch (IllegalArgumentException e) {
                logger.debug("Field {} not found {}", compare, e.getMessage());
            } catch (IllegalAccessException e) {
                logger.debug("Field {} not found {}", compare, e.getMessage());
            }
        }
        return buf.toString();
    }

    @Override
    public void setThingHandler(ThingHandler handler) {
        if (handler instanceof ConnectedDriveBridgeHandler) {
            bridgeHandler = (ConnectedDriveBridgeHandler) handler;
            bridgeHandler.setDiscoveryService(this);
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return bridgeHandler;
    }

    @Override
    public int getScanTimeout() {
        return DISCOVERY_TIMEOUT;
    }

    @Override
    public boolean isBackgroundDiscoveryEnabled() {
        return false;
    }

    @Override
    protected void startScan() {
        bridgeHandler.requestVehicles();
    }

    @Override
    public void abortScan() {
        // nothing to abort
    }

    @Override
    public void activate() {
        super.activate(null);
    }

    @Override
    public void deactivate() {
        super.deactivate();
    }
}

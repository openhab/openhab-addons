/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.mybmw.internal.discovery;

import static org.openhab.binding.mybmw.internal.MyBMWConstants.SUPPORTED_THING_SET;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mybmw.internal.MyBMWConstants;
import org.openhab.binding.mybmw.internal.dto.vehicle.Vehicle;
import org.openhab.binding.mybmw.internal.handler.MyBMWBridgeHandler;
import org.openhab.binding.mybmw.internal.handler.RemoteServiceHandler;
import org.openhab.binding.mybmw.internal.utils.Constants;
import org.openhab.binding.mybmw.internal.utils.VehicleStatusUtils;
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
 * The {@link VehicleDiscovery} requests data from BMW API and is identifying the Vehicles after response
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class VehicleDiscovery extends AbstractDiscoveryService implements DiscoveryService, ThingHandlerService {
    private static final Logger LOGGER = LoggerFactory.getLogger(VehicleDiscovery.class);
    public static final String SUPPORTED_SUFFIX = "Supported";
    public static final String ENABLE_SUFFIX = "Enable";
    public static final String ENABLED_SUFFIX = "Enabled";
    private static final int DISCOVERY_TIMEOUT = 10;
    private Optional<MyBMWBridgeHandler> bridgeHandler = Optional.empty();

    public VehicleDiscovery() {
        super(SUPPORTED_THING_SET, DISCOVERY_TIMEOUT, false);
    }

    public void onResponse(List<Vehicle> vehicleList) {
        bridgeHandler.ifPresent(bridge -> {
            final ThingUID bridgeUID = bridge.getThing().getUID();
            vehicleList.forEach(vehicle -> {
                // the DriveTrain field in the delivered json is defining the Vehicle Type
                String vehicleType = VehicleStatusUtils.vehicleType(vehicle.driveTrain, vehicle.model).toString();
                SUPPORTED_THING_SET.forEach(entry -> {
                    if (entry.getId().equals(vehicleType)) {
                        ThingUID uid = new ThingUID(entry, vehicle.vin, bridgeUID.getId());
                        Map<String, String> properties = new HashMap<>();
                        // Vehicle Properties
                        properties.put("vehicleModel", vehicle.model);
                        properties.put("vehicleDriveTrain", vehicle.driveTrain);
                        properties.put("vehicleConstructionYear", Integer.toString(vehicle.year));
                        properties.put("vehicleBodytype", vehicle.bodyType);

                        properties.put("servicesSupported", getServices(vehicle, SUPPORTED_SUFFIX, true));
                        properties.put("servicesUnsupported", getServices(vehicle, SUPPORTED_SUFFIX, false));
                        String servicesEnabled = getServices(vehicle, ENABLED_SUFFIX, true) + Constants.SEMICOLON
                                + getServices(vehicle, ENABLE_SUFFIX, true);
                        properties.put("servicesEnabled", servicesEnabled.trim());
                        String servicesDisabled = getServices(vehicle, ENABLED_SUFFIX, false) + Constants.SEMICOLON
                                + getServices(vehicle, ENABLE_SUFFIX, false);
                        properties.put("servicesDisabled", servicesDisabled.trim());

                        // For RemoteServices we need to do it step-by-step
                        StringBuffer remoteServicesEnabled = new StringBuffer();
                        StringBuffer remoteServicesDisabled = new StringBuffer();
                        if (vehicle.capabilities.lock.isEnabled) {
                            remoteServicesEnabled.append(
                                    RemoteServiceHandler.RemoteService.DOOR_LOCK.getLabel() + Constants.SEMICOLON);
                        } else {
                            remoteServicesDisabled.append(
                                    RemoteServiceHandler.RemoteService.DOOR_LOCK.getLabel() + Constants.SEMICOLON);
                        }
                        if (vehicle.capabilities.unlock.isEnabled) {
                            remoteServicesEnabled.append(
                                    RemoteServiceHandler.RemoteService.DOOR_UNLOCK.getLabel() + Constants.SEMICOLON);
                        } else {
                            remoteServicesDisabled.append(
                                    RemoteServiceHandler.RemoteService.DOOR_UNLOCK.getLabel() + Constants.SEMICOLON);
                        }
                        if (vehicle.capabilities.lights.isEnabled) {
                            remoteServicesEnabled.append(
                                    RemoteServiceHandler.RemoteService.LIGHT_FLASH.getLabel() + Constants.SEMICOLON);
                        } else {
                            remoteServicesDisabled.append(
                                    RemoteServiceHandler.RemoteService.LIGHT_FLASH.getLabel() + Constants.SEMICOLON);
                        }
                        if (vehicle.capabilities.horn.isEnabled) {
                            remoteServicesEnabled.append(
                                    RemoteServiceHandler.RemoteService.HORN_BLOW.getLabel() + Constants.SEMICOLON);
                        } else {
                            remoteServicesDisabled.append(
                                    RemoteServiceHandler.RemoteService.HORN_BLOW.getLabel() + Constants.SEMICOLON);
                        }
                        if (vehicle.capabilities.vehicleFinder.isEnabled) {
                            remoteServicesEnabled.append(
                                    RemoteServiceHandler.RemoteService.VEHICLE_FINDER.getLabel() + Constants.SEMICOLON);
                        } else {
                            remoteServicesDisabled.append(
                                    RemoteServiceHandler.RemoteService.VEHICLE_FINDER.getLabel() + Constants.SEMICOLON);
                        }
                        if (vehicle.capabilities.climateNow.isEnabled) {
                            remoteServicesEnabled.append(RemoteServiceHandler.RemoteService.CLIMATE_NOW_START.getLabel()
                                    + Constants.SEMICOLON);
                        } else {
                            remoteServicesDisabled
                                    .append(RemoteServiceHandler.RemoteService.CLIMATE_NOW_START.getLabel()
                                            + Constants.SEMICOLON);
                        }
                        properties.put("remoteServicesEnabled", remoteServicesEnabled.toString().trim());
                        properties.put("remoteServicesDisabled", remoteServicesDisabled.toString().trim());

                        // Update Properties for already created Things
                        bridge.getThing().getThings().forEach(vehicleThing -> {
                            Configuration c = vehicleThing.getConfiguration();
                            if (c.containsKey(MyBMWConstants.VIN)) {
                                String thingVIN = c.get(MyBMWConstants.VIN).toString();
                                if (vehicle.vin.equals(thingVIN)) {
                                    vehicleThing.setProperties(properties);
                                }
                            }
                        });

                        // Properties needed for functional Thing
                        properties.put(MyBMWConstants.VIN, vehicle.vin);
                        properties.put("vehicleBrand", vehicle.brand);
                        properties.put("refreshInterval",
                                Integer.toString(MyBMWConstants.DEFAULT_REFRESH_INTERVAL_MINUTES));

                        String vehicleLabel = vehicle.brand + " " + vehicle.model;
                        Map<String, Object> convertedProperties = new HashMap<String, Object>(properties);
                        thingDiscovered(DiscoveryResultBuilder.create(uid).withBridge(bridgeUID)
                                .withRepresentationProperty(MyBMWConstants.VIN).withLabel(vehicleLabel)
                                .withProperties(convertedProperties).build());
                    }
                });
            });
        });
    }

    @Override
    public void setThingHandler(ThingHandler handler) {
        if (handler instanceof MyBMWBridgeHandler) {
            bridgeHandler = Optional.of((MyBMWBridgeHandler) handler);
            bridgeHandler.get().setDiscoveryService(this);
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return bridgeHandler.orElse(null);
    }

    @Override
    protected void startScan() {
        bridgeHandler.ifPresent(MyBMWBridgeHandler::requestVehicles);
    }

    @Override
    public void deactivate() {
        super.deactivate();
    }

    public static String getServices(Vehicle vehicle, String suffix, boolean enabled) {
        StringBuffer sb = new StringBuffer();
        List<String> l = getObject(vehicle.capabilities, enabled);
        for (String capEntry : l) {
            // remove "is" prefix
            String cut = capEntry.substring(2);
            if (cut.endsWith(suffix)) {
                if (sb.length() > 0) {
                    sb.append(Constants.SEMICOLON);
                }
                sb.append(cut.substring(0, cut.length() - suffix.length()));
            }
        }
        return sb.toString();
    }

    /**
     * Get all field names from a DTO with a specific value
     * Used to get e.g. all services which are "ACTIVATED"
     *
     * @param DTO Object
     * @param compare String which needs to map with the value
     * @return String with all field names matching this value separated with Spaces
     */
    public static List<String> getObject(Object dto, Object compare) {
        List<String> l = new ArrayList<String>();
        for (Field field : dto.getClass().getDeclaredFields()) {
            try {
                Object value = field.get(dto);
                if (compare.equals(value)) {
                    l.add(field.getName());
                }
            } catch (IllegalArgumentException | IllegalAccessException e) {
                LOGGER.debug("Field {} not found {}", compare, e.getMessage());
            }
        }
        return l;
    }
}

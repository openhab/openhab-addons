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
package org.openhab.binding.mybmw.internal.discovery;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mybmw.internal.MyBMWConstants;
import org.openhab.binding.mybmw.internal.dto.network.NetworkException;
import org.openhab.binding.mybmw.internal.dto.vehicle.Vehicle;
import org.openhab.binding.mybmw.internal.dto.vehicle.VehicleAttributes;
import org.openhab.binding.mybmw.internal.dto.vehicle.VehicleCapabilities;
import org.openhab.binding.mybmw.internal.handler.MyBMWBridgeHandler;
import org.openhab.binding.mybmw.internal.handler.backend.MyBMWHttpProxy;
import org.openhab.binding.mybmw.internal.handler.backend.MyBMWProxy;
import org.openhab.binding.mybmw.internal.handler.enums.RemoteService;
import org.openhab.binding.mybmw.internal.utils.Constants;
import org.openhab.binding.mybmw.internal.utils.VehicleStatusUtils;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link VehicleDiscovery} requests data from BMW API and is identifying
 * the Vehicles after response
 *
 * @author Bernd Weymann - Initial contribution
 * @author Martin Grassl - refactoring
 */
@NonNullByDefault
public class VehicleDiscovery extends AbstractDiscoveryService implements ThingHandlerService {

    private final Logger logger = LoggerFactory.getLogger(VehicleDiscovery.class);

    private static final int DISCOVERY_TIMEOUT = 10;

    private Optional<MyBMWBridgeHandler> bridgeHandler = Optional.empty();
    private Optional<MyBMWProxy> myBMWProxy = Optional.empty();
    private Optional<ThingUID> bridgeUid = Optional.empty();

    public VehicleDiscovery() {
        super(MyBMWConstants.SUPPORTED_THING_SET, DISCOVERY_TIMEOUT, false);
    }

    @Override
    public void setThingHandler(ThingHandler handler) {
        if (handler instanceof MyBMWBridgeHandler) {
            logger.trace("xxxVehicleDiscovery.setThingHandler for MybmwBridge");
            bridgeHandler = Optional.of((MyBMWBridgeHandler) handler);
            bridgeHandler.get().setVehicleDiscovery(this);
            bridgeUid = Optional.of(bridgeHandler.get().getThing().getUID());
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return bridgeHandler.orElse(null);
    }

    @Override
    protected void startScan() {
        logger.trace("xxxVehicleDiscovery.startScan");
        discoverVehicles();
    }

    @Override
    public void deactivate() {
        logger.trace("xxxVehicleDiscovery.deactivate");

        super.deactivate();
    }

    public void discoverVehicles() {
        logger.trace("xxxVehicleDiscovery.discoverVehicles");

        if (!myBMWProxy.isPresent()) {
            myBMWProxy = bridgeHandler.get().getMyBmwProxy();
        }

        try {
            Optional<List<@NonNull Vehicle>> vehicleList = myBMWProxy.map(prox -> {
                try {
                    return prox.requestVehicles();
                } catch (NetworkException e) {
                    throw new IllegalStateException("vehicles could not be discovered: " + e.getMessage(), e);
                }
            });
            vehicleList.ifPresentOrElse(vehicles -> {
                bridgeHandler.ifPresent(bridge -> bridge.vehicleDiscoverySuccess());
                processVehicles(vehicles);
            }, () -> bridgeHandler.ifPresent(bridge -> bridge.vehicleDiscoveryError()));
        } catch (IllegalStateException ex) {
            bridgeHandler.ifPresent(bridge -> bridge.vehicleDiscoveryError());
        }
    }

    /**
     * this method is called by the bridgeHandler if the list of vehicles was retrieved successfully
     * 
     * @param vehicleList
     */
    private void processVehicles(List<Vehicle> vehicleList) {
        logger.trace("xxxVehicleDiscovery.processVehicles");

        vehicleList.forEach(vehicle -> {
            // the DriveTrain field in the delivered json is defining the Vehicle Type
            String vehicleType = VehicleStatusUtils
                    .vehicleType(vehicle.getVehicleBase().getAttributes().getDriveTrain(),
                            vehicle.getVehicleBase().getAttributes().getModel())
                    .toString();
            MyBMWConstants.SUPPORTED_THING_SET.forEach(entry -> {
                if (entry.getId().equals(vehicleType)) {
                    ThingUID uid = new ThingUID(entry, vehicle.getVehicleBase().getVin(), bridgeUid.get().getId());

                    Map<String, String> properties = generateProperties(vehicle);

                    boolean thingFound = false;
                    // Update Properties for already created Things
                    List<Thing> vehicleThings = bridgeHandler.get().getThing().getThings();
                    for (Thing vehicleThing : vehicleThings) {
                        Configuration configuration = vehicleThing.getConfiguration();
                        // boolean thingFound = true;
                        if (configuration.containsKey(MyBMWConstants.VIN)) {
                            String thingVIN = configuration.get(MyBMWConstants.VIN).toString();
                            if (vehicle.getVehicleBase().getVin().equals(thingVIN)) {
                                vehicleThing.setProperties(properties);
                                thingFound = true;
                            }
                        }
                    }

                    if (!thingFound) {
                        // Properties needed for functional Thing
                        VehicleAttributes vehicleAttributes = vehicle.getVehicleBase().getAttributes();
                        Map<String, Object> convertedProperties = new HashMap<String, Object>(properties);
                        convertedProperties.put(MyBMWConstants.VIN, vehicle.getVehicleBase().getVin());
                        convertedProperties.put("vehicleBrand", vehicleAttributes.getBrand());
                        convertedProperties.put("refreshInterval",
                                Integer.toString(MyBMWConstants.DEFAULT_REFRESH_INTERVAL_MINUTES));

                        String vehicleLabel = vehicleAttributes.getBrand() + " " + vehicleAttributes.getModel();
                        thingDiscovered(DiscoveryResultBuilder.create(uid).withBridge(bridgeUid.get())
                                .withRepresentationProperty(MyBMWConstants.VIN).withLabel(vehicleLabel)
                                .withProperties(convertedProperties).build());
                    }
                }
            });
        });
    }

    private Map<String, String> generateProperties(Vehicle vehicle) {
        Map<String, String> properties = new HashMap<>();

        // Vehicle Properties
        VehicleAttributes vehicleAttributes = vehicle.getVehicleBase().getAttributes();
        properties.put("vehicleModel", vehicleAttributes.getModel());
        properties.put("vehicleDriveTrain", vehicleAttributes.getDriveTrain());
        properties.put("vehicleConstructionYear", Integer.toString(vehicleAttributes.getYear()));
        properties.put("vehicleBodytype", vehicleAttributes.getBodyType());

        VehicleCapabilities vehicleCapabilities = vehicle.getVehicleState().getCapabilities();

        properties.put("servicesSupported",
                vehicleCapabilities.getCapabilitiesAsString(VehicleCapabilities.SUPPORTED_SUFFIX, true));
        properties.put("servicesUnsupported",
                vehicleCapabilities.getCapabilitiesAsString(VehicleCapabilities.SUPPORTED_SUFFIX, false));
        properties.put("servicesEnabled",
                vehicleCapabilities.getCapabilitiesAsString(VehicleCapabilities.ENABLED_SUFFIX, true));
        properties.put("servicesDisabled",
                vehicleCapabilities.getCapabilitiesAsString(VehicleCapabilities.ENABLED_SUFFIX, false));

        // For RemoteServices we need to do it step-by-step
        StringBuffer remoteServicesEnabled = new StringBuffer();
        StringBuffer remoteServicesDisabled = new StringBuffer();
        if (vehicleCapabilities.isLock()) {
            remoteServicesEnabled.append(RemoteService.DOOR_LOCK.getLabel() + Constants.SEMICOLON);
        } else {
            remoteServicesDisabled.append(RemoteService.DOOR_LOCK.getLabel() + Constants.SEMICOLON);
        }
        if (vehicleCapabilities.isUnlock()) {
            remoteServicesEnabled.append(RemoteService.DOOR_UNLOCK.getLabel() + Constants.SEMICOLON);
        } else {
            remoteServicesDisabled.append(RemoteService.DOOR_UNLOCK.getLabel() + Constants.SEMICOLON);
        }
        if (vehicleCapabilities.isLights()) {
            remoteServicesEnabled.append(RemoteService.LIGHT_FLASH.getLabel() + Constants.SEMICOLON);
        } else {
            remoteServicesDisabled.append(RemoteService.LIGHT_FLASH.getLabel() + Constants.SEMICOLON);
        }
        if (vehicleCapabilities.isHorn()) {
            remoteServicesEnabled.append(RemoteService.HORN_BLOW.getLabel() + Constants.SEMICOLON);
        } else {
            remoteServicesDisabled.append(RemoteService.HORN_BLOW.getLabel() + Constants.SEMICOLON);
        }
        if (vehicleCapabilities.isVehicleFinder()) {
            remoteServicesEnabled.append(RemoteService.VEHICLE_FINDER.getLabel() + Constants.SEMICOLON);
        } else {
            remoteServicesDisabled.append(RemoteService.VEHICLE_FINDER.getLabel() + Constants.SEMICOLON);
        }
        if (vehicleCapabilities.isVehicleFinder()) {
            remoteServicesEnabled.append(RemoteService.CLIMATE_NOW_START.getLabel() + Constants.SEMICOLON);
        } else {
            remoteServicesDisabled.append(RemoteService.CLIMATE_NOW_START.getLabel() + Constants.SEMICOLON);
        }
        properties.put("remoteServicesEnabled", remoteServicesEnabled.toString().trim());
        properties.put("remoteServicesDisabled", remoteServicesDisabled.toString().trim());

        return properties;
    }
}

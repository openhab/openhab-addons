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
package org.openhab.binding.mybmw.internal.discovery;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.mybmw.internal.MyBMWConstants;
import org.openhab.binding.mybmw.internal.dto.vehicle.Vehicle;
import org.openhab.binding.mybmw.internal.dto.vehicle.VehicleAttributes;
import org.openhab.binding.mybmw.internal.dto.vehicle.VehicleCapabilities;
import org.openhab.binding.mybmw.internal.handler.MyBMWBridgeHandler;
import org.openhab.binding.mybmw.internal.handler.backend.MyBMWProxy;
import org.openhab.binding.mybmw.internal.handler.backend.NetworkException;
import org.openhab.binding.mybmw.internal.handler.enums.RemoteService;
import org.openhab.binding.mybmw.internal.utils.Constants;
import org.openhab.binding.mybmw.internal.utils.VehicleStatusUtils;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.config.discovery.AbstractThingHandlerDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link VehicleDiscovery} requests data from BMW API and is identifying
 * the Vehicles after response
 *
 * @author Bernd Weymann - Initial contribution
 * @author Martin Grassl - refactoring
 */
@Component(scope = ServiceScope.PROTOTYPE, service = VehicleDiscovery.class)
@NonNullByDefault
public class VehicleDiscovery extends AbstractThingHandlerDiscoveryService<MyBMWBridgeHandler> {

    private final Logger logger = LoggerFactory.getLogger(VehicleDiscovery.class);

    private static final int DISCOVERY_TIMEOUT = 10;

    private Optional<MyBMWProxy> myBMWProxy = Optional.empty();
    private @NonNullByDefault({}) ThingUID bridgeUid;

    public VehicleDiscovery() {
        super(MyBMWBridgeHandler.class, MyBMWConstants.SUPPORTED_THING_SET, DISCOVERY_TIMEOUT, false);
    }

    @Override
    public void initialize() {
        thingHandler.setVehicleDiscovery(this);
        bridgeUid = thingHandler.getThing().getUID();
        super.initialize();
    }

    @Override
    protected void startScan() {
        logger.trace("VehicleDiscovery.startScan");
        discoverVehicles();
    }

    public void discoverVehicles() {
        logger.trace("VehicleDiscovery.discoverVehicles");

        myBMWProxy = thingHandler.getMyBmwProxy();

        try {
            Optional<List<@NonNull Vehicle>> vehicleList = myBMWProxy.map(prox -> {
                try {
                    return prox.requestVehicles();
                } catch (NetworkException e) {
                    throw new IllegalStateException("vehicles could not be discovered: " + e.getMessage(), e);
                }
            });
            vehicleList.ifPresentOrElse(vehicles -> {
                thingHandler.vehicleDiscoverySuccess();
                processVehicles(vehicles);
            }, () -> thingHandler.vehicleDiscoveryError());
        } catch (IllegalStateException ex) {
            thingHandler.vehicleDiscoveryError();
        }
    }

    /**
     * this method is called by the bridgeHandler if the list of vehicles was retrieved successfully
     * 
     * it iterates through the list of existing things and checks if the vehicles found via the API
     * call are already known to OH. If not, it creates a new thing and puts it into the inbox
     * 
     * @param vehicleList
     */
    private void processVehicles(List<Vehicle> vehicleList) {
        logger.trace("VehicleDiscovery.processVehicles");

        vehicleList.forEach(vehicle -> {
            // the DriveTrain field in the delivered json is defining the Vehicle Type
            String vehicleType = VehicleStatusUtils
                    .vehicleType(vehicle.getVehicleBase().getAttributes().getDriveTrain(),
                            vehicle.getVehicleBase().getAttributes().getModel())
                    .toString();
            MyBMWConstants.SUPPORTED_THING_SET.forEach(entry -> {
                if (entry.getId().equals(vehicleType)) {
                    ThingUID uid = new ThingUID(entry, vehicle.getVehicleBase().getVin(), bridgeUid.getId());

                    Map<String, String> properties = generateProperties(vehicle);

                    boolean thingFound = false;
                    // Update Properties for already created Things
                    List<Thing> vehicleThings = thingHandler.getThing().getThings();
                    for (Thing vehicleThing : vehicleThings) {
                        Configuration configuration = vehicleThing.getConfiguration();

                        if (configuration.containsKey(MyBMWConstants.VIN)) {
                            String thingVIN = configuration.get(MyBMWConstants.VIN).toString();
                            if (vehicle.getVehicleBase().getVin().equals(thingVIN)) {
                                vehicleThing.setProperties(properties);
                                thingFound = true;
                            }
                        }
                    }

                    // the vehicle found is not yet known to OH, so put it into the inbox
                    if (!thingFound) {
                        // Properties needed for functional Thing
                        VehicleAttributes vehicleAttributes = vehicle.getVehicleBase().getAttributes();
                        Map<String, Object> convertedProperties = new HashMap<>(properties);
                        convertedProperties.put(MyBMWConstants.VIN, vehicle.getVehicleBase().getVin());
                        convertedProperties.put(MyBMWConstants.VEHICLE_BRAND, vehicleAttributes.getBrand());
                        convertedProperties.put(MyBMWConstants.REFRESH_INTERVAL,
                                Integer.toString(MyBMWConstants.DEFAULT_REFRESH_INTERVAL_MINUTES));

                        String vehicleLabel = vehicleAttributes.getBrand() + " " + vehicleAttributes.getModel();
                        thingDiscovered(DiscoveryResultBuilder.create(uid).withBridge(bridgeUid)
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
        properties.put(MyBMWConstants.VEHICLE_MODEL, vehicleAttributes.getModel());
        properties.put(MyBMWConstants.VEHICLE_DRIVE_TRAIN, vehicleAttributes.getDriveTrain());
        properties.put(MyBMWConstants.VEHICLE_CONSTRUCTION_YEAR, Integer.toString(vehicleAttributes.getYear()));
        properties.put(MyBMWConstants.VEHICLE_BODYTYPE, vehicleAttributes.getBodyType());

        VehicleCapabilities vehicleCapabilities = vehicle.getVehicleState().getCapabilities();

        properties.put(MyBMWConstants.SERVICES_SUPPORTED,
                vehicleCapabilities.getCapabilitiesAsString(VehicleCapabilities.SUPPORTED_SUFFIX, true));
        properties.put(MyBMWConstants.SERVICES_UNSUPPORTED,
                vehicleCapabilities.getCapabilitiesAsString(VehicleCapabilities.SUPPORTED_SUFFIX, false));
        properties.put(MyBMWConstants.SERVICES_ENABLED,
                vehicleCapabilities.getCapabilitiesAsString(VehicleCapabilities.ENABLED_SUFFIX, true));
        properties.put(MyBMWConstants.SERVICES_DISABLED,
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
        properties.put(MyBMWConstants.REMOTE_SERVICES_ENABLED, remoteServicesEnabled.toString().trim());
        properties.put(MyBMWConstants.REMOTE_SERVICES_DISABLED, remoteServicesDisabled.toString().trim());

        return properties;
    }
}

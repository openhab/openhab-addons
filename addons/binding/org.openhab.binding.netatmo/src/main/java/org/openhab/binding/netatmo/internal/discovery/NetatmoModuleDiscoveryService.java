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
package org.openhab.binding.netatmo.internal.discovery;

import io.rudolph.netatmo.api.common.model.*;
import io.rudolph.netatmo.api.energy.model.BaseRoom;
import io.rudolph.netatmo.api.energy.model.Home;
import io.rudolph.netatmo.api.energy.model.HomesDataBody;
import io.rudolph.netatmo.api.energy.model.module.ThermostatModule;
import io.rudolph.netatmo.api.energy.model.module.ValveModule;
import io.rudolph.netatmo.api.presence.model.PresenceHome;
import io.rudolph.netatmo.api.presence.model.SecurityHome;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.netatmo.internal.handler.NetatmoBridgeHandler;
import org.openhab.binding.netatmo.internal.handler.NetatmoDataListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import static org.openhab.binding.netatmo.internal.NetatmoBindingConstants.*;

/**
 * The {@link NetatmoModuleDiscoveryService} searches for available Netatmo
 * devices and modules connected to the API console
 *
 * @author Gaël L'hopital - Initial contribution
 * @author Ing. Peter Weiss - Welcome camera implementation
 */
public class NetatmoModuleDiscoveryService extends AbstractDiscoveryService implements NetatmoDataListener {
    private static final int SEARCH_TIME = 2;
    @NonNull
    private final NetatmoBridgeHandler netatmoBridgeHandler;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public NetatmoModuleDiscoveryService(@NonNull NetatmoBridgeHandler netatmoBridgeHandler) {
        super(SUPPORTED_DEVICE_THING_TYPES_UIDS, SEARCH_TIME);
        this.netatmoBridgeHandler = netatmoBridgeHandler;
    }

    @Override
    public void activate(@Nullable Map<@NonNull String, @Nullable Object> configProperties) {
        super.activate(configProperties);
        netatmoBridgeHandler.registerDataListener(this);
    }

    @Override
    public void deactivate() {
        netatmoBridgeHandler.unregisterDataListener(this);
        super.deactivate();
    }

    @Override
    public void startScan() {
        if (netatmoBridgeHandler.configuration.readStation) {
            StationResults stationDataBody = netatmoBridgeHandler.api.getWeatherApi().getStationData(null, null)
                    .executeSync();
            if (stationDataBody != null) {
                stationDataBody.getDevices().forEach(this::discoverWeatherStation);
            }
        }
        if (netatmoBridgeHandler.configuration.readHealthyHomeCoach) {
            StationResults homecoachDataBody = netatmoBridgeHandler.api.getAirCareApi().getHomeCoachsData(null)
                    .executeSync();
            if (homecoachDataBody != null) {
                homecoachDataBody.getDevices().forEach(this::discoverHomeCoach);
            }
        }
        if (netatmoBridgeHandler.configuration.readThermostat) {
            HomesDataBody energyDataBody = netatmoBridgeHandler.api
                    .getEnergyApi()
                    .getCombinedHome(null)
                    .executeSync();
            if (energyDataBody != null) {
                energyDataBody.getHomes().forEach(home -> {
                    discoverHome(home);

                    home.getModules()
                        .forEach(module -> discoverModule(module, home.getId()));

                    home.getRooms().
                            forEach(room -> discoverRoom(room, home.getId()));
                });
            }
        }

        if (netatmoBridgeHandler.configuration.readWelcome) {
            SecurityHome welcomeHomeData = netatmoBridgeHandler.api.getWelcomeApi().getHomeData(null, null)
                    .executeSync();
            if (welcomeHomeData != null && welcomeHomeData.getHomes() != null) {
                welcomeHomeData.getHomes().forEach(this::discoverWelcomeHome);
            }
        }

        stopScan();

    }

    @Override
    public void onDataRefreshed(Object data) {
        if (data instanceof Device) {
            Device deviceData = (Device) data;
            if (deviceData.getType() == DeviceType.BASESTATION) {
                discoverWeatherStation(deviceData);
            }
            return;
        }

        if (data instanceof Module) {
            Module moduleData = (Module) data;
            switch (moduleData.getType()) {
                case RELAY:
                case VALVE:
                case INDOORMODULE:
                    discoverModule(moduleData, null);
                    break;
            }
        }

        if (data instanceof PresenceHome) {
            PresenceHome home = (PresenceHome) data;
            discoverWelcomeHome(home);
        }
    }

    private void discoverHome(Home home) {
        onDeviceAddedInternal(home.getId(),
                null,
                HOME_PROPERTY,
                home.getName(),
                null);
    }

    private void discoverRoom(BaseRoom room, String parentId) {
        onDeviceAddedInternal(room.getId(),
                parentId,
                ROOM_PROPERTY,
                room.getName(),
                null);
    }

    private void discoverModule(Module module, String parentId) {
        switch (module.getType()) {
            case INDOORMODULE:
                ClimateModule indoorModule = (ClimateModule) module;
                onDeviceAddedInternal(module.getId(),
                        parentId,
                        module.getType(),
                        module.getModuleName(),
                        indoorModule.getFirmware());
            case THERMOSTAT:
                ThermostatModule thermostatModule = (ThermostatModule) module;
                onDeviceAddedInternal(module.getId(),
                        thermostatModule.getBridgeId(),
                        module.getType(),
                        module.getModuleName(),
                        thermostatModule.getFirmware());
                break;
            case VALVE:
                ValveModule energyModule = (ValveModule) module;
                logger.error("*** Valve: {}", energyModule);
                onDeviceAddedInternal(module.getId(),
                        energyModule.getBridgeId(),
                        module.getType(),
                        module.getModuleName(),
                        energyModule.getFirmware());
                break;
            case RELAY:

                final String tParentId;
                if (parentId == null) {
                    tParentId = netatmoBridgeHandler.findNAThing(module.getId()).get().getParentId();
                } else {
                    tParentId = parentId;
                }
                onDeviceAddedInternal(module.getId(),
                        tParentId,
                        module.getType(),
                        module.getModuleName(),
                        null);
        }
    }

    private void discoverHomeCoach(Device device) {
        onDeviceAddedInternal(device.getId(), null, device.getType(), device.getModuleName(), device.getFirmware());

    }

    private void discoverWeatherStation(Device station) {
        onDeviceAddedInternal(station.getId(), null, station.getType(), station.getStationName(),
                station.getFirmware());
        if (station.getModules() == null) {
            return;
        }
        station.getModules().forEach(module -> {
            Integer firmware = null;
            switch (module.getType()) {
                case INDOORMODULE:
                case OUTDOORMODULE:
                case RAINGAUGEMODULE:
                case WINDMODULE:
                    firmware = ((ClimateModule) module).getFirmware();

            }
            onDeviceAddedInternal(module.getId(), station.getId(), module.getType(), module.getModuleName(), firmware);
        });
    }

    private void discoverWelcomeHome(PresenceHome home) {
        // I observed that Thermostat homes are also reported here by Netatmo API
        // So I ignore homes that have an empty list of cameras
        if (home.getCameras().size() > 0) {
            onDeviceAddedInternal(home.getId(), null, WELCOME_HOME_THING_TYPE.getId(), home.getName(), null);
            // Discover Cameras
            home.getCameras().forEach(camera -> {
                onDeviceAddedInternal(camera.getId(),
                        home.getId(),
                        camera.getType(),
                        camera.getName(),
                        null);
            });

            // Discover Known Persons
            home.getPersons().stream().filter(person -> person.getPseudo() != null).forEach(person -> {
                onDeviceAddedInternal(person.getId(),
                        home.getId(),
                        WELCOME_PERSON_THING_TYPE.getId(),
                        person.getPseudo(),
                        null);
            });
        }
    }

    private void onDeviceAddedInternal(String id, String parentId, DeviceType type, String name,
                                       Integer firmwareVersion) {
        onDeviceAddedInternal(id, parentId, type.getValue(), name, firmwareVersion);
    }

    private void onDeviceAddedInternal(String id, String parentId, String type, String name, Integer firmwareVersion) {
        ThingUID thingUID = findThingUID(type, id);
        Map<String, Object> properties = new HashMap<>();

        logger.debug("set Equipment_ID: " + id + "parentId: " + parentId);
        properties.put(EQUIPMENT_ID, id);
        if (parentId != null) {
            properties.put(PARENT_ID, parentId);
        }
        if (firmwareVersion != null) {
            properties.put(Thing.PROPERTY_VENDOR, VENDOR);
            properties.put(Thing.PROPERTY_FIRMWARE_VERSION, firmwareVersion);
            properties.put(Thing.PROPERTY_MODEL_ID, type);
            properties.put(Thing.PROPERTY_SERIAL_NUMBER, id);
        }
        addDiscoveredThing(thingUID, properties, name);
    }

    private void addDiscoveredThing(ThingUID thingUID, Map<String, Object> properties, String displayLabel) {
        logger.debug("Add module " + thingUID + " label: " + displayLabel);
        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID)
                .withProperties(properties)
                .withBridge(netatmoBridgeHandler.getThing().getUID())
                .withLabel(displayLabel)
                .withRepresentationProperty(EQUIPMENT_ID).build();

        thingDiscovered(discoveryResult);
    }

    private ThingUID findThingUID(String thingType, String thingId) throws IllegalArgumentException {
        for (ThingTypeUID supportedThingTypeUID : getSupportedThingTypes()) {
            String uid = supportedThingTypeUID.getId();
            logger.debug("*** thingType " + thingType + " id: " + thingId + " uid: " + uid);

            if (uid.equalsIgnoreCase(thingType)) {
                return new ThingUID(supportedThingTypeUID, netatmoBridgeHandler.getThing().getUID(),
                        thingId.replaceAll("[^a-zA-Z0-9_]", ""));
            }
        }

        throw new IllegalArgumentException("Unsupported device type discovered : " + thingType);
    }

}

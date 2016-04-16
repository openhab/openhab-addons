/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.netatmo.discovery;

import static org.openhab.binding.netatmo.NetatmoBindingConstants.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.netatmo.handler.NetatmoBridgeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.client.model.NADevice;
import io.swagger.client.model.NADeviceListResponse;
import io.swagger.client.model.NAModule;

/**
 * The {@link NetatmoModuleDiscoveryService} searches for available Netatmo
 * devices and modules connected to the API console
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
public class NetatmoModuleDiscoveryService extends AbstractDiscoveryService {
    private static Logger logger = LoggerFactory.getLogger(NetatmoModuleDiscoveryService.class);
    private final static int SEARCH_TIME = 2;
    private NetatmoBridgeHandler netatmoBridgeHandler;

    public NetatmoModuleDiscoveryService(NetatmoBridgeHandler netatmoBridgeHandler) {
        super(SUPPORTED_DEVICE_THING_TYPES_UIDS, SEARCH_TIME);
        this.netatmoBridgeHandler = netatmoBridgeHandler;
    }

    private void screenDevicesAndModules(NADeviceListResponse deviceList) {
        if (deviceList != null) {
            List<NADevice> devices = deviceList.getBody().getDevices();
            if (devices != null) {
                for (NADevice naDevice : devices) {
                    onDeviceAddedInternal(naDevice);
                    List<NAModule> modules = deviceList.getBody().getModules();
                    if (modules != null) {
                        for (NAModule naModule : modules) {
                            onModuleAddedInternal(naModule);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void startScan() {
        NADeviceListResponse deviceList;
        try {
            deviceList = netatmoBridgeHandler.getStationApi().devicelist("app_station", null, false);
            screenDevicesAndModules(deviceList);

            deviceList = netatmoBridgeHandler.getThermostatApi().devicelist("app_thermostat", null, false);
            screenDevicesAndModules(deviceList);
        } catch (Exception e) {
            logger.warn(e.getMessage());
        }

        stopScan();
    }

    private void onDeviceAddedInternal(NADevice naDevice) {
        ThingUID thingUID = findThingUID(naDevice.getType(), naDevice.getId());
        Map<String, Object> properties = new HashMap<>(1);

        properties.put(EQUIPMENT_ID, naDevice.getId());

        String name = naDevice.getModuleName();

        addDiscoveredThing(thingUID, properties, (name == null) ? naDevice.getStationName() : name);
    }

    private void onModuleAddedInternal(NAModule naModule) {
        ThingUID thingUID = findThingUID(naModule.getType(), naModule.getId());
        Map<String, Object> properties = new HashMap<>(2);

        properties.put(EQUIPMENT_ID, naModule.getId());
        properties.put(PARENT_ID, naModule.getMainDevice());

        addDiscoveredThing(thingUID, properties, naModule.getModuleName());
    }

    private void addDiscoveredThing(ThingUID thingUID, Map<String, Object> properties, String displayLabel) {
        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                .withBridge(netatmoBridgeHandler.getThing().getUID()).withLabel(displayLabel).build();

        thingDiscovered(discoveryResult);
    }

    private ThingUID findThingUID(String thingType, String thingId) throws IllegalArgumentException {
        for (ThingTypeUID supportedThingTypeUID : getSupportedThingTypes()) {
            String uid = supportedThingTypeUID.getId();

            if (uid.equalsIgnoreCase(thingType)) {

                return new ThingUID(supportedThingTypeUID, netatmoBridgeHandler.getThing().getUID(),
                        thingId.replaceAll("[^a-zA-Z0-9_]", ""));
            }
        }

        throw new IllegalArgumentException("Unsupported device type discovered :" + thingType);
    }

}

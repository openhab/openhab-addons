/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.netatmo.discovery;

import static org.openhab.binding.netatmo.NetatmoBindingConstants.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.ThingFactory;
import org.openhab.binding.netatmo.handler.NetatmoBridgeHandler;
import org.openhab.binding.netatmo.internal.NADeviceAdapter;
import org.openhab.binding.netatmo.internal.NAModuleAdapter;
import org.openhab.binding.netatmo.internal.NAPlugAdapter;
import org.openhab.binding.netatmo.internal.NAStationAdapter;

import io.swagger.client.model.NAMain;
import io.swagger.client.model.NAPlug;
import io.swagger.client.model.NAStationDataBody;
import io.swagger.client.model.NAThermostatDataBody;

/**
 * The {@link NetatmoModuleDiscoveryService} searches for available Netatmo
 * devices and modules connected to the API console
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
public class NetatmoModuleDiscoveryService extends AbstractDiscoveryService {
    private final static int SEARCH_TIME = 2;
    private static ThingUID welcomeBridgeUID = null;

    private NetatmoBridgeHandler<?> netatmoBridgeHandler;

    public NetatmoModuleDiscoveryService(NetatmoBridgeHandler<?> netatmoBridgeHandler) {
        super(SUPPORTED_DEVICE_THING_TYPES_UIDS, SEARCH_TIME);
        this.netatmoBridgeHandler = netatmoBridgeHandler;
    }

    private void screenModules(NADeviceAdapter<?> device) {

        Map<String, NAModuleAdapter> modules = device.getModules();
        for (NAModuleAdapter naModule : modules.values()) {
            onModuleAddedInternal(device.getId(), naModule);
        }

    }

    @Override
    public void startScan() {
        NAStationDataBody stationsDataBody = netatmoBridgeHandler.getStationsDataBody(null);
        if (stationsDataBody != null) {
            List<NAMain> stationDevices = stationsDataBody.getDevices();
            for (NAMain device : stationDevices) {
                NADeviceAdapter<NAMain> deviceAdapter = new NAStationAdapter(device);
                onDeviceAddedInternal(deviceAdapter);
                screenModules(deviceAdapter);
            }
        }

        NAThermostatDataBody thermostatsDataBody = netatmoBridgeHandler.getThermostatsDataBody(null);
        if (thermostatsDataBody != null) {
            List<NAPlug> thermostatDevices = thermostatsDataBody.getDevices();
            for (NAPlug device : thermostatDevices) {
                NADeviceAdapter<?> deviceAdapter = new NAPlugAdapter(device);
                onDeviceAddedInternal(deviceAdapter);
                screenModules(deviceAdapter);
            }
        }

        ThingUID thingUID = findBridgeUID("NAWelcomeBridge");
        Map<String, Object> properties = new HashMap<>(0);
        addDiscoveredThing(thingUID, properties, "Welcome bridge");

        stopScan();
    }

    private void onDeviceAddedInternal(NADeviceAdapter<?> naDevice) {
        ThingUID thingUID = findThingUID(naDevice.getType(), naDevice.getId());
        Map<String, Object> properties = new HashMap<>(1);

        properties.put(EQUIPMENT_ID, naDevice.getId());

        addDiscoveredThing(thingUID, properties, naDevice.getTypeName());
    }

    private void onModuleAddedInternal(String deviceId, NAModuleAdapter naModule) {
        ThingUID thingUID = findThingUID(naModule.getType(), naModule.getId());
        Map<String, Object> properties = new HashMap<>(2);

        properties.put(EQUIPMENT_ID, naModule.getId());
        properties.put(PARENT_ID, deviceId);

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

    private ThingUID findBridgeUID(String thingType) throws IllegalArgumentException {
        for (ThingTypeUID myThingTypeUID : getSupportedThingTypes()) {
            String uid = myThingTypeUID.getId();

            if (uid.equalsIgnoreCase(thingType)) {
                for (Thing myThing : netatmoBridgeHandler.getThing().getThings()) {
                    if (myThingTypeUID.equals(myThing.getThingTypeUID())) {
                        welcomeBridgeUID = myThing.getUID();
                    }
                }

                if (welcomeBridgeUID == null) {
                    ArrayList<ThingTypeUID> thingTypUIDs = new ArrayList<ThingTypeUID>(1);
                    thingTypUIDs.add(myThingTypeUID);
                    removeOlderResults(getTimestampOfLastScan(), thingTypUIDs);

                    ThingUID thingUID = ThingFactory.generateRandomThingUID(myThingTypeUID);
                    welcomeBridgeUID = new ThingUID(myThingTypeUID, netatmoBridgeHandler.getThing().getUID(),
                            thingUID.getId().replaceAll("[^a-zA-Z0-9_]", ""));
                }

                return welcomeBridgeUID;
            }
        }

        throw new IllegalArgumentException("Unsupported device type discovered :" + thingType);
    }

}

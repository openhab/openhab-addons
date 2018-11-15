/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.deconz.internal.discovery;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.deconz.internal.BindingConstants;
import org.openhab.binding.deconz.internal.dto.SensorMessage;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

/**
 * Every bridge will add its discovered sensors to this discovery service to make them
 * available to the framework.
 *
 * @author David Graeff - Initial contribution
 */
public class ThingDiscoveryService extends AbstractDiscoveryService {

    private ServiceRegistration<?> reg = null;
    private final List<DiscoveryResult> results = new ArrayList<>();

    public ThingDiscoveryService() {
        super(Stream
                .of(BindingConstants.THING_TYPE_PRESENCE_SENSOR, BindingConstants.THING_TYPE_POWER_SENSOR,
                        BindingConstants.THING_TYPE_DAYLIGHT_SENSOR, BindingConstants.THING_TYPE_SWITCH)
                .collect(Collectors.toSet()), 0, true);
    }

    public void stop() {
        if (reg != null) {
            reg.unregister();
        }
        reg = null;
    }

    /**
     * Just re-add all sensor things to the discovery inbox.
     */
    @Override
    protected void startScan() {
        results.forEach(discoveryResult -> thingDiscovered(discoveryResult));
        super.stopScan();
    }

    /**
     * Add a sensor device to the discovery inbox.
     *
     * @param sensor The sensor description
     * @param bridgeUID The bridge UID
     */
    public void addDevice(SensorMessage sensor, String sensorID, ThingUID bridgeUID) {
        ThingTypeUID thingTypeUID;
        if (sensor.type.contains("Daylight")) { // Deconz specific: Software simulated daylight sensor
            thingTypeUID = BindingConstants.THING_TYPE_DAYLIGHT_SENSOR;
        } else if (sensor.type.contains("Power")) { // ZHAPower, CLIPPower
            thingTypeUID = BindingConstants.THING_TYPE_POWER_SENSOR;
        } else if (sensor.type.contains("Presence")) { // ZHAPresence, CLIPPrensence
            thingTypeUID = BindingConstants.THING_TYPE_PRESENCE_SENSOR;
        } else if (sensor.type.contains("Switch")) { // ZHASwitch
            thingTypeUID = BindingConstants.THING_TYPE_SWITCH;
        } else {
            return;
        }

        ThingUID uid = new ThingUID(thingTypeUID, bridgeUID, sensor.uniqueid.replaceAll("[^a-z0-9\\[\\]]", ""));

        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(uid).withBridge(bridgeUID)
                .withLabel(sensor.name + " (" + sensor.manufacturername + ")").withProperty("id", sensorID)
                .withProperty("uid", sensor.uniqueid).withRepresentationProperty("uid").build();
        results.add(discoveryResult);
        thingDiscovered(discoveryResult);
    }

    public void start(BundleContext bundleContext) {
        if (reg != null) {
            return;
        }
        reg = bundleContext.registerService(DiscoveryService.class.getName(), this, new Hashtable<String, Object>());
    }
}

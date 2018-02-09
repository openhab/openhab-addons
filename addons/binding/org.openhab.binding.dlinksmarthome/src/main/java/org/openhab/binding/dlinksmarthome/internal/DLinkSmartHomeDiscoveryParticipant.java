/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.dlinksmarthome.internal;

import static org.openhab.binding.dlinksmarthome.DLinkSmartHomeBindingConstants.*;
import static org.openhab.binding.dlinksmarthome.internal.motionsensor.DLinkMotionSensorConfig.IP_ADDRESS;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.jmdns.ServiceInfo;

import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.io.transport.mdns.discovery.MDNSDiscoveryParticipant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DLinkSmartHomeDiscoveryParticipant} is responsible for discovering devices through UPnP.
 *
 * @author Mike Major - Initial contribution
 *
 */
public class DLinkSmartHomeDiscoveryParticipant implements MDNSDiscoveryParticipant {

    private static final String SERVICE_TYPE = "_dhnap._tcp.local.";
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return SUPPORTED_THING_TYPES_UIDS;
    }

    @Override
    public String getServiceType() {
        return SERVICE_TYPE;
    }

    @Override
    public DiscoveryResult createResult(final ServiceInfo serviceInfo) {
        final ThingUID thingUID = getThingUID(serviceInfo);

        if (thingUID == null) {
            return null;
        }

        final ThingTypeUID thingTypeUID = getThingType(serviceInfo);

        if (THING_TYPE_DCHS150.equals(thingTypeUID)) {
            return createMotionSensor(thingUID, thingTypeUID, serviceInfo);
        } else {
            return null;
        }
    }

    @Override
    public ThingUID getThingUID(final ServiceInfo serviceInfo) {
        final ThingTypeUID thingTypeUID = getThingType(serviceInfo);

        if (thingTypeUID != null) {
            final String mac = serviceInfo.getPropertyString("mac").replace(":", "").toLowerCase();
            return new ThingUID(thingTypeUID, mac);
        } else {
            return null;
        }
    }

    private ThingTypeUID getThingType(final ServiceInfo serviceInfo) {
        final String model = serviceInfo.getPropertyString("model_number");

        if (model == null) {
            return null;
        } else if (model.equals("DCH-S150")) {
            return THING_TYPE_DCHS150;
        } else {
            logger.debug("D-Link HNAP Type: {}", model);
            return null;
        }
    }

    private DiscoveryResult createMotionSensor(final ThingUID thingUID, final ThingTypeUID thingType,
            final ServiceInfo serviceInfo) {

        final String host = serviceInfo.getHostAddresses()[0];
        final String mac = serviceInfo.getPropertyString("mac");

        final Map<String, Object> properties = new HashMap<>();
        properties.put(IP_ADDRESS, host);

        logger.debug("DCH-S150 found: {}", host);

        return DiscoveryResultBuilder.create(thingUID).withThingType(thingType).withProperties(properties)
                .withLabel("Motion Sensor (" + mac + ")").build();
    }
}

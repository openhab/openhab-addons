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
package org.openhab.binding.dlinksmarthome.internal;

import static org.openhab.binding.dlinksmarthome.internal.DLinkSmartHomeBindingConstants.*;
import static org.openhab.binding.dlinksmarthome.internal.motionsensor.DLinkMotionSensorConfig.IP_ADDRESS;

import java.net.Inet4Address;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.jmdns.ServiceInfo;

import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.mdns.MDNSDiscoveryParticipant;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DLinkSmartHomeDiscoveryParticipant} is responsible for discovering devices through UPnP.
 *
 * @author Mike Major - Initial contribution
 *
 */
@Component
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
        final Inet4Address[] addresses = serviceInfo.getInet4Addresses();
        if (addresses.length != 1) {
            return null;
        }
        final String host = addresses[0].getHostAddress();
        final String mac = serviceInfo.getPropertyString("mac");

        final Map<String, Object> properties = new HashMap<>();
        properties.put(IP_ADDRESS, host);

        logger.debug("DCH-S150 found: {}", host);

        return DiscoveryResultBuilder.create(thingUID).withThingType(thingType).withProperties(properties)
                .withLabel("Motion Sensor (" + mac + ")").build();
    }
}

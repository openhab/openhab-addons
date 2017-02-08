/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lightify.internal.discovery;

import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.io.transport.mdns.discovery.MDNSDiscoveryParticipant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jmdns.ServiceInfo;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.openhab.binding.lightify.internal.LightifyConstants.PROPERTY_ADDRESS;
import static org.openhab.binding.lightify.internal.LightifyConstants.PROPERTY_ID;
import static org.openhab.binding.lightify.internal.LightifyConstants.SUPPORTED_THING_TYPES_UIDS;
import static org.openhab.binding.lightify.internal.LightifyConstants.THING_TYPE_LIGHTIFY_GATEWAY;
import static org.openhab.binding.lightify.internal.LightifyUtils.extractLightifyUID;
import static org.openhab.binding.lightify.internal.LightifyUtils.isLightifyGateway;

/**
 * @author Christoph Engelbert (@noctarius2k) - Initial contribution
 */
public class LightifyDiscoveryParticipant implements MDNSDiscoveryParticipant {

    private static final Logger LOGGER = LoggerFactory.getLogger(LightifyDiscoveryParticipant.class);

    private static final String SERVICE_TYPE = "_http._tcp.local.";

    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return SUPPORTED_THING_TYPES_UIDS;
    }

    public String getServiceType() {
        return SERVICE_TYPE;
    }

    public DiscoveryResult createResult(ServiceInfo serviceInfo) {
        if (!isLightifyGateway(serviceInfo)) {
            return null;
        }

        // Check if we have an available IP address
        InetAddress[] inetAddresses = serviceInfo.getInetAddresses();
        if (inetAddresses.length == 0) {
            return null;
        }

        ThingUID thingUID = getThingUID(serviceInfo);

        Map<String, Object> properties = new HashMap<>();
        properties.put(PROPERTY_ID, thingUID.getId());
        properties.put(PROPERTY_ADDRESS, inetAddresses[0].getHostAddress());

        return DiscoveryResultBuilder.create(thingUID) //
                                     .withThingType(THING_TYPE_LIGHTIFY_GATEWAY) //
                                     .withProperties(properties).build();
    }

    @Override
    public ThingUID getThingUID(ServiceInfo serviceInfo) {
        if (!isLightifyGateway(serviceInfo)) {
            return null;
        }

        return new ThingUID(THING_TYPE_LIGHTIFY_GATEWAY, extractLightifyUID(serviceInfo.getName()));
    }
}

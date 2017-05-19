/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.hyperion.internal;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.jmdns.ServiceInfo;

import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.io.transport.mdns.discovery.MDNSDiscoveryParticipant;
import org.openhab.binding.hyperion.HyperionBindingConstants;

public class HyperionDiscoveryParticipant implements MDNSDiscoveryParticipant {

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return HyperionBindingConstants.SUPPORTED_THING_TYPES_UIDS;
    }

    @Override
    public String getServiceType() {
        return "_hyperiond-json._tcp.local.";
    }

    @Override
    public DiscoveryResult createResult(ServiceInfo service) {

        final Map<String, Object> properties = new HashMap<>(2);
        String host = service.getHostAddresses()[0];
        int port = service.getPort();

        properties.put(HyperionBindingConstants.HOST, host);
        properties.put(HyperionBindingConstants.PORT, port);

        // String friendlyName = "Hyperion Server";
        String friendlyName = service.getName();
        ThingUID uid = getThingUID(service);

        final DiscoveryResult result = DiscoveryResultBuilder.create(uid)
                .withThingType(HyperionBindingConstants.THING_TYPE_SERVER_NG).withProperties(properties)
                .withLabel(friendlyName).build();
        return result;
    }

    @Override
    public ThingUID getThingUID(ServiceInfo service) {
        return new ThingUID(HyperionBindingConstants.THING_TYPE_SERVER_NG, "server");
    }

}

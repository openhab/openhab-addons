/*
 * Copyright (c) 2014-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.osramlightify.internal.discovery;

import javax.jmdns.ServiceInfo;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.io.transport.mdns.discovery.MDNSDiscoveryParticipant;

import static org.openhab.binding.osramlightify.LightifyBindingConstants.SUPPORTED_BRIDGE_THING_TYPES_UIDS;
import static org.openhab.binding.osramlightify.LightifyBindingConstants.THING_TYPE_LIGHTIFY_GATEWAY;

/**
 * Auto-discovery participant to find Lightify gateway devices on the local network.
 * Devices are discovered using mDNS with looking for _http._tcp.local services with a
 * service name starting with "Lightify-".
 *
 * @author Mike Jagdis - Initial contribution
 */
public final class LightifyDiscoveryParticipant implements MDNSDiscoveryParticipant {

    private static final String SERVICE_TYPE = "_http._tcp.local.";

    private static Map<ThingUID, InetAddress[]> inetAddressesForThing = Collections.synchronizedMap(new HashMap<>());

    public static InetAddress[] getInetAddressesFor(ThingUID thingUID) {
        return inetAddressesForThing.get(thingUID);
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return SUPPORTED_BRIDGE_THING_TYPES_UIDS;
    }

    @Override
    public String getServiceType() {
        return SERVICE_TYPE;
    }

    @Override
    public DiscoveryResult createResult(ServiceInfo serviceInfo) {
        InetAddress[] inetAddresses = serviceInfo.getInetAddresses();

        if (inetAddresses.length == 0 || !serviceInfo.getName().contains("Lightify-")) {
            return null;
        }

        ThingUID thingUID = getThingUID(serviceInfo);

        inetAddressesForThing.put(thingUID, inetAddresses);

        return DiscoveryResultBuilder.create(thingUID)
            .withLabel(serviceInfo.getName())
            .build();
    }

    @Override
    public ThingUID getThingUID(ServiceInfo serviceInfo) {
        if (!serviceInfo.getName().contains("Lightify-")) {
            return null;
        }

        return new ThingUID(THING_TYPE_LIGHTIFY_GATEWAY, serviceInfo.getName().replace("Lightify-", ""));
    }
}

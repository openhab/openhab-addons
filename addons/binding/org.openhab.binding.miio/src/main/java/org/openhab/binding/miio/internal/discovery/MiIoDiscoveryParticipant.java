/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.miio.internal.discovery;

import static org.openhab.binding.miio.MiIoBindingConstants.*;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.jmdns.ServiceInfo;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.io.transport.mdns.discovery.MDNSDiscoveryParticipant;
import org.openhab.binding.miio.internal.MiIoDevices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * discovers Mi IO devices announced by mDNS
 *
 * @author Marcel Verpaalen - Initial contribution
 *
 */
public class MiIoDiscoveryParticipant implements MDNSDiscoveryParticipant {

    private Logger logger = LoggerFactory.getLogger(MiIoDiscoveryParticipant.class);

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return (NONGENERIC_THING_TYPES_UIDS);
    }

    @Override
    public String getServiceType() {
        return "_miio._udp.local.";
    }

    @Override
    public ThingUID getThingUID(@Nullable ServiceInfo service) {
        if (service == null) {
            return null;
        }
        logger.trace("ServiceInfo: {}", service);
        String id[] = service.getName().split("_miio");
        if (id.length != 2) {
            logger.trace("mDNS Could not identify Type / Device Id from '{}'", service.getName());
            return null;
        }
        int did;
        try {
            did = Integer.parseUnsignedInt(id[1]);
        } catch (Exception e) {
            logger.trace("mDNS Could not identify Device ID from '{}'", id[1]);
            return null;
        }
        ThingTypeUID thingType = MiIoDevices.getType(id[0].replaceAll("-", ".")).getThingType();
        String uidName = String.format("%08X", did);
        logger.debug("mDNS {} identified as thingtype {} with did {} ({})", id[0], thingType, uidName, did);
        return new ThingUID(thingType, uidName);
    }

    private InetAddress getIpAddress(ServiceInfo service) {
        InetAddress address = null;
        for (InetAddress addr : service.getInet4Addresses()) {
            return addr;
        }
        // Fallback for Inet6addresses
        for (InetAddress addr : service.getInet6Addresses()) {
            return addr;
        }
        return address;
    }

    @Override
    public DiscoveryResult createResult(ServiceInfo service) {
        DiscoveryResult result = null;
        ThingUID uid = getThingUID(service);
        if (uid != null) {
            Map<String, Object> properties = new HashMap<>(2);
            // remove the domain from the name
            InetAddress ip = getIpAddress(service);
            if (ip == null) {
                return null;
            }
            String inetAddress = ip.toString().substring(1); // trim leading slash
            String id = uid.getId();
            String label = "Xiaomi Mi IO Device " + id + " (" + Long.parseUnsignedLong(id, 16) + ") "
                    + service.getName();
            properties.put(PROPERTY_HOST_IP, inetAddress);
            properties.put(PROPERTY_DID, id);
            result = DiscoveryResultBuilder.create(uid).withProperties(properties).withRepresentationProperty(id)
                    .withLabel(label).build();
            logger.debug("Mi IO mDNS Discovery found {} with address '{}:{}' name '{}'", uid, inetAddress,
                    service.getPort(), label);
        }
        return result;
    }
}

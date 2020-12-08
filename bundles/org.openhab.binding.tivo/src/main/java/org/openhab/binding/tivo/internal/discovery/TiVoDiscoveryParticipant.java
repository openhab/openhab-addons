/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tivo.internal.discovery;

import java.net.InetAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.jmdns.ServiceInfo;

import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.io.transport.mdns.discovery.MDNSDiscoveryParticipant;
import org.openhab.binding.tivo.TiVoBindingConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class TiVoDiscoveryParticipant.
 * *
 *
 * @author Jayson Kubilis (DigitalBytes) - Initial contribution
 * @author Andrew Black (AndyXMB) - minor updates.
 */
public class TiVoDiscoveryParticipant implements MDNSDiscoveryParticipant {
    private Logger logger = LoggerFactory.getLogger(TiVoDiscoveryParticipant.class);

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return Collections.singleton(TiVoBindingConstants.THING_TYPE_TIVO);
    }

    @Override
    public String getServiceType() {
        logger.debug("TiVo Discover getServiceType");
        return "_tivo-remote._tcp.local.";
    }

    @Override
    public DiscoveryResult createResult(ServiceInfo service) {
        logger.debug("TiVo Discover createResult");
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

            String label = service.getName();

            int port = service.getPort();

            properties.put(TiVoBindingConstants.CONFIG_ADDRESS, inetAddress);
            properties.put(TiVoBindingConstants.CONFIG_PORT, port);
            properties.put(TiVoBindingConstants.CONFIG_NAME, label);

            result = DiscoveryResultBuilder.create(uid).withProperties(properties).withLabel("Tivo: " + label).build();
            logger.debug("Created {} for TiVo host '{}' name '{}'", result,
                    properties.get(TiVoBindingConstants.CONFIG_ADDRESS), label);
        }
        return result;
    }

    /**
     * @see org.eclipse.smarthome.io.transport.mdns.discovery.MDNSDiscoveryParticipant#getThingUID(javax.jmdns.ServiceInfo)
     */
    @Override
    public ThingUID getThingUID(ServiceInfo service) {
        logger.debug("TiVo Discover getThingUID");
        if (service != null) {
            logger.trace("ServiceInfo: {}", service);
            if (service.getType() != null) {
                if (service.getType().equals(getServiceType())) {
                    String uidName = getUIDName(service);
                    return new ThingUID(TiVoBindingConstants.THING_TYPE_TIVO, uidName);
                }
            }
        }
        return null;
    }

    /**
     * Gets the UID name, replacing any non AlphaNumeric characters with underscores.
     *
     * @param service the service
     * @return the UID name
     */
    private String getUIDName(ServiceInfo service) {
        return service.getName().replaceAll("[^A-Za-z0-9_]", "_");
    }

    /**
     * {@link InetAddress} gets the IP address of the device in v4 or v6 format.
     *
     * @param ServiceInfo service
     * @return InetAddress the IP address
     *
     */
    private InetAddress getIpAddress(ServiceInfo service) {
        InetAddress address = null;
        for (InetAddress addr : service.getInet4Addresses()) {
            return addr;
        }
        // Fall back for Inet6addresses
        for (InetAddress addr : service.getInet6Addresses()) {
            return addr;
        }
        return address;
    }

}

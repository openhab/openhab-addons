/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.neeo.internal.discovery;

import static org.openhab.binding.neeo.internal.NeeoConstants.BRIDGE_TYPE_BRAIN;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.jmdns.ServiceInfo;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.mdns.MDNSDiscoveryParticipant;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.neeo.internal.NeeoConstants;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of {@link MDNSDiscoveryParticipant} that will discover NEEO brain(s).
 *
 * @author Tim Roberts - initial contribution
 */
@NonNullByDefault
@Component(immediate = true)
public class NeeoBrainDiscovery implements MDNSDiscoveryParticipant {

    /** The logger */
    private Logger logger = LoggerFactory.getLogger(NeeoBrainDiscovery.class);

    @Override
    public Set<@Nullable ThingTypeUID> getSupportedThingTypeUIDs() {
        return Collections.singleton(BRIDGE_TYPE_BRAIN);
    }

    @Override
    public String getServiceType() {
        return NeeoConstants.NEEO_MDNS_TYPE;
    }

    @Nullable
    @Override
    public DiscoveryResult createResult(@Nullable ServiceInfo service) {
        if (service == null) {
            return null;
        }

        final ThingUID uid = getThingUID(service);
        if (uid == null) {
            return null;
        }

        logger.debug("createResult is evaluating: {}", service);

        final Map<String, Object> properties = new HashMap<>(2);

        final InetAddress ip = getIpAddress(service);
        if (ip == null) {
            logger.debug("Application not 'neeo' in MDNS serviceinfo: {}", service);
            return null;
        }
        final String inetAddress = ip.getHostAddress();

        final String id = uid.getId();
        final String label = service.getName() + " (" + id + ")";

        properties.put(NeeoConstants.CONFIG_IPADDRESS, inetAddress);
        properties.put(NeeoConstants.CONFIG_ENABLEFORWARDACTIONS, true);

        logger.debug("Adding NEEO Brain to inbox: {} at {}", id, inetAddress);
        return DiscoveryResultBuilder.create(uid).withProperties(properties).withLabel(label).build();
    }

    @Nullable
    @Override
    public ThingUID getThingUID(@Nullable ServiceInfo service) {
        if (service == null) {
            return null;
        }

        logger.debug("getThingUID is evaluating: {}", service);
        if (!StringUtils.equals("neeo", service.getApplication())) {
            logger.debug("Application not 'neeo' in MDNS serviceinfo: {}", service);
            return null;
        }

        if (getIpAddress(service) == null) {
            logger.debug("No IP address found in MDNS serviceinfo: {}", service);
            return null;
        }

        String model = service.getPropertyString("hon"); // model
        if (model == null) {
            final String server = service.getServer(); // NEEO-xxxxx.local.
            if (server != null) {
                final int idx = server.indexOf(".");
                if (idx >= 0) {
                    model = server.substring(0, idx);
                }
            }
        }
        if (model == null || model.length() <= 5 || !model.toLowerCase().startsWith("neeo")) {
            logger.debug("No 'hon' found in MDNS serviceinfo: {}", service);
            return null;
        }

        final String id = model.substring(5);
        logger.debug("NEEO Brain Found: {}", id);

        return new ThingUID(BRIDGE_TYPE_BRAIN, id);
    }

    /**
     * Gets the ip address found in the {@link ServiceInfo}
     *
     * @param service a non-null service
     * @return the ip address of the service or null if none found.
     */
    @Nullable
    private InetAddress getIpAddress(ServiceInfo service) {
        Objects.requireNonNull(service, "service cannot be null");

        for (String addr : service.getHostAddresses()) {
            try {
                return InetAddress.getByName(addr);
            } catch (UnknownHostException e) {
                // ignore
            }
        }

        for (InetAddress addr : service.getInet4Addresses()) {
            return addr;
        }
        // Fallback for Inet6addresses
        for (InetAddress addr : service.getInet6Addresses()) {
            return addr;
        }
        return null;
    }

}

/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.shieldtv.internal.discovery;

import static org.openhab.binding.shieldtv.internal.ShieldTVBindingConstants.*;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.jmdns.ServiceInfo;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.config.discovery.mdns.MDNSDiscoveryParticipant;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of {@link MDNSDiscoveryParticipant} that will discover SHIELDTV(s).
 * 
 * @author Ben Rosenblum - initial contribution
 */
@NonNullByDefault
@Component(service = MDNSDiscoveryParticipant.class, configurationPid = "discovery.shieldtv")
public class ShieldTVDiscoveryParticipant implements MDNSDiscoveryParticipant {

    private final Logger logger = LoggerFactory.getLogger(ShieldTVDiscoveryParticipant.class);
    private static final String SHIELDTV_MDNS_SERVICE_TYPE = "_nv_shield_remote._tcp.local.";

    @Override
    public Set<@Nullable ThingTypeUID> getSupportedThingTypeUIDs() {
        return Collections.singleton(THING_TYPE_SHIELDTV);
    }

    @Override
    public String getServiceType() {
        return SHIELDTV_MDNS_SERVICE_TYPE;
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
            logger.debug("Application not 'shieldtv' in MDNS serviceinfo: {}", service);
            return null;
        }
        final String inetAddress = ip.getHostAddress();

        final String id = uid.getId();
        final String label = service.getName() + " (" + id + ")";

        properties.put(IPADDRESS, inetAddress);

        logger.debug("Adding SHIELDTV to inbox: {} at {}", id, inetAddress);
        return DiscoveryResultBuilder.create(uid).withProperties(properties).withLabel(label).build();
    }

    @Nullable
    @Override
    public ThingUID getThingUID(@Nullable ServiceInfo service) {
        if (service == null) {
            return null;
        }

        logger.debug("getThingUID is evaluating: {}", service);
        if (!"shieldtv".equals(service.getApplication())) {
            logger.debug("Application not 'shieldtv' in MDNS serviceinfo: {}", service);
            return null;
        }

        if (getIpAddress(service) == null) {
            logger.debug("No IP address found in MDNS serviceinfo: {}", service);
            return null;
        }

        String model = service.getPropertyString("hon"); // model
        if (model == null) {
            final String server = service.getServer(); // SHIELDTV-xxxxx.local.
            if (server != null) {
                final int idx = server.indexOf(".");
                if (idx >= 0) {
                    model = server.substring(0, idx);
                }
            }
        }
        if (model == null || model.length() <= 5 || !model.toLowerCase().startsWith("shieldtv")) {
            logger.debug("No 'hon' found in MDNS serviceinfo: {}", service);
            return null;
        }

        final String id = model.substring(5);
        logger.debug("SHIELDTV Brain Found: {}", id);

        return new ThingUID(THING_TYPE_SHIELDTV, id);
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

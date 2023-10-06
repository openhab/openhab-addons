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
package org.openhab.binding.androidtv.internal.discovery;

import static org.openhab.binding.androidtv.internal.AndroidTVBindingConstants.*;

import java.net.InetAddress;
import java.util.Map;
import java.util.Set;

import javax.jmdns.ServiceInfo;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.mdns.MDNSDiscoveryParticipant;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of {@link MDNSDiscoveryParticipant} that will discover SHIELDTV(s).
 * 
 * @author Ben Rosenblum - initial contribution
 */
@NonNullByDefault
@Component(service = MDNSDiscoveryParticipant.class, immediate = true, configurationPid = "discovery.shieldtv")
public class ShieldTVDiscoveryParticipant implements MDNSDiscoveryParticipant {

    private final Logger logger = LoggerFactory.getLogger(ShieldTVDiscoveryParticipant.class);
    private static final String SHIELDTV_MDNS_SERVICE_TYPE = "_nv_shield_remote._tcp.local.";

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return SUPPORTED_THING_TYPES;
    }

    @Override
    public String getServiceType() {
        return SHIELDTV_MDNS_SERVICE_TYPE;
    }

    @Override
    public @Nullable DiscoveryResult createResult(@Nullable ServiceInfo service) {
        if (service == null || !service.hasData()) {
            return null;
        }

        InetAddress[] ipAddresses = service.getInet4Addresses();

        if (ipAddresses.length > 0) {
            String ipAddress = ipAddresses[0].getHostAddress();
            String serverId = service.getPropertyString("SERVER");
            String serverCapability = service.getPropertyString("SERVER_CAPABILITY");

            if (logger.isDebugEnabled()) {
                String nice = service.getNiceTextString();
                String qualifiedName = service.getQualifiedName();
                logger.debug("ShieldTV mDNS discovery notified of ShieldTV mDNS service: {}", nice);
                logger.trace("ShieldTV mDNS service qualifiedName: {}", qualifiedName);
                logger.trace("ShieldTV mDNS service ipAddresses: {} ({})", ipAddresses, ipAddresses.length);
                logger.trace("ShieldTV mDNS service selected ipAddress: {}", ipAddress);
                logger.trace("ShieldTV mDNS service property SERVER: {}", serverId);
                logger.trace("ShieldTV mDNS service property SERVER_CAPABILITY: {}", serverCapability);
            }

            final ThingUID uid = getThingUID(service);
            if (uid != null) {
                final String id = uid.getId();
                final String label = service.getName() + " (" + id + ")";
                final Map<String, Object> properties = Map.of(PROPERTY_IP_ADDRESS, ipAddress);

                return DiscoveryResultBuilder.create(uid).withProperties(properties).withLabel(label).build();
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    public @Nullable ThingUID getThingUID(@Nullable ServiceInfo service) {
        if (service == null || !service.hasData() || (service.getPropertyString("SERVER") == null)) {
            return null;
        }

        return new ThingUID(THING_TYPE_SHIELDTV, service.getPropertyString("SERVER").substring(8));
    }
}

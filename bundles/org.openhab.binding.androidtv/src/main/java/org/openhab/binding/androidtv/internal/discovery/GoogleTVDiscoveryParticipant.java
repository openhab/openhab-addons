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
 * Implementation of {@link MDNSDiscoveryParticipant} that will discover GOOGLETV(s).
 * 
 * @author Ben Rosenblum - initial contribution
 */
@NonNullByDefault
@Component(service = MDNSDiscoveryParticipant.class, immediate = true, configurationPid = "discovery.googletv")
public class GoogleTVDiscoveryParticipant implements MDNSDiscoveryParticipant {

    private final Logger logger = LoggerFactory.getLogger(GoogleTVDiscoveryParticipant.class);
    private static final String GOOGLETV_MDNS_SERVICE_TYPE = "_androidtvremote2._tcp.local.";

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return SUPPORTED_THING_TYPES;
    }

    @Override
    public String getServiceType() {
        return GOOGLETV_MDNS_SERVICE_TYPE;
    }

    @Override
    public @Nullable DiscoveryResult createResult(@Nullable ServiceInfo service) {
        if ((service == null) || !service.hasData()) {
            return null;
        }

        InetAddress[] ipAddresses = service.getInet4Addresses();

        if (ipAddresses.length > 0) {
            String ipAddress = ipAddresses[0].getHostAddress();
            String macAddress = service.getPropertyString("bt");

            if (logger.isDebugEnabled()) {
                String nice = service.getNiceTextString();
                String qualifiedName = service.getQualifiedName();
                logger.debug("GoogleTV mDNS discovery notified of GoogleTV mDNS service: {}", nice);
                logger.trace("GoogleTV mDNS service qualifiedName: {}", qualifiedName);
                logger.trace("GoogleTV mDNS service ipAddresses: {} ({})", ipAddresses, ipAddresses.length);
                logger.trace("GoogleTV mDNS service selected ipAddress: {}", ipAddress);
                logger.trace("GoogleTV mDNS service property macAddress: {}", macAddress);
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
        if ((service == null) || !service.hasData() || (service.getPropertyString("bt") == null)) {
            return null;
        }

        return new ThingUID(THING_TYPE_GOOGLETV, service.getPropertyString("bt").replace(":", ""));
    }
}

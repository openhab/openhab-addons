/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.vizio.internal.discovery;

import static org.openhab.binding.vizio.internal.VizioBindingConstants.*;

import java.net.InetAddress;
import java.util.Set;

import javax.jmdns.ServiceInfo;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.mdns.MDNSDiscoveryParticipant;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link VizioDiscoveryParticipant} is responsible processing the
 * results of searches for mDNS services of type _viziocast._tcp.local.
 *
 * @author Michael Lobstein - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "discovery.vizio")
public class VizioDiscoveryParticipant implements MDNSDiscoveryParticipant {
    private final Logger logger = LoggerFactory.getLogger(VizioDiscoveryParticipant.class);

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return SUPPORTED_THING_TYPES_UIDS;
    }

    @Override
    public String getServiceType() {
        return "_viziocast._tcp.local.";
    }

    @Override
    public @Nullable DiscoveryResult createResult(ServiceInfo service) {
        DiscoveryResult result = null;

        ThingUID thingUid = getThingUID(service);
        if (thingUid != null) {
            InetAddress ip = getIpAddress(service);
            if (ip == null) {
                return null;
            }
            String inetAddress = ip.toString().substring(1); // trim leading slash
            String label = service.getName();
            int port = service.getPort();

            result = DiscoveryResultBuilder.create(thingUid).withLabel(label).withRepresentationProperty(PROPERTY_UUID)
                    .withProperty(PROPERTY_UUID, thingUid.getId())
                    .withProperty(Thing.PROPERTY_MODEL_ID, service.getPropertyString("mdl"))
                    .withProperty(PROPERTY_HOST_NAME, inetAddress).withProperty(PROPERTY_PORT, port).build();
            logger.debug("Created {} for Vizio TV at {}, name: '{}'", result, inetAddress, label);
        }
        return result;
    }

    /**
     * @see org.openhab.core.config.discovery.mdns.MDNSDiscoveryParticipant#getThingUID(javax.jmdns.ServiceInfo)
     */
    @Override
    public @Nullable ThingUID getThingUID(ServiceInfo service) {
        if (service.getType() != null && service.getType().equals(getServiceType())) {
            String uidName = getUIDName(service);
            return uidName != null ? new ThingUID(THING_TYPE_VIZIO_TV, uidName) : null;
        }
        return null;
    }

    /**
     * Gets the UID name from the mdns record txt info (mac address), fall back with IP address
     *
     * @param service the mdns service
     * @return the UID name
     */
    private @Nullable String getUIDName(ServiceInfo service) {
        String uid = service.getPropertyString("eth");

        if (uid == null || uid.endsWith("000") || uid.length() < 12) {
            uid = service.getPropertyString("wifi");
        }

        if (uid == null || uid.endsWith("000") || uid.length() < 12) {
            InetAddress ip = getIpAddress(service);
            if (ip == null) {
                return null;
            } else {
                uid = ip.toString();
            }
        }
        return uid.replaceAll("[^A-Za-z0-9_]", "_");
    }

    /**
     * {@link InetAddress} gets the IP address of the device in v4 or v6 format.
     *
     * @param ServiceInfo service
     * @return InetAddress the IP address
     *
     */
    private @Nullable InetAddress getIpAddress(ServiceInfo service) {
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

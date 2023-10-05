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
package org.openhab.binding.enphase.internal.discovery;

import static org.openhab.binding.enphase.internal.EnphaseBindingConstants.CONFIG_HOSTNAME;
import static org.openhab.binding.enphase.internal.EnphaseBindingConstants.CONFIG_SERIAL_NUMBER;
import static org.openhab.binding.enphase.internal.EnphaseBindingConstants.DISCOVERY_SERIAL;
import static org.openhab.binding.enphase.internal.EnphaseBindingConstants.DISCOVERY_VERSION;
import static org.openhab.binding.enphase.internal.EnphaseBindingConstants.PROPERTY_VERSION;
import static org.openhab.binding.enphase.internal.EnphaseBindingConstants.THING_TYPE_ENPHASE_ENVOY;

import java.net.Inet4Address;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.jmdns.ServiceInfo;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.enphase.internal.EnphaseBindingConstants;
import org.openhab.binding.enphase.internal.EnvoyHostAddressCache;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.mdns.MDNSDiscoveryParticipant;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MDNS discovery participant for discovering Envoy gateways.
 * This service also keeps track of any discovered Envoys host name to provide this information for existing Envoy
 * bridges so the bridge cat get the host name/ip address if that is unknown.
 *
 * @author Thomas Hentschel - Initial contribution
 * @author Hilbrand Bouwkamp - Initial contribution
 */
@Component(service = { EnvoyHostAddressCache.class, MDNSDiscoveryParticipant.class })
@NonNullByDefault
public class EnvoyDiscoveryParticipant implements MDNSDiscoveryParticipant, EnvoyHostAddressCache {
    private static final String ENVOY_MDNS_ID = "envoy";

    private final Logger logger = LoggerFactory.getLogger(EnvoyDiscoveryParticipant.class);

    private final Map<String, @Nullable String> lastKnownHostAddresses = new ConcurrentHashMap<>();

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return Set.of(THING_TYPE_ENPHASE_ENVOY);
    }

    @Override
    public String getServiceType() {
        return "_enphase-envoy._tcp.local.";
    }

    @Override
    public @Nullable DiscoveryResult createResult(final ServiceInfo info) {
        final String id = info.getName();

        logger.debug("id found: {} with type: {}", id, info.getType());

        if (!id.contains(ENVOY_MDNS_ID)) {
            return null;
        }

        if (info.getInet4Addresses().length == 0 || info.getInet4Addresses()[0] == null) {
            return null;
        }

        final ThingUID uid = getThingUID(info);

        if (uid == null) {
            return null;
        }

        final Inet4Address hostname = info.getInet4Addresses()[0];
        final String serialNumber = info.getPropertyString(DISCOVERY_SERIAL);

        if (serialNumber == null) {
            logger.debug("No serial number found in data for discovered Envoy {}: {}", id, info);
            return null;
        }
        final String version = info.getPropertyString(DISCOVERY_VERSION);
        final String hostAddress = hostname == null ? "" : hostname.getHostAddress();

        lastKnownHostAddresses.put(serialNumber, hostAddress);
        final Map<String, Object> properties = new HashMap<>(3);

        properties.put(CONFIG_SERIAL_NUMBER, serialNumber);
        properties.put(CONFIG_HOSTNAME, hostAddress);
        properties.put(PROPERTY_VERSION, version);
        return DiscoveryResultBuilder.create(uid).withProperties(properties)
                .withRepresentationProperty(CONFIG_SERIAL_NUMBER)
                .withLabel("Enphase Envoy " + EnphaseBindingConstants.defaultPassword(serialNumber)).build();
    }

    @Override
    public String getLastKnownHostAddress(final String serialNumber) {
        final String hostAddress = lastKnownHostAddresses.get(serialNumber);

        return hostAddress == null ? "" : hostAddress;
    }

    @Override
    public @Nullable ThingUID getThingUID(final ServiceInfo info) {
        final String name = info.getName();

        if (!name.contains(ENVOY_MDNS_ID)) {
            logger.trace("Found other type of device that is not recognized as an Envoy: {}", name);
            return null;
        }
        if (info.getInet4Addresses().length == 0 || info.getInet4Addresses()[0] == null) {
            logger.debug("Found an Envoy, but no ip address is given: {}", info);
            return null;
        }
        logger.debug("ServiceInfo addr: {}", info.getInet4Addresses()[0]);
        if (getServiceType().equals(info.getType())) {
            final String serial = info.getPropertyString(DISCOVERY_SERIAL);

            logger.debug("Discovered an Envoy with serial number '{}'", serial);
            return new ThingUID(THING_TYPE_ENPHASE_ENVOY, serial);
        }
        return null;
    }
}

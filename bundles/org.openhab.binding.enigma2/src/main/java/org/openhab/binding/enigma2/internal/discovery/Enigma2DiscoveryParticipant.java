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
package org.openhab.binding.enigma2.internal.discovery;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import javax.jmdns.ServiceInfo;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.enigma2.internal.Enigma2BindingConstants;
import org.openhab.binding.enigma2.internal.Enigma2HttpClient;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.mdns.MDNSDiscoveryParticipant;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link Enigma2DiscoveryParticipant} is responsible processing the
 * results of searches for mDNS services of type _http._tcp.local. and finding a webinterface
 *
 * @author Guido Dolfen - Initial contribution
 */
@NonNullByDefault
@Component(service = MDNSDiscoveryParticipant.class)
public class Enigma2DiscoveryParticipant implements MDNSDiscoveryParticipant {

    private final Logger logger = LoggerFactory.getLogger(Enigma2DiscoveryParticipant.class);

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return Enigma2BindingConstants.SUPPORTED_THING_TYPES_UIDS;
    }

    @Override
    public @Nullable DiscoveryResult createResult(ServiceInfo info) {
        logger.debug("ServiceInfo {}", info);
        String ipAddress = getIPAddress(info);
        if (ipAddress != null && isEnigma2Device(ipAddress)) {
            logger.debug("Enigma2 device discovered: IP-Adress={}, name={}", ipAddress, info.getName());
            ThingUID uid = getThingUID(info);
            if (uid != null) {
                Map<String, Object> properties = new HashMap<>();
                properties.put(Enigma2BindingConstants.CONFIG_HOST, ipAddress);
                properties.put(Enigma2BindingConstants.CONFIG_REFRESH, 5);
                properties.put(Enigma2BindingConstants.CONFIG_TIMEOUT, 5);
                return DiscoveryResultBuilder.create(uid).withProperties(properties).withLabel(info.getName()).build();
            }
        }
        return null;
    }

    @Override
    public @Nullable ThingUID getThingUID(ServiceInfo info) {
        logger.debug("ServiceInfo {}", info);
        String ipAddress = getIPAddress(info);
        if (ipAddress != null) {
            return new ThingUID(Enigma2BindingConstants.THING_TYPE_DEVICE, ipAddress.replace(".", "_"));
        }
        return null;
    }

    @Override
    public String getServiceType() {
        return "_http._tcp.local.";
    }

    private boolean isEnigma2Device(String ipAddress) {
        try {
            return getEnigma2HttpClient().get("http://" + ipAddress + "/web/about").contains("e2enigmaversion");
        } catch (IOException ignore) {
            return false;
        }
    }

    private @Nullable String getIPAddress(ServiceInfo info) {
        InetAddress[] addresses = info.getInet4Addresses();
        if (addresses.length > 1) {
            logger.debug("Enigma2 device {} reports multiple addresses - using the first one! {}", info.getName(),
                    addresses);
        }
        return Stream.of(addresses).findFirst().map(InetAddress::getHostAddress).orElse(null);
    }

    /**
     * Getter for Test-Injection
     * 
     * @return HttpGet.
     */
    Enigma2HttpClient getEnigma2HttpClient() {
        return new Enigma2HttpClient(5);
    }
}

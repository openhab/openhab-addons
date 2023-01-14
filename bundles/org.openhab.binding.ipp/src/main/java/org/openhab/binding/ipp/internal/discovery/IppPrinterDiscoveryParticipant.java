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
package org.openhab.binding.ipp.internal.discovery;

import static org.openhab.binding.ipp.internal.IppBindingConstants.*;

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
 * discovers ipp printers announced by mDNS
 *
 * @author Tobias Bräutigam - Initial contribution
 */
@Component
@NonNullByDefault
public class IppPrinterDiscoveryParticipant implements MDNSDiscoveryParticipant {

    private final Logger logger = LoggerFactory.getLogger(IppPrinterDiscoveryParticipant.class);

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return Set.of(PRINTER_THING_TYPE);
    }

    @Override
    public String getServiceType() {
        return "_ipp._tcp.local.";
    }

    @Override
    public @Nullable ThingUID getThingUID(ServiceInfo service) {
        logger.trace("ServiceInfo: {}", service);
        if (getServiceType().equals(service.getType())) {
            String uidName = getUIDName(service);
            return new ThingUID(PRINTER_THING_TYPE, uidName);
        }
        return null;
    }

    private String getUIDName(ServiceInfo service) {
        return service.getName().replaceAll("[^A-Za-z0-9_]", "_");
    }

    private @Nullable InetAddress getIpAddress(ServiceInfo service) {
        for (InetAddress addr : service.getInet4Addresses()) {
            return addr;
        }
        // Fallback for Inet6addresses
        for (InetAddress addr : service.getInet6Addresses()) {
            return addr;
        }
        return null;
    }

    @Override
    public @Nullable DiscoveryResult createResult(ServiceInfo service) {
        String rp = service.getPropertyString("rp");
        if (rp == null) {
            return null;
        }
        ThingUID uid = getThingUID(service);
        if (uid != null) {
            // remove the domain from the name
            InetAddress ip = getIpAddress(service);
            if (ip == null) {
                return null;
            }
            String inetAddress = ip.toString().substring(1); // trim leading slash
            String label = service.getName();
            int port = service.getPort();
            String uuid = service.getPropertyString("UUID");

            Map<String, Object> properties = Map.of( //
                    PRINTER_PARAMETER_URL, "http://" + inetAddress + ":" + port + "/" + rp, //
                    PRINTER_PARAMETER_NAME, label, //
                    PRINTER_PARAMETER_UUID, uuid //
            );

            return DiscoveryResultBuilder.create(uid).withProperties(properties)
                    .withRepresentationProperty(PRINTER_PARAMETER_UUID).withLabel(label).build();
        }
        return null;
    }
}

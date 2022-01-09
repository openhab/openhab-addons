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
package org.openhab.binding.hpprinter.internal;

import static org.openhab.binding.hpprinter.internal.HPPrinterBindingConstants.THING_PRINTER;

import java.net.InetAddress;
import java.util.HashMap;
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
 * The {@link HPPrinterDiscoveryParticipant} class discovers HP Printers over
 * mDNS.
 *
 * @author Stewart Cossey - Initial contribution
 */
@NonNullByDefault
@Component
public class HPPrinterDiscoveryParticipant implements MDNSDiscoveryParticipant {

    private final Logger logger = LoggerFactory.getLogger(HPPrinterDiscoveryParticipant.class);

    @Override
    public @Nullable DiscoveryResult createResult(ServiceInfo service) {
        if (!service.hasData()) {
            logger.trace("Service has no data.");
            return null;
        }

        // We only care about HP Printers
        String ty = service.getPropertyString("ty");
        if (ty != null && ty.toLowerCase().startsWith("hp")) {
            String rp = service.getPropertyString("rp");
            if (rp == null) {
                return null;
            }

            logger.debug("Found HP Printer ID: {}", ty);
            ThingUID uid = getThingUID(service);
            if (uid != null) {
                Map<String, Object> properties = new HashMap<>(2);
                InetAddress ip = getIpAddress(service);
                if (ip == null) {
                    return null;
                }
                String inetAddress = ip.toString().substring(1); // trim leading slash
                String label = service.getName();

                properties.put(HPPrinterConfiguration.IP_ADDRESS, inetAddress);
                properties.put(HPPrinterConfiguration.UUID, service.getPropertyString("UUID"));
                DiscoveryResult result = DiscoveryResultBuilder.create(uid).withProperties(properties)
                        .withRepresentationProperty(HPPrinterConfiguration.UUID).withLabel(label).build();
                logger.trace("Created a DiscoveryResult {} for printer on host '{}' name '{}'", result, inetAddress,
                        label);
                return result;
            } else {
                logger.debug("Found unsupported HP Printer ID: {}", ty);
            }
        } else {
            logger.trace("Ignore non HP device {}", ty);
        }

        return null;
    }

    private String getUIDName(ServiceInfo service) {
        return service.getName().replaceAll("[^A-Za-z0-9_]", "_");
    }

    @Override
    public String getServiceType() {
        return "_printer._tcp.local.";
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return HPPrinterBindingConstants.SUPPORTED_THING_TYPES_UIDS;
    }

    @Override
    public @Nullable ThingUID getThingUID(ServiceInfo service) {
        if (service.getType() != null) {
            logger.trace("ServiceInfo: {}", service);
            if (service.getType().equals(getServiceType())) {
                String uidName = getUIDName(service);
                ThingTypeUID mdl = findThingType();

                return new ThingUID(mdl, uidName);
            }
        }
        return null;
    }

    @Nullable
    private InetAddress getIpAddress(ServiceInfo service) {
        InetAddress address = null;
        for (InetAddress addr : service.getInet4Addresses()) {
            logger.debug("Get IP address for device {}", addr);
            return addr;
        }
        // Fallback for Inet6addresses
        for (InetAddress addr : service.getInet6Addresses()) {
            logger.debug("Get IP address for device {}", addr);
            return addr;
        }
        return address;
    }

    private ThingTypeUID findThingType() {
        return THING_PRINTER;
    }
}

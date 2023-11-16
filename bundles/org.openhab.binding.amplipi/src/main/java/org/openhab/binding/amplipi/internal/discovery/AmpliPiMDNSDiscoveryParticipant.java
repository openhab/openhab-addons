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
package org.openhab.binding.amplipi.internal.discovery;

import java.net.InetAddress;
import java.util.Set;

import javax.jmdns.ServiceInfo;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.amplipi.internal.AmpliPiBindingConstants;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.mdns.MDNSDiscoveryParticipant;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;

/**
 * This is a discovery participant which finds AmpliPis on the local network
 * through their mDNS announcements.
 *
 * @author Kai Kreuzer - Initial contribution
 *
 */
@NonNullByDefault
@Component
public class AmpliPiMDNSDiscoveryParticipant implements MDNSDiscoveryParticipant {

    private static final String AMPLIPI_API = "amplipi-api";

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return Set.of(AmpliPiBindingConstants.THING_TYPE_CONTROLLER);
    }

    @Override
    public String getServiceType() {
        return "_http._tcp.local.";
    }

    @Override
    public @Nullable DiscoveryResult createResult(ServiceInfo service) {
        ThingUID uid = getThingUID(service);
        if (uid != null) {
            return DiscoveryResultBuilder.create(uid).withLabel("AmpliPi Controller")
                    .withProperty(AmpliPiBindingConstants.CFG_PARAM_HOSTNAME, getIpAddress(service).getHostAddress())
                    .withRepresentationProperty(AmpliPiBindingConstants.CFG_PARAM_HOSTNAME).build();
        } else {
            return null;
        }
    }

    @Override
    public @Nullable ThingUID getThingUID(ServiceInfo service) {
        if (service.getName().equals(AMPLIPI_API)) {
            InetAddress ip = getIpAddress(service);
            if (ip != null) {
                String id = ip.toString().substring(1).replace(".", "");
                return new ThingUID(AmpliPiBindingConstants.THING_TYPE_CONTROLLER, id);
            }
        }
        return null;
    }

    private @Nullable InetAddress getIpAddress(ServiceInfo service) {
        if (service.getInet4Addresses().length > 0) {
            return service.getInet4Addresses()[0];
        } else {
            return null;
        }
    }
}

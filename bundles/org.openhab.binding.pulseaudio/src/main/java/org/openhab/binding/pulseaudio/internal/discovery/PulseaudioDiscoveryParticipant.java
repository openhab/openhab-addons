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
package org.openhab.binding.pulseaudio.internal.discovery;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.jmdns.ServiceInfo;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.pulseaudio.internal.PulseaudioBindingConstants;
import org.openhab.binding.pulseaudio.internal.handler.PulseaudioBridgeHandler;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.mdns.MDNSDiscoveryParticipant;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PulseaudioDiscoveryParticipant} is responsible processing the
 * results of searches for mDNS services of type _pulse-server._tcp.local.
 *
 * @author Tobias Bräutigam - Initial contribution
 */
@Component
@NonNullByDefault
public class PulseaudioDiscoveryParticipant implements MDNSDiscoveryParticipant {

    private final Logger logger = LoggerFactory.getLogger(PulseaudioDiscoveryParticipant.class);

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return PulseaudioBridgeHandler.SUPPORTED_THING_TYPES_UIDS;
    }

    @Override
    public @Nullable DiscoveryResult createResult(ServiceInfo info) {
        DiscoveryResult result = null;
        ThingUID uid = getThingUID(info);
        if (uid != null) {
            Map<String, Object> properties = new HashMap<>(3);
            String label = "Pulseaudio server";
            try {
                label = info.getName();
            } catch (Exception e) {
                // ignore and use default label
            }
            // remove the domain from the name
            String hostname = info.getServer().replace("." + info.getDomain() + ".", "");
            try (Socket testSocket = new Socket(hostname, 4712)) {
                logger.debug("testing connection to pulseaudio server {}:4712", hostname);

                if (testSocket.isConnected()) {
                    properties.put(PulseaudioBindingConstants.BRIDGE_PARAMETER_HOST, hostname);
                    // we do not read the port here because the given port is 4713 and we need 4712 to query the server
                    result = DiscoveryResultBuilder.create(uid).withProperties(properties).withLabel(label).build();

                    logger.trace("Created a DiscoveryResult for device '{}' on host '{}'", info.getName(), hostname);
                }
                return result;
            } catch (IOException e) {
            }
        }
        return result;
    }

    @Override
    public @Nullable ThingUID getThingUID(ServiceInfo info) {
        logger.debug("ServiceInfo: {}", info);
        if (info.getType() != null) {
            if (info.getType().equals(getServiceType())) {
                logger.trace("Discovered a pulseaudio server thing with name '{}'", info.getName());
                return new ThingUID(PulseaudioBindingConstants.BRIDGE_THING_TYPE, info.getName().replace("@", "_AT_"));
            }
        }
        return null;
    }

    @Override
    public String getServiceType() {
        return "_pulse-server._tcp.local.";
    }
}

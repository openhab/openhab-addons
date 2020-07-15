/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.miele.internal.discovery;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.jmdns.ServiceInfo;

import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.mdns.MDNSDiscoveryParticipant;
import org.eclipse.smarthome.config.discovery.mdns.internal.MDNSDiscoveryService;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.miele.internal.MieleBindingConstants;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MieleMDNSDiscoveryParticipant} is responsible for discovering Miele XGW3000 Gateways. It uses the central
 * {@link MDNSDiscoveryService}.
 *
 * @author Karel Goderis - Initial contribution
 * @author Martin Lepsy - Added check for Miele gateway for cleaner discovery
 *
 */
@Component(immediate = true)
public class MieleMDNSDiscoveryParticipant implements MDNSDiscoveryParticipant {

    private final Logger logger = LoggerFactory.getLogger(MieleMDNSDiscoveryParticipant.class);
    private static final String PATH_TO_CHECK_FOR_XGW3000 = "/rest/";
    private static final String SERVICE_NAME = "mieleathome";
    private static final String PATH_PROPERTY_NAME = "path";

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return Collections.singleton(MieleBindingConstants.THING_TYPE_XGW3000);
    }

    @Override
    public String getServiceType() {
        return "_mieleathome._tcp.local.";
    }

    @Override
    public DiscoveryResult createResult(ServiceInfo service) {
        if (isMieleGateway(service)) {
            ThingUID uid = getThingUID(service);

            if (uid != null) {
                Map<String, Object> properties = new HashMap<>(2);

                InetAddress[] addresses = service.getInetAddresses();
                if (addresses.length > 0 && addresses[0] != null) {
                    properties.put(MieleBindingConstants.HOST, addresses[0].getHostAddress());

                    Socket socket = null;
                    try {
                        socket = new Socket(addresses[0], 80);
                        InetAddress ourAddress = socket.getLocalAddress();
                        properties.put(MieleBindingConstants.INTERFACE, ourAddress.getHostAddress());
                    } catch (IOException e) {
                        logger.error("An exception occurred while connecting to the Miele Gateway : '{}'",
                                e.getMessage());
                    }
                }

                return DiscoveryResultBuilder.create(uid).withProperties(properties)
                        .withRepresentationProperty(uid.getId()).withLabel("Miele XGW3000 Gateway").build();
            }
        }
        return null;
    }

    @Override
    public ThingUID getThingUID(ServiceInfo service) {
        if (service.getType() != null) {
            if (service.getType().equals(getServiceType())) {
                logger.trace("Discovered a Miele@Home gateway thing with name '{}'", service.getName());
                return new ThingUID(MieleBindingConstants.THING_TYPE_XGW3000, service.getName().replace(" ", "_"));
            }
        }

        return null;
    }

    /**
     * Checks if service is a Miele XGW3000 Gateway
     *
     * application must be mieleathome
     * must contain path with value /rest/
     *
     * @param service the service to check
     * @return true, if the discovered service is a Miele XGW3000 Gateway
     */
    private boolean isMieleGateway(ServiceInfo service) {
        return service.getApplication().contains(SERVICE_NAME) && service.getPropertyString(PATH_PROPERTY_NAME) != null
                && service.getPropertyString(PATH_PROPERTY_NAME).equalsIgnoreCase(PATH_TO_CHECK_FOR_XGW3000);
    }
}

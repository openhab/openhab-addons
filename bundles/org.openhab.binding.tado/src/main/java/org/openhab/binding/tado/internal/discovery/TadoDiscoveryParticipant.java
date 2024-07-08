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
package org.openhab.binding.tado.internal.discovery;

import static org.openhab.binding.tado.internal.TadoBindingConstants.THING_TYPE_HOME;

import java.util.Set;
import java.util.regex.Pattern;

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
 * Discovers Tado Internet Bridges by means of mDNS
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@Component(configurationPid = "discovery.tado")
@NonNullByDefault
public class TadoDiscoveryParticipant implements MDNSDiscoveryParticipant {

    private static final String SERVICE_TYPE = "_hap._tcp.local.";
    private static final String TADO_INTERNET_BRIDGE = "tado Internet Bridge";
    private static final String DISCOVERY_BRIDGE_LABEL_KEY = "discovery.bridge.label";

    public static final Pattern VALID_IP_V4_ADDRESS = Pattern
            .compile("\\b((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)(\\.|$)){4}\\b");

    private final Logger logger = LoggerFactory.getLogger(TadoDiscoveryParticipant.class);

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return Set.of(THING_TYPE_HOME);
    }

    @Override
    public String getServiceType() {
        return SERVICE_TYPE;
    }

    @Override
    public @Nullable DiscoveryResult createResult(ServiceInfo service) {
        ThingUID thingUID = getThingUID(service);
        if (thingUID != null) {
            String ipAddress = thingUID.getId().replace("_", ".");
            DiscoveryResult hub = DiscoveryResultBuilder.create(thingUID)
                    .withLabel(String.format("@text/%s [\"%s\"]", DISCOVERY_BRIDGE_LABEL_KEY, ipAddress)).build();
            logger.debug("mDNS discovered tado° internet bridge '{}' on '{}'", thingUID, ipAddress);
            return hub;
        }
        return null;
    }

    @Override
    public @Nullable ThingUID getThingUID(ServiceInfo service) {
        if (service.getName().startsWith(TADO_INTERNET_BRIDGE)) {
            for (String host : service.getHostAddresses()) {
                if (VALID_IP_V4_ADDRESS.matcher(host).matches()) {
                    return new ThingUID(THING_TYPE_HOME, host.replace('.', '_'));
                }
            }
        }
        return null;
    }
}

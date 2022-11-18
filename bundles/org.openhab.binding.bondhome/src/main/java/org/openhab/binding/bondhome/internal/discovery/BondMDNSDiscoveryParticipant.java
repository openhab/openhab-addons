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
package org.openhab.binding.bondhome.internal.discovery;

import static org.openhab.binding.bondhome.internal.BondHomeBindingConstants.*;
import static org.openhab.core.thing.Thing.*;

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
 * This class identifies Bond Bridges by their mDNS service information.
 *
 * @author Sara Geleskie Damiano - Initial contribution
 */
@Component(service = MDNSDiscoveryParticipant.class, configurationPid = "discovery.mdns.bondhome")
@NonNullByDefault
public class BondMDNSDiscoveryParticipant implements MDNSDiscoveryParticipant {

    private final Logger logger = LoggerFactory.getLogger(BondMDNSDiscoveryParticipant.class);

    private static final String SERVICE_TYPE = "_bond._tcp.local.";

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return SUPPORTED_BRIDGE_TYPES;
    }

    @Override
    public String getServiceType() {
        return SERVICE_TYPE;
    }

    @Override
    public @Nullable ThingUID getThingUID(@Nullable ServiceInfo service) {
        if (service != null) {
            return new ThingUID(THING_TYPE_BOND_BRIDGE, service.getName());
        }
        return null;
    }

    @Override
    public @Nullable DiscoveryResult createResult(ServiceInfo service) {
        ThingUID thingUID = getThingUID(service);
        if (thingUID != null) {
            logger.debug("Discovered Bond Bridge: {}", service);
            return DiscoveryResultBuilder.create(thingUID).withProperty(PROPERTY_SERIAL_NUMBER, service.getName())
                    .withLabel("@text/discovery.bridge.label").withRepresentationProperty(PROPERTY_SERIAL_NUMBER)
                    .build();
        }
        return null;
    }
}

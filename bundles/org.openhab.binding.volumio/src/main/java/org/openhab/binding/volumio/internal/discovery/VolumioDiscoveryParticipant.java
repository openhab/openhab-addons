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
package org.openhab.binding.volumio.internal.discovery;

import static org.openhab.binding.volumio.internal.VolumioBindingConstants.THING_TYPE_VOLUMIO;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.jmdns.ServiceInfo;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.volumio.internal.VolumioBindingConstants;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.mdns.MDNSDiscoveryParticipant;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Patrick Sernetz - Initial contribution
 * @author Chris Wohlbrecht - Adaption for openHAB 3
 * @author Michael Loercher - Adaption for openHAB 3
 */
@NonNullByDefault
@Component(configurationPid = "discovery.volumio")
public class VolumioDiscoveryParticipant implements MDNSDiscoveryParticipant {

    private final Logger logger = LoggerFactory.getLogger(VolumioDiscoveryParticipant.class);

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return Set.of(THING_TYPE_VOLUMIO);
    }

    @Override
    public String getServiceType() {
        return VolumioBindingConstants.DISCOVERY_SERVICE_TYPE;
    }

    @Override
    public @Nullable DiscoveryResult createResult(ServiceInfo serviceInfo) {
        String volumioName = serviceInfo.getPropertyString(VolumioBindingConstants.DISCOVERY_NAME_PROPERTY);
        Map<String, Object> properties = new HashMap<>();
        ThingUID thingUID = getThingUID(serviceInfo);

        logger.debug("Service Device: {}", serviceInfo);
        logger.debug("Thing UID: {}", thingUID);

        DiscoveryResult discoveryResult = null;
        if (thingUID != null) {
            properties.put("hostname", serviceInfo.getServer());
            properties.put("port", serviceInfo.getPort());
            properties.put("protocol", "http");

            discoveryResult = DiscoveryResultBuilder.create(thingUID).withProperties(properties).withLabel(volumioName)
                    .build();
            logger.debug("DiscoveryResult: {}", discoveryResult);
        }
        return discoveryResult;
    }

    @Override
    public @Nullable ThingUID getThingUID(ServiceInfo serviceInfo) {
        Collections.list(serviceInfo.getPropertyNames()).forEach(s -> logger.debug("PropertyName: {}", s));

        String volumioName = serviceInfo.getPropertyString("volumioName");
        if (volumioName == null) {
            return null;
        }

        String uuid = serviceInfo.getPropertyString("UUID");
        if (uuid == null) {
            return null;
        }

        String uuidAndServername = String.format("%s-%s", uuid, volumioName);
        logger.debug("return new ThingUID({}, {});", THING_TYPE_VOLUMIO, uuidAndServername);
        return new ThingUID(THING_TYPE_VOLUMIO, uuidAndServername);
    }
}

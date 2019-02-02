/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.nanoleaf.internal.discovery;

import static org.openhab.binding.nanoleaf.internal.NanoleafBindingConstants.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.jmdns.ServiceInfo;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.mdns.MDNSDiscoveryParticipant;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.nanoleaf.internal.NanoleafBindingConstants;
import org.openhab.binding.nanoleaf.internal.NanoleafHandlerFactory;
import org.openhab.binding.nanoleaf.internal.OpenAPIUtils;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link NanoleafMDNSDiscoveryParticipant} is responsible for discovering new Nanoleaf controllers (bridges).
 *
 * @author Martin Raepple - Initial contribution
 */

@Component(immediate = true, configurationPid = "discovery.nanoleaf")
@NonNullByDefault
public class NanoleafMDNSDiscoveryParticipant implements MDNSDiscoveryParticipant {

    // see http://forum.nanoleaf.me/docs/openapi#_gf9l5guxt8r0
    private static final String SERVICE_TYPE = "_nanoleafapi._tcp.local.";

    private final Logger logger = LoggerFactory.getLogger(NanoleafMDNSDiscoveryParticipant.class);

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return NanoleafHandlerFactory.SUPPORTED_THING_TYPES_UIDS;
    }

    @Override
    public String getServiceType() {
        return SERVICE_TYPE;
    }

    @Override
    public @Nullable DiscoveryResult createResult(ServiceInfo service) {
        final ThingUID uid = getThingUID(service);
        if (uid == null) {
            return null;
        }
        final Map<String, Object> properties = new HashMap<>(2);
        String host = service.getHostAddresses()[0];
        properties.put(CONFIG_ADDRESS, host);
        int port = service.getPort();
        properties.put(CONFIG_PORT, port);
        String firmwareVersion = service.getPropertyString("srcvers");

        logger.debug("Nanoleaf controller with FW version {} found at {} {}", firmwareVersion, host, port);

        if (OpenAPIUtils.checkRequiredFirmware(firmwareVersion)) {
            final DiscoveryResult result = DiscoveryResultBuilder.create(uid).withThingType(getThingType(service))
                    .withProperties(properties).withLabel("Nanoleaf Controller").build();
            logger.debug("Nanoleaf controller added to inbox: {} at {}", uid.getId(), host);
            return result;
        } else {
            logger.error("Nanoleaf controller firmware is too old: {}. Must be equal or higher than {}",
                    firmwareVersion, NanoleafBindingConstants.API_MIN_FW_VER);
            return null;
        }
    }

    @Override
    public @Nullable ThingUID getThingUID(ServiceInfo service) {
        ThingTypeUID thingTypeUID = getThingType(service);
        if (thingTypeUID != null) {
            String id = service.getPropertyString("id").replace(":", "");
            return new ThingUID(thingTypeUID, id);
        } else {
            return null;
        }
    }

    private @Nullable ThingTypeUID getThingType(final ServiceInfo service) {
        String model = service.getPropertyString("md"); // model
        logger.debug("Nanoleaf Type: {}", model);
        if (model == null) {
            return null;
        }
        return THING_TYPE_CONTROLLER;
    }
}

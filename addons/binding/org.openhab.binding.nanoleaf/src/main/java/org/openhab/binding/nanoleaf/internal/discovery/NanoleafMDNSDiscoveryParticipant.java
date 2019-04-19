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
import org.eclipse.smarthome.core.thing.Thing;
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
        properties.put(Thing.PROPERTY_FIRMWARE_VERSION, firmwareVersion);
        properties.put(Thing.PROPERTY_MODEL_ID, service.getPropertyString("md"));
        properties.put(Thing.PROPERTY_VENDOR, "Nanoleaf");

        logger.debug("Adding Nanoleaf controller with FW version {} found at {} {} to inbox", firmwareVersion, host,
                port);
        if (!OpenAPIUtils.checkRequiredFirmware(firmwareVersion)) {
            logger.warn("Nanoleaf controller firmware is too old. Must be {} or higher",
                    NanoleafBindingConstants.API_MIN_FW_VER);
        }
        final DiscoveryResult result = DiscoveryResultBuilder.create(uid).withThingType(getThingType(service))
                .withProperties(properties).withLabel(service.getName()).withRepresentationProperty(CONFIG_ADDRESS)
                .build();
        return result;
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

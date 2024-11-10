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
package org.openhab.binding.nanoleaf.internal.discovery;

import static org.openhab.binding.nanoleaf.internal.NanoleafBindingConstants.*;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.jmdns.ServiceInfo;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.nanoleaf.internal.NanoleafHandlerFactory;
import org.openhab.binding.nanoleaf.internal.OpenAPIUtils;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.mdns.MDNSDiscoveryParticipant;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link NanoleafMDNSDiscoveryParticipant} is responsible for discovering new Nanoleaf controllers (bridges).
 *
 * @author Martin Raepple - Initial contribution
 * @author Stefan Höhn - further improvements for static defined things
 * @see <a href="https://openhab.org/documentation/development/bindings/discovery-services.html">MSDN
 *      Discovery</a>
 */
@Component(configurationPid = "discovery.nanoleaf")
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
        try {
            if (InetAddress.getByName(host).getAddress().length != 4) {
                logger.debug("Ignoring IPv6 address for nanoleaf controllers: {}", host);
                return null;
            }
        } catch (UnknownHostException e) {
            logger.warn("Error while checking IP address for nanoleaf controller: {}", host);
            return null;
        }
        properties.put(CONFIG_ADDRESS, host);
        int port = service.getPort();
        properties.put(CONFIG_PORT, port);
        String firmwareVersion = service.getPropertyString("srcvers");
        properties.put(Thing.PROPERTY_FIRMWARE_VERSION, firmwareVersion);
        String modelId = service.getPropertyString("md");
        properties.put(Thing.PROPERTY_MODEL_ID, modelId);
        properties.put(Thing.PROPERTY_VENDOR, "Nanoleaf");
        String qualifiedName = service.getQualifiedName();
        logger.debug("Device found: {}", qualifiedName);

        logger.trace("Discovered nanoleaf host: {} port: {} firmWare: {} modelId: {} qualifiedName: {}", host, port,
                firmwareVersion, modelId, qualifiedName);
        logger.debug("Adding Nanoleaf controller {} with FW version {} found at {}:{} to inbox", qualifiedName,
                firmwareVersion, host, port);
        if (!OpenAPIUtils.checkRequiredFirmware(service.getPropertyString("md"), firmwareVersion)) {
            logger.debug("Nanoleaf controller firmware is too old. Must be {} or higher",
                    MODEL_ID_LIGHTPANELS.equals(modelId) ? API_MIN_FW_VER_LIGHTPANELS : API_MIN_FW_VER_CANVAS);
        }

        return DiscoveryResultBuilder.create(uid).withThingType(getThingType(service)).withProperties(properties)
                .withLabel(service.getName()).withRepresentationProperty(CONFIG_ADDRESS).build();
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

/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.roku.internal.discovery;

import static org.openhab.binding.roku.internal.RokuBindingConstants.*;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.jupnp.model.meta.RemoteDevice;
import org.openhab.binding.roku.internal.RokuHttpException;
import org.openhab.binding.roku.internal.communication.RokuCommunicator;
import org.openhab.binding.roku.internal.dto.DeviceInfo;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.upnp.UpnpDiscoveryParticipant;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * Discovery Service for Roku devices.
 *
 * @author Michael Lobstein - Initial contribution
 *
 */
@NonNullByDefault
@Component(immediate = true)
public class RokuDiscoveryParticipant implements UpnpDiscoveryParticipant {

    private final Logger logger = LoggerFactory.getLogger(RokuDiscoveryParticipant.class);
    private final HttpClient httpClient;

    private static final String ROKU_COM = "roku-com";
    private static final String ROKU_TV = "Roku TV";

    @Activate
    public RokuDiscoveryParticipant(@Reference HttpClientFactory httpClientFactory) {
        this.httpClient = httpClientFactory.getCommonHttpClient();
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return SUPPORTED_THING_TYPES_UIDS;
    }

    @Override
    public @Nullable DiscoveryResult createResult(RemoteDevice device) {
        final ThingUID uid = getThingUID(device);
        if (uid != null) {
            final Map<String, Object> properties = new HashMap<>(3);
            final String host = device.getIdentity().getDescriptorURL().getHost();
            final int port = device.getIdentity().getDescriptorURL().getPort();
            String label;

            try {
                final DeviceInfo deviceInfo = new RokuCommunicator(httpClient, host, port).getDeviceInfo();

                // replace extraneous characters with spaces and remove any consecutive spaces
                label = (deviceInfo.getFriendlyModelName() + " " + deviceInfo.getUserDeviceLocation())
                        .replaceAll("[^a-zA-Z0-9\\-_]", " ").trim().replaceAll("  +", " ");
            } catch (RokuHttpException e) {
                logger.debug("Unable to retrieve Roku device-info. Exception: {}", e.getMessage(), e);
                label = device.getDetails().getFriendlyName() + " "
                        + device.getDetails().getModelDetails().getModelName().trim();
            }

            properties.put(PROPERTY_UUID, uid.getId());
            properties.put(PROPERTY_HOST_NAME, host);
            properties.put(PROPERTY_PORT, port);

            final DiscoveryResult result = DiscoveryResultBuilder.create(uid).withProperties(properties)
                    .withRepresentationProperty(PROPERTY_UUID).withLabel(label).build();

            logger.debug("Created a DiscoveryResult for device '{}' with UID '{}'", label, uid.getId());
            return result;
        } else {
            return null;
        }
    }

    @Override
    public @Nullable ThingUID getThingUID(RemoteDevice device) {
        if (ROKU_COM.equals(device.getType().getNamespace())) {
            logger.debug("Roku UPnP device found at {}", device.getIdentity().getDescriptorURL().getHost());
            final String serialNumber = device.getDetails().getSerialNumber();

            if (serialNumber == null || serialNumber.isBlank()) {
                logger.debug("Roku UPnP device has null serial number!");
                return null;
            }

            final String id = serialNumber.toLowerCase(Locale.ENGLISH);
            if (device.getDetails().getFriendlyName().contains(ROKU_TV)) {
                return new ThingUID(THING_TYPE_ROKU_TV, id);
            } else {
                return new ThingUID(THING_TYPE_ROKU_PLAYER, id);
            }
        }

        return null;
    }
}

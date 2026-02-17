/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.linkplay.internal.discovery;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.jupnp.model.meta.RemoteDevice;
import org.jupnp.model.meta.Service;
import org.jupnp.model.types.ServiceId;
import org.jupnp.model.types.UDAServiceId;
import org.openhab.binding.linkplay.internal.LinkPlayBindingConstants;
import org.openhab.binding.linkplay.internal.client.http.LinkPlayConnectionUtils;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.upnp.UpnpDiscoveryParticipant;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A UPnP discovery participant for LinkPlay devices.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
@Component(service = UpnpDiscoveryParticipant.class)
public class LinkPlayUpnpDiscoveryParticipant implements UpnpDiscoveryParticipant {

    private final Logger logger = LoggerFactory.getLogger(LinkPlayUpnpDiscoveryParticipant.class);

    private static final int TTL_SECONDS = 300;
    private static final ServiceId SERVICE_ID_AV_TRANSPORT = new UDAServiceId("AVTransport");
    private static final ServiceId SERVICE_ID_RENDERING_CONTROL = new UDAServiceId("RenderingControl");

    private final HttpClient httpClient;

    @Activate
    public LinkPlayUpnpDiscoveryParticipant() {
        try {
            httpClient = new HttpClient(new SslContextFactory.Client(true));
            httpClient.start();
        } catch (Exception e) {
            throw new IllegalStateException("Could not create HTTP client", e);
        }
    }

    @Deactivate
    public void deactivate() {
        try {
            httpClient.stop();
        } catch (Exception e) {
            logger.debug("Failed to stop HTTP client: {}", e.getMessage(), e);
        }
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return LinkPlayBindingConstants.SUPPORTED_THING_TYPES_UIDS;
    }

    @Override
    public @Nullable ThingUID getThingUID(RemoteDevice device) {
        if (!hasRequiredServices(device)) {
            return null;
        }
        String udnIdentifier = device.getIdentity().getUdn().getIdentifierString();
        if (udnIdentifier == null || udnIdentifier.isEmpty()) {
            return null;
        }
        String udn = udnIdentifier.replace("uuid:", "").replace("-", "");
        return new ThingUID(LinkPlayBindingConstants.THING_TYPE_PLAYER, udn);
    }

    @Override
    public @Nullable DiscoveryResult createResult(RemoteDevice device) {
        ThingUID thingUID = getThingUID(device);
        if (thingUID == null) {
            return null;
        }

        String host = device.getIdentity().getDescriptorURL().getHost();
        if (host == null || host.isEmpty()) {
            logger.trace("no host for device {}", device.getDetails().getFriendlyName());
            return null;
        }

        Integer port = LinkPlayConnectionUtils.testConnection(httpClient, host);
        if (port == null) {
            return null;
        }

        String friendlyName = device.getDetails().getFriendlyName();
        String deviceUDN = device.getIdentity().getUdn().getIdentifierString().replace("uuid:", "");

        Map<String, Object> properties = new HashMap<>();
        properties.put(LinkPlayBindingConstants.CONFIG_UDN, deviceUDN);

        String label = String.format("LinkPlay: %s", friendlyName);
        logger.debug("Building discovery result for {}: label={}, properties={}", thingUID, label, properties);

        return DiscoveryResultBuilder.create(thingUID).withLabel(label).withProperties(properties)
                .withRepresentationProperty(LinkPlayBindingConstants.CONFIG_UDN).withTTL(TTL_SECONDS).build();
    }

    private boolean hasRequiredServices(@Nullable RemoteDevice device) {
        if (device == null) {
            return false;
        }

        Service<?, ?> avTransportService = device.findService(SERVICE_ID_AV_TRANSPORT);
        Service<?, ?> renderingControlService = device.findService(SERVICE_ID_RENDERING_CONTROL);

        boolean hasRequiredServices = (avTransportService != null && renderingControlService != null);
        logger.trace("Device {} has required services: AVTransport={}, RenderingControl={}",
                device.getDetails().getFriendlyName(), avTransportService != null, renderingControlService != null);

        return hasRequiredServices;
    }
}

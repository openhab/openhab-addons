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
package org.openhab.binding.hasslink.internal.discovery;

import static org.openhab.binding.hasslink.internal.HassLinkBindingConstants.THING_TYPE_BRIDGE;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.jmdns.ServiceInfo;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hasslink.internal.HassLinkBindingConstants;
import org.openhab.binding.hasslink.internal.util.EndpointUtils;
import org.openhab.binding.hasslink.internal.util.EntityUtils;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.mdns.MDNSDiscoveryParticipant;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;

/**
 * Discovers Home Assistant servers advertised via mDNS.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
@Component(service = { MDNSDiscoveryParticipant.class }, immediate = true, property = { "class.id=hasslink" })
public class HassLinkMDNSDiscoveryParticipant implements MDNSDiscoveryParticipant {
    private static final String SERVICE_TYPE = "_home-assistant._tcp.local.";

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return Set.of(THING_TYPE_BRIDGE);
    }

    @Override
    public String getServiceType() {
        return SERVICE_TYPE;
    }

    @Override
    public @Nullable DiscoveryResult createResult(ServiceInfo service) {
        if (service.getHostAddresses().length == 0) {
            return null;
        }

        ThingUID thingUid = getThingUID(service);
        if (thingUid == null) {
            return null;
        }

        String url = service.getPropertyString("internal_url");
        if (url == null) {
            url = service.getPropertyString("base_url");
        }

        String host = service.getHostAddresses()[0];
        int port = service.getPort();

        String serverEndpoint = EndpointUtils.normalizeEndpoint(host, port);

        Map<String, Object> properties = new HashMap<>();
        properties.put("host", host);
        properties.put("port", port);
        properties.put("secure", url != null && url.startsWith("https://"));
        properties.put(HassLinkBindingConstants.ENDPOINT_PROPERTY, serverEndpoint);

        String location = service.getPropertyString("location_name");
        String rawName = service.getName() != null ? service.getName().split("\\.")[0] : null;

        String displayName = (location != null && !location.isBlank()) ? location : rawName;

        String label = "Home Assistant";
        if (displayName != null && !displayName.isBlank() && !label.equalsIgnoreCase(displayName.trim())) {
            label = "Home Assistant (" + displayName.trim() + ")";
        }

        return DiscoveryResultBuilder.create(thingUid) //
                .withProperties(properties) //
                .withLabel(label) //
                .withRepresentationProperty(HassLinkBindingConstants.ENDPOINT_PROPERTY) //
                .build();
    }

    @Override
    public @Nullable ThingUID getThingUID(ServiceInfo service) {
        String uuid = service.getPropertyString("uuid");
        if (uuid != null && !uuid.isBlank()) {
            return new ThingUID(THING_TYPE_BRIDGE, EntityUtils.sanitize(uuid));
        }
        return null;
    }
}

/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.snapcast.internal.discovery;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.jmdns.ServiceInfo;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.mdns.MDNSDiscoveryParticipant;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.snapcast.internal.SnapcastBindingConstants;
import org.osgi.service.component.annotations.Component;

/**
 * Discovery service for snapcast servers
 *
 * @author Steffen Brandemann - Initial contribution
 */
@Component(service = MDNSDiscoveryParticipant.class, immediate = true)
public class ServerDiscoveryService implements MDNSDiscoveryParticipant {

    public ServerDiscoveryService() {
    }

    @Override
    public Set<@NonNull ThingTypeUID> getSupportedThingTypeUIDs() {
        return Collections.singleton(SnapcastBindingConstants.THING_TYPE_SERVER);
    }

    @Override
    public String getServiceType() {
        return "_snapcast-jsonrpc._tcp.local.";
    }

    @Override
    public @Nullable DiscoveryResult createResult(ServiceInfo service) {
        DiscoveryResult result = null;

        ThingUID uid = getThingUID(service);
        String label = getLabel(service);
        Map<String, Object> properties = getProperties(service);

        if (uid != null && properties != null) {
            result = DiscoveryResultBuilder.create(uid).withProperties(properties).withLabel(label).build();
        }

        return result;
    }

    @Override
    public @Nullable ThingUID getThingUID(ServiceInfo service) {
        String name = service.getName();
        if (name != null) {
            return new ThingUID(SnapcastBindingConstants.THING_TYPE_SERVER, name.replaceAll("[#\\s]", ""));
        } else {
            return null;
        }
    }

    private String getLabel(ServiceInfo service) {
        return service.getName();
    }

    private Map<String, Object> getProperties(ServiceInfo service) {
        final String[] hostAddresses = service.getHostAddresses();
        if (hostAddresses != null && hostAddresses.length > 0) {
            Map<String, Object> properties = new HashMap<>();
            properties.put(SnapcastBindingConstants.CONFIG_SERVER_HOST, hostAddresses[0]);
            properties.put(SnapcastBindingConstants.CONFIG_SERVER_PORT, service.getPort());
            return properties;
        } else {
            return null;
        }
    }

}

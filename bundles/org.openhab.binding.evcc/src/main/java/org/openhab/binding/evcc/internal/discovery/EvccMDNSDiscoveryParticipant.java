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
package org.openhab.binding.evcc.internal.discovery;

import static org.openhab.binding.evcc.internal.EvccBindingConstants.CONFIG_HOST;
import static org.openhab.binding.evcc.internal.EvccBindingConstants.CONFIG_PORT;
import static org.openhab.binding.evcc.internal.EvccBindingConstants.THING_TYPE_SERVER;

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

/**
 * The {@link EvccMDNSDiscoveryParticipant} is responsible for scanning the network for evcc instances
 *
 * @author Marcel Goerentz - Initial contribution
 */
@NonNullByDefault
@Component(service = MDNSDiscoveryParticipant.class, configurationPid = "mdns-discovery.evcc")
public class EvccMDNSDiscoveryParticipant implements MDNSDiscoveryParticipant {

    private static final String SERVICE_TYPE = "_http._tcp.local.";
    private static final String SERVICE_NAME = "evcc";
    private static final int DEFAULT_PORT = 7070;

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return Set.of(THING_TYPE_SERVER);
    }

    @Override
    public String getServiceType() {
        return SERVICE_TYPE;
    }

    @Override
    public @Nullable DiscoveryResult createResult(ServiceInfo serviceInfo) {
        ThingUID uid = getThingUID(serviceInfo);
        if (uid == null) {
            return null;
        }
        String host = hostname(serviceInfo);
        int port = serviceInfo.getPort();
        if (port <= 0) {
            port = DEFAULT_PORT;
        }
        return DiscoveryResultBuilder.create(uid).withLabel("evcc instance (" + host + ")")
                .withProperty(CONFIG_HOST, host).withProperty(CONFIG_PORT, port).withRepresentationProperty(CONFIG_HOST)
                .build();
    }

    @Override
    public @Nullable ThingUID getThingUID(ServiceInfo serviceInfo) {
        if (!SERVICE_NAME.equalsIgnoreCase(serviceInfo.getName())) {
            return null;
        }
        String host = hostname(serviceInfo);
        if (host.isEmpty()) {
            return null;
        }
        return new ThingUID(THING_TYPE_SERVER, host.replaceAll("[^A-Za-z0-9_]", "_"));
    }

    /**
     * Returns evcc's advertised mDNS hostname (for example {@code evcc.local}) without the trailing dot.
     * <p>
     * The hostname is preferred over the raw IP address because it is stable across DHCP lease changes, so a bridge
     * configured with it keeps working after evcc's address changes. evcc always advertises the fixed service instance
     * name {@code evcc} and carries no unique identifier, so the hostname is also the only host-specific value
     * available.
     *
     * @param serviceInfo the discovered mDNS service
     * @return the hostname without a trailing dot, or an empty string if none is advertised
     */
    private String hostname(ServiceInfo serviceInfo) {
        String server = serviceInfo.getServer();
        return server == null ? "" : server.replaceAll("\\.+$", "");
    }

    @Override
    public long getRemovalGracePeriodSeconds(ServiceInfo service) {
        return 0L;
    }
}

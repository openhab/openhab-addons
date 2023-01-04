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
package org.openhab.binding.unifi.internal.api.cache;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.unifi.internal.api.dto.UniFiClient;
import org.openhab.binding.unifi.internal.api.dto.UniFiDevice;
import org.openhab.binding.unifi.internal.api.dto.UniFiPortTuple;
import org.openhab.binding.unifi.internal.api.dto.UniFiSite;
import org.openhab.binding.unifi.internal.api.dto.UniFiSwitchPorts;
import org.openhab.binding.unifi.internal.api.dto.UniFiWlan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to manager cache for the controller keeping track of all specific cache objects.
 *
 * @author Matthew Bowman - Initial contribution
 * @author Hilbrand Bouwkamp - Moved cache to this dedicated class.
 */
@NonNullByDefault
public class UniFiControllerCache {

    private final Logger logger = LoggerFactory.getLogger(UniFiControllerCache.class);

    private final UniFiSiteCache sitesCache = new UniFiSiteCache();
    private final UniFiWlanCache wlansCache = new UniFiWlanCache();
    private final UniFiDeviceCache devicesCache = new UniFiDeviceCache();
    private final UniFiClientCache clientsCache = new UniFiClientCache();
    private final UniFiClientCache insightsCache = new UniFiClientCache();
    private final Map<String, UniFiSwitchPorts> devicesToPortTables = new ConcurrentHashMap<>();

    public void clear() {
        sitesCache.clear();
        wlansCache.clear();
        devicesCache.clear();
        clientsCache.clear();
        insightsCache.clear();
    }

    // Sites Cache

    public List<UniFiSite> setSites(final UniFiSite @Nullable [] sites) {
        sitesCache.putAll(sites);
        return List.of(sites);
    }

    public @Nullable UniFiSite getSite(final @Nullable String id) {
        return sitesCache.get(id);
    }

    public Collection<UniFiSite> getSites() {
        return sitesCache.values();
    }

    // Wlans Cache

    public void putWlans(final UniFiWlan @Nullable [] wlans) {
        wlansCache.putAll(wlans);
    }

    public @Nullable UniFiWlan getWlan(@Nullable final String id) {
        return wlansCache.get(id);
    }

    public Collection<UniFiWlan> getWlans() {
        return wlansCache.values();
    }

    // Devices Cache

    public void putDevices(final UniFiDevice @Nullable [] devices) {
        devicesCache.putAll(devices);
        if (devices != null) {
            Stream.of(devices).filter(Objects::nonNull).forEach(d -> {
                Stream.ofNullable(d.getPortTable()).forEach(pt -> {
                    final UniFiSwitchPorts switchPorts = devicesToPortTables.computeIfAbsent(d.getMac(),
                            p -> new UniFiSwitchPorts());

                    Stream.of(pt).forEach(p -> {
                        @SuppressWarnings("null")
                        final UniFiPortTuple tuple = switchPorts.computeIfAbsent(p.getPortIdx());

                        tuple.setDevice(d);
                        tuple.setTable(p);
                    });
                });
                Stream.ofNullable(d.getPortOverrides()).forEach(po -> {
                    final UniFiSwitchPorts tupleTable = devicesToPortTables.get(d.getMac());

                    if (tupleTable != null) {
                        Stream.of(po).forEach(p -> tupleTable.setOverride(p));
                    }
                });
            });
        }
    }

    public @Nullable UniFiDevice getDevice(@Nullable final String id) {
        return devicesCache.get(id);
    }

    public UniFiSwitchPorts getSwitchPorts(@Nullable final String deviceId) {
        return deviceId == null ? new UniFiSwitchPorts()
                : devicesToPortTables.getOrDefault(deviceId, new UniFiSwitchPorts());
    }

    public Collection<UniFiSwitchPorts> getSwitchPorts() {
        return devicesToPortTables.values();
    }

    // Clients Cache

    public void putClients(final UniFiClient @Nullable [] clients) {
        clientsCache.putAll(clients);
    }

    public Collection<UniFiClient> getClients() {
        return clientsCache.values();
    }

    public long countClients(final UniFiSite site, final Function<UniFiClient, Boolean> filter) {
        return getClients().stream().filter(c -> site.isSite(c.getSite())).filter(filter::apply).count();
    }

    public @Nullable UniFiClient getClient(@Nullable final String cid) {
        UniFiClient client = null;
        if (cid != null && !cid.isBlank()) {
            synchronized (this) {
                // mgb: first check active clients and fallback to insights if not found
                client = clientsCache.get(cid);
                if (client == null) {
                    final String id = clientsCache.getId(cid);

                    client = insightsCache.get(id == null ? cid : id);
                }
            }
            if (client == null) {
                logger.debug("Could not find a matching client for cid = {}", cid);
            }
        }
        return client;
    }

    public synchronized Stream<UniFiClient> getClientStreamForSite(final UniFiSite site) {
        return clientsCache.values().stream().filter(client -> client.getSite().equals(site));
    }

    // Insights Cache

    public void putInsights(final UniFiClient @Nullable [] insights) {
        insightsCache.putAll(insights);
    }
}

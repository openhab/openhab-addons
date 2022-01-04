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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.unifi.internal.api.model.UniFiClient;
import org.openhab.binding.unifi.internal.api.model.UniFiDevice;
import org.openhab.binding.unifi.internal.api.model.UniFiPortTable;
import org.openhab.binding.unifi.internal.api.model.UniFiSite;
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

    private UniFiSiteCache sitesCache = new UniFiSiteCache();
    private final UniFiDeviceCache devicesCache = new UniFiDeviceCache();
    private final UniFiClientCache clientsCache = new UniFiClientCache();
    private final UniFiClientCache insightsCache = new UniFiClientCache();
    private final Map<String, Map<Integer, UniFiPortTable>> devicesToPortTables = new ConcurrentHashMap<>();

    public void clear() {
        sitesCache.clear();
        devicesCache.clear();
        clientsCache.clear();
        insightsCache.clear();
    }

    // Sites Cache

    public List<UniFiSite> setSites(final UniFiSite @Nullable [] sites) {
        final UniFiSiteCache cache = new UniFiSiteCache();
        cache.putAll(sites);
        sitesCache = cache;
        return List.of(sites);
    }

    // Site Cache

    public @Nullable UniFiSite getSite(final @Nullable String id) {
        return sitesCache.get(id);
    }

    // Devices Cache

    public void putDevices(final UniFiDevice @Nullable [] devices) {
        devicesCache.putAll(devices);
        if (devices != null) {
            Stream.of(devices).filter(Objects::nonNull).forEach(d -> {
                Stream.ofNullable(d.getPortTable()).filter(ptl -> ptl.length > 0 && ptl[0].isPortPoe()).forEach(pt -> {
                    Stream.of(pt).forEach(p -> p.setDevice(d));
                    devicesToPortTables.put(d.getMac(),
                            Stream.of(pt).collect(Collectors.toMap(UniFiPortTable::getPortIdx, Function.identity())));
                });
            });
        }
    }

    public @Nullable UniFiDevice getDevice(@Nullable final String id) {
        return devicesCache.get(id);
    }

    public Map<Integer, UniFiPortTable> getSwitchPorts(@Nullable final String deviceId) {
        return deviceId == null ? Map.of() : devicesToPortTables.getOrDefault(deviceId, Map.of());
    }

    public Collection<Map<Integer, UniFiPortTable>> getSwitchPorts() {
        return devicesToPortTables.values();
    }

    // Clients Cache

    public void putClients(final UniFiClient @Nullable [] clients) {
        clientsCache.putAll(clients);
    }

    public Collection<UniFiClient> getClients() {
        return clientsCache.values();
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

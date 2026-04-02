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
package org.openhab.binding.ddwrt.internal.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Central cache for all API objects discovered across the DD-WRT network.
 * All maps are keyed by the representation property (MAC, interfaceId, ruleId).
 *
 * Thread-safe via ConcurrentHashMap.
 *
 * @author Lee Ballard - Initial contribution
 */
@NonNullByDefault
public class DDWRTNetworkCache {

    private final Logger logger = Objects.requireNonNull(LoggerFactory.getLogger(DDWRTNetworkCache.class));

    private final Map<String, DDWRTBaseDevice> devicesByMac = new ConcurrentHashMap<>();
    private final Map<String, DDWRTWirelessClient> wirelessClientsByMac = new ConcurrentHashMap<>();
    private final Map<String, String> hostnameToMac = new ConcurrentHashMap<>();
    private final Map<String, DDWRTRadio> radiosByInterfaceId = new ConcurrentHashMap<>();
    private final Map<String, DDWRTFirewallRule> firewallRulesByRuleId = new ConcurrentHashMap<>();
    private final Map<String, DDWRTDhcpLease> dhcpLeasesByMac = new ConcurrentHashMap<>();
    private final Map<String, String> dhcpHostnameToMac = new ConcurrentHashMap<>();

    // MACs that have been merged away by MAC randomization detection.
    // These should not be re-created as wireless clients by refreshDhcpLeases.
    private final java.util.Set<String> mergedAwayMacs = ConcurrentHashMap.newKeySet();

    // Listeners keyed by normalized lookup key (lowercase hostname or MAC)
    private final Map<String, List<CacheChangeListener>> listenersByKey = new ConcurrentHashMap<>();

    // ---- Listeners ----

    /**
     * Register a listener to be notified when an entity matching the given key changes.
     * The key should be a lowercase hostname (for MAC-randomizing clients) or normalized MAC.
     */
    public void addChangeListener(String key, CacheChangeListener listener) {
        listenersByKey.computeIfAbsent(key.toLowerCase(Locale.ROOT), k -> new CopyOnWriteArrayList<>()).add(listener);
    }

    /**
     * Unregister a previously registered listener.
     */
    public void removeChangeListener(String key, CacheChangeListener listener) {
        List<CacheChangeListener> listeners = listenersByKey.get(key.toLowerCase(Locale.ROOT));
        if (listeners != null) {
            listeners.remove(listener);
            if (listeners.isEmpty()) {
                listenersByKey.remove(key.toLowerCase(Locale.ROOT), listeners);
            }
        }
    }

    /**
     * Fire change notifications for all keys associated with an entity.
     * Notifies listeners registered under the MAC and/or hostname.
     */
    private void fireChange(String mac, String hostname) {
        // Collect unique listeners across all keys to avoid notifying the same handler twice
        // (e.g. handler registered under both MAC and sanitized hostname)
        Set<CacheChangeListener> unique = Collections.newSetFromMap(new IdentityHashMap<>());
        collectListeners(mac, unique);
        if (!hostname.isEmpty()) {
            String sanitized = hostname.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
            collectListeners(hostname.toLowerCase(Locale.ROOT), unique);
            if (!sanitized.equals(hostname.toLowerCase(Locale.ROOT))) {
                collectListeners(sanitized, unique);
            }
        }
        for (CacheChangeListener listener : unique) {
            try {
                listener.onCacheChanged();
            } catch (Exception e) {
                logger.debug("Error notifying cache listener: {}", e.getMessage());
            }
        }
    }

    private void collectListeners(String key, Set<CacheChangeListener> dest) {
        List<CacheChangeListener> listeners = listenersByKey.get(key);
        if (listeners != null) {
            dest.addAll(listeners);
        }
    }

    private void notifyListenersForKey(String key) {
        List<CacheChangeListener> listeners = listenersByKey.get(key);
        if (listeners != null) {
            for (CacheChangeListener listener : listeners) {
                try {
                    listener.onCacheChanged();
                } catch (Exception e) {
                    logger.debug("Error notifying cache listener for key '{}': {}", key, e.getMessage());
                }
            }
        }
    }

    // ---- Devices ----

    public @Nullable DDWRTBaseDevice getDevice(String mac) {
        return devicesByMac.get(normalizeMac(mac));
    }

    public DDWRTBaseDevice putDevice(String mac, DDWRTBaseDevice device) {
        String key = normalizeMac(mac);
        DDWRTBaseDevice existing = devicesByMac.putIfAbsent(key, device);
        return existing != null ? existing : device;
    }

    public void replaceDevice(String mac, DDWRTBaseDevice device) {
        devicesByMac.put(normalizeMac(mac), device);
    }

    public List<DDWRTBaseDevice> getDevices() {
        return Objects.requireNonNull(Collections.unmodifiableList(new ArrayList<>(devicesByMac.values())));
    }

    public void removeDevice(String mac) {
        devicesByMac.remove(normalizeMac(mac));
    }

    // ---- Wireless Clients ----

    public @Nullable DDWRTWirelessClient getWirelessClient(String mac) {
        return wirelessClientsByMac.get(normalizeMac(mac));
    }

    public void putWirelessClient(String mac, DDWRTWirelessClient client) {
        String normalizedMac = normalizeMac(mac);
        wirelessClientsByMac.put(normalizedMac, client);
        // Maintain hostname index
        if (!client.getHostname().isEmpty()) {
            hostnameToMac.put(Objects.requireNonNull(client.getHostname().toLowerCase(Locale.ROOT)), normalizedMac);
        }
        fireChange(normalizedMac, client.getHostname());
    }

    /**
     * Thread-safe update of wireless client using compute pattern.
     * The mapping function is applied atomically with the existing client as input.
     */
    public DDWRTWirelessClient computeWirelessClient(String mac,
            java.util.function.Function<DDWRTWirelessClient, DDWRTWirelessClient> mappingFunction) {
        DDWRTWirelessClient result = Objects
                .requireNonNull(wirelessClientsByMac.compute(normalizeMac(mac), (key, existing) -> {
                    DDWRTWirelessClient client = existing != null ? existing : new DDWRTWirelessClient(key);
                    DDWRTWirelessClient updated = mappingFunction.apply(client);
                    // Maintain hostname index
                    if (!updated.getHostname().isEmpty()) {
                        hostnameToMac.put(Objects.requireNonNull(updated.getHostname().toLowerCase(Locale.ROOT)), key);
                    }
                    return updated;
                }));
        // Fire change notification after compute completes
        fireChange(normalizeMac(mac), result.getHostname());
        return result;
    }

    /**
     * Find a wireless client by hostname. Tries exact lowercase match first,
     * then falls back to sanitized form (letters and digits only) to match thing IDs.
     */
    public @Nullable DDWRTWirelessClient getWirelessClientByHostname(String hostname) {
        if (hostname.isEmpty()) {
            return null;
        }
        // Try exact lowercase match first (e.g., "lee-pixel-8a")
        String mac = hostnameToMac.get(hostname.toLowerCase(Locale.ROOT));
        if (mac != null) {
            return wirelessClientsByMac.get(mac);
        }
        // Try sanitized form to match thing IDs (e.g., "leepixel8a" -> "lee-pixel-8a")
        for (Map.Entry<String, String> entry : hostnameToMac.entrySet()) {
            if (entry.getKey().replaceAll("[^a-z0-9]", "").equals(hostname.toLowerCase(Locale.ROOT))) {
                return wirelessClientsByMac.get(entry.getValue());
            }
        }
        return null;
    }

    /**
     * Get the MAC address currently associated with a hostname.
     */
    public @Nullable String getMacForHostname(String hostname) {
        if (hostname.isEmpty()) {
            return null;
        }
        return hostnameToMac.get(hostname.toLowerCase(Locale.ROOT));
    }

    /**
     * Handle MAC randomization: when a new MAC appears with a hostname that already exists
     * under a different MAC, merge the old client data into the new one and remove the old entry.
     * Returns the old MAC if a merge occurred, null otherwise.
     */
    public @Nullable String mergeRandomizedMac(String newMac, String hostname) {
        if (hostname.isEmpty()) {
            return null;
        }
        String normalizedNewMac = normalizeMac(newMac);
        String oldMac = hostnameToMac.get(hostname.toLowerCase(Locale.ROOT));
        if (oldMac != null && !oldMac.equals(normalizedNewMac)) {
            DDWRTWirelessClient oldClient = wirelessClientsByMac.get(oldMac);
            if (oldClient != null && hostname.equalsIgnoreCase(oldClient.getHostname())) {
                logger.debug("MAC randomization detected for '{}': old MAC={}, new MAC={}", hostname, oldMac,
                        normalizedNewMac);

                // Carry AP info from old client to new client if new client lacks it
                DDWRTWirelessClient newClient = wirelessClientsByMac.get(normalizedNewMac);
                if (newClient != null && newClient.getApMac().isEmpty() && !oldClient.getApMac().isEmpty()) {
                    newClient.setApMac(oldClient.getApMac());
                    newClient.setIface(oldClient.getIface());
                    newClient.setRadioName(oldClient.getRadioName());
                    newClient.setSsid(oldClient.getSsid());
                    if (oldClient.getChannel() > 0) {
                        newClient.setChannel(oldClient.getChannel());
                    }
                    logger.debug("Carried AP info from old MAC {} to new MAC {}: ap={}, ssid={}", oldMac,
                            normalizedNewMac, oldClient.getApMac(), oldClient.getSsid());
                }

                // Track old MAC as merged-away so refreshDhcpLeases won't re-create it
                mergedAwayMacs.add(oldMac);
                // The new MAC is now active — un-merge it if it was previously merged away
                mergedAwayMacs.remove(normalizedNewMac);

                // Remove old entry and update hostname index
                wirelessClientsByMac.remove(oldMac);
                hostnameToMac.put(Objects.requireNonNull(hostname.toLowerCase(Locale.ROOT)), normalizedNewMac);

                // Notify listeners under both old and new MAC, and hostname
                fireChange(oldMac, hostname);
                fireChange(normalizedNewMac, hostname);
                return oldMac;
            }
        }
        return null;
    }

    /**
     * Check if a MAC has been merged away by MAC randomization detection.
     * Merged-away MACs should not be re-created as separate wireless client entries.
     */
    public boolean isMergedAwayMac(String mac) {
        return mergedAwayMacs.contains(normalizeMac(mac));
    }

    public List<DDWRTWirelessClient> getWirelessClients() {
        return Objects.requireNonNull(Collections.unmodifiableList(new ArrayList<>(wirelessClientsByMac.values())));
    }

    public void removeWirelessClient(String mac) {
        DDWRTWirelessClient removed = wirelessClientsByMac.remove(normalizeMac(mac));
        if (removed != null && !removed.getHostname().isEmpty()) {
            // Only remove from hostname index if it still points to this MAC
            hostnameToMac.remove(removed.getHostname().toLowerCase(Locale.ROOT), normalizeMac(mac));
        }
    }

    // ---- Radios ----

    public @Nullable DDWRTRadio getRadio(String interfaceId) {
        return radiosByInterfaceId.get(interfaceId.toLowerCase(Locale.ROOT));
    }

    public void putRadio(String interfaceId, DDWRTRadio radio) {
        radiosByInterfaceId.put(Objects.requireNonNull(interfaceId.toLowerCase(Locale.ROOT)), radio);
        notifyListenersForKey(interfaceId.toLowerCase(Locale.ROOT));
    }

    public List<DDWRTRadio> getRadios() {
        return Objects.requireNonNull(Collections.unmodifiableList(new ArrayList<>(radiosByInterfaceId.values())));
    }

    public void removeRadio(String interfaceId) {
        radiosByInterfaceId.remove(interfaceId.toLowerCase(Locale.ROOT));
    }

    // ---- Firewall Rules ----

    public @Nullable DDWRTFirewallRule getFirewallRule(String ruleId) {
        return firewallRulesByRuleId.get(ruleId);
    }

    public void putFirewallRule(String ruleId, DDWRTFirewallRule rule) {
        firewallRulesByRuleId.put(ruleId, rule);
    }

    public List<DDWRTFirewallRule> getFirewallRules() {
        return Objects.requireNonNull(Collections.unmodifiableList(new ArrayList<>(firewallRulesByRuleId.values())));
    }

    public void removeFirewallRule(String ruleId) {
        firewallRulesByRuleId.remove(ruleId);
    }

    // ---- DHCP Leases ----

    public @Nullable DDWRTDhcpLease getDhcpLease(String mac) {
        return dhcpLeasesByMac.get(normalizeMac(mac));
    }

    public void putDhcpLease(String mac, DDWRTDhcpLease lease) {
        String normalizedMac = normalizeMac(mac);
        dhcpLeasesByMac.put(normalizedMac, lease);
        // Maintain hostname index for DHCP leases
        if (!lease.getHostname().isEmpty()) {
            dhcpHostnameToMac.put(Objects.requireNonNull(lease.getHostname().toLowerCase(Locale.ROOT)), normalizedMac);
        }
    }

    /**
     * Find a DHCP lease by hostname. Returns null if no lease has this hostname.
     */
    public @Nullable DDWRTDhcpLease getDhcpLeaseByHostname(String hostname) {
        if (hostname.isEmpty()) {
            return null;
        }
        String mac = dhcpHostnameToMac.get(hostname.toLowerCase(Locale.ROOT));
        return mac != null ? dhcpLeasesByMac.get(mac) : null;
    }

    public List<DDWRTDhcpLease> getDhcpLeases() {
        return Objects.requireNonNull(Collections.unmodifiableList(new ArrayList<>(dhcpLeasesByMac.values())));
    }

    public void clearDhcpLeases() {
        dhcpLeasesByMac.clear();
        dhcpHostnameToMac.clear();
    }

    // ---- Aggregate counts ----

    public int getTotalWirelessClients() {
        return (int) wirelessClientsByMac.values().stream().filter(DDWRTWirelessClient::isOnline).count();
    }

    public int getTotalDevices() {
        return devicesByMac.size();
    }

    // ---- Clear ----

    public void clearAll() {
        devicesByMac.clear();
        wirelessClientsByMac.clear();
        hostnameToMac.clear();
        radiosByInterfaceId.clear();
        firewallRulesByRuleId.clear();
        dhcpLeasesByMac.clear();
        dhcpHostnameToMac.clear();
    }

    private static String normalizeMac(String mac) {
        return Objects.requireNonNull(mac.trim().toLowerCase(Locale.ROOT));
    }
}

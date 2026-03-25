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
package org.openhab.binding.ddwrt.internal.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

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

    private static final Logger logger = Objects.requireNonNull(LoggerFactory.getLogger(DDWRTNetworkCache.class));

    private final Map<String, DDWRTBaseDevice> devicesByMac = new ConcurrentHashMap<>();
    private final Map<String, DDWRTWirelessClient> wirelessClientsByMac = new ConcurrentHashMap<>();
    private final Map<String, String> hostnameToMac = new ConcurrentHashMap<>();
    private final Map<String, DDWRTRadio> radiosByInterfaceId = new ConcurrentHashMap<>();
    private final Map<String, DDWRTFirewallRule> firewallRulesByRuleId = new ConcurrentHashMap<>();
    private final Map<String, DDWRTDhcpLease> dhcpLeasesByMac = new ConcurrentHashMap<>();

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
            hostnameToMac.put(Objects.requireNonNull(client.getHostname().toLowerCase()), normalizedMac);
        }
    }

    /**
     * Thread-safe update of wireless client using compute pattern.
     * The mapping function is applied atomically with the existing client as input.
     */
    public DDWRTWirelessClient computeWirelessClient(String mac,
            java.util.function.Function<DDWRTWirelessClient, DDWRTWirelessClient> mappingFunction) {
        return wirelessClientsByMac.compute(normalizeMac(mac), (key, existing) -> {
            DDWRTWirelessClient client = existing != null ? existing : new DDWRTWirelessClient(key);
            DDWRTWirelessClient result = mappingFunction.apply(client);
            // Maintain hostname index
            if (!result.getHostname().isEmpty()) {
                hostnameToMac.put(Objects.requireNonNull(result.getHostname().toLowerCase()), key);
            }
            return result;
        });
    }

    /**
     * Find a wireless client by hostname. Returns null if no client has this hostname.
     */
    public @Nullable DDWRTWirelessClient getWirelessClientByHostname(String hostname) {
        if (hostname.isEmpty()) {
            return null;
        }
        String mac = hostnameToMac.get(hostname.toLowerCase());
        return mac != null ? wirelessClientsByMac.get(mac) : null;
    }

    /**
     * Get the MAC address currently associated with a hostname.
     */
    public @Nullable String getMacForHostname(String hostname) {
        if (hostname.isEmpty()) {
            return null;
        }
        return hostnameToMac.get(hostname.toLowerCase());
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
        String oldMac = hostnameToMac.get(hostname.toLowerCase());
        if (oldMac != null && !oldMac.equals(normalizedNewMac)) {
            DDWRTWirelessClient oldClient = wirelessClientsByMac.get(oldMac);
            if (oldClient != null && hostname.equalsIgnoreCase(oldClient.getHostname())) {
                logger.debug("MAC randomization detected for '{}': old MAC={}, new MAC={}", hostname, oldMac,
                        normalizedNewMac);
                // Remove old entry
                wirelessClientsByMac.remove(oldMac);
                // Update hostname index to point to new MAC
                hostnameToMac.put(Objects.requireNonNull(hostname.toLowerCase()), normalizedNewMac);
                return oldMac;
            }
        }
        return null;
    }

    public List<DDWRTWirelessClient> getWirelessClients() {
        return Objects.requireNonNull(Collections.unmodifiableList(new ArrayList<>(wirelessClientsByMac.values())));
    }

    public void removeWirelessClient(String mac) {
        DDWRTWirelessClient removed = wirelessClientsByMac.remove(normalizeMac(mac));
        if (removed != null && !removed.getHostname().isEmpty()) {
            // Only remove from hostname index if it still points to this MAC
            hostnameToMac.remove(removed.getHostname().toLowerCase(), normalizeMac(mac));
        }
    }

    // ---- Radios ----

    public @Nullable DDWRTRadio getRadio(String interfaceId) {
        return radiosByInterfaceId.get(interfaceId.toLowerCase());
    }

    public void putRadio(String interfaceId, DDWRTRadio radio) {
        radiosByInterfaceId.put(Objects.requireNonNull(interfaceId.toLowerCase()), radio);
    }

    public List<DDWRTRadio> getRadios() {
        return Objects.requireNonNull(Collections.unmodifiableList(new ArrayList<>(radiosByInterfaceId.values())));
    }

    public void removeRadio(String interfaceId) {
        radiosByInterfaceId.remove(interfaceId.toLowerCase());
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
        dhcpLeasesByMac.put(normalizeMac(mac), lease);
    }

    public List<DDWRTDhcpLease> getDhcpLeases() {
        return Objects.requireNonNull(Collections.unmodifiableList(new ArrayList<>(dhcpLeasesByMac.values())));
    }

    public void clearDhcpLeases() {
        dhcpLeasesByMac.clear();
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
    }

    private static String normalizeMac(String mac) {
        return Objects.requireNonNull(mac.trim().toLowerCase());
    }
}

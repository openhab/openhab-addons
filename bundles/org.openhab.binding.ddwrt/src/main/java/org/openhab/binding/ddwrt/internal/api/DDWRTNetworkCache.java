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

    private final Map<String, DDWRTBaseDevice> devicesByMac = new ConcurrentHashMap<>();
    private final Map<String, DDWRTWirelessClient> wirelessClientsByMac = new ConcurrentHashMap<>();
    private final Map<String, DDWRTRadio> radiosByInterfaceId = new ConcurrentHashMap<>();
    private final Map<String, DDWRTFirewallRule> firewallRulesByRuleId = new ConcurrentHashMap<>();

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
        wirelessClientsByMac.put(normalizeMac(mac), client);
    }

    public List<DDWRTWirelessClient> getWirelessClients() {
        return Objects.requireNonNull(Collections.unmodifiableList(new ArrayList<>(wirelessClientsByMac.values())));
    }

    public void removeWirelessClient(String mac) {
        wirelessClientsByMac.remove(normalizeMac(mac));
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
        radiosByInterfaceId.clear();
        firewallRulesByRuleId.clear();
    }

    private static String normalizeMac(String mac) {
        return Objects.requireNonNull(mac.trim().toLowerCase());
    }
}

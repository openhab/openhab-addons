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
package org.openhab.binding.ddwrt.internal;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.thing.Thing;

/**
 * Resolves client names by exact MAC address from metadata published by other bindings.
 *
 * @author Lee Ballard - Initial contribution
 */
@NonNullByDefault
final class ClientNameResolver {

    private static final Set<String> MAC_KEYS = Set.of("mac", "macaddress", "mac-address", "mac_address");
    private static final Set<String> NAME_KEYS = Set.of("alias", "nickname");
    private static final Set<String> GENERIC_NAMES = Set.of("device", "unknown", "unknown device", "wemo device");

    private final Map<String, Set<String>> namesByMac = new HashMap<>();

    void addThing(Thing thing) {
        if (isDdwrt(thing.getThingTypeUID().getBindingId())) {
            return;
        }
        Map<String, Object> properties = new LinkedHashMap<>(thing.getConfiguration().getProperties());
        properties.putAll(thing.getProperties());
        addIdentity(thing.getLabel(), properties);
    }

    void addDiscoveryResult(DiscoveryResult result) {
        if (!isDdwrt(result.getThingTypeUID().getBindingId())) {
            addIdentity(result.getLabel(), result.getProperties());
        }
    }

    void addIdentity(@Nullable String label, Map<String, ?> properties) {
        String mac = normalizeMac(findProperty(properties, MAC_KEYS));
        if (mac.isEmpty()) {
            return;
        }

        String name = findProperty(properties, NAME_KEYS);
        if (name.isEmpty() && label != null) {
            name = label.trim();
        }
        if (isUsefulName(name, mac)) {
            Objects.requireNonNull(namesByMac.computeIfAbsent(mac, ignored -> new HashSet<>())).add(name);
        }
    }

    Optional<String> resolve(String mac) {
        Set<String> names = namesByMac.get(normalizeMac(mac));
        if (names == null || names.isEmpty()) {
            return Optional.empty();
        }

        Map<String, String> namesByCaseInsensitiveValue = new HashMap<>();
        names.forEach(name -> namesByCaseInsensitiveValue.putIfAbsent(name.toLowerCase(Locale.ROOT), name));
        return namesByCaseInsensitiveValue.size() == 1
                ? Optional.of(namesByCaseInsensitiveValue.values().iterator().next())
                : Optional.empty();
    }

    private static boolean isDdwrt(String bindingId) {
        return DDWRTBindingConstants.THING_TYPE_CLIENT.getBindingId().equals(bindingId);
    }

    private static String findProperty(Map<String, ?> properties, Set<String> keys) {
        for (Map.Entry<String, ?> entry : properties.entrySet()) {
            if (keys.contains(entry.getKey().toLowerCase(Locale.ROOT))) {
                Object propertyValue = entry.getValue();
                if (propertyValue != null) {
                    String value = propertyValue.toString().trim();
                    if (!value.isEmpty()) {
                        return value;
                    }
                }
            }
        }
        return "";
    }

    private static boolean isUsefulName(String name, String mac) {
        return !name.isEmpty() && !GENERIC_NAMES.contains(name.toLowerCase(Locale.ROOT))
                && !normalizeMac(name).equals(mac);
    }

    static String normalizeMac(String mac) {
        String normalized = mac.replaceAll("[^0-9A-Fa-f]", "").toLowerCase(Locale.ROOT);
        return normalized.length() == 12 ? normalized : "";
    }
}

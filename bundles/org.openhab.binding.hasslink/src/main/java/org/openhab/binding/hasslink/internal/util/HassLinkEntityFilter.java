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
package org.openhab.binding.hasslink.internal.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hasslink.internal.config.HassLinkBridgeConfiguration;
import org.openhab.binding.hasslink.internal.config.HassLinkDeviceConfiguration;
import org.openhab.binding.hasslink.internal.registry.DeviceRegistryEntry;
import org.openhab.binding.hasslink.internal.registry.EntityRegistryEntry;
import org.openhab.binding.hasslink.internal.registry.HomeAssistantRegistry;

/**
 * Utility class for filtering Home Assistant entities based on device and bridge configurations.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
public class HassLinkEntityFilter {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(HassLinkEntityFilter.class);

    /**
     * Filters a physical hardware device against Bridge configuration rules (Areas, Labels, and valid child entities).
     */
    public static boolean isDeviceAllowedByBridge(DeviceRegistryEntry device, HomeAssistantRegistry registry,
            HassLinkBridgeConfiguration bridgeConfig) {

        String areaId = device.areaId();

        // 1. Device Area Filtering
        if (areaId != null && !areaId.isBlank()) {
            if (containsIgnoreCase(bridgeConfig.excludedAreas, areaId)) {
                return false;
            }
            if (hasActiveFilter(bridgeConfig.includedAreas)
                    && !containsIgnoreCase(bridgeConfig.includedAreas, areaId)) {
                return false;
            }
        } else if (hasActiveFilter(bridgeConfig.includedAreas)) {
            return false;
        }

        // 2. Device Label Filtering (Checks HA Device labels directly)
        Set<String> deviceLabels = device.labels();
        if (hasAnyMatchIgnoreCase(bridgeConfig.excludedLabels, deviceLabels)) {
            return false;
        }
        if (hasActiveFilter(bridgeConfig.includedLabels)
                && !hasAnyMatchIgnoreCase(bridgeConfig.includedLabels, deviceLabels)) {
            return false;
        }

        // 3. Ensure the device has at least one valid child entity.
        // Child entities inherit parent device labels/area via isAllowedByBridge(entity, device, ...)
        return registry.getEntityIdsForDevice(device.id()).stream() //
                .map(registry::getEntity) //
                .flatMap(Optional::stream) //
                .anyMatch(entity -> !entity.isDisabled() && isAllowedByBridge(entity, device, false, bridgeConfig));
    }

    /**
     * Filters an entity against Device configuration rules (Domains, Labels, Explicit Entity IDs).
     */
    public static boolean isEntityAllowedForDevice(String entityId, @Nullable EntityRegistryEntry entityEntry,
            HassLinkDeviceConfiguration config) {

        // 1. Explicit Thing entityIds ALWAYS take absolute priority (bypasses all device filters/exclusions)
        if (containsIgnoreCase(config.entityIds, entityId)) {
            return true;
        }

        // 2. Explicit entity-level exclude
        if (containsIgnoreCase(config.excludedEntities, entityId)) {
            return false;
        }

        // 3. Strict Included Entities Allowlist
        // If includedEntities is configured, ONLY matching entities from this device are allowed.
        if (hasActiveFilter(config.includedEntities)) {
            if (!containsIgnoreCase(config.includedEntities, entityId)) {
                return false;
            }
        }

        // 4. Domain Filtering
        String domain = entityId.contains(".") ? entityId.substring(0, entityId.indexOf('.')) : "";
        if (containsIgnoreCase(config.excludedDomains, domain)) {
            return false;
        }
        if (hasActiveFilter(config.includedDomains) && !containsIgnoreCase(config.includedDomains, domain)) {
            return false;
        }

        // 5. Label Filtering
        Set<String> entityLabels = entityEntry != null ? entityEntry.labels() : Set.of();
        if (hasAnyMatchIgnoreCase(config.excludedLabels, entityLabels)) {
            return false;
        }
        if (hasActiveFilter(config.includedLabels) && !hasAnyMatchIgnoreCase(config.includedLabels, entityLabels)) {
            return false;
        }

        return true;
    }

    /**
     * Filters devices/entities against Bridge configuration rules (Independent entities, Areas, Labels).
     */
    public static boolean isAllowedByBridge(EntityRegistryEntry entity, boolean isStandaloneEntity,
            HassLinkBridgeConfiguration bridgeConfig) {
        return isAllowedByBridge(entity, null, isStandaloneEntity, bridgeConfig);
    }

    /**
     * Filters devices/entities against Bridge configuration rules (Independent entities, Areas, Labels),
     * taking into account attributes inherited from a parent device.
     */
    public static boolean isAllowedByBridge(EntityRegistryEntry entity, @Nullable DeviceRegistryEntry parentDevice,
            boolean isStandaloneEntity, HassLinkBridgeConfiguration bridgeConfig) {

        String entityId = entity.entityId();
        // 1. Domain Filtering
        String domain = entityId.contains(".") ? entityId.substring(0, entityId.indexOf('.')) : "";
        if (containsIgnoreCase(bridgeConfig.excludedDomains, domain)) {
            return false;
        }
        if (hasActiveFilter(bridgeConfig.includedDomains)
                && !containsIgnoreCase(bridgeConfig.includedDomains, domain)) {
            return false;
        }

        // 2. Independent Entities Filter
        if (isStandaloneEntity && bridgeConfig.ignoreIndependentEntities) {
            return false;
        }

        // 3. Area Filtering (Inherits parent device area if entity area is unassigned)
        String areaId = entity.areaId();
        if ((areaId == null || areaId.isBlank()) && parentDevice != null) {
            areaId = parentDevice.areaId();
        }

        if (areaId != null && !areaId.isBlank()) {
            if (containsIgnoreCase(bridgeConfig.excludedAreas, areaId)) {
                return false;
            }
            if (hasActiveFilter(bridgeConfig.includedAreas)
                    && !containsIgnoreCase(bridgeConfig.includedAreas, areaId)) {
                return false;
            }
        } else if (hasActiveFilter(bridgeConfig.includedAreas)) {
            return false;
        }

        // 4. Label Filtering (Combines entity labels and inherited parent device labels)
        Set<String> labels = new HashSet<>(entity.labels());
        if (parentDevice != null) {
            labels.addAll(parentDevice.labels());
        }

        if (hasAnyMatchIgnoreCase(bridgeConfig.excludedLabels, labels)) {
            return false;
        }
        if (hasActiveFilter(bridgeConfig.includedLabels)
                && !hasAnyMatchIgnoreCase(bridgeConfig.includedLabels, labels)) {
            return false;
        }

        return true;
    }

    /**
     * Checks if a filter collection contains at least one non-blank rule.
     */
    private static boolean hasActiveFilter(@Nullable Collection<String> filterList) {
        if (filterList == null || filterList.isEmpty()) {
            return false;
        }
        return filterList.stream().anyMatch(s -> !s.isBlank());
    }

    private static boolean matchesGlobOrEquals(String pattern, String input) {
        String trimmedPattern = pattern.trim();
        if (trimmedPattern.isEmpty()) {
            return false;
        }
        LOGGER.debug("Matching input '{}' against pattern '{}'", input, trimmedPattern);
        if (trimmedPattern.equalsIgnoreCase(input)) {
            return true;
        }
        if (trimmedPattern.contains("*") || trimmedPattern.contains("?")) {
            String regex = "(?i)^" + Pattern.quote(trimmedPattern).replace("*", "\\E.*\\Q").replace("?", "\\E.\\Q")
                    + "$";
            return input.matches(regex);
        }
        return false;
    }

    private static boolean containsIgnoreCase(@Nullable Collection<String> list, String target) {
        if (list == null || list.isEmpty() || target.isBlank()) {
            return false;
        }
        return list.stream().anyMatch(pattern -> matchesGlobOrEquals(pattern, target));
    }

    private static boolean hasAnyMatchIgnoreCase(@Nullable Collection<String> filterList,
            Collection<String> targetList) {
        if (filterList == null || filterList.isEmpty() || targetList.isEmpty()) {
            return false;
        }
        return filterList.stream().anyMatch(
                filterPattern -> targetList.stream().anyMatch(target -> matchesGlobOrEquals(filterPattern, target)));
    }
}

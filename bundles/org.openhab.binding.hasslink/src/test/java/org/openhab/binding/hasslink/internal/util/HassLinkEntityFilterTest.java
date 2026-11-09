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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.openhab.binding.hasslink.internal.config.HassLinkBridgeConfiguration;
import org.openhab.binding.hasslink.internal.config.HassLinkDeviceConfiguration;
import org.openhab.binding.hasslink.internal.registry.DeviceRegistryEntry;
import org.openhab.binding.hasslink.internal.registry.EntityRegistryEntry;

/**
 * Tests for {@link HassLinkEntityFilter}.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@DisplayName("HassLinkEntityFilter Tests")
class HassLinkEntityFilterTest {

    @Nested
    @DisplayName("Bridge-Level Filtering")
    class BridgeFilteringTests {

        @Test
        @DisplayName("supports * and ? glob patterns")
        void bridgeFiltersMatchGlobs() {
            HassLinkBridgeConfiguration bridgeConfig = bridgeConfig();
            bridgeConfig.includedDomains = List.of("li*");

            assertThat(HassLinkEntityFilter.isAllowedByBridge(entity("light.kitchen"), false, bridgeConfig), is(true));
            assertThat(HassLinkEntityFilter.isAllowedByBridge(entity("sensor.kitchen"), false, bridgeConfig),
                    is(false));

            bridgeConfig.includedDomains = List.of("l?ght");
            assertThat(HassLinkEntityFilter.isAllowedByBridge(entity("light.kitchen"), false, bridgeConfig), is(true));
            assertThat(HassLinkEntityFilter.isAllowedByBridge(entity("liight.kitchen"), false, bridgeConfig),
                    is(false));
        }

        @Test
        @DisplayName("glob and exact matching are case-insensitive")
        void matchingIgnoresCase() {
            HassLinkBridgeConfiguration bridgeConfig = bridgeConfig();
            bridgeConfig.includedDomains = List.of("LiGhT");

            assertThat(HassLinkEntityFilter.isAllowedByBridge(entity("LIGHT.kitchen"), false, bridgeConfig), is(true));
        }

        @Test
        @DisplayName("empty bridge filters allow an otherwise valid entity")
        void emptyBridgeFiltersAllowEntity() {
            HassLinkBridgeConfiguration bridgeConfig = bridgeConfig();
            bridgeConfig.includedDomains = List.of();
            bridgeConfig.excludedDomains = List.of();
            bridgeConfig.includedAreas = List.of();
            bridgeConfig.excludedAreas = List.of();

            assertThat(HassLinkEntityFilter.isAllowedByBridge(entity("light.kitchen"), false, bridgeConfig), is(true));
        }

        @Test
        @DisplayName("ignoreIndependentEntities controls standalone entities")
        void ignoreIndependentEntitiesIsRespected() {
            HassLinkBridgeConfiguration bridgeConfig = bridgeConfig();
            bridgeConfig.ignoreIndependentEntities = true;
            assertThat(HassLinkEntityFilter.isAllowedByBridge(entity("light.kitchen"), true, bridgeConfig), is(false));

            bridgeConfig.ignoreIndependentEntities = false;
            assertThat(HassLinkEntityFilter.isAllowedByBridge(entity("light.kitchen"), true, bridgeConfig), is(true));
        }

        @Test
        @DisplayName("inherits an unassigned entity's parent area")
        void bridgeFiltersUseParentDeviceAttributes() {
            HassLinkBridgeConfiguration bridgeConfig = bridgeConfig();
            bridgeConfig.includedAreas = List.of("living_room");
            DeviceRegistryEntry device = new DeviceRegistryEntry("device", "living_room", Set.of(), null, null, null,
                    null, null, null, null);

            assertThat(HassLinkEntityFilter.isAllowedByBridge(entity("light.kitchen"), device, false, bridgeConfig),
                    is(true));
        }

        @Test
        @DisplayName("direct entity area takes precedence over parent device area")
        void directEntityAreaTakesPrecedence() {
            HassLinkBridgeConfiguration bridgeConfig = bridgeConfig();
            bridgeConfig.excludedAreas = List.of("garage");

            DeviceRegistryEntry device = new DeviceRegistryEntry("dev1", "garage", Set.of(), null, null, null, null,
                    null, null, null);
            EntityRegistryEntry entity = new EntityRegistryEntry("light.strip", "dev1", "living_room", Set.of(), null,
                    null, null);

            assertThat(HassLinkEntityFilter.isAllowedByBridge(entity, device, false, bridgeConfig), is(true));
        }

        @Test
        @DisplayName("exclusions take precedence over inclusions")
        void bridgeExclusionsOverrideInclusions() {
            HassLinkBridgeConfiguration bridgeConfig = bridgeConfig();
            bridgeConfig.includedDomains = List.of("light");
            bridgeConfig.excludedDomains = List.of("light");

            assertThat(HassLinkEntityFilter.isAllowedByBridge(entity("light.kitchen"), false, bridgeConfig), is(false));
        }

        @Test
        @DisplayName("excluded areas block entities even if domain is included")
        void bridgeExcludedAreasBlockEntity() {
            HassLinkBridgeConfiguration bridgeConfig = bridgeConfig();
            bridgeConfig.includedDomains = List.of("light");
            bridgeConfig.excludedAreas = List.of("garage");

            DeviceRegistryEntry device = new DeviceRegistryEntry("dev1", "garage", Set.of(), null, null, null, null,
                    null, null, null);

            assertThat(HassLinkEntityFilter.isAllowedByBridge(entity("light.garage_door"), device, false, bridgeConfig),
                    is(false));
        }

        @Test
        @DisplayName("handles null entity or device registry entries gracefully")
        void handlesNullRegistryEntries() {
            HassLinkBridgeConfiguration bridgeConfig = bridgeConfig();
            bridgeConfig.includedDomains = List.of("light");

            assertThat(HassLinkEntityFilter.isAllowedByBridge(entity("light.unregistered"), null, false, bridgeConfig),
                    is(true));
        }
    }

    @Nested
    @DisplayName("Device-Level Filtering")
    class DeviceFilteringTests {

        @Test
        @DisplayName("explicit device entities override device filters")
        void explicitDeviceEntitiesOverrideFilters() {
            HassLinkDeviceConfiguration config = new HassLinkDeviceConfiguration();
            config.entityIds = List.of("LIGHT.KITCHEN");
            config.excludedEntities = List.of("light.*");
            config.includedDomains = List.of("sensor");

            assertThat(HassLinkEntityFilter.isEntityAllowedForDevice("light.kitchen", null, config), is(true));
        }

        @Test
        @DisplayName("explicit entity exclusions take priority over ordinary device filters")
        void explicitEntityExclusionsAreApplied() {
            HassLinkDeviceConfiguration config = new HassLinkDeviceConfiguration();
            config.excludedEntities = List.of("light.kit?hen");

            assertThat(HassLinkEntityFilter.isEntityAllowedForDevice("light.kitchen", null, config), is(false));
            assertThat(HassLinkEntityFilter.isEntityAllowedForDevice("light.lounge", null, config), is(true));
        }

        @Test
        @DisplayName("device domain filters block unlisted domains when entityIds is empty")
        void deviceDomainFiltersApply() {
            HassLinkDeviceConfiguration config = new HassLinkDeviceConfiguration();
            config.includedDomains = List.of("switch", "sensor");

            assertThat(HassLinkEntityFilter.isEntityAllowedForDevice("switch.relay", null, config), is(true));
            assertThat(HassLinkEntityFilter.isEntityAllowedForDevice("light.kitchen", null, config), is(false));
        }

        @Test
        @DisplayName("device entity globs are case-insensitive")
        void deviceEntityGlobsIgnoreCase() {
            HassLinkDeviceConfiguration config = new HassLinkDeviceConfiguration();
            config.excludedEntities = List.of("LIGHT.*");

            assertThat(HassLinkEntityFilter.isEntityAllowedForDevice("light.kitchen", null, config), is(false));
        }

        @Test
        @DisplayName("empty device filters allow entities without registry metadata")
        void emptyDeviceFiltersAllowEntity() {
            HassLinkDeviceConfiguration config = new HassLinkDeviceConfiguration();

            assertThat(HassLinkEntityFilter.isEntityAllowedForDevice("light.kitchen", null, config), is(true));
        }
    }

    private static HassLinkBridgeConfiguration bridgeConfig() {
        HassLinkBridgeConfiguration config = new HassLinkBridgeConfiguration();
        config.excludedDomains = List.of();
        return config;
    }

    private static EntityRegistryEntry entity(String entityId) {
        return new EntityRegistryEntry(entityId, null, null, Set.of(), null, null, null);
    }
}

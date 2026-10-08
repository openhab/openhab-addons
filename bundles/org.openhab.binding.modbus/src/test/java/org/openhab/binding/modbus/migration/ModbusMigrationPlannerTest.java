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
package org.openhab.binding.modbus.migration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

class ModbusMigrationPlannerTest {

    private final ModbusMigrationPlanner planner = new ModbusMigrationPlanner();

    @Test
    void previewGroupsEligibleDataThingsAndEmitsReviewArtifactsWithoutMutatingInput() {
        LegacyPoller poller = new LegacyPoller("modbus:poller:tcp:plant", "modbus:tcp:plant",
                Map.of("start", 100, "length", 2, "type", "holding", "refresh", 500));
        LegacyData data = new LegacyData("modbus:data:tcp:plant:temperature", poller.uid(),
                Map.of("readStart", "100", "readValueType", "int16"),
                List.of(new LegacyLink("Temperature", "modbus:data:tcp:plant:temperature:number", "Number")));

        MigrationPreview preview = planner.preview(List.of(poller), List.of(data));

        assertEquals(1, preview.groups().size());
        assertEquals("modbus:poller2:tcp:plant", preview.groups().getFirst().targetPollerUid());
        assertTrue(preview.yaml().contains("modbus:poller2:tcp:plant"));
        assertTrue(preview.yaml().contains("temperature:"));
        assertTrue(preview.manifestJson().contains("modbus:data:tcp:plant:temperature:number"));
        assertTrue(preview.manifestJson().contains("modbus:poller2:tcp:plant:temperature"));
        assertFalse(preview.identity().isBlank());
        assertEquals(Map.of("readStart", "100", "readValueType", "int16"), data.configuration());
    }

    @Test
    void previewEmitsLiteralDeveloperSidebarReviewTermsForEachMigratedItem() {
        LegacyPoller poller = new LegacyPoller("modbus:poller:tcp:plant", "modbus:tcp:plant",
                Map.of("start", 100, "length", 2, "type", "holding"));
        LegacyData data = new LegacyData("modbus:data:tcp:plant:temperature", poller.uid(),
                Map.of("readStart", "100", "readValueType", "int16"),
                List.of(new LegacyLink("Temperature", "modbus:data:tcp:plant:temperature:number", "Number")));

        MigrationPreview preview = planner.preview(List.of(poller), List.of(data));

        assertEquals(
                """
                        IMPACT ASSESSMENT (review only; no rules, scripts, widgets, or profiles are rewritten)
                        - Item: Temperature
                          Source channel: modbus:data:tcp:plant:temperature:number
                          Target channel: modbus:poller2:tcp:plant:temperature
                          Profile configuration: {}
                          Target link action: CREATE
                          Developer Sidebar search queries (literal terms):
                            - Temperature
                            - modbus:data:tcp:plant:temperature
                            - modbus:poller2:tcp:plant
                        NOTE: Developer Sidebar search is a user review aid, not authoritative dependency detection. Review dynamic, group, tag, external references, and scripts, widgets, and profiles manually.
                        """,
                preview.impactReport());
    }

    @Test
    void previewPreservesTheProvenSystemDefaultProfileAndRefusesUnknownProfileConfiguration() {
        LegacyPoller poller = new LegacyPoller("modbus:poller:tcp:plant", "modbus:tcp:plant",
                Map.of("start", 100, "length", 2, "type", "holding"));
        LegacyData data = new LegacyData("modbus:data:tcp:plant:temperature", poller.uid(),
                Map.of("readStart", "100", "readValueType", "int16"),
                List.of(new LegacyLink("Temperature", "modbus:data:tcp:plant:temperature:number", "Number",
                        Map.of("profile", "system:default")),
                        new LegacyLink("AdjustedTemperature", "modbus:data:tcp:plant:temperature:number", "Number",
                                Map.of("profile", "modbus:gain-offset", "gain", "2"))));

        MigrationPreview preview = planner.preview(List.of(poller), List.of(data));

        assertEquals(Map.of("profile", "system:default"),
                preview.groups().getFirst().links().getFirst().configuration());
        assertEquals(1, preview.groups().getFirst().links().size());
        assertEquals(
                List.of(new ManualMigrationWork("modbus:data:tcp:plant:temperature:number",
                        "Item link profile configuration is not proven compatible; target link was not planned")),
                preview.manualWork());
        assertTrue(preview.impactReport()
                .contains("Profile configuration: {\"gain\":\"2\",\"profile\":\"modbus:gain-offset\"}"));
        assertTrue(preview.impactReport().contains("Target link action: MANUAL"));
    }

    @Test
    void previewReportsCustomTransformsAndJsonWritesAsManualWork() {
        LegacyPoller poller = new LegacyPoller("modbus:poller:tcp:plant", "modbus:tcp:plant",
                Map.of("start", 100, "length", 2, "type", "holding"));
        LegacyData transformed = new LegacyData("modbus:data:tcp:plant:scaled", poller.uid(),
                Map.of("readStart", "100", "readValueType", "int16", "readTransform", "JS(scale.js)"), List.of());
        LegacyData jsonWrite = new LegacyData("modbus:data:tcp:plant:writer", poller.uid(),
                Map.of("writeStart", "100", "writeType", "holding", "writeTransform", "JSONPATH($.value)"), List.of());

        MigrationPreview preview = planner.preview(List.of(poller), List.of(transformed, jsonWrite));

        assertTrue(preview.groups().isEmpty());
        assertEquals(2, preview.manualWork().size());
        assertTrue(preview.manualWork().stream().anyMatch(work -> work.reason().contains("custom readTransform")));
        assertTrue(preview.manualWork().stream().anyMatch(work -> work.reason().contains("JSON write")));
    }

    @Test
    void previewIdentityChangesWhenSourceTopologyChanges() {
        LegacyPoller poller = new LegacyPoller("modbus:poller:tcp:plant", "modbus:tcp:plant",
                Map.of("start", 100, "length", 2, "type", "holding"));
        LegacyData first = new LegacyData("modbus:data:tcp:plant:temperature", poller.uid(),
                Map.of("readStart", "100", "readValueType", "int16"), List.of());
        LegacyData second = new LegacyData("modbus:data:tcp:plant:pressure", poller.uid(),
                Map.of("readStart", "101", "readValueType", "uint16"), List.of());

        assertFalse(planner.preview(List.of(poller), List.of(first)).identity()
                .equals(planner.preview(List.of(poller), List.of(first, second)).identity()));
    }
}

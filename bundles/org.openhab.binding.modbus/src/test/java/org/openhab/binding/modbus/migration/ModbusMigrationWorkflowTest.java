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
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

class ModbusMigrationWorkflowTest {

    @Test
    void applyRequiresTheExactPreviewIdentityAndRollbackLeavesLegacyUntouched() {
        LegacyPoller poller = poller();
        LegacyData data = data();
        RecordingApplier applier = new RecordingApplier();
        ModbusMigrationWorkflow workflow = new ModbusMigrationWorkflow(new ModbusMigrationPlanner(), applier);

        MigrationPreview preview = workflow.preview(List.of(poller), List.of(data));

        assertThrows(IllegalArgumentException.class,
                () -> workflow.apply("not-a-preview", List.of(poller), List.of(data)));
        workflow.apply(preview.identity(), List.of(poller), List.of(data));
        assertEquals(List.of("create:modbus:poller2:tcp:plant"), applier.operations);
        workflow.rollback(preview.identity());
        assertEquals(List.of("create:modbus:poller2:tcp:plant", "remove-targets:modbus:poller2:tcp:plant"),
                applier.operations);
        assertThrows(IllegalStateException.class, () -> workflow.cleanupLegacy(preview.identity()));
    }

    @Test
    void failedApplyIsNotMarkedAsAppliedSoItCannotRollbackUnverifiedChanges() {
        LegacyPoller poller = poller();
        LegacyData data = data();
        ModbusMigrationWorkflow workflow = new ModbusMigrationWorkflow(new ModbusMigrationPlanner(),
                new MigrationApplier() {
                    @Override
                    public void createTargets(MigrationPreview preview) {
                        throw new IllegalStateException("provider failed after compensating");
                    }

                    @Override
                    public void removeTargets(MigrationPreview preview) {
                    }

                    @Override
                    public void removeLegacy(MigrationPreview preview) {
                    }
                });
        MigrationPreview preview = workflow.preview(List.of(poller), List.of(data));

        assertThrows(IllegalStateException.class,
                () -> workflow.apply(preview.identity(), List.of(poller), List.of(data)));

        assertThrows(IllegalStateException.class, () -> workflow.rollback(preview.identity()));
    }

    @Test
    void cleanupIsASeparateExplicitActionAfterApply() {
        LegacyPoller poller = poller();
        LegacyData data = data();
        RecordingApplier applier = new RecordingApplier();
        ModbusMigrationWorkflow workflow = new ModbusMigrationWorkflow(new ModbusMigrationPlanner(), applier);
        MigrationPreview preview = workflow.preview(List.of(poller), List.of(data));

        workflow.apply(preview.identity(), List.of(poller), List.of(data));
        workflow.cleanupLegacy(preview.identity());

        assertEquals(List.of("create:modbus:poller2:tcp:plant", "remove-legacy:modbus:poller:tcp:plant"),
                applier.operations);
    }

    @Test
    void failedManagedRollbackCanBeRetried() {
        org.openhab.core.thing.ManagedThingProvider provider = org.mockito.Mockito
                .mock(org.openhab.core.thing.ManagedThingProvider.class);
        org.openhab.core.thing.link.ItemChannelLinkRegistry links = org.mockito.Mockito
                .mock(org.openhab.core.thing.link.ItemChannelLinkRegistry.class);
        org.openhab.core.thing.ThingUID target = new org.openhab.core.thing.ThingUID("modbus:poller2:tcp:plant");
        org.mockito.Mockito.when(provider.remove(target)).thenThrow(new IllegalStateException("removal failed"))
                .thenReturn(null);
        ModbusMigrationWorkflow workflow = new ModbusMigrationWorkflow(new ModbusMigrationPlanner(),
                new ManagedModbusMigrationApplier(provider, links));
        MigrationPreview preview = workflow.preview(List.of(poller()), List.of(data()));
        workflow.apply(preview.identity(), List.of(poller()), List.of(data()));

        assertThrows(IllegalStateException.class, () -> workflow.rollback(preview.identity()));
        workflow.rollback(preview.identity());
        org.mockito.Mockito.verify(provider, org.mockito.Mockito.times(2)).remove(target);
        assertThrows(IllegalStateException.class, () -> workflow.rollback(preview.identity()));
    }

    private static LegacyPoller poller() {
        return new LegacyPoller("modbus:poller:tcp:plant", "modbus:tcp:plant",
                Map.of("start", 100, "length", 2, "type", "holding"));
    }

    private static LegacyData data() {
        return new LegacyData("modbus:data:tcp:plant:temperature", "modbus:poller:tcp:plant",
                Map.of("readStart", "100", "readValueType", "int16"), List.of());
    }

    private static final class RecordingApplier implements MigrationApplier {
        private final List<String> operations = new java.util.ArrayList<>();

        @Override
        public void createTargets(MigrationPreview preview) {
            preview.groups().forEach(group -> operations.add("create:" + group.targetPollerUid()));
        }

        @Override
        public void removeTargets(MigrationPreview preview) {
            preview.groups().forEach(group -> operations.add("remove-targets:" + group.targetPollerUid()));
        }

        @Override
        public void removeLegacy(MigrationPreview preview) {
            preview.groups().forEach(group -> operations.add("remove-legacy:" + group.sourcePollerUid()));
        }
    }
}

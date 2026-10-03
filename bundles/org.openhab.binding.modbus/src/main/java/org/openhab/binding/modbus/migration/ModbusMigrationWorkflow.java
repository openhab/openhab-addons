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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Public, opt-in migration workflow. Preview is the only operation that can inspect configuration without a supplied
 * preview identity; apply, rollback, and cleanup are deliberately separate actions.
 */
@NonNullByDefault
public final class ModbusMigrationWorkflow {
    private final ModbusMigrationPlanner planner;
    private final MigrationApplier applier;
    private final Map<String, MigrationPreview> previews = new HashMap<>();
    private final Set<String> applied = new HashSet<>();

    public ModbusMigrationWorkflow(ModbusMigrationPlanner planner, MigrationApplier applier) {
        this.planner = planner;
        this.applier = applier;
    }

    public synchronized MigrationPreview preview(List<LegacyPoller> pollers, List<LegacyData> dataThings) {
        MigrationPreview preview = planner.preview(pollers, dataThings);
        previews.put(preview.identity(), preview);
        return preview;
    }

    public synchronized void apply(String previewIdentity, List<LegacyPoller> pollers, List<LegacyData> dataThings) {
        MigrationPreview currentPreview = planner.preview(pollers, dataThings);
        if (!previewIdentity.equals(currentPreview.identity()) || !previews.containsKey(previewIdentity)) {
            throw new IllegalArgumentException("apply requires the exact, current preview identity");
        }
        if (!applied.contains(previewIdentity)) {
            applier.createTargets(currentPreview);
            applied.add(previewIdentity);
        }
    }

    public synchronized void rollback(String previewIdentity) {
        MigrationPreview preview = appliedPreview(previewIdentity);
        applier.removeTargets(preview);
        applied.remove(previewIdentity);
    }

    public synchronized void cleanupLegacy(String previewIdentity) {
        MigrationPreview preview = appliedPreview(previewIdentity);
        applier.removeLegacy(preview);
    }

    private MigrationPreview appliedPreview(String previewIdentity) {
        if (!applied.contains(previewIdentity)) {
            throw new IllegalStateException("the preview has not been applied");
        }
        MigrationPreview preview = previews.get(previewIdentity);
        if (preview == null) {
            throw new IllegalArgumentException("unknown preview identity");
        }
        return preview;
    }
}

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
package org.openhab.binding.rachio.internal.api.json;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Smart Hose day view.
 *
 * @author Kovacs Istvan - Initial contribution
 */
@NonNullByDefault
public class RachioValveDayView {
    public String date = "";
    public @Nullable List<RachioValveDayRun> runs = new ArrayList<>();
    public @Nullable List<RachioValveDayRun> valveRuns = new ArrayList<>();
    public @Nullable List<RachioValveDayRun> plannedRuns = new ArrayList<>();
    public @Nullable List<RachioValveDayRun> completedRuns = new ArrayList<>();

    public List<RachioValveDayRun> getRuns() {
        List<RachioValveDayRun> allRuns = new ArrayList<>();
        addRuns(allRuns, runs);
        addRuns(allRuns, valveRuns);
        addRuns(allRuns, plannedRuns);
        addRuns(allRuns, completedRuns);
        return allRuns;
    }

    private void addRuns(List<RachioValveDayRun> allRuns, @Nullable List<RachioValveDayRun> runs) {
        if (runs != null) {
            allRuns.addAll(runs);
        }
    }
}

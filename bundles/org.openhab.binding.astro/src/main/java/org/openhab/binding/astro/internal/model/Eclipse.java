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
package org.openhab.binding.astro.internal.model;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.astro.internal.util.DateTimeUtils;

/**
 * Holds eclipse informations.
 *
 * @author Gerhard Riegler - Initial contribution
 */
@NonNullByDefault
public class Eclipse {
    private record EclipseData(Instant when, double elevation) {
    }

    private final Map<EclipseKind, @Nullable EclipseData> entries = new HashMap<>(EclipseKind.values().length);

    public Eclipse(EclipseKind... eclipses) {
        for (EclipseKind eclipseKind : eclipses) {
            entries.put(eclipseKind, null);
        }
    }

    public Set<EclipseKind> getKinds() {
        return entries.keySet();
    }

    /**
     * Returns the date of the next total eclipse.
     */
    public @Nullable Instant getTotal() {
        return getDate(EclipseKind.TOTAL);
    }

    /**
     * Returns the date of the next partial eclipse.
     */
    public @Nullable Instant getPartial() {
        return getDate(EclipseKind.PARTIAL);
    }

    /**
     * Returns the date of the next ring eclipse.
     */
    public @Nullable Instant getRing() {
        return getDate(EclipseKind.RING);
    }

    /**
     * Returns the elevation of the next total eclipse.
     */
    public @Nullable Double getTotalElevation() {
        return getElevation(EclipseKind.TOTAL);
    }

    /**
     * Returns the elevation of the next partial eclipse.
     */
    public @Nullable Double getPartialElevation() {
        return getElevation(EclipseKind.PARTIAL);
    }

    /**
     * Returns the elevation of the next ring eclipse.
     */
    public @Nullable Double getRingElevation() {
        return getElevation(EclipseKind.RING);
    }

    public @Nullable Instant getDate(EclipseKind eclipseKind) {
        EclipseData entry = entries.get(eclipseKind);
        return entry != null ? entry.when : null;
    }

    private @Nullable Double getElevation(EclipseKind eclipseKind) {
        EclipseData entry = entries.get(eclipseKind);
        return entry != null ? entry.elevation : null;
    }

    public void set(EclipseKind eclipseKind, double jdEclipse, double elevation) {
        Instant eclipseDate = DateTimeUtils.jdToInstant(jdEclipse);
        entries.put(eclipseKind, new EclipseData(eclipseDate, elevation));
    }
}

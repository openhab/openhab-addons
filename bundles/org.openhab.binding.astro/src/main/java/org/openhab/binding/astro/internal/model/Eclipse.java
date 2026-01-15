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
import java.util.Arrays;
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
 * @author Gaël L'hopital - Immutable and Instant
 */
@NonNullByDefault
public class Eclipse {
    private final Map<EclipseKind, @Nullable EclipseData> entries = new HashMap<>(EclipseKind.values().length);

    private record EclipseData(Instant when, double elevation) {
    }

    public Eclipse(EclipseKind... eclipses) {
        Arrays.stream(EclipseKind.values()).forEach(ek -> entries.put(ek, null));
    }

    public Set<EclipseKind> getEclipseKinds() {
        return entries.keySet();
    }

    public @Nullable Instant getDate(EclipseKind eclipseKind) {
        EclipseData entry = entries.get(eclipseKind);
        return entry != null ? entry.when : null;
    }

    public @Nullable Double getElevation(EclipseKind eclipseKind) {
        EclipseData entry = entries.get(eclipseKind);
        return entry != null ? entry.elevation : null;
    }

    public void set(EclipseKind eclipseKind, double jdEclipse, double elevation) {
        Instant eclipseDate = DateTimeUtils.jdToInstant(jdEclipse);
        entries.put(eclipseKind, new EclipseData(eclipseDate, elevation));
    }
}

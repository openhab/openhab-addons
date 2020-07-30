/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import java.util.AbstractMap.SimpleEntry;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Holds eclipse informations.
 *
 * @author Gerhard Riegler - Initial contribution
 */
@NonNullByDefault
public class Eclipse {
    private final Map<EclipseKind, Entry<Calendar, @Nullable Double>> entries = new HashMap<>();

    public Eclipse(List<EclipseKind> eclipses) {
        eclipses.forEach(eclipseKind -> {
            entries.put(eclipseKind, new SimpleEntry<>(Calendar.getInstance(), null));
        });
    }

    public Set<EclipseKind> getKinds() {
        return entries.keySet();
    }

    /**
     * Returns the date of the next total eclipse.
     */
    public @Nullable Calendar getTotal() {
        return entries.get(EclipseKind.TOTAL).getKey();
    }

    /**
     * Returns the date of the next partial eclipse.
     */
    public @Nullable Calendar getPartial() {
        return entries.get(EclipseKind.PARTIAL).getKey();
    }

    public @Nullable Calendar getRing() {
        return entries.get(EclipseKind.RING).getKey();
    }

    public @Nullable Double getTotalElevation() {
        return entries.get(EclipseKind.TOTAL).getValue();
    }

    public @Nullable Double getPartialElevation() {
        return entries.get(EclipseKind.PARTIAL).getValue();
    }

    public @Nullable Double getRingElevation() {
        return entries.get(EclipseKind.RING).getValue();
    }

    public Calendar getDate(EclipseKind eclipseKind) {
        return entries.get(eclipseKind).getKey();
    }

    public void set(EclipseKind eclipseKind, Calendar eclipseDate, @Nullable Position position) {
        entries.put(eclipseKind,
                new SimpleEntry<>(eclipseDate, position != null ? position.getElevationAsDouble() : null));
    }

}

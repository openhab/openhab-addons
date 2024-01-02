/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.astro.internal.handler.AstroThingHandler;
import org.openhab.core.i18n.TimeZoneProvider;

/**
 * Holds eclipse informations.
 *
 * @author Gerhard Riegler - Initial contribution
 */
@NonNullByDefault
public class Eclipse {
    private final Map<EclipseKind, @Nullable Entry<Calendar, @Nullable Double>> entries = new HashMap<>();

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
    public @Nullable Calendar getTotal() {
        return getDate(EclipseKind.TOTAL);
    }

    /**
     * Returns the date of the next partial eclipse.
     */
    public @Nullable Calendar getPartial() {
        return getDate(EclipseKind.PARTIAL);
    }

    /**
     * Returns the date of the next ring eclipse.
     */
    public @Nullable Calendar getRing() {
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

    public @Nullable Calendar getDate(EclipseKind eclipseKind) {
        Entry<Calendar, @Nullable Double> entry = entries.get(eclipseKind);
        return entry != null ? entry.getKey() : null;
    }

    private @Nullable Double getElevation(EclipseKind eclipseKind) {
        Entry<Calendar, @Nullable Double> entry = entries.get(eclipseKind);
        return entry != null ? entry.getValue() : null;
    }

    public void set(EclipseKind eclipseKind, Calendar eclipseDate, @Nullable Position position) {
        entries.put(eclipseKind,
                new SimpleEntry<>(eclipseDate, position != null ? position.getElevationAsDouble() : null));
    }

    public void setElevations(AstroThingHandler astroHandler, TimeZoneProvider timeZoneProvider) {
        getKinds().forEach(eclipseKind -> {
            Calendar eclipseDate = getDate(eclipseKind);
            if (eclipseDate != null) {
                set(eclipseKind, eclipseDate,
                        astroHandler.getPositionAt(eclipseDate.toInstant().atZone(timeZoneProvider.getTimeZone())));
            }
        });
    }
}

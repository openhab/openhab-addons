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
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.astro.internal.util.DateTimeUtils;

/**
 * Holds eclipse informations.
 *
 * @author Gerhard Riegler - Initial contribution
 * @author Gaël L'hopital - Immutable and Instant
 */
@NonNullByDefault
public class EclipseSet {
    public static final EclipseSet NONE = new EclipseSet();
    private final List<EclipseData> eclipses;

    public record EclipseData(EclipseKind eclipseKind, Instant when, double elevation) {
        public EclipseData(EclipseKind eclipseKind, double jdInstant, Position position) {
            this(eclipseKind, DateTimeUtils.jdToInstant(jdInstant), position.getElevationAsDouble());
        }
    }

    private EclipseSet() {
        this.eclipses = List.of();
    }

    public EclipseSet(EclipseData[] comingEclipses) {
        this.eclipses = Arrays.asList(comingEclipses);
    }

    public boolean needsRecalc(double jdNow) {
        Instant now = DateTimeUtils.jdToInstant(jdNow);
        return eclipses.isEmpty() || eclipses.stream().map(EclipseData::when).anyMatch(when -> when.isAfter(now));
    }

    public Stream<EclipseData> getEclipses() {
        return eclipses.stream();
    }

    private EclipseData internalGet(EclipseKind eclipseKind) {
        return getEclipses().filter(ed -> ed.eclipseKind.equals(eclipseKind)).findFirst().orElseThrow(
                () -> new IllegalArgumentException("This EclipseSet does not contain %s".formatted(eclipseKind)));
    }

    public Instant getDate(EclipseKind eclipseKind) {
        return internalGet(eclipseKind).when;
    }

    public Double getElevation(EclipseKind eclipseKind) {
        return internalGet(eclipseKind).elevation;
    }
}

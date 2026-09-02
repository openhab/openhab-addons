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
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.astro.internal.calc.EclipseCalc.LocalizedEclipse;
import org.openhab.binding.astro.internal.util.DateTimeUtils;

/**
 * Holds eclipse information.
 *
 * @author Gerhard Riegler - Initial contribution
 * @author Gaël L'hopital - Immutable and Instant
 */
@NonNullByDefault
public class EclipseSet {
    public static final EclipseSet NONE = new EclipseSet();
    private final List<LocalizedEclipse> eclipses;

    private EclipseSet() {
        this.eclipses = List.of();
    }

    public EclipseSet(Stream<LocalizedEclipse> eclipseDatas) {
        this.eclipses = eclipseDatas.toList();
    }

    public boolean needsRecalc(double jdNow) {
        Instant now = DateTimeUtils.jdToInstant(jdNow);
        return eclipses.isEmpty() || eclipses.stream().map(LocalizedEclipse::when).anyMatch(when -> when.isBefore(now));
    }

    public Stream<LocalizedEclipse> getEclipses() {
        return eclipses.stream();
    }

    private LocalizedEclipse internalGet(EclipseKind eclipseKind) {
        return getEclipses().filter(ed -> ed.matches(eclipseKind)).findFirst().orElseThrow(
                () -> new IllegalArgumentException("This EclipseSet does not contain %s".formatted(eclipseKind)));
    }

    public Instant getDate(EclipseKind eclipseKind) {
        return internalGet(eclipseKind).when();
    }

    public Double getElevation(EclipseKind eclipseKind) {
        return internalGet(eclipseKind).elevation();
    }
}

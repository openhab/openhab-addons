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

import java.time.Duration;
import java.time.Instant;
import java.time.InstantSource;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Time;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.astro.internal.util.AstroConstants;
import org.openhab.binding.astro.internal.util.DateTimeUtils;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;

/**
 * Holds the calculates moon phase informations.
 *
 * @author Gerhard Riegler - Initial contribution
 * @author Christoph Weitkamp - Introduced UoM
 */
@NonNullByDefault
public class MoonPhase {
    private static final Set<MoonPhaseName> USED_PHASES = Set.of(MoonPhaseName.NEW, MoonPhaseName.FULL,
            MoonPhaseName.FIRST_QUARTER, MoonPhaseName.THIRD_QUARTER);
    public static final MoonPhase NONE = new MoonPhase();
    private static final Duration SYNODIC_MONTH = Duration
            .ofSeconds((long) (AstroConstants.LUNAR_SYNODIC_MONTH_DAYS * AstroConstants.SECONDS_PER_DAY));
    public static final MoonPhase DEFAULT = new MoonPhase();

    private final Map<MoonPhaseName, Instant> phases;
    private final @Nullable Instant parentNewMoon;
    private final @Nullable InstantSource instantSource;

    private int illumination;
    private @Nullable MoonPhaseName name;

    private MoonPhase() {
        this.phases = Map.of();
        this.parentNewMoon = null;
        this.instantSource = null;
    }

    public MoonPhase(InstantSource instantSource, double parentNewMoon, Map<MoonPhaseName, Double> comingPhases) {
        this.instantSource = instantSource;
        this.parentNewMoon = DateTimeUtils.jdToInstant(parentNewMoon);
        this.phases = comingPhases.keySet().stream().collect(Collectors.toMap(Function.identity(),
                k -> DateTimeUtils.jdToInstant(Objects.requireNonNull(comingPhases.get(k)))));
    }

    public Instant getPhase(MoonPhaseName phase) {
        if (!USED_PHASES.contains(phase)) {
            throw new IllegalArgumentException("The phase '%s' is not handled".formatted(phase.toString()));
        }
        return Objects.requireNonNull(phases.get(phase));
    }

    /**
     * Returns the age.
     */
    public QuantityType<Time> getAge() {
        return new QuantityType<>(getAgeDouble(), Units.SECOND);
    }

    private double getAgeDouble() {
        return getAgePercentDouble() * SYNODIC_MONTH.getSeconds();
    }

    /**
     * Returns the illumination.
     */
    public QuantityType<Dimensionless> getIllumination() {
        return new QuantityType<>(illumination, Units.ONE);
    }

    /**
     * Sets the illumination.
     */
    public void setIllumination(double illumination) {
        this.illumination = (int) Math.round(illumination * 100);
    }

    public void updateName(double julianDate, ZoneId zone) {
        this.name = MoonPhase.remarkableSteps().filter(mp -> isPhaseDay(julianDate, mp, zone)).findFirst()
                .orElseGet(() -> MoonPhaseName.fromAgePercent(getAgePercentDouble()));
    }

    /**
     * Returns the phase name.
     */
    @Nullable
    public MoonPhaseName getName() {
        return name;
    }

    public double getAgePercentDouble() {
        if (parentNewMoon == null || instantSource == null) {
            throw new IllegalArgumentException("getAgePercentDouble() must not be called on NONE instance");
        }
        return ((double) Duration.between(parentNewMoon, instantSource.instant()).getSeconds())
                / Duration.between(parentNewMoon, getPhase(MoonPhaseName.NEW)).getSeconds();
    }

    /**
     * Returns the age in degree.
     */
    public QuantityType<Angle> getAgeDegree() {
        return new QuantityType<>(getAgePercentDouble() * 360, Units.DEGREE_ANGLE);
    }

    /**
     * Returns the age in percent.
     */
    public QuantityType<Dimensionless> getAgePercent() {
        return new QuantityType<>(getAgePercentDouble(), Units.ONE);
    }

    public boolean needsRecalc(double jdNow) {
        Instant now = DateTimeUtils.jdToInstant(jdNow);
        return phases.isEmpty() || phases.values().stream().anyMatch(when -> when.isBefore(now));
    }

    public boolean isPhaseDay(double julianDate, MoonPhaseName phaseName, ZoneId zone) {
        Instant instant = DateTimeUtils.jdToInstant(julianDate);
        Instant phaseDate = getPhase(phaseName);
        return DateTimeUtils.isSameDay(instant, phaseDate, zone)
                || DateTimeUtils.isSameDay(instant, phaseDate.minus(SYNODIC_MONTH), zone);
    }

    public static final Stream<MoonPhaseName> remarkableSteps() {
        return USED_PHASES.stream().sorted(Comparator.comparing(mpn -> mpn.cycleProgress));
    }
}
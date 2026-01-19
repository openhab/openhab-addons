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
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Time;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.astro.internal.util.DateTimeUtils;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * Holds the calculates moon phase informations.
 *
 * @author Gerhard Riegler - Initial contribution
 * @author Christoph Weitkamp - Introduced UoM
 */
@NonNullByDefault
public class MoonPhaseSet {
    public static final MoonPhaseSet NONE = new MoonPhaseSet();

    private final Map<MoonPhase, Instant> phases;
    private final Instant parentNewMoon;
    private final InstantSource instantSource;

    private double illumination;
    private @Nullable MoonPhase name;

    private MoonPhaseSet(InstantSource instantSource, Instant parentNewMoon, Map<MoonPhase, Instant> phases) {
        this.phases = phases;
        this.parentNewMoon = parentNewMoon;
        this.instantSource = instantSource;
    }

    private MoonPhaseSet() {
        this(InstantSource.system(), Instant.MIN, Map.of());
    }

    public MoonPhaseSet(InstantSource instantSource, double parentNewMoon, Map<MoonPhase, Double> comingPhases) {
        this(instantSource, DateTimeUtils.jdToInstant(parentNewMoon),
                comingPhases.keySet().stream().collect(Collectors.toMap(Function.identity(),
                        k -> DateTimeUtils.jdToInstant(Objects.requireNonNull(comingPhases.get(k))))));
    }

    public Instant getPhase(MoonPhase phase) {
        if (!MoonPhase.remarkables().contains(phase)) {
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
        return getAgePercentDouble() * getMonthDuration();
    }

    public double getAgePercentDouble() {
        return ((double) Duration.between(parentNewMoon, instantSource.instant()).getSeconds()) / getMonthDuration();
    }

    /**
     * Returns the age in percent.
     */
    public QuantityType<Dimensionless> getAgePercent() {
        return new QuantityType<>(getAgePercentDouble(), Units.ONE);
    }

    /**
     * Returns the age in degree.
     */
    public QuantityType<Angle> getAgeDegree() {
        return new QuantityType<>(getAgePercentDouble() * 360, Units.DEGREE_ANGLE);
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
        this.illumination = illumination;
    }

    public void updateName(double julianDate, ZoneId zone) {
        this.name = MoonPhase.remarkables().stream().filter(mp -> isPhaseDay(julianDate, mp, zone)).findFirst()
                .orElseGet(() -> MoonPhase.fromAgePercent(getAgePercentDouble()));
    }

    /**
     * Returns the phase name.
     */
    public State getName() {
        if (name != null) {
            return new StringType(name.toString());
        }
        return UnDefType.UNDEF;
    }

    public boolean needsRecalc(double jdNow) {
        Instant now = DateTimeUtils.jdToInstant(jdNow);
        return phases.isEmpty() || phases.values().stream().anyMatch(when -> when.isBefore(now));
    }

    public boolean isPhaseDay(double julianDate, MoonPhase phaseName, ZoneId zone) {
        Instant instant = DateTimeUtils.jdToInstant(julianDate);
        Instant phaseDate = getPhase(phaseName);
        return DateTimeUtils.isSameDay(instant, phaseDate, zone)
                || (MoonPhase.NEW.equals(phaseName) && DateTimeUtils.isSameDay(instant, parentNewMoon, zone));
    }

    private long getMonthDuration() {
        return Duration.between(parentNewMoon, getPhase(MoonPhase.NEW)).getSeconds();
    }
}

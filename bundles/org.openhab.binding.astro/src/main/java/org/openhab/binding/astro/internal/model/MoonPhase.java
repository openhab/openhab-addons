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

<<<<<<< Upstream, based on main
<<<<<<< Upstream, based on main
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
=======
=======
import java.time.Duration;
>>>>>>> f203b2c Finalized modifications at this step
import java.time.Instant;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
<<<<<<< Upstream, based on main
>>>>>>> bb4de3d Starting to work on transition to Instant for MoonPhase
=======
import java.util.Objects;
>>>>>>> f203b2c Finalized modifications at this step

import javax.measure.quantity.Angle;
import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Time;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.astro.internal.util.AstroConstants;
import org.openhab.binding.astro.internal.util.DateTimeUtils;
import org.openhab.core.library.types.QuantityType;
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
public class MoonPhase {
<<<<<<< Upstream, based on main
<<<<<<< Upstream, based on main
    private final Map<MoonPhaseName, @Nullable Calendar> phases = new HashMap<>(MoonPhaseName.values().length);

=======
    private final Map<MoonPhaseName, @Nullable Instant> phases = new HashMap<>(MoonPhaseName.values().length);
>>>>>>> bb4de3d Starting to work on transition to Instant for MoonPhase
    private double age;
    private double illumination;
    private double agePercent;
    private double ageDegree;
=======
    private static final Duration SYNODIC_MONTH = Duration
            .ofSeconds((long) (AstroConstants.SYNODIC_MONTH * AstroConstants.SECONDS_PER_DAY));
    public static final MoonPhase DEFAULT = new MoonPhase();
>>>>>>> f203b2c Finalized modifications at this step

    private final Map<MoonPhaseName, Double> phases;
    private int illumination;
    private @Nullable MoonPhaseName name;

<<<<<<< Upstream, based on main
    public MoonPhase() {
<<<<<<< Upstream, based on main
        phases.put(MoonPhaseName.FIRST_QUARTER, null);
        phases.put(MoonPhaseName.FULL, null);
        phases.put(MoonPhaseName.THIRD_QUARTER, null);
        phases.put(MoonPhaseName.NEW, null);
=======
        Arrays.stream(MoonPhaseName.values()).filter(phase -> !Double.isNaN(phase.mode))
                .forEach(phase -> phases.put(phase, null));
=======
    private MoonPhase() {
        phases = Map.of();
>>>>>>> f203b2c Finalized modifications at this step
    }

<<<<<<< Upstream, based on main
    public @Nullable Instant getPhase(MoonPhaseName phase) {
        return phases.get(phase);
>>>>>>> bb4de3d Starting to work on transition to Instant for MoonPhase
=======
    public MoonPhase(Map<MoonPhaseName, Double> comingPhases) {
        this.phases = new HashMap<>(comingPhases);
    }

    public Instant getPhase(MoonPhaseName phase) {
        return DateTimeUtils.jdToInstant(Objects.requireNonNull(phases.get(phase)));
>>>>>>> f203b2c Finalized modifications at this step
    }

    /**
     * Returns the date at which the moon is in the first quarter.
     */
<<<<<<< Upstream, based on main
    @Nullable
<<<<<<< Upstream, based on main
    public Calendar getFirstQuarter() {
        return getPhaseDate(MoonPhaseName.FIRST_QUARTER);
=======
=======
>>>>>>> f203b2c Finalized modifications at this step
    public Instant getFirstQuarter() {
        return getPhase(MoonPhaseName.FIRST_QUARTER);
    }

<<<<<<< Upstream, based on main
    public void setPhase(MoonPhaseName phase, double jdWhen) {
        phases.put(phase, DateTimeUtils.jdToInstant(jdWhen));
>>>>>>> bb4de3d Starting to work on transition to Instant for MoonPhase
    }

=======
>>>>>>> f203b2c Finalized modifications at this step
    /**
     * Returns the date of the full moon.
     */
<<<<<<< Upstream, based on main
    @Nullable
<<<<<<< Upstream, based on main
    public Calendar getFull() {
        return getPhaseDate(MoonPhaseName.FULL);
=======
=======
>>>>>>> f203b2c Finalized modifications at this step
    public Instant getFull() {
        return getPhase(MoonPhaseName.FULL);
>>>>>>> bb4de3d Starting to work on transition to Instant for MoonPhase
    }

    /**
     * Returns the date at which the moon is in the third quarter.
     */
<<<<<<< Upstream, based on main
    @Nullable
<<<<<<< Upstream, based on main
    public Calendar getThirdQuarter() {
        return getPhaseDate(MoonPhaseName.THIRD_QUARTER);
=======
=======
>>>>>>> f203b2c Finalized modifications at this step
    public Instant getThirdQuarter() {
        return getPhase(MoonPhaseName.THIRD_QUARTER);
>>>>>>> bb4de3d Starting to work on transition to Instant for MoonPhase
    }

    /**
     * Returns the date of the new moon.
     */
<<<<<<< Upstream, based on main
    @Nullable
<<<<<<< Upstream, based on main
    public Calendar getNew() {
        return getPhaseDate(MoonPhaseName.NEW);
    }

    @Nullable
    public Calendar getPhaseDate(MoonPhaseName moonPhase) {
        if (!phases.containsKey(moonPhase)) {
            throw new IllegalArgumentException("MoonPhase does not handle %s".formatted(moonPhase.toString()));
        }
        return phases.get(moonPhase);
    }

    public void setPhase(MoonPhaseName moonPhase, @Nullable Calendar calendar) {
        if (!phases.containsKey(moonPhase)) {
            throw new IllegalArgumentException("MoonPhase does not handle %s".formatted(moonPhase.toString()));
        }
        phases.put(moonPhase, calendar);
    }

    public Stream<MoonPhaseName> remarkablePhases() {
        return phases.keySet().stream();
=======
=======
>>>>>>> f203b2c Finalized modifications at this step
    public Instant getNew() {
        return getPhase(MoonPhaseName.NEW);
>>>>>>> bb4de3d Starting to work on transition to Instant for MoonPhase
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
<<<<<<< Upstream, based on main
    public State getIllumination() {
        return Double.isNaN(illumination) ? UnDefType.UNDEF
                : illumination < 0 ? UnDefType.NULL : new QuantityType<>(illumination, Units.PERCENT);
=======
    public QuantityType<Dimensionless> getIllumination() {
        return new QuantityType<>(illumination, Units.ONE);
>>>>>>> f203b2c Finalized modifications at this step
    }

    /**
     * Sets the illumination.
     */
    public void setIllumination(double illumination) {
        this.illumination = (int) Math.round(illumination * 100);
        boolean waxing = getAgePercentDouble() < 0.5;
        if (illumination == 0) {
            name = MoonPhaseName.NEW;
        } else if (illumination < 50) {
            name = waxing ? MoonPhaseName.WAXING_CRESCENT : MoonPhaseName.WANING_CRESCENT;
        } else if (illumination == 50) {
            name = waxing ? MoonPhaseName.FIRST_QUARTER : MoonPhaseName.THIRD_QUARTER;
        } else if (illumination < 100) {
            name = waxing ? MoonPhaseName.WAXING_GIBBOUS : MoonPhaseName.WANING_GIBBOUS;
        } else {
            name = MoonPhaseName.FULL;
        }
    }

    /**
     * Returns the phase name.
     */
    @Nullable
    public MoonPhaseName getName() {
        return name;
    }

    /**
     * Sets the phase name.
     */
    public void setName(@Nullable MoonPhaseName name) {
        this.name = name;
    }

    public double getAgePercentDouble() {
        var parentNewMoon = getNew().minus(SYNODIC_MONTH);
        return ((double) Duration.between(parentNewMoon, Instant.now()).getSeconds()) / SYNODIC_MONTH.getSeconds();
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

    public boolean needsRecalc(double julianDate) {
        return phases.isEmpty() || phases.values().stream().anyMatch(d -> d < julianDate);
    }

    public boolean isPhaseDay(double julianDate, MoonPhaseName phaseName, ZoneId zone) {
        Instant instant = DateTimeUtils.jdToInstant(julianDate);
        Instant phaseDate = getPhase(phaseName);
        return DateTimeUtils.isSameDay(instant, phaseDate, zone)
                || DateTimeUtils.isSameDay(instant, phaseDate.minus(SYNODIC_MONTH), zone);
    }

}

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

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Time;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
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
    private final Map<MoonPhaseName, @Nullable Calendar> phases = new HashMap<>(MoonPhaseName.values().length);

    private double age;
    private double illumination;
    private double agePercent;
    private double ageDegree;

    private @Nullable MoonPhaseName name;

    public MoonPhase() {
        phases.put(MoonPhaseName.FIRST_QUARTER, null);
        phases.put(MoonPhaseName.FULL, null);
        phases.put(MoonPhaseName.THIRD_QUARTER, null);
        phases.put(MoonPhaseName.NEW, null);
    }

    /**
     * Returns the date at which the moon is in the first quarter.
     */
    @Nullable
    public Calendar getFirstQuarter() {
        return getPhaseDate(MoonPhaseName.FIRST_QUARTER);
    }

    /**
     * Returns the date of the full moon.
     */
    @Nullable
    public Calendar getFull() {
        return getPhaseDate(MoonPhaseName.FULL);
    }

    /**
     * Returns the date at which the moon is in the third quarter.
     */
    @Nullable
    public Calendar getThirdQuarter() {
        return getPhaseDate(MoonPhaseName.THIRD_QUARTER);
    }

    /**
     * Returns the date of the new moon.
     */
    @Nullable
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
    }

    /**
     * Returns the age in days.
     */
    public QuantityType<Time> getAge() {
        return new QuantityType<>(age, Units.DAY);
    }

    /**
     * Sets the age in days.
     */
    public void setAge(double age) {
        this.age = age;
    }

    /**
     * Returns the illumination.
     */
    public State getIllumination() {
        return Double.isNaN(illumination) ? UnDefType.UNDEF
                : illumination < 0 ? UnDefType.NULL : new QuantityType<>(illumination, Units.PERCENT);
    }

    /**
     * Sets the illumination.
     */
    public void setIllumination(double illumination) {
        this.illumination = illumination;
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

    /**
     * Returns the age in degree.
     */
    public QuantityType<Angle> getAgeDegree() {
        return new QuantityType<>(ageDegree, Units.DEGREE_ANGLE);
    }

    /**
     * Sets the age in degree.
     */
    public void setAgeDegree(double ageDegree) {
        this.ageDegree = ageDegree;
    }

    /**
     * Returns the age in percent.
     */
    public QuantityType<Dimensionless> getAgePercent() {
        return new QuantityType<>(agePercent, Units.PERCENT);
    }

    /**
     * Sets the age in percent.
     */
    public void setAgePercent(double agePercent) {
        this.agePercent = agePercent;
    }
}

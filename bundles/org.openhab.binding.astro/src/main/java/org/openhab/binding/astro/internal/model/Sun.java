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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Holds the calculated sun data.
 *
 * @author Gerhard Riegler - Initial contribution
 */
@NonNullByDefault
public class Sun extends RiseSet implements Planet {

    private Map<SunPhase, Range> ranges = new HashMap<>();

    private Position position = Position.NONE;
    private Zodiac zodiac = Zodiac.NONE;
    private EclipseSet eclipseSet = EclipseSet.NONE;
    private Radiation radiation = Radiation.NONE;

    private @Nullable Season season = null;

    private @Nullable SunPhase sunPhase;

    private Circadian circadian = Circadian.NONE;

    /**
     * Returns the requested range.
     */
    @Nullable
    public Range getRange(SunPhase sunPhase) {
        return ranges.get(sunPhase);
    }

    /**
     * Sets the given range.
     */
    public void setRange(SunPhase sunPhase, Range range) {
        ranges.put(sunPhase, range);
    }

    /**
     * Sets the rise range.
     */
    @Override
    public void setRise(Range rise) {
        super.setRise(rise);
        ranges.put(SunPhase.SUN_RISE, rise);
    }

    /**
     * Sets the set range.
     */
    @Override
    public void setSet(Range set) {
        super.setSet(set);
        ranges.put(SunPhase.SUN_SET, set);
    }

    /**
     * Returns the sun position.
     */
    public Position getPosition() {
        return position;
    }

    /**
     * Returns the sun radiation
     */
    public Radiation getRadiation() {
        return radiation;
    }

    public void setRadiation(Radiation radiation) {
        this.radiation = radiation;
    }

    /**
     * Sets the sun position.
     */
    public void setPosition(Position position) {
        this.position = position;
    }

    /**
     * Returns the zodiac.
     */
    public Zodiac getZodiac() {
        return zodiac;
    }

    /**
     * Sets the zodiac.
     */
    public void setZodiac(Zodiac zodiac) {
        this.zodiac = zodiac;
    }

    /**
     * Returns the seasons.
     */
    @Nullable
    public Season getSeason() {
        return season;
    }

    /**
     * Sets the seasons.
     */
    public void setSeason(Season season) {
        this.season = season;
    }

    /**
     * Returns the eclipses.
     */
    public EclipseSet getEclipseSet() {
        return eclipseSet;
    }

    public void setEclipseSet(EclipseSet eclipseSet) {
        this.eclipseSet = eclipseSet;
    }

    /**
     * Returns the sun phase.
     */
    @Nullable
    public SunPhase getSunPhase() {
        return sunPhase;
    }

    /**
     * Sets the sun phase.
     */
    public void setSunPhase(@Nullable SunPhase sunPhase) {
        this.sunPhase = sunPhase;
    }

    /**
     * Returns all ranges of the sun.
     */
    public Map<SunPhase, Range> getAllRanges() {
        return ranges;
    }

    public Circadian getCircadian() {
        return circadian;
    }

    public void setCircadian(Circadian circadian) {
        this.circadian = circadian;
    }
}

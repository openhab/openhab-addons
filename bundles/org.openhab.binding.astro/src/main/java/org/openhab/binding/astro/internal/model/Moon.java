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

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Holds the calculated moon data.
 *
 * @author Gerhard Riegler - Initial contribution
 */
@NonNullByDefault
public class Moon extends RiseSet implements Planet {
    private final EnumMap<DistanceType, MoonDistance> distances = new EnumMap<>(DistanceType.class);

    private EclipseSet eclipseSet = EclipseSet.NONE;
    private MoonPhaseSet phaseSet = MoonPhaseSet.NONE;
    private Position position = MoonPosition.NONE;
    private Zodiac zodiac = Zodiac.NONE;

    public Moon() {
        EnumSet.allOf(DistanceType.class).forEach(d -> distances.put(d, MoonDistance.NONE));
    }

    /**
     * Returns the moon phase.
     */
    public MoonPhaseSet getPhaseSet() {
        return phaseSet;
    }

    /**
     * Sets the moon phase.
     */
    public void setPhaseSet(MoonPhaseSet phaseSet) {
        this.phaseSet = phaseSet;
    }

    public MoonDistance getDistanceType(DistanceType type) {
        return Objects.requireNonNull(distances.get(type));
    }

    public void setDistance(DistanceType type, MoonDistance moonDistance) {
        distances.put(type, moonDistance);
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
     * Returns the position.
     */
    public Position getPosition() {
        return position;
    }

    /**
     * Sets the position.
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
}

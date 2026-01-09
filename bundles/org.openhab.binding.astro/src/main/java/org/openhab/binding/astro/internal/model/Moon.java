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

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Holds the calculated moon data.
 *
 * @author Gerhard Riegler - Initial contribution
 */
@NonNullByDefault
public class Moon extends RiseSet implements Planet {
    private final Map<DistanceType, MoonDistance> distances = new HashMap<>(DistanceType.values().length);

    private MoonPhase phase = new MoonPhase();
    private Eclipse eclipse = new Eclipse(EclipseKind.PARTIAL, EclipseKind.TOTAL);
    private Position position = new Position();
    private Zodiac zodiac = Zodiac.NULL;

    public Moon() {
        EnumSet.allOf(DistanceType.class).forEach(d -> distances.put(d, MoonDistance.NULL));
    }

    /**
     * Returns the moon phase.
     */
    public MoonPhase getPhase() {
        return phase;
    }

    /**
     * Sets the moon phase.
     */
    public void setPhase(MoonPhase phase) {
        this.phase = phase;
    }

    public MoonDistance getDistanceType(DistanceType type) {
        return Objects.requireNonNull(distances.get(type));
    }

    /**
     * Returns the apogee.
     */
    public MoonDistance getApogee() {
        return getDistanceType(DistanceType.APOGEE);
    }

    /**
     * Returns the perigee.
     */
    public MoonDistance getPerigee() {
        return getDistanceType(DistanceType.PERIGEE);
    }

    public void setDistance(DistanceType type, MoonDistance moonDistance) {
        distances.put(type, moonDistance);
    }

    /**
     * Returns the eclipses.
     */
    public Eclipse getEclipse() {
        return eclipse;
    }

    /**
     * Sets the eclipses.
     */
    public void setEclipse(Eclipse eclipse) {
        this.eclipse = eclipse;
    }

    /**
     * Returns the current distance.
     */
    public MoonDistance getDistance() {
        return getDistanceType(DistanceType.CURRENT);
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

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
<<<<<<< Upstream, based on moon_distance
    private final Map<DistanceType, MoonDistance> distances = new HashMap<>(DistanceType.values().length);
=======
    private final Eclipse eclipse = new Eclipse(EclipseKind.PARTIAL, EclipseKind.TOTAL);
>>>>>>> 0596b7c Reworked sun and moon position Reworked eclipse calculations Transitioned these to Instant Added unit tests for eclipses

    private EclipseSet eclipseSet = EclipseSet.NONE;
    private MoonPhase phase = new MoonPhase();
<<<<<<< Upstream, based on main
    private Position position = MoonPosition.NONE;
    private Zodiac zodiac = Zodiac.NONE;
=======
<<<<<<< Upstream, based on moon_distance
    private Eclipse eclipse = new Eclipse(EclipseKind.PARTIAL, EclipseKind.TOTAL);
    private Position position = new Position();
=======
    private MoonDistance apogee = new MoonDistance();
    private MoonDistance perigee = new MoonDistance();
    private MoonDistance distance = new MoonDistance();
<<<<<<< Upstream, based on moon_distance
    private Position position = SunPosition.NULL;
>>>>>>> 0596b7c Reworked sun and moon position Reworked eclipse calculations Transitioned these to Instant Added unit tests for eclipses
=======
    private Position position = MoonPosition.NULL;
>>>>>>> b61414e Rebased. Corrected moon_day dynamic icons Reworked sun and moon position Reworked eclipse calculations Transitioned these to Instant Added unit tests for eclipses
    private Zodiac zodiac = Zodiac.NULL;
>>>>>>> 48a7069 Reworked sun and moon position Reworked eclipse calculations Transitioned these to Instant Added unit tests for eclipses

    public Moon() {
        EnumSet.allOf(DistanceType.class).forEach(d -> distances.put(d, MoonDistance.NONE));
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

    public void setDistance(DistanceType type, MoonDistance moonDistance) {
        distances.put(type, moonDistance);
    }

    /**
     * Returns the eclipses.
     */
    public EclipseSet getEclipseSet() {
        return eclipseSet;
    }

<<<<<<< Upstream, based on main
    public void setEclipseSet(EclipseSet eclipseSet) {
        this.eclipseSet = eclipseSet;
=======
    /**
     * Returns the current distance.
     */
    public MoonDistance getDistance() {
        return getDistanceType(DistanceType.CURRENT);
>>>>>>> 48a7069 Reworked sun and moon position Reworked eclipse calculations Transitioned these to Instant Added unit tests for eclipses
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

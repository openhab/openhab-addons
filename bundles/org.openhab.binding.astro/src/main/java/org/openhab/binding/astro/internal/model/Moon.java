/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

/**
 * Holds the calculated moon data.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class Moon extends RiseSet implements Planet {
    private MoonPhase phase = new MoonPhase();
    private MoonDistance apogee = new MoonDistance();
    private MoonDistance perigee = new MoonDistance();
    private MoonDistance distance = new MoonDistance();
    private Eclipse eclipse = new Eclipse(EclipseKind.PARTIAL, EclipseKind.TOTAL);
    private Position position = new Position();
    private Zodiac zodiac = new Zodiac(null);

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

    /**
     * Returns the apogee.
     */
    public MoonDistance getApogee() {
        return apogee;
    }

    /**
     * Sets the apogee.
     */
    public void setApogee(MoonDistance apogee) {
        this.apogee = apogee;
    }

    /**
     * Returns the perigee.
     */
    public MoonDistance getPerigee() {
        return perigee;
    }

    /**
     * Sets the perigee.
     */
    public void setPerigee(MoonDistance perigee) {
        this.perigee = perigee;
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
        return distance;
    }

    /**
     * Sets the current distance.
     */
    public void setDistance(MoonDistance distance) {
        this.distance = distance;
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

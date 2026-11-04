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
package org.openhab.binding.robonect.internal.model;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Blade information from the mower.
 *
 * @author Christian Jonak-Moechel - Initial contribution
 */
@NonNullByDefault
public class Blades {

    private int quality;

    private int hours;

    private int days;

    /**
     * Returns the quality of the blades in %.
     *
     * @return the quality of the blades in %.
     */
    public int getQuality() {
        return quality;
    }

    /**
     * Returns the number of hours the blades have been in use.
     *
     * @return the number of hours the blades have been in use
     */
    public int getHours() {
        return hours;
    }

    /**
     * Returns the number of days since the blades were last changed.
     *
     * @return the number of days since the blades were last changed
     */
    public int getDays() {
        return days;
    }
}

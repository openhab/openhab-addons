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
     * @return - the quality of the blades in %.
     */
    public int getQuality() {
        return quality;
    }

    /**
     * @return - the hours of how many days the blades have been actively in use
     */
    public int getHours() {
        return hours;
    }

    /**
     * @return - the days since the blades have been changed the last time
     */
    public int getDays() {
        return days;
    }
}

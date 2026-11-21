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
import org.eclipse.jdt.annotation.Nullable;

/**
 * Measurements for one drive motor returned by the Robonect API.
 *
 * @author Nico Brüttner - Initial contribution
 */
@NonNullByDefault
public class DriveMotor {
    private @Nullable Integer power;
    private @Nullable Integer speed;
    private @Nullable Integer current;

    /**
     * Returns the drive motor power as a percentage.
     *
     * @return the drive motor power as a percentage, or {@code null} if absent
     */
    public @Nullable Integer getPower() {
        return power;
    }

    /**
     * Returns the drive motor speed in centimetres per second.
     *
     * @return the drive motor speed in centimetres per second, or {@code null} if absent
     */
    public @Nullable Integer getSpeed() {
        return speed;
    }

    /**
     * Returns the drive motor current in milliamperes.
     *
     * @return the drive motor current in milliamperes, or {@code null} if absent
     */
    public @Nullable Integer getCurrent() {
        return current;
    }
}

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
 * Blade motor measurements returned by the Robonect API.
 *
 * @author Nico Brüttner - Initial contribution
 */
@NonNullByDefault
public class BladeMotor {
    private @Nullable Integer speed;
    private @Nullable Integer current;
    private @Nullable Integer average;

    /**
     * Returns the blade motor speed in revolutions per minute.
     *
     * @return the blade motor speed in revolutions per minute, or {@code null} if absent
     */
    public @Nullable Integer getSpeed() {
        return speed;
    }

    /**
     * Returns the blade motor current in milliamperes.
     *
     * @return the blade motor current in milliamperes, or {@code null} if absent
     */
    public @Nullable Integer getCurrent() {
        return current;
    }

    /**
     * Returns the average blade motor value.
     *
     * @return the average blade motor value, or {@code null} if absent
     */
    public @Nullable Integer getAverage() {
        return average;
    }
}

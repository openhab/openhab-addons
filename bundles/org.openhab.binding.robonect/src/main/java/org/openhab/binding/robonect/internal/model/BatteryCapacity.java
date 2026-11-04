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
 * Battery capacity values returned by the Robonect API.
 *
 * @author Nico Brüttner - Initial contribution
 */
@NonNullByDefault
public class BatteryCapacity {

    private @Nullable Integer full;
    private @Nullable Integer remaining;

    /**
     * Returns the full battery capacity in milliampere-hours.
     *
     * @return the full battery capacity in milliampere-hours, or {@code null} if absent
     */
    public @Nullable Integer getFull() {
        return full;
    }

    /**
     * Returns the remaining battery capacity in milliampere-hours.
     *
     * @return the remaining battery capacity in milliampere-hours, or {@code null} if absent
     */
    public @Nullable Integer getRemaining() {
        return remaining;
    }
}

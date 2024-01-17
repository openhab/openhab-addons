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
package org.openhab.binding.airquality.internal.api;

import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link Index} enum lists standard ranges of AQI indices
 * along with their appreciation category.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public enum Index {
    ZERO(0, 50, Appreciation.GOOD),
    FIFTY(51, 100, Appreciation.MODERATE),
    ONE_HUNDRED(101, 150, Appreciation.UNHEALTHY_FSG),
    ONE_HUNDRED_FIFTY(151, 200, Appreciation.UNHEALTHY),
    TWO_HUNDRED(201, 300, Appreciation.VERY_UNHEALTHY),
    THREE_HUNDRED(301, 400, Appreciation.HAZARDOUS),
    FOUR_HUNDRED(401, 500, Appreciation.HAZARDOUS);

    private double min;
    private double max;
    private Appreciation category;

    Index(double min, double max, Appreciation category) {
        this.min = min;
        this.max = max;
        this.category = category;
    }

    public double getMin() {
        return min;
    }

    public double getSpan() {
        return max - min;
    }

    boolean contains(double idx) {
        return min <= idx && idx <= max;
    }

    public static @Nullable Index find(double idx) {
        return Stream.of(Index.values()).filter(i -> i.contains(idx)).findFirst().orElse(null);
    }

    public Appreciation getCategory() {
        return category;
    }
}

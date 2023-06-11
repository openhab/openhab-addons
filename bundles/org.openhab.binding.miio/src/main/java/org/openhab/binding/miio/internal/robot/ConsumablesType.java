/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.miio.internal.robot;

import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Vacuum Consumables
 *
 * @author Marcel Verpaalen - Initial contribution
 */
@NonNullByDefault
public enum ConsumablesType {
    MAIN_BRUSH(300, "Main Brush"),
    SIDE_BRUSH(200, "Side Brush"),
    FILTER(150, "Filter"),
    SENSOR(30, "Sensor"),
    UNKNOWN(0, "Unknown");

    private final int lifeTime;
    private final String description;

    ConsumablesType(int lifeTime, String description) {
        this.lifeTime = lifeTime;
        this.description = description;
    }

    public static double remainingHours(int usedSeconds, ConsumablesType consumableType) {
        return Math.max((double) consumableType.lifeTime - TimeUnit.SECONDS.toHours(usedSeconds), 0);
    }

    public static int remainingPercent(int usedSeconds, ConsumablesType consumableType) {
        return (int) (100D * remainingHours(usedSeconds, consumableType) / consumableType.lifeTime);
    }

    public int getLifeTime() {
        return lifeTime;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return description;
    }
}

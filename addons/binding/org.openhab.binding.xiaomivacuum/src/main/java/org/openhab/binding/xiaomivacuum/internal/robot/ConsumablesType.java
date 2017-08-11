/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.xiaomivacuum.internal.robot;

import java.util.concurrent.TimeUnit;

/**
 * Vacuum Consumables
 *
 * @author Marcel Verpaalen - Initial contribution
 */
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

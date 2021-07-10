/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.netatmo.internal.api.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.netatmo.internal.api.NetatmoConstants.BatteryState;

/**
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */

@NonNullByDefault
public class NAModule extends NAThing {
    private BatteryState batteryState = BatteryState.UNKNOWN;

    private int batteryPercent = -1;

    public int getBatteryPercent() {
        if (batteryPercent != -1) {
            return batteryPercent;
        }
        if (batteryState != BatteryState.UNKNOWN) {
            return batteryState.getLevel();
        }
        return -1;
    }

    public BatteryState getBatteryState() {
        return this.batteryState;
    }
}

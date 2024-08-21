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
package org.openhab.binding.netatmo.internal.api.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.netatmo.internal.api.data.NetatmoConstants.BatteryState;

/**
 * The {@link Module} holds status information of a Netatmo module.
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */

@NonNullByDefault
public class Module extends NAThing {
    private BatteryState batteryState = BatteryState.UNKNOWN;
    private int batteryPercent = -1;

    public int getBatteryPercent() {
        return batteryPercent != -1 ? batteryPercent : batteryState.level;
    }

    public BatteryState getBatteryState() {
        return batteryState;
    }
}

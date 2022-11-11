/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.boschshc.internal.services.batterylevel;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.boschshc.internal.devices.bridge.dto.DeviceServiceData;
import org.openhab.binding.boschshc.internal.devices.bridge.dto.Fault;
import org.openhab.binding.boschshc.internal.devices.bridge.dto.Faults;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * Possible battery levels.
 *
 * @author David Pace - Initial contribution
 *
 */
@NonNullByDefault
public enum BatteryLevel {
    OK,
    LOW_BATTERY,
    CRITICAL_LOW,
    CRITICALLY_LOW_BATTERY,
    NOT_AVAILABLE;

    /**
     * Derives a battery level by analyzing the fault elements in the given device service data.
     * <p>
     * Note that no fault elements are present when the battery level is OK.
     *
     * @param deviceServiceData a device service data model
     * @return the derived battery level
     */
    public static BatteryLevel fromDeviceServiceData(DeviceServiceData deviceServiceData) {
        Faults faults = deviceServiceData.faults;
        if (faults == null || faults.entries == null || faults.entries.isEmpty()) {
            return OK;
        }

        for (Fault faultEntry : faults.entries) {
            if ("warning".equalsIgnoreCase(faultEntry.category)) {
                BatteryLevel batteryLevelState = BatteryLevel.get(faultEntry.type);
                if (batteryLevelState != null) {
                    return batteryLevelState;
                }
            }
        }

        return OK;
    }

    /**
     * Returns the corresponding battery level for the given string or <code>null</code> if no state matches.
     *
     * @param identifier the battery level identifier
     *
     * @return the matching battery level or <code>null</code>
     */
    public static @Nullable BatteryLevel get(String identifier) {
        for (BatteryLevel batteryLevelState : values()) {
            if (batteryLevelState.toString().equalsIgnoreCase(identifier)) {
                return batteryLevelState;
            }
        }

        return null;
    }

    /**
     * Transforms a Bosch-specific battery level to a percentage for the <code>system.battery-level</code> channel.
     *
     * @return a percentage between 0 and 100 as integer
     */
    public State toState() {
        switch (this) {
            case LOW_BATTERY:
                return new DecimalType(10);
            case CRITICAL_LOW:
            case CRITICALLY_LOW_BATTERY:
                return new DecimalType(1);
            case NOT_AVAILABLE:
                return UnDefType.UNDEF;
            default:
                return new DecimalType(100);
        }
    }

    /**
     * Transforms a Bosch-specific battery level to an <code>ON</code>/<code>OFF</code> state for the
     * <code>system.low-battery</code> channel.
     * <p>
     * If the result is <code>ON</code>, the battery is low; if the result is <code>OFF</code> the battery level is OK.
     *
     * @return
     */
    public OnOffType toLowBatteryState() {
        switch (this) {
            case LOW_BATTERY:
            case CRITICAL_LOW:
            case CRITICALLY_LOW_BATTERY:
                return OnOffType.ON;
            default:
                return OnOffType.OFF;
        }
    }
}

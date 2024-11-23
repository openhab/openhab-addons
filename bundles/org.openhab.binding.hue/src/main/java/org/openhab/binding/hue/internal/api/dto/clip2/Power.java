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
package org.openhab.binding.hue.internal.api.dto.clip2;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hue.internal.api.dto.clip2.enums.BatteryStateType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.types.State;

import com.google.gson.annotations.SerializedName;

/**
 * DTO for CLIP 2 power state.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class Power {
    private @Nullable @SerializedName("battery_state") String batteryState;
    private @SerializedName("battery_level") int batteryLevel;

    public BatteryStateType getBatteryState() {
        String batteryState = this.batteryState;
        if (batteryState != null) {
            try {
                return BatteryStateType.valueOf(batteryState.toUpperCase());
            } catch (IllegalArgumentException e) {
            }
        }
        return BatteryStateType.CRITICAL;
    }

    public int getBatteryLevel() {
        return batteryLevel;
    }

    public State getBatteryLowState() {
        return OnOffType.from(getBatteryState() != BatteryStateType.NORMAL);
    }

    public State getBatteryLevelState() {
        return new DecimalType(getBatteryLevel());
    }
}

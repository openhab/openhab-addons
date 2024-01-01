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
package org.openhab.binding.somneo.internal.model;

import java.time.Duration;
import java.time.LocalTime;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

import com.google.gson.annotations.SerializedName;

/**
 * This class represents the alarm state from the API.
 *
 * @author Michael Myrcik - Initial contribution
 */
public class AlarmStateData {

    private static final int POWER_WAKE_ENABLED = 255;

    @SerializedName("prfen")
    private List<Boolean> enabled;

    @SerializedName("prfvs")
    private List<Boolean> configured;

    @SerializedName("pwrsv")
    private List<Integer> powerWake;

    public @NonNull State getEnabledState(int position) {
        final List<Boolean> enabled = this.enabled;
        if (enabled == null) {
            return UnDefType.NULL;
        }
        return OnOffType.from(enabled.get(position - 1));
    }

    public @NonNull State getConfiguredState(int position) {
        final List<Boolean> configured = this.configured;
        if (configured == null) {
            return UnDefType.NULL;
        }
        return OnOffType.from(configured.get(position - 1));
    }

    public @NonNull State getPowerWakeState(int position) {
        final List<Integer> powerWake = this.powerWake;
        if (powerWake == null) {
            return UnDefType.NULL;
        }
        position -= 1;
        position *= 3;
        return OnOffType.from((int) powerWake.get(position) == POWER_WAKE_ENABLED);
    }

    public @NonNull State getPowerWakeDelayState(int position, LocalTime alarmTime) {
        final State powerWakeState = getPowerWakeState(position);
        if (UnDefType.NULL.equals(powerWakeState)) {
            return UnDefType.NULL;
        }
        if (OnOffType.OFF.equals(powerWakeState)) {
            return QuantityType.valueOf(0, Units.MINUTE);
        }
        if (alarmTime == null) {
            return UnDefType.NULL;
        }

        final LocalTime powerWakeTime = getPowerWakeTime(position);
        if (powerWakeTime == null) {
            return UnDefType.NULL;
        }

        Duration delay = Duration.between(alarmTime, powerWakeTime);
        if (delay == null) {
            return UnDefType.NULL;
        }

        return QuantityType.valueOf(delay.toMinutes(), Units.MINUTE);
    }

    private LocalTime getPowerWakeTime(int position) {
        final List<Integer> powerWake = this.powerWake;
        if (powerWake == null) {
            return null;
        }
        position--;
        position *= 3;
        final Integer hour = powerWake.get(position + 1);
        final Integer minute = powerWake.get(position + 2);
        return LocalTime.of(hour, minute);
    }

    public int getAlarmCount() {
        final List<Boolean> configured = this.configured;
        if (configured == null) {
            return 0;
        }
        return configured.size();
    }
}

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
package org.openhab.binding.plugwise.internal.config;

import static org.openhab.binding.plugwise.internal.PlugwiseUtils.*;
import static org.openhab.binding.plugwise.internal.protocol.field.Sensitivity.MEDIUM;

import java.time.Duration;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.plugwise.internal.protocol.field.MACAddress;
import org.openhab.binding.plugwise.internal.protocol.field.Sensitivity;

/**
 * The {@link PlugwiseScanConfig} class represents the configuration for a Plugwise Scan.
 *
 * @author Wouter Born - Initial contribution
 */
@NonNullByDefault
public class PlugwiseScanConfig {

    private String macAddress = "";
    private String sensitivity = upperUnderscoreToLowerCamel(MEDIUM.name());
    private int switchOffDelay = 5; // minutes
    private boolean daylightOverride = false;
    private int wakeupInterval = 1440; // minutes (1 day)
    private int wakeupDuration = 10; // seconds
    private boolean recalibrate = false;
    private boolean updateConfiguration = true;

    public MACAddress getMACAddress() {
        return new MACAddress(macAddress);
    }

    public Sensitivity getSensitivity() {
        return Sensitivity.valueOf(lowerCamelToUpperUnderscore(sensitivity));
    }

    public Duration getSwitchOffDelay() {
        return Duration.ofMinutes(switchOffDelay);
    }

    public boolean isDaylightOverride() {
        return daylightOverride;
    }

    public Duration getWakeupInterval() {
        return Duration.ofMinutes(wakeupInterval);
    }

    public Duration getWakeupDuration() {
        return Duration.ofSeconds(wakeupDuration);
    }

    public boolean isRecalibrate() {
        return recalibrate;
    }

    public boolean isUpdateConfiguration() {
        return updateConfiguration;
    }

    public boolean equalScanParameters(PlugwiseScanConfig other) {
        return this.sensitivity.equals(other.sensitivity) && this.switchOffDelay == other.switchOffDelay
                && this.daylightOverride == other.daylightOverride;
    }

    public boolean equalSleepParameters(PlugwiseScanConfig other) {
        return this.wakeupInterval == other.wakeupInterval && this.wakeupDuration == other.wakeupDuration;
    }

    @Override
    public String toString() {
        return "PlugwiseScanConfig [macAddress=" + macAddress + ", sensitivity=" + sensitivity + ", switchOffDelay="
                + switchOffDelay + ", daylightOverride=" + daylightOverride + ", wakeupInterval=" + wakeupInterval
                + ", wakeupDuration=" + wakeupDuration + ", recalibrate=" + recalibrate + ", updateConfiguration="
                + updateConfiguration + "]";
    }
}

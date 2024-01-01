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
package org.openhab.binding.plugwise.internal.config;

import java.time.Duration;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.plugwise.internal.protocol.field.MACAddress;

/**
 * The {@link PlugwiseSwitchConfig} class represents the configuration for a Plugwise Switch.
 *
 * @author Wouter Born - Initial contribution
 */
@NonNullByDefault
public class PlugwiseSwitchConfig {

    private String macAddress = "";
    private int wakeupInterval = 1440; // minutes (1 day)
    private int wakeupDuration = 10; // seconds
    private boolean updateConfiguration = true;

    public MACAddress getMACAddress() {
        return new MACAddress(macAddress);
    }

    public Duration getWakeupInterval() {
        return Duration.ofMinutes(wakeupInterval);
    }

    public Duration getWakeupDuration() {
        return Duration.ofSeconds(wakeupDuration);
    }

    public boolean isUpdateConfiguration() {
        return updateConfiguration;
    }

    public boolean equalSleepParameters(PlugwiseSwitchConfig other) {
        return this.wakeupInterval == other.wakeupInterval && this.wakeupDuration == other.wakeupDuration;
    }

    @Override
    public String toString() {
        return "PlugwiseSwitchConfig [macAddress=" + macAddress + ", wakeupInterval=" + wakeupInterval
                + ", wakeupDuration=" + wakeupDuration + ", updateConfiguration=" + updateConfiguration + "]";
    }
}

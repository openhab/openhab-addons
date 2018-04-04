/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.plugwise.internal.config;

import java.time.Duration;

import org.openhab.binding.plugwise.internal.protocol.field.MACAddress;

/**
 * The {@link PlugwiseSwitchConfig} class represents the configuration for a Plugwise Switch.
 *
 * @author Wouter Born - Initial contribution
 */
public class PlugwiseSwitchConfig {

    private String macAddress;
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

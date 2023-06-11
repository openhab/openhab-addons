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
import static org.openhab.binding.plugwise.internal.config.PlugwiseRelayConfig.PowerStateChanging.COMMAND_SWITCHING;

import java.time.Duration;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.plugwise.internal.protocol.field.MACAddress;

/**
 * The {@link PlugwiseRelayConfig} class represents the configuration for a Plugwise relay device (Circle, Circle+,
 * Stealth).
 *
 * @author Wouter Born - Initial contribution
 */
@NonNullByDefault
public class PlugwiseRelayConfig {

    public enum PowerStateChanging {
        COMMAND_SWITCHING,
        ALWAYS_ON,
        ALWAYS_OFF
    }

    private String macAddress = "";
    private String powerStateChanging = upperUnderscoreToLowerCamel(COMMAND_SWITCHING.name());
    private boolean suppliesPower = false;
    private int measurementInterval = 60; // minutes
    private boolean temporarilyNotInNetwork = false;
    private boolean updateConfiguration = true;

    public MACAddress getMACAddress() {
        return new MACAddress(macAddress);
    }

    public PowerStateChanging getPowerStateChanging() {
        return PowerStateChanging.valueOf(lowerCamelToUpperUnderscore(powerStateChanging));
    }

    public boolean isSuppliesPower() {
        return suppliesPower;
    }

    public Duration getMeasurementInterval() {
        return Duration.ofMinutes(measurementInterval);
    }

    public boolean isTemporarilyNotInNetwork() {
        return temporarilyNotInNetwork;
    }

    public boolean isUpdateConfiguration() {
        return updateConfiguration;
    }

    @Override
    public String toString() {
        return "PlugwiseRelayConfig [macAddress=" + macAddress + ", powerStateChanging=" + powerStateChanging
                + ", suppliesPower=" + suppliesPower + ", measurementInterval=" + measurementInterval
                + ", temporarilyNotInNetwork=" + temporarilyNotInNetwork + ", updateConfiguration="
                + updateConfiguration + "]";
    }
}

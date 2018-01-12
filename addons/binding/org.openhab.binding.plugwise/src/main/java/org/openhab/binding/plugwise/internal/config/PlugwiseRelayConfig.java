/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.plugwise.internal.config;

import static com.google.common.base.CaseFormat.*;
import static org.openhab.binding.plugwise.internal.config.PlugwiseRelayConfig.PowerStateChanging.COMMAND_SWITCHING;

import java.time.Duration;

import org.openhab.binding.plugwise.internal.protocol.field.MACAddress;

/**
 * The {@link PlugwiseRelayConfig} class represents the configuration for a Plugwise relay device (Circle, Circle+,
 * Stealth).
 *
 * @author Wouter Born - Initial contribution
 */
public class PlugwiseRelayConfig {

    public enum PowerStateChanging {
        COMMAND_SWITCHING,
        ALWAYS_ON,
        ALWAYS_OFF;
    }

    private String macAddress;
    private String powerStateChanging = UPPER_UNDERSCORE.to(LOWER_CAMEL, COMMAND_SWITCHING.name());
    private boolean suppliesPower = false;
    private int measurementInterval = 60; // minutes
    private boolean temporarilyNotInNetwork = false;
    private boolean updateConfiguration = true;

    public MACAddress getMACAddress() {
        return new MACAddress(macAddress);
    }

    public PowerStateChanging getPowerStateChanging() {
        return PowerStateChanging.valueOf(LOWER_CAMEL.to(UPPER_UNDERSCORE, powerStateChanging));
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

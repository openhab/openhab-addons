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
package org.openhab.binding.plugwiseha.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link PlugwiseHAThingConfig} encapsulates the configuration options for
 * an instance of the {@link PlugwiseHAApplianceHandler} and the
 * {@link PlugwiseHAZoneHandler}
 *
 * @author Bas van Wetten - Initial contribution
 * @author Leo Siepel - finish initial contribution
 */
@NonNullByDefault
public class PlugwiseHAThingConfig {

    private String id = "";

    private int lowBatteryPercentage = 15;

    // Getters

    public String getId() {
        return id;
    }

    public int getLowBatteryPercentage() {
        return this.lowBatteryPercentage;
    }

    // Member methods

    public boolean isValid() {
        return !id.isBlank() && lowBatteryPercentage > 0 && lowBatteryPercentage < 100;
    }

    @Override
    public String toString() {
        return "PlugwiseHAThingConfig{id = " + id + ", lowBatteryPercentage = " + lowBatteryPercentage + "}";
    }
}

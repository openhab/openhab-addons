/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.sunsa.internal.domain;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Describes a Sunsa device.
 *
 * @author jirom - Initial contribution
 */
@Value.Immutable
@JsonSerialize(as = ImmutableDevice.class)
@JsonDeserialize(builder = ImmutableDevice.Builder.class)
@NonNullByDefault
public abstract class Device {
    @JsonProperty("idDevice")
    public abstract String getId();

    @Value.Default
    @JsonProperty("name")
    public String getName() {
        return "";
    }

    @Value.Default
    @JsonProperty("batteryPercentage")
    public String getRawBatteryLevel() {
        return "0";
    }

    @Value.Default
    @JsonProperty("isConnected")
    public boolean isConnected() {
        return false;
    }

    /**
     * Returns the position of the blinds, can be increments of 10 from -100 to 100, where 0 is open.
     */
    @Value.Default
    @JsonProperty("position")
    public int getRawPosition() {
        return 0;
    }

    @Value.Derived
    @JsonIgnore
    public int getBatteryLevel() {
        return Integer.valueOf(getRawBatteryLevel());
    }
}

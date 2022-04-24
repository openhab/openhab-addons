/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.groupepsa.internal.rest.api.dto;

import java.time.Duration;
import java.time.ZonedDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Arjan Mels - Initial contribution
 */
@NonNullByDefault
public class Charging {

    private @Nullable String chargingMode;
    private @Nullable Double chargingRate;
    private @Nullable ZonedDateTime nextDelayedTime;
    private @Nullable Boolean plugged;
    private @Nullable Duration remainingTime;
    private @Nullable String status;

    public @Nullable String getChargingMode() {
        return chargingMode;
    }

    public @Nullable Double getChargingRate() {
        return chargingRate;
    }

    public void setChargingRate(Double chargingRate) {
        this.chargingRate = chargingRate;
    }

    public @Nullable ZonedDateTime getNextDelayedTime() {
        return nextDelayedTime;
    }

    public @Nullable Boolean isPlugged() {
        return plugged;
    }

    public @Nullable Duration getRemainingTime() {
        return remainingTime;
    }

    public @Nullable String getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("chargingMode", chargingMode).append("chargingRate", chargingRate)
                .append("nextDelayedTime", nextDelayedTime).append("plugged", plugged)
                .append("remainingTime", remainingTime).append("status", status).toString();
    }
}

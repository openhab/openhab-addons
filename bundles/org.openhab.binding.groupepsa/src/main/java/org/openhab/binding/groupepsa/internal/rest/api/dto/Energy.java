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
package org.openhab.binding.groupepsa.internal.rest.api.dto;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Arjan Mels - Initial contribution
 */
@NonNullByDefault
public class Energy {

    private @Nullable ZonedDateTime updatedAt;
    private @Nullable ZonedDateTime createdAt;
    private @Nullable BigDecimal autonomy;
    private @Nullable BatteryStatus battery;
    private @Nullable Charging charging;
    private @Nullable BigDecimal consumption;
    private @Nullable BigDecimal level;
    private @Nullable BigDecimal residual;
    private @Nullable String type;

    public @Nullable ZonedDateTime getUpdatedAt() {
        return updatedAt;
    }

    public @Nullable ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public @Nullable BigDecimal getAutonomy() {
        return autonomy;
    }

    public @Nullable BatteryStatus getBattery() {
        return battery;
    }

    public @Nullable Charging getCharging() {
        return charging;
    }

    public @Nullable BigDecimal getConsumption() {
        return consumption;
    }

    public @Nullable BigDecimal getLevel() {
        return level;
    }

    public @Nullable BigDecimal getResidual() {
        return residual;
    }

    public @Nullable String getType() {
        return type;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("updatedAt", updatedAt).append("createdAt", createdAt)
                .append("autonomy", autonomy).append("battery", battery).append("charging", charging)
                .append("consumption", consumption).append("level", level).append("residual", residual)
                .append("type", type).toString();
    }
}

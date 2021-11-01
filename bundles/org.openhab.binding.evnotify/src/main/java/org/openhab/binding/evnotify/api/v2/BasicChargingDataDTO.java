/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.evnotify.api.v2;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;

import com.google.gson.annotations.SerializedName;

/**
 * Represents the basic data that is returned by evnotify v2 API.
 *
 * e.g.
 *
 * {
 * "soc_display": 93,
 * "soc_bms": 88.5,
 * "last_soc": 1631220014
 * }
 *
 * @author Michael Schmidt - Initial contribution
 */
public class BasicChargingDataDTO {

    @SerializedName("soc_display")
    public Float stateOfChargeDisplay;

    @SerializedName("soc_bms")
    public Float stateOfChargeBms;

    @SerializedName("last_soc")
    public Integer lastStateOfCharge;

    public Float getStateOfChargeDisplay() {
        return stateOfChargeDisplay;
    }

    public Float getStateOfChargeBms() {
        return stateOfChargeBms;
    }

    public OffsetDateTime getLastStateOfCharge() {
        return lastStateOfCharge == null ? null
                : OffsetDateTime.from(Instant.ofEpochMilli(lastStateOfCharge).atZone(ZoneId.of("Europe/Berlin")));
    }
}

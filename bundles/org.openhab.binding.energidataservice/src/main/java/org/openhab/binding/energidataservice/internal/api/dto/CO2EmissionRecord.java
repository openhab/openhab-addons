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
package org.openhab.binding.energidataservice.internal.api.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * Record as part of {@link CO2EmissionRecords} from Energi Data Service.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public record CO2EmissionRecord(@SerializedName("Minutes5UTC") Instant start,
        @SerializedName("CO2Emission") BigDecimal emission) {

    public Instant end() {
        return start.plus(5, ChronoUnit.MINUTES);
    }
}

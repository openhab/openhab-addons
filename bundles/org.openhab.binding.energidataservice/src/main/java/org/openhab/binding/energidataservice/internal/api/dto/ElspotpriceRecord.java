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
package org.openhab.binding.energidataservice.internal.api.dto;

import java.math.BigDecimal;
import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * Record as part of {@link ElspotpriceRecords} from Energi Data Service.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public record ElspotpriceRecord(@SerializedName("HourUTC") Instant hour,
        @SerializedName("SpotPriceDKK") BigDecimal spotPriceDKK,
        @SerializedName("SpotPriceEUR") BigDecimal spotPriceEUR) {
}

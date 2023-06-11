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
package org.openhab.binding.boschindego.internal.dto.response.weather;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;

import com.google.gson.annotations.SerializedName;

/**
 * Interval.
 * 
 * @author Jacob Laursen - Initial contribution
 */
public class Interval {

    @SerializedName("dateTime")
    public String date;

    public int intervalLength;

    @SerializedName("prrr")
    public int rain;

    @SerializedName("tt")
    public float temperature;

    public void setDate(final Instant date) {
        this.date = date.toString();
    }

    public Instant getDate() {
        try {
            return ZonedDateTime.parse(date).toInstant();
        } catch (final DateTimeParseException e) {
            // Ignored
        }
        return null;
    }
}

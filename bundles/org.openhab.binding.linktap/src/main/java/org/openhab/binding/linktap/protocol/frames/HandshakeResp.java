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
package org.openhab.binding.linktap.protocol.frames;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collection;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * The {@link HandshakeResp} informs the Gateway of the current date, time and weekday in response to
 * a HandshakeReq Frame.
 *
 * @provides Gw: Expects response of HandshakeResp, to inform the Gateway of the current local Date and Time
 * @replyTo HandshakeReq
 *
 * @author David Goodyear - Initial contribution
 */
@NonNullByDefault
public class HandshakeResp extends GatewayDeviceResponse {

    public HandshakeResp() {
    }

    /**
     * Defines the date in the format YYYYMMDD
     */
    @SerializedName("date")
    @Expose
    public String date = EMPTY_STRING;

    /**
     * Defines the time for the GW in the format HHMMSS
     */
    @SerializedName("time")
    @Expose
    public String time = EMPTY_STRING;

    /**
     * Defines the weekday for the GW
     * 1 represents Monday.... 7 represents Sunday
     */
    @SerializedName("wday")
    @Expose
    public int wday = DEFAULT_INT;

    static final String DATE_PATTERN = "yyyyMMdd";
    static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DATE_PATTERN);

    static final String TIME_PATTERN = "HHmmss";
    static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern(TIME_PATTERN);

    public Collection<ValidationError> getValidationErrors() {
        final Collection<ValidationError> errors = super.getValidationErrors();

        if (wday < 1 || wday > 7) {
            errors.add(new ValidationError("wday", "not in range 1 -> 7"));
        }
        try {
            LocalDate.parse(date, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            errors.add(new ValidationError("date", "is invalid"));
        }

        try {
            LocalTime.parse(time, TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            errors.add(new ValidationError("time", "is invalid"));
        }

        return errors;
    }
}

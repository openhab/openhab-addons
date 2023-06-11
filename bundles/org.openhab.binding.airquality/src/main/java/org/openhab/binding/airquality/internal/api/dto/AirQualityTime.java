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
package org.openhab.binding.airquality.internal.api.dto;

import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link AirQualityTime} is responsible for storing
 * the "time" node from the waqi.org JSON response
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class AirQualityTime {
    private String iso = ""; // ISO representation of the timestamp, including TZ

    /**
     * Get observation time
     *
     * @return {ZonedDateTime}
     */
    public ZonedDateTime getObservationTime() throws DateTimeParseException {
        return ZonedDateTime.parse(iso);
    }
}

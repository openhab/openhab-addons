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
package org.openhab.binding.energidataservice.internal.config;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.energidataservice.internal.api.ChargeTypeCode;
import org.openhab.binding.energidataservice.internal.api.DateQueryParameter;
import org.openhab.binding.energidataservice.internal.api.DateQueryParameterType;

/**
 * The {@link DatahubPriceConfiguration} class contains fields mapping channel configuration parameters.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class DatahubPriceConfiguration {

    /**
     * Comma-separated list of charge type codes, e.g. "CD,CD R".
     */
    public String chargeTypeCodes = "";

    /**
     * Comma-separated list of notes, e.g. "Nettarif C".
     */
    public String notes = "";

    /**
     * Query start date parameter expressed as either yyyy-mm-dd or one of StartOfDay, StartOfMonth or StartOfYear.
     */
    public String start = "";

    /**
     * Query start date offset expressed as an ISO 8601 duration.
     */
    public String offset = "";

    /**
     * Check if any filter values are provided.
     *
     * @return true if either charge type codes, notes or query start date is provided.
     */
    public boolean hasAnyFilterOverrides() {
        return !chargeTypeCodes.isBlank() || !notes.isBlank() || !start.isBlank();
    }

    /**
     * Get parsed set of charge type codes from comma-separated string.
     *
     * @return Set of charge type codes.
     */
    public Set<ChargeTypeCode> getChargeTypeCodes() {
        return chargeTypeCodes.isBlank() ? new HashSet<>()
                : new HashSet<ChargeTypeCode>(
                        Arrays.stream(chargeTypeCodes.split(",")).map(ChargeTypeCode::new).toList());
    }

    /**
     * Get parsed set of notes from comma-separated string.
     *
     * @return Set of notes.
     */
    public Set<String> getNotes() {
        return notes.isBlank() ? new HashSet<>() : new HashSet<String>(Arrays.asList(notes.split(",")));
    }

    /**
     * Get query start parameter.
     *
     * @return null if invalid, otherwise an initialized {@link DateQueryParameter}.
     */
    public @Nullable DateQueryParameter getStart() {
        if (start.isBlank()) {
            return DateQueryParameter.EMPTY;
        }
        Duration durationOffset = Duration.ZERO;
        if (!offset.isBlank()) {
            try {
                durationOffset = Duration.parse(offset);
            } catch (DateTimeParseException e) {
                return null;
            }
        }
        if (start.equals(DateQueryParameterType.START_OF_DAY.toString())) {
            return DateQueryParameter.of(DateQueryParameterType.START_OF_DAY, durationOffset);
        }
        if (start.equals(DateQueryParameterType.START_OF_MONTH.toString())) {
            return DateQueryParameter.of(DateQueryParameterType.START_OF_MONTH, durationOffset);
        }
        if (start.equals(DateQueryParameterType.START_OF_YEAR.toString())) {
            return DateQueryParameter.of(DateQueryParameterType.START_OF_YEAR, durationOffset);
        }
        try {
            return DateQueryParameter.of(LocalDate.parse(start));
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}

/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.millheat.internal.dto;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Body of {@code POST} and {@code PATCH /houses/&#123;houseId&#125;/mode/vacation}. Vacation mode
 * is cleared with a {@code DELETE} to the same path, which takes no body.
 *
 * @author Petter L. H. Eide - Initial contribution
 */
public record VacationModeRequest(@Nullable Long startDate, @Nullable Long endDate,
        @Nullable Double vacationTemperature, @Nullable String vacationModeType,
        @Nullable Boolean isVacationModeActive) {

    /** Vacation mode falls back to each room's away temperature. */
    public static final String TYPE_AWAY_TEMPERATURE = "use_away_temperature";
    /** Vacation mode uses the dedicated vacation temperature. */
    public static final String TYPE_VACATION_TEMPERATURE = "use_vacation_temperature";
}

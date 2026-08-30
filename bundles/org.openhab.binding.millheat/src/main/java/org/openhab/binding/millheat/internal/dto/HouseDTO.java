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
 * A house as returned by {@code GET /houses}. This carries the complete vacation state, so no
 * additional request is needed to populate the vacation channels of a home thing.
 *
 * @author Petter L. H. Eide - Initial contribution
 */
public record HouseDTO(String id, @Nullable String name, @Nullable String country, @Nullable String postalCode,
        @Nullable String timezone, @Nullable String ownerId, @Nullable String mode,
        @Nullable Boolean isVacationModeActive, @Nullable Long vacationStartDate, @Nullable Long vacationEndDate,
        @Nullable Double vacationTemperature, @Nullable String vacationModeType, @Nullable String overrideModeType,
        @Nullable Long overrideEndDate) {
}

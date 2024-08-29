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
package org.openhab.binding.visualcrossing.internal.api.dto;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Martin Grześlowski - Initial contribution
 */
@NonNullByDefault
public record WeatherResponse(@Nullable Integer queryCost, @Nullable Double latitude, @Nullable Double longitude,
        @Nullable String resolvedAddress, @Nullable String address, @Nullable String timezone,
        @Nullable Double tzoffset, @Nullable String description, @Nullable List<Day> days,
        @Nullable CurrentConditions currentConditions) implements Cost {
}

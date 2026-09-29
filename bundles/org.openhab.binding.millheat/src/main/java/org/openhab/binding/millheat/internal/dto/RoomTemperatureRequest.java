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
 * Body of {@code POST /rooms/&#123;roomId&#125;/temperature}. Fields left null are not changed.
 *
 * @author Petter L. H. Eide - Initial contribution
 */
public record RoomTemperatureRequest(@Nullable Double roomComfortTemperature, @Nullable Double roomSleepTemperature,
        @Nullable Double roomAwayTemperature) {
}

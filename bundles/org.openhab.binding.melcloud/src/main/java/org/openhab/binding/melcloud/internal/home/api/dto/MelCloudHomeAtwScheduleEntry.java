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
package org.openhab.binding.melcloud.internal.home.api.dto;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * One entry of an Air-to-Water unit's cloud schedule, as reported back to the client (assumed embedded in
 * {@link MelCloudHomeAtwUnit}). Provisional: modeled on a single captured payload and not confirmed by any API
 * documentation.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class MelCloudHomeAtwScheduleEntry {

    public String id = "";
    public List<String> days = List.of();
    public String time = "";
    public boolean power;
    public @Nullable Double setTankWaterTemperature;
    public boolean forcedHotWaterMode;
    // Unconfirmed against any API documentation (see class Javadoc) — observed only in a forum user's own capture.
    public boolean zone1Active;
    public boolean zone2Active;
    public boolean hotWaterActive;
    public @Nullable Double setTemperatureZone1;
    public @Nullable Double setTemperatureZone2;
    public @Nullable String operationModeZone1;
    public @Nullable String operationModeZone2;
}

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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Request body for {@code PUT /monitor/atwunit/{id}} (see ADR-003). Same full-payload-with-nulls contract as
 * {@link MelCloudHomeAtaControlRequest}.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class MelCloudHomeAtwControlRequest {

    public @Nullable Boolean power;
    public @Nullable Double setTankWaterTemperature;
    public @Nullable Boolean forcedHotWaterMode;
    public @Nullable Double setTemperatureZone1;
    public @Nullable Double setTemperatureZone2;
    public @Nullable String operationModeZone1;
    public @Nullable String operationModeZone2;
    public @Nullable Boolean inStandbyMode;
    public @Nullable Double setHeatFlowTemperatureZone1;
    public @Nullable Double setCoolFlowTemperatureZone1;
    public @Nullable Double setHeatFlowTemperatureZone2;
    public @Nullable Double setCoolFlowTemperatureZone2;
}

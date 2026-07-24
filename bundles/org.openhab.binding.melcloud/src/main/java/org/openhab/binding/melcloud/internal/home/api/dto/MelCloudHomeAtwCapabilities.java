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

/**
 * Air-to-Water unit capability flags, as returned nested under a unit's {@code capabilities} field by
 * {@code GET /context} (see ADR-003).
 *
 * <p>
 * Deliberately does <b>not</b> expose the server-reported temperature range fields
 * ({@code minSetTemperature}/{@code maxSetTemperature}/{@code minSetTankTemperature}/{@code maxSetTankTemperature}):
 * the reference implementation this ADR is based on flags these as unreliable and substitutes hardcoded safe
 * defaults instead — see {@code melCloudHomeAtwUnit.xml}'s static channel ranges.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class MelCloudHomeAtwCapabilities {

    public boolean hasHotWater = true;
    public boolean hasZone2;
    public boolean hasHalfDegrees;
    public boolean hasCoolingMode;
    public boolean hasMeasuredEnergyConsumption;
    public boolean hasMeasuredEnergyProduction;
    public boolean hasEstimatedEnergyConsumption = true;
    public boolean hasEstimatedEnergyProduction = true;
    public int ftcModel = 3;
}

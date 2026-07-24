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
 * Air-to-Air unit capability flags/limits, as returned nested under a unit's {@code capabilities} field by
 * {@code GET /context} (see ADR-003). Field names match the JSON wire format verbatim (Gson's default binding).
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class MelCloudHomeAtaCapabilities {

    public int numberOfFanSpeeds = 5;
    public double minTempHeat = 10.0;
    public double maxTempHeat = 31.0;
    public double minTempCoolDry = 16.0;
    public double maxTempCoolDry = 31.0;
    public boolean hasHalfDegreeIncrements = true;
    public boolean hasSwing = true;
    public boolean hasStandby;
    public boolean hasEnergyConsumedMeter;
}

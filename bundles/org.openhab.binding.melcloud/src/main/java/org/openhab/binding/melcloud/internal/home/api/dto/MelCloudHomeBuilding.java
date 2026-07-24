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

/**
 * A building, as returned nested under {@code buildings}/{@code guestBuildings} by {@code GET /context}
 * (see ADR-003).
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class MelCloudHomeBuilding {

    public String id = "";
    public String name = "";
    public List<MelCloudHomeAtaUnit> airToAirUnits = List.of();
    public List<MelCloudHomeAtwUnit> airToWaterUnits = List.of();
}

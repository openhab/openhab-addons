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
package org.openhab.binding.daikin.internal.api;

import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Holds information from the get_year_power_ex call.
 *
 * @author Lukas Agethen - Initial contribution
 *
 */
@NonNullByDefault
public class EnergyInfoYear {

    public Integer @Nullable [] energyHeatingThisYear;
    public Integer @Nullable [] energyCoolingThisYear;

    private EnergyInfoYear() {
    }

    public static EnergyInfoYear parse(String response) {
        Logger logger = LoggerFactory.getLogger(EnergyInfoYear.class);
        logger.trace("Parsing string: \"{}\"", response);

        Map<String, String> responseMap = InfoParser.parse(response);

        EnergyInfoYear info = new EnergyInfoYear();
        info.energyHeatingThisYear = Optional.ofNullable(responseMap.get("curr_year_heat"))
                .flatMap(value -> InfoParser.parseArrayOfInt(value, 12)).orElse(null);
        info.energyCoolingThisYear = Optional.ofNullable(responseMap.get("curr_year_cool"))
                .flatMap(value -> InfoParser.parseArrayOfInt(value, 12)).orElse(null);
        return info;
    }
}

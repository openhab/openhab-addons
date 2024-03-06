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
package org.openhab.binding.daikin.internal.api;

import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
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
    private static final Logger LOGGER = LoggerFactory.getLogger(EnergyInfoYear.class);

    public Optional<Integer[]> energyHeatingThisYear = Optional.empty();

    public Optional<Integer[]> energyCoolingThisYear = Optional.empty();

    private EnergyInfoYear() {
    }

    public static EnergyInfoYear parse(String response) {
        LOGGER.trace("Parsing string: \"{}\"", response);

        Map<String, String> responseMap = InfoParser.parse(response);

        EnergyInfoYear info = new EnergyInfoYear();
        info.energyHeatingThisYear = Optional.ofNullable(responseMap.get("curr_year_heat"))
                .flatMap(value -> InfoParser.parseArrayOfInt(value, 12));

        info.energyCoolingThisYear = Optional.ofNullable(responseMap.get("curr_year_cool"))
                .flatMap(value -> InfoParser.parseArrayOfInt(value, 12));

        return info;
    }
}

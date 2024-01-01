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
 * Holds information from the get_week_power_ex call.
 *
 * @author Wouter Denayer - Initial contribution
 *
 */
@NonNullByDefault
public class EnergyInfoDayAndWeek {
    public Optional<Double> energyHeatingToday = Optional.empty();
    public Optional<Double> energyHeatingThisWeek = Optional.empty();
    public Optional<Double> energyHeatingLastWeek = Optional.empty();
    public Optional<Double> energyCoolingToday = Optional.empty();
    public Optional<Double> energyCoolingThisWeek = Optional.empty();
    public Optional<Double> energyCoolingLastWeek = Optional.empty();

    private static final Logger LOGGER = LoggerFactory.getLogger(EnergyInfoDayAndWeek.class);

    private EnergyInfoDayAndWeek() {
    }

    public static EnergyInfoDayAndWeek parse(String response) {
        LOGGER.trace("Parsing string: \"{}\"", response);

        // /aircon/get_week_power_ex
        // ret=OK,s_dayw=0,week_heat=1/1/1/1/1/5/2/1/1/1/1/2/1/1,week_cool=0/0/0/0/0/0/0/0/0/0/0/0/0/0
        // week_heat=<today>/<today-1>/<today-2>/<today-3>/...
        Map<String, String> responseMap = InfoParser.parse(response);
        EnergyInfoDayAndWeek info = new EnergyInfoDayAndWeek();
        if ("OK".equals(responseMap.get("ret"))) {
            Optional<Integer> dayOfWeek = Optional.ofNullable(responseMap.get("s_dayw"))
                    .flatMap(value -> InfoParser.parseInt(value));

            if (dayOfWeek.isPresent()) {
                // Daikin API week starts on Sunday, ours on Monday
                int thisWeekLastDayIndex = (dayOfWeek.get().intValue() == 0) ? 7 : dayOfWeek.get().intValue();

                // get the heating info
                String[] heatingValues = responseMap.get("week_heat").split("/");
                info.energyHeatingToday = Optional.of(Double.parseDouble(heatingValues[0]) / 10);
                double thisWeekEnergy = 0;
                for (int i = 0; i < thisWeekLastDayIndex; i += 1) {
                    thisWeekEnergy += Integer.parseInt(heatingValues[i]);
                }
                double previousWeekEnergy = 0;
                for (int i = thisWeekLastDayIndex; i < thisWeekLastDayIndex + 7; i += 1) {
                    previousWeekEnergy += Integer.parseInt(heatingValues[i]);
                }
                info.energyHeatingThisWeek = Optional.of(thisWeekEnergy / 10);
                info.energyHeatingLastWeek = Optional.of(previousWeekEnergy / 10);

                // get the cooling info
                String[] coolingValues = responseMap.get("week_cool").split("/");
                info.energyCoolingToday = Optional.of(Double.parseDouble(coolingValues[0]) / 10);
                thisWeekEnergy = 0;
                for (int i = 0; i < thisWeekLastDayIndex; i += 1) {
                    thisWeekEnergy += Integer.parseInt(coolingValues[i]);
                }
                previousWeekEnergy = 0;
                for (int i = thisWeekLastDayIndex; i < thisWeekLastDayIndex + 7; i += 1) {
                    previousWeekEnergy += Integer.parseInt(coolingValues[i]);
                }
                info.energyCoolingThisWeek = Optional.of(thisWeekEnergy / 10);
                info.energyCoolingLastWeek = Optional.of(previousWeekEnergy / 10);
            }
        } else {
            LOGGER.debug("EnergyInfoDayAndWeek::parse() did not receive 'ret=OK' from adapter");
        }
        return info;
    }
}

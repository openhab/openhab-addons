/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.solarforecast.internal.forecastsolar;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.TreeMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.json.JSONObject;
import org.openhab.binding.solarforecast.internal.SolarForecast;
import org.openhab.binding.solarforecast.internal.SolarForecastBindingConstants;
import org.openhab.binding.solarforecast.internal.Utils;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ForecastSolarObject} holds complete data for forecast
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class ForecastSolarObject implements SolarForecast {
    private final Logger logger = LoggerFactory.getLogger(ForecastSolarObject.class);
    private static final double UNDEF = -1;
    private final TreeMap<LocalDateTime, Double> wattHourMap = new TreeMap<LocalDateTime, Double>();
    private final TreeMap<LocalDateTime, Double> wattMap = new TreeMap<LocalDateTime, Double>();
    private Optional<String> rawData = Optional.empty();
    private boolean valid = false;
    private LocalDateTime expirationDateTime;

    public ForecastSolarObject() {
        expirationDateTime = LocalDateTime.now();
    }

    public ForecastSolarObject(String content, LocalDateTime now, LocalDateTime expirationDate) {
        expirationDateTime = expirationDate;
        if (!content.equals(SolarForecastBindingConstants.EMPTY)) {
            rawData = Optional.of(content);
            JSONObject contentJson = new JSONObject(content);
            JSONObject resultJson = contentJson.getJSONObject("result");
            JSONObject wattHourJson = resultJson.getJSONObject("watt_hours");
            JSONObject wattJson = resultJson.getJSONObject("watts");
            Iterator<String> iter = wattHourJson.keys();
            // put all values of the current day into sorted tree map
            while (iter.hasNext()) {
                String dateStr = iter.next();
                // convert date time into machine readable format
                LocalDateTime ldt = LocalDateTime.parse(dateStr.replace(" ", "T"));
                wattHourMap.put(ldt, wattHourJson.getDouble(dateStr));
                wattMap.put(ldt, wattJson.getDouble(dateStr));
            }
            valid = true;
        }
    }

    public boolean isValid() {
        if (valid) {
            if (!wattHourMap.isEmpty()) {
                if (expirationDateTime.isAfter(LocalDateTime.now())) {
                    return true;
                } else {
                    logger.debug("Forecast data expired");
                }
            } else {
                logger.debug("Empty data map");
            }
        } else {
            logger.debug("No Forecast data available");
        }
        return false;
    }

    public double getActualValue(LocalDateTime now) {
        if (wattHourMap.isEmpty()) {
            return UNDEF;
        }
        Entry<LocalDateTime, Double> f = wattHourMap.floorEntry(now);
        Entry<LocalDateTime, Double> c = wattHourMap.ceilingEntry(now);
        if (f != null) {
            if (f.getKey().toLocalDate().equals(now.toLocalDate())) {
                if (c != null) {
                    if (c.getKey().toLocalDate().equals(now.toLocalDate())) {
                        // we're during suntime!
                        double production = c.getValue() - f.getValue();
                        int interpolation = now.getMinute() - f.getKey().getMinute();
                        double interpolationProduction = production * interpolation / 60;
                        double actualProduction = f.getValue() + interpolationProduction;
                        return actualProduction / 1000.0;
                    } else {
                        // ceiling from wrong date
                        return f.getValue() / 1000.0;
                    }
                } else {
                    // sun is down
                    return f.getValue() / 1000.0;
                }
            } else {
                // floor from wrong date
                return 0;
            }
        } else {
            // no floor - sun not rised yet
            return 0;
        }
    }

    public double getActualPowerValue(LocalDateTime now) {
        if (wattMap.isEmpty()) {
            return UNDEF;
        }
        double actualPowerValue = 0;
        Entry<LocalDateTime, Double> f = wattMap.floorEntry(now);
        Entry<LocalDateTime, Double> c = wattMap.ceilingEntry(now);
        if (f != null) {
            if (c != null) {
                // we're during suntime!
                double powerCeiling = c.getValue();
                double powerFloor = f.getValue();
                // calculate in minutes from floor to now, e.g. 20 minutes
                // => take 2/3 of floor and 1/3 of ceiling
                double interpolation = (now.getMinute() - f.getKey().getMinute()) / 60.0;
                actualPowerValue = ((1 - interpolation) * powerFloor) + (interpolation * powerCeiling);
                return actualPowerValue / 1000.0;
            } else {
                // sun is down
                return 0;
            }
        } else {
            // no floor - sun not rised yet
            return 0;
        }
    }

    private double getDayTotal(LocalDate ld) {
        if (rawData.isEmpty()) {
            return UNDEF;
        }
        JSONObject contentJson = new JSONObject(rawData.get());
        JSONObject resultJson = contentJson.getJSONObject("result");
        JSONObject wattsDay = resultJson.getJSONObject("watt_hours_day");

        if (wattsDay.has(ld.toString())) {
            return wattsDay.getDouble(ld.toString()) / 1000.0;
        }
        return UNDEF;
    }

    public double getDayTotal(LocalDateTime now, int offset) {
        if (rawData.isEmpty()) {
            return UNDEF;
        }
        LocalDate ld = now.plusDays(offset).toLocalDate();
        return getDayTotal(ld);
    }

    public double getRemainingProduction(LocalDateTime now) {
        if (wattHourMap.isEmpty()) {
            return UNDEF;
        }
        return getDayTotal(now, 0) - getActualValue(now);
    }

    @Override
    public String toString() {
        return "Expiration: " + expirationDateTime + ", Valid: " + valid + ", Data:" + wattHourMap;
    }

    // SolarForecast Interface
    @Override
    public State getDay(LocalDate localDate) {
        double measure = getDayTotal(localDate);
        logger.trace("Actions: deliver measure {}", measure);
        return Utils.getEnergyState(measure);
    }

    @Override
    public State getEnergy(LocalDateTime localDateTimeBegin, LocalDateTime localDateTimeEnd) {
        LocalDate beginDate = localDateTimeBegin.toLocalDate();
        LocalDate endDate = localDateTimeEnd.toLocalDate();
        double measure = UNDEF;
        if (beginDate.equals(endDate)) {
            measure = getDayTotal(localDateTimeEnd, 0) - getActualValue(localDateTimeBegin)
                    - getRemainingProduction(localDateTimeEnd);
        } else {
            measure = getRemainingProduction(localDateTimeBegin);
            beginDate = beginDate.plusDays(1);
            while (beginDate.isBefore(endDate)) {
                double day = getDayTotal(beginDate);
                if (day > 0) {
                    measure += day;
                }
                beginDate = beginDate.plusDays(1);
            }
            measure += getActualValue(localDateTimeEnd);
        }
        return Utils.getEnergyState(measure);
    }

    @Override
    public State getPower(LocalDateTime localDateTime) {
        double measure = getActualPowerValue(localDateTime);
        logger.trace("Actions: deliver measure {}", measure);
        return Utils.getPowerState(measure);
    }

    @Override
    public LocalDateTime getForecastBegin() {
        if (!wattHourMap.isEmpty()) {
            LocalDateTime ldt = wattHourMap.firstEntry().getKey();
            return ldt;
        }
        return LocalDateTime.MIN;
    }

    @Override
    public LocalDateTime getForecastEnd() {
        if (!wattHourMap.isEmpty()) {
            LocalDateTime ldt = wattHourMap.lastEntry().getKey();
            return ldt;
        }
        return LocalDateTime.MIN;
    }
}

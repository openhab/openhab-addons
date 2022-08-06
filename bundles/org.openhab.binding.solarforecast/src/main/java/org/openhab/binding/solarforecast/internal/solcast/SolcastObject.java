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
package org.openhab.binding.solarforecast.internal.solcast;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openhab.binding.solarforecast.internal.SolarForecast;
import org.openhab.binding.solarforecast.internal.SolarForecastBindingConstants;
import org.openhab.binding.solarforecast.internal.Utils;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SolcastObject} holds complete data for forecast
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class SolcastObject implements SolarForecast {
    private final Logger logger = LoggerFactory.getLogger(SolcastObject.class);
    private static final double UNDEF = -1;
    private final TreeMap<LocalDate, TreeMap<ZonedDateTime, Double>> dataMap = new TreeMap<LocalDate, TreeMap<ZonedDateTime, Double>>();
    private final TreeMap<LocalDate, TreeMap<ZonedDateTime, Double>> optimisticDataMap = new TreeMap<LocalDate, TreeMap<ZonedDateTime, Double>>();
    private final TreeMap<LocalDate, TreeMap<ZonedDateTime, Double>> pessimisticDataMap = new TreeMap<LocalDate, TreeMap<ZonedDateTime, Double>>();
    private Optional<JSONObject> rawData = Optional.of(new JSONObject());
    private ZonedDateTime expirationDateTime;
    private boolean valid = false;

    public SolcastObject() {
        // invalid forecast object
        expirationDateTime = ZonedDateTime.now(SolcastConstants.zonedId);
    }

    public SolcastObject(String content, ZonedDateTime zdt) {
        expirationDateTime = zdt;
        add(content);
    }

    public void join(String content) {
        add(content);
    }

    private void add(String content) {
        if (!content.equals(SolarForecastBindingConstants.EMPTY)) {
            valid = true;
            JSONObject contentJson = new JSONObject(content);
            JSONArray resultJsonArray;
            if (contentJson.has("forecasts")) {
                resultJsonArray = contentJson.getJSONArray("forecasts");
                rawData.get().put("forecasts", resultJsonArray);
            } else {
                resultJsonArray = contentJson.getJSONArray("estimated_actuals");
                rawData.get().put("estimated_actuals", resultJsonArray);
            }
            for (int i = 0; i < resultJsonArray.length(); i++) {
                JSONObject jo = resultJsonArray.getJSONObject(i);
                String periodEnd = jo.getString("period_end");
                LocalDate ld = getZdtFromUTC(periodEnd).toLocalDate();
                TreeMap<ZonedDateTime, Double> forecastMap = dataMap.get(ld);
                if (forecastMap == null) {
                    forecastMap = new TreeMap<ZonedDateTime, Double>();
                }
                forecastMap.put(getZdtFromUTC(periodEnd), jo.getDouble("pv_estimate"));
                dataMap.put(ld, forecastMap);

                if (jo.has("pv_estimate10")) {
                    TreeMap<ZonedDateTime, Double> pessimisticForecastMap = pessimisticDataMap.get(ld);
                    if (pessimisticForecastMap == null) {
                        pessimisticForecastMap = new TreeMap<ZonedDateTime, Double>();
                    }
                    pessimisticForecastMap.put(getZdtFromUTC(periodEnd), jo.getDouble("pv_estimate10"));
                    pessimisticDataMap.put(ld, pessimisticForecastMap);
                }
                if (jo.has("pv_estimate90")) {
                    TreeMap<ZonedDateTime, Double> optimisticForecastMap = optimisticDataMap.get(ld);
                    if (optimisticForecastMap == null) {
                        optimisticForecastMap = new TreeMap<ZonedDateTime, Double>();
                    }
                    optimisticForecastMap.put(getZdtFromUTC(periodEnd), jo.getDouble("pv_estimate90"));
                    optimisticDataMap.put(ld, optimisticForecastMap);
                }
            }
        }
    }

    public boolean isValid() {
        if (valid) {
            if (!dataMap.isEmpty()) {
                if (expirationDateTime.isAfter(ZonedDateTime.now(SolcastConstants.zonedId))) {
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

    public double getActualValue(ZonedDateTime now) {
        if (dataMap.isEmpty()) {
            return UNDEF;
        }
        LocalDate ld = now.toLocalDate();
        TreeMap<ZonedDateTime, Double> dtm = dataMap.get(ld);
        if (dtm == null) {
            return UNDEF;
        }
        double forecastValue = 0;
        Set<ZonedDateTime> keySet = dtm.keySet();
        for (ZonedDateTime key : keySet) {
            if (key.isBefore(now)) {
                // value are reported in PT30M = 30 minutes interval with kw value
                // for kw/h it's half the value
                Double addedValue = dtm.get(key);
                if (addedValue != null) {
                    forecastValue += addedValue.doubleValue() / 2;
                }
            }
        }

        Entry<ZonedDateTime, Double> f = dtm.floorEntry(now);
        Entry<ZonedDateTime, Double> c = dtm.ceilingEntry(now);
        if (f != null) {
            if (c != null) {
                // we're during suntime!
                double production = c.getValue();
                int interpolation = now.getMinute() - f.getKey().getMinute();
                double interpolationProduction = production * interpolation / 60;
                forecastValue += interpolationProduction;
                return forecastValue;
            } else {
                // sun is down
                return forecastValue;
            }
        } else {
            // no floor - sun not rised yet
            return 0;
        }
    }

    public double getActualPowerValue(ZonedDateTime now) {
        if (dataMap.isEmpty()) {
            return UNDEF;
        }
        LocalDate ld = now.toLocalDate();
        TreeMap<ZonedDateTime, Double> dtm = dataMap.get(ld);
        if (dtm == null) {
            return UNDEF;
        }
        double actualPowerValue = 0;
        Entry<ZonedDateTime, Double> f = dtm.floorEntry(now);
        Entry<ZonedDateTime, Double> c = dtm.ceilingEntry(now);
        if (f != null) {
            if (c != null) {
                // we're during suntime!
                double powerCeiling = c.getValue();
                double powerFloor = f.getValue();
                // calculate in minutes from floor to now, e.g. 20 minutes
                // => take 2/3 of floor and 1/3 of ceiling
                double interpolation = (now.getMinute() - f.getKey().getMinute()) / 60.0;
                actualPowerValue = ((1 - interpolation) * powerFloor) + (interpolation * powerCeiling);
                return Math.round(actualPowerValue * 1000) / 1000.0;
            } else {
                // sun is down
                return 0;
            }
        } else {
            // no floor - sun not rised yet
            return 0;
        }
    }

    public double getDayTotal(ZonedDateTime now, int offset) {
        LocalDate ld = now.plusDays(offset).toLocalDate();
        return getDayTotal(ld, offset);
    }

    private double getDayTotal(LocalDate now, int offset) {
        TreeMap<ZonedDateTime, Double> dtm = dataMap.get(now.plusDays(offset));
        if (dtm != null) {
            return getTotalValue(dtm);
        } else {
            return UNDEF;
        }
    }

    public double getOptimisticDayTotal(ZonedDateTime now, int offset) {
        LocalDate ld = now.plusDays(offset).toLocalDate();
        TreeMap<ZonedDateTime, Double> dtm = optimisticDataMap.get(ld);
        if (dtm != null) {
            return getTotalValue(dtm);
        } else {
            return UNDEF;
        }
    }

    public double getPessimisticDayTotal(ZonedDateTime now, int offset) {
        LocalDate ld = now.plusDays(offset).toLocalDate();
        TreeMap<ZonedDateTime, Double> dtm = pessimisticDataMap.get(ld);
        if (dtm != null) {
            return getTotalValue(dtm);
        } else {
            return UNDEF;
        }
    }

    private double getTotalValue(TreeMap<ZonedDateTime, Double> map) {
        double forecastValue = 0;
        Set<ZonedDateTime> keySet = map.keySet();
        for (ZonedDateTime key : keySet) {
            // value are reported in PT30M = 30 minutes interval with kw value
            // for kw/h it's half the value
            Double addedValue = map.get(key);
            if (addedValue != null) {
                forecastValue += addedValue.doubleValue() / 2;
            }
        }
        return forecastValue;
    }

    public double getRemainingProduction(ZonedDateTime now) {
        if (dataMap.isEmpty()) {
            return UNDEF;
        }
        return getDayTotal(now, 0) - getActualValue(now);
    }

    @Override
    public String toString() {
        return "Expiration: " + expirationDateTime + ", Valid: " + valid + ", Data:" + dataMap;
    }

    public String getRaw() {
        return rawData.get().toString();
    }

    public static ZonedDateTime getZdtFromUTC(String utc) {
        Instant timestamp = Instant.parse(utc);
        return timestamp.atZone(SolcastConstants.zonedId);
    }

    // SolarForecast Interface
    @Override
    public State getDay(LocalDate localDate) {
        double measure = getDayTotal(localDate, 0);
        return Utils.getEnergyState(measure);
    }

    @Override
    public State getEnergy(LocalDateTime localDateTimeBegin, LocalDateTime localDateTimeEnd) {
        ZonedDateTime zdtBegin = localDateTimeBegin.atZone(SolcastConstants.zonedId);
        ZonedDateTime zdtEnd = localDateTimeEnd.atZone(SolcastConstants.zonedId);
        LocalDate beginDate = zdtBegin.toLocalDate();
        LocalDate endDate = zdtEnd.toLocalDate();
        double measure = UNDEF;
        if (beginDate.equals(endDate)) {
            measure = getDayTotal(zdtEnd, 0) - getActualValue(zdtBegin) - getRemainingProduction(zdtEnd);
        } else {
            measure = getRemainingProduction(zdtBegin);
            beginDate = beginDate.plusDays(1);
            while (beginDate.isBefore(endDate)) {
                double day = getDayTotal(beginDate, 0);
                if (day > 0) {
                    measure += day;
                }
                beginDate = beginDate.plusDays(1);
            }
            measure += getActualValue(zdtEnd);
        }
        return Utils.getEnergyState(Math.round(measure * 1000) / 1000.0);
    }

    @Override
    public State getPower(LocalDateTime localDateTime) {
        ZonedDateTime zdt = localDateTime.atZone(SolcastConstants.zonedId);
        double measure = getActualPowerValue(zdt);
        return Utils.getPowerState(measure);
    }

    @Override
    public LocalDateTime getForecastBegin() {
        if (!dataMap.isEmpty()) {
            ZonedDateTime zdt = dataMap.firstEntry().getValue().firstEntry().getKey();
            return zdt.toLocalDateTime();
        }
        return LocalDateTime.MIN;
    }

    @Override
    public LocalDateTime getForecastEnd() {
        if (!dataMap.isEmpty()) {
            ZonedDateTime zdt = dataMap.lastEntry().getValue().lastEntry().getKey();
            return zdt.toLocalDateTime();
        }
        return LocalDateTime.MIN;
    }
}

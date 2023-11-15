/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
import java.util.Arrays;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openhab.binding.solarforecast.internal.SolarForecastBindingConstants;
import org.openhab.binding.solarforecast.internal.actions.SolarForecast;
import org.openhab.binding.solarforecast.internal.utils.Utils;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SolcastObject} holds complete data for forecast
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class SolcastObject implements SolarForecast {
    private static final double UNDEF = -1;
    private static final TreeMap<ZonedDateTime, Double> EMPTY_MAP = new TreeMap<ZonedDateTime, Double>();

    private final Logger logger = LoggerFactory.getLogger(SolcastObject.class);
    private final TreeMap<LocalDate, TreeMap<ZonedDateTime, Double>> estimationDataMap = new TreeMap<LocalDate, TreeMap<ZonedDateTime, Double>>();
    private final TreeMap<LocalDate, TreeMap<ZonedDateTime, Double>> optimisticDataMap = new TreeMap<LocalDate, TreeMap<ZonedDateTime, Double>>();
    private final TreeMap<LocalDate, TreeMap<ZonedDateTime, Double>> pessimisticDataMap = new TreeMap<LocalDate, TreeMap<ZonedDateTime, Double>>();
    private final TimeZoneProvider timeZoneProvider;

    private Optional<JSONObject> rawData = Optional.of(new JSONObject());
    private Instant expirationDateTime;
    private boolean valid = false;

    public enum QueryMode {
        Estimation("Estimation"),
        Optimistic(SolarForecast.OPTIMISTIC),
        Pessimistic(SolarForecast.PESSIMISTIC),
        Error("Error");

        String modeDescirption;

        QueryMode(String description) {
            modeDescirption = description;
        }

        @Override
        public String toString() {
            return modeDescirption;
        }
    }

    public SolcastObject(TimeZoneProvider tzp) {
        // invalid forecast object
        timeZoneProvider = tzp;
        expirationDateTime = Instant.now();
    }

    public SolcastObject(String content, Instant expiration, TimeZoneProvider tzp) {
        expirationDateTime = expiration;
        timeZoneProvider = tzp;
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

            // prepare data for raw channel
            if (contentJson.has("forecasts")) {
                resultJsonArray = contentJson.getJSONArray("forecasts");
                addJSONArray(resultJsonArray);
                rawData.get().put("forecasts", resultJsonArray);
            }
            if (contentJson.has("estimated_actuals")) {
                resultJsonArray = contentJson.getJSONArray("estimated_actuals");
                addJSONArray(resultJsonArray);
                rawData.get().put("estimated_actuals", resultJsonArray);
            }
        }
    }

    private void addJSONArray(JSONArray resultJsonArray) {
        // sort data into TreeMaps
        for (int i = 0; i < resultJsonArray.length(); i++) {
            JSONObject jo = resultJsonArray.getJSONObject(i);
            String periodEnd = jo.getString("period_end");
            LocalDate ld = getZdtFromUTC(periodEnd).toLocalDate();
            TreeMap<ZonedDateTime, Double> forecastMap = estimationDataMap.get(ld);
            if (forecastMap == null) {
                forecastMap = new TreeMap<ZonedDateTime, Double>();
                estimationDataMap.put(ld, forecastMap);
            }
            forecastMap.put(getZdtFromUTC(periodEnd), jo.getDouble("pv_estimate"));

            // fill pessimistic values
            TreeMap<ZonedDateTime, Double> pessimisticForecastMap = pessimisticDataMap.get(ld);
            if (pessimisticForecastMap == null) {
                pessimisticForecastMap = new TreeMap<ZonedDateTime, Double>();
                pessimisticDataMap.put(ld, pessimisticForecastMap);
            }
            if (jo.has("pv_estimate10")) {
                pessimisticForecastMap.put(getZdtFromUTC(periodEnd), jo.getDouble("pv_estimate10"));
            } else {
                pessimisticForecastMap.put(getZdtFromUTC(periodEnd), jo.getDouble("pv_estimate"));
            }

            // fill optimistic values
            TreeMap<ZonedDateTime, Double> optimisticForecastMap = optimisticDataMap.get(ld);
            if (optimisticForecastMap == null) {
                optimisticForecastMap = new TreeMap<ZonedDateTime, Double>();
                optimisticDataMap.put(ld, optimisticForecastMap);
            }
            if (jo.has("pv_estimate90")) {
                optimisticForecastMap.put(getZdtFromUTC(periodEnd), jo.getDouble("pv_estimate90"));
            } else {
                optimisticForecastMap.put(getZdtFromUTC(periodEnd), jo.getDouble("pv_estimate"));
            }
        }
    }

    public boolean isValid() {
        if (valid) {
            if (!estimationDataMap.isEmpty()) {
                if (expirationDateTime.isAfter(Instant.now())) {
                    return true;
                }
            }
        }
        return false;
    }

    public double getActualValue(ZonedDateTime query, QueryMode mode) {
        LocalDate ld = query.toLocalDate();
        TreeMap<ZonedDateTime, Double> dtm = getDataMap(ld, mode);
        if (dtm.isEmpty()) {
            return UNDEF;
        }
        double previousEstimate = 0;
        double forecastValue = 0;
        Set<ZonedDateTime> keySet = dtm.keySet();
        for (ZonedDateTime key : keySet) {
            if (key.isBefore(query) || query.isEqual(key)) {
                // value are reported in PT30M = 30 minutes interval with kw value
                // for kw/h it's half the value
                Double endValue = dtm.get(key);
                // production during period is half of previous and next value
                if (endValue != null) {
                    double addedValue = (endValue.doubleValue() + previousEstimate) / 2.0 / 2.0;
                    // System.out.println(
                    // "End " + endValue.doubleValue() + " Prev: " + previousEstimate + " Add " + addedValue);
                    forecastValue += addedValue;
                    previousEstimate = endValue.doubleValue();
                } else {
                    logger.trace("No estimation found for {}", key);
                }
            }
        }

        Entry<ZonedDateTime, Double> f = dtm.floorEntry(query);
        Entry<ZonedDateTime, Double> c = dtm.ceilingEntry(query);
        if (f != null) {
            if (c != null) {
                if (c.getValue() > 0) {
                    int interpolation = query.getMinute() - f.getKey().getMinute();
                    double interpolationProduction = getActualPowerValue(query, mode) * interpolation / 30.0 / 2.0;
                    forecastValue += interpolationProduction;
                    // System.out.println(interpolationProduction);
                    return forecastValue;
                } else {
                    // if ceiling value is 0 there's no further production in this period
                    return forecastValue;
                }
            } else {
                // if ceiling is null we're at the very end of the day
                return forecastValue;
            }
        } else {
            // if floor is null we're at the very beginning of the day => 0
            return 0;
        }
    }

    /**
     * Get power values
     */

    public double getActualPowerValue(ZonedDateTime query, QueryMode mode) {
        LocalDate ld = query.toLocalDate();
        TreeMap<ZonedDateTime, Double> dtm = getDataMap(ld, mode);
        if (dtm.isEmpty()) {
            return UNDEF;
        }
        double actualPowerValue = 0;
        Entry<ZonedDateTime, Double> f = dtm.floorEntry(query);
        Entry<ZonedDateTime, Double> c = dtm.ceilingEntry(query);
        if (f != null) {
            if (c != null) {
                double powerCeiling = c.getValue();
                if (powerCeiling > 0) {
                    double powerFloor = f.getValue();
                    // calculate in minutes from floor to now, e.g. 20 minutes from PT30M 30 minutes
                    // => take 1/3 of floor and 2/3 of ceiling
                    double interpolation = (query.getMinute() - f.getKey().getMinute()) / 30.0;
                    actualPowerValue = ((1 - interpolation) * powerFloor) + (interpolation * powerCeiling);
                    return actualPowerValue;
                } else {
                    // if power ceiling == 0 there's no production in this period
                    return 0;
                }
            } else {
                // if ceiling is null we're at the very end of this day => 0
                return 0;
            }
        } else {
            // if floor is null we're at the very beginning of this day => 0
            return 0;
        }
    }

    /**
     * Daily totals
     */

    public double getDayTotal(LocalDate query, QueryMode mode) {
        TreeMap<ZonedDateTime, Double> dtm = getDataMap(query, mode);
        if (dtm.isEmpty()) {
            return UNDEF;
        }
        double previousEstimate = 0;
        double forecastValue = 0;
        Set<ZonedDateTime> keySet = dtm.keySet();
        for (ZonedDateTime key : keySet) {
            // value are reported in PT30M = 30 minutes interval with kw value
            // for kw/h it's half the value
            Double endValue = dtm.get(key);
            // production during period is half of previous and next value
            if (endValue != null) {
                double addedValue = (endValue.doubleValue() + previousEstimate) / 2.0 / 2.0;
                // System.out.println(key + " add " + addedValue);
                forecastValue += addedValue;
                previousEstimate = endValue.doubleValue();
            } else {
                logger.trace("No estimation found for {}", key);
            }
        }
        return forecastValue;
    }

    public double getRemainingProduction(ZonedDateTime query, QueryMode mode) {
        LocalDate ld = query.toLocalDate();
        TreeMap<ZonedDateTime, Double> dtm = getDataMap(ld, mode);
        if (dtm.isEmpty()) {
            return UNDEF;
        }
        return getDayTotal(query.toLocalDate(), mode) - getActualValue(query, mode);
    }

    @Override
    public String toString() {
        return "Expiration: " + expirationDateTime + ", Valid: " + valid + ", Data:" + estimationDataMap;
    }

    public String getRaw() {
        return rawData.get().toString();
    }

    private TreeMap<ZonedDateTime, Double> getDataMap(LocalDate ld, QueryMode mode) {
        TreeMap<ZonedDateTime, Double> returnMap = EMPTY_MAP;
        switch (mode) {
            case Estimation:
                returnMap = estimationDataMap.get(ld);
                break;
            case Optimistic:
                returnMap = optimisticDataMap.get(ld);
                break;
            case Pessimistic:
                returnMap = pessimisticDataMap.get(ld);
                break;
            case Error:
                // nothing to do
                break;
            default:
                // nothing to do
                break;
        }
        if (returnMap == null) {
            return EMPTY_MAP;
        }
        return returnMap;
    }

    /**
     * SolarForecast Interface
     */
    @Override
    public State getDay(LocalDate localDate, String... args) {
        QueryMode mode = evalArguments(args);
        if (mode.equals(QueryMode.Error)) {
            return UnDefType.UNDEF;
        } else if (mode.equals(QueryMode.Optimistic) || mode.equals(QueryMode.Pessimistic)) {
            if (localDate.isBefore(LocalDate.now())) {
                logger.info("{} forecasts only available for future", mode);
                return UnDefType.UNDEF;
            }
        }
        double measure = getDayTotal(localDate, mode);
        return Utils.getEnergyState(measure);
    }

    @Override
    public State getEnergy(LocalDateTime localDateTimeBegin, LocalDateTime localDateTimeEnd, String... args) {
        if (localDateTimeEnd.isBefore(localDateTimeBegin)) {
            logger.info("End {} defined before Start {}", localDateTimeEnd, localDateTimeBegin);
            return UnDefType.UNDEF;
        }
        QueryMode mode = evalArguments(args);
        if (mode.equals(QueryMode.Error)) {
            return UnDefType.UNDEF;
        } else if (mode.equals(QueryMode.Optimistic) || mode.equals(QueryMode.Pessimistic)) {
            if (localDateTimeEnd.isBefore(LocalDateTime.now())) {
                logger.info("{} forecasts only available for future", mode);
                return UnDefType.UNDEF;
            }
        }
        ZonedDateTime zdtBegin = localDateTimeBegin.atZone(timeZoneProvider.getTimeZone());
        ZonedDateTime zdtEnd = localDateTimeEnd.atZone(timeZoneProvider.getTimeZone());
        LocalDate beginDate = zdtBegin.toLocalDate();
        LocalDate endDate = zdtEnd.toLocalDate();
        double measure = UNDEF;
        if (beginDate.isEqual(endDate)) {
            measure = getDayTotal(zdtEnd.toLocalDate(), mode) - getActualValue(zdtBegin, mode)
                    - getRemainingProduction(zdtEnd, mode);
        } else {
            measure = getRemainingProduction(zdtBegin, mode);
            beginDate = beginDate.plusDays(1);
            while (beginDate.isBefore(endDate) && measure >= 0) {
                double day = getDayTotal(beginDate, mode);
                if (day > 0) {
                    measure += day;
                }
                beginDate = beginDate.plusDays(1);
            }
            double lastDay = getActualValue(zdtEnd, mode);
            if (lastDay >= 0) {
                measure += lastDay;
            }
        }
        return Utils.getEnergyState(measure);
    }

    @Override
    public State getPower(LocalDateTime queryDateTime, String... args) {
        // eliminate error cases and return immediately
        QueryMode mode = evalArguments(args);
        if (mode.equals(QueryMode.Error)) {
            return UnDefType.UNDEF;
        } else if (mode.equals(QueryMode.Optimistic) || mode.equals(QueryMode.Pessimistic)) {
            if (queryDateTime.isBefore(LocalDateTime.now().minusMinutes(1))) {
                logger.info("{} forecasts only available for future", mode);
                return UnDefType.UNDEF;
            }
        }

        ZonedDateTime zdt = queryDateTime.atZone(timeZoneProvider.getTimeZone());
        double measure = getActualPowerValue(zdt, mode);
        return Utils.getPowerState(measure);
    }

    @Override
    public LocalDateTime getForecastBegin() {
        if (!estimationDataMap.isEmpty()) {
            ZonedDateTime zdt = estimationDataMap.firstEntry().getValue().firstEntry().getKey();
            return zdt.toLocalDateTime();
        }
        return LocalDateTime.MIN;
    }

    @Override
    public LocalDateTime getForecastEnd() {
        if (!estimationDataMap.isEmpty()) {
            ZonedDateTime zdt = estimationDataMap.lastEntry().getValue().lastEntry().getKey();
            return zdt.toLocalDateTime();
        }
        return LocalDateTime.MIN;
    }

    private QueryMode evalArguments(String[] args) {
        if (args.length > 0) {
            if (args.length > 1) {
                logger.info("Too many arguments {}", Arrays.toString(args));
                return QueryMode.Error;
            }

            if (SolarForecast.OPTIMISTIC.equals(args[0])) {
                return QueryMode.Optimistic;
            } else if (SolarForecast.PESSIMISTIC.equals(args[0])) {
                return QueryMode.Pessimistic;
            } else {
                logger.info("Argument {} not supported", args[0]);
                return QueryMode.Error;
            }
        } else {
            return QueryMode.Estimation;
        }
    }

    public ZonedDateTime getZdtFromUTC(String utc) {
        Instant timestamp = Instant.parse(utc);
        return timestamp.atZone(timeZoneProvider.getTimeZone());
    }

}

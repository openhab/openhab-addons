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
package org.openhab.binding.solarforecast.internal.forecastsolar;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.TreeMap;

import javax.measure.quantity.Energy;
import javax.measure.quantity.Power;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.json.JSONException;
import org.json.JSONObject;
import org.openhab.binding.solarforecast.internal.SolarForecastBindingConstants;
import org.openhab.binding.solarforecast.internal.SolarForecastException;
import org.openhab.binding.solarforecast.internal.actions.SolarForecast;
import org.openhab.binding.solarforecast.internal.solcast.SolcastObject.QueryMode;
import org.openhab.binding.solarforecast.internal.utils.Utils;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.types.TimeSeries;
import org.openhab.core.types.TimeSeries.Policy;
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
    private final TreeMap<ZonedDateTime, Double> wattHourMap = new TreeMap<>();
    private final TreeMap<ZonedDateTime, Double> wattMap = new TreeMap<>();
    private final DateTimeFormatter dateInputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private DateTimeFormatter dateOutputFormatter = DateTimeFormatter
            .ofPattern(SolarForecastBindingConstants.PATTERN_FORMAT).withZone(ZoneId.systemDefault());
    private ZoneId zone = ZoneId.systemDefault();
    private Optional<String> rawData = Optional.empty();
    private Instant expirationDateTime;
    private String identifier;

    public ForecastSolarObject(String id) {
        expirationDateTime = Instant.now().minusSeconds(1);
        identifier = id;
    }

    public ForecastSolarObject(String id, String content, Instant expirationDate) throws SolarForecastException {
        expirationDateTime = expirationDate;
        identifier = id;
        if (!content.isEmpty()) {
            rawData = Optional.of(content);
            try {
                JSONObject contentJson = new JSONObject(content);
                JSONObject resultJson = contentJson.getJSONObject("result");
                JSONObject wattHourJson = resultJson.getJSONObject("watt_hours");
                JSONObject wattJson = resultJson.getJSONObject("watts");
                String zoneStr = contentJson.getJSONObject("message").getJSONObject("info").getString("timezone");
                zone = ZoneId.of(zoneStr);
                dateOutputFormatter = DateTimeFormatter.ofPattern(SolarForecastBindingConstants.PATTERN_FORMAT)
                        .withZone(zone);
                Iterator<String> iter = wattHourJson.keys();
                // put all values of the current day into sorted tree map
                while (iter.hasNext()) {
                    String dateStr = iter.next();
                    // convert date time into machine readable format
                    try {
                        ZonedDateTime zdt = LocalDateTime.parse(dateStr, dateInputFormatter).atZone(zone);
                        wattHourMap.put(zdt, wattHourJson.getDouble(dateStr));
                        wattMap.put(zdt, wattJson.getDouble(dateStr));
                    } catch (DateTimeParseException dtpe) {
                        logger.warn("Error parsing time {} Reason: {}", dateStr, dtpe.getMessage());
                        throw new SolarForecastException(this,
                                "Error parsing time " + dateStr + " Reason: " + dtpe.getMessage());
                    }
                }
            } catch (JSONException je) {
                throw new SolarForecastException(this,
                        "Error parsing JSON response " + content + " Reason: " + je.getMessage());
            }
        }
    }

    public boolean isExpired() {
        return expirationDateTime.isBefore(Instant.now());
    }

    public double getActualEnergyValue(ZonedDateTime queryDateTime) throws SolarForecastException {
        Entry<ZonedDateTime, Double> f = wattHourMap.floorEntry(queryDateTime);
        Entry<ZonedDateTime, Double> c = wattHourMap.ceilingEntry(queryDateTime);
        if (f != null && c == null) {
            // only floor available
            if (f.getKey().toLocalDate().equals(queryDateTime.toLocalDate())) {
                // floor has valid date
                return f.getValue() / 1000.0;
            } else {
                // floor date doesn't fit
                throwOutOfRangeException(queryDateTime.toInstant());
            }
        } else if (f == null && c != null) {
            if (c.getKey().toLocalDate().equals(queryDateTime.toLocalDate())) {
                // only ceiling from correct date available - no valid data reached yet
                return 0;
            } else {
                // ceiling date doesn't fit
                throwOutOfRangeException(queryDateTime.toInstant());
            }
        } else if (f != null && c != null) {
            // ceiling and floor available
            if (f.getKey().toLocalDate().equals(queryDateTime.toLocalDate())) {
                if (c.getKey().toLocalDate().equals(queryDateTime.toLocalDate())) {
                    // we're during suntime!
                    double production = c.getValue() - f.getValue();
                    long floorToCeilingDuration = Duration.between(f.getKey(), c.getKey()).toMinutes();
                    if (floorToCeilingDuration == 0) {
                        return f.getValue() / 1000.0;
                    }
                    long floorToQueryDuration = Duration.between(f.getKey(), queryDateTime).toMinutes();
                    double interpolation = (double) floorToQueryDuration / (double) floorToCeilingDuration;
                    double interpolationProduction = production * interpolation;
                    double actualProduction = f.getValue() + interpolationProduction;
                    return actualProduction / 1000.0;
                } else {
                    // ceiling from wrong date, but floor is valid
                    return f.getValue() / 1000.0;
                }
            } else {
                // floor invalid - ceiling not reached
                return 0;
            }
        } // else both null - date time doesn't fit to forecast data
        throwOutOfRangeException(queryDateTime.toInstant());
        return -1;
    }

    @Override
    public TimeSeries getEnergyTimeSeries(QueryMode mode) {
        TimeSeries ts = new TimeSeries(Policy.REPLACE);
        wattHourMap.forEach((timestamp, energy) -> {
            ts.add(timestamp.toInstant(), Utils.getEnergyState(energy / 1000.0));
        });
        return ts;
    }

    public double getActualPowerValue(ZonedDateTime queryDateTime) {
        double actualPowerValue = 0;
        Entry<ZonedDateTime, Double> f = wattMap.floorEntry(queryDateTime);
        Entry<ZonedDateTime, Double> c = wattMap.ceilingEntry(queryDateTime);
        if (f != null && c == null) {
            // only floor available
            if (f.getKey().toLocalDate().equals(queryDateTime.toLocalDate())) {
                // floor has valid date
                return f.getValue() / 1000.0;
            } else {
                // floor date doesn't fit
                throwOutOfRangeException(queryDateTime.toInstant());
            }
        } else if (f == null && c != null) {
            if (c.getKey().toLocalDate().equals(queryDateTime.toLocalDate())) {
                // only ceiling from correct date available - no valid data reached yet
                return 0;
            } else {
                // ceiling date doesn't fit
                throwOutOfRangeException(queryDateTime.toInstant());
            }
        } else if (f != null && c != null) {
            // we're during suntime!
            long floorToCeilingDuration = Duration.between(f.getKey(), c.getKey()).toMinutes();
            double powerFloor = f.getValue();
            if (floorToCeilingDuration == 0) {
                return powerFloor / 1000.0;
            }
            double powerCeiling = c.getValue();
            // calculate in minutes from floor to now, e.g. 20 minutes
            // => take 2/3 of floor and 1/3 of ceiling
            long floorToQueryDuration = Duration.between(f.getKey(), queryDateTime).toMinutes();
            double interpolation = (double) floorToQueryDuration / (double) floorToCeilingDuration;
            actualPowerValue = ((1 - interpolation) * powerFloor) + (interpolation * powerCeiling);
            return actualPowerValue / 1000.0;
        } // else both null - this shall not happen
        throwOutOfRangeException(queryDateTime.toInstant());
        return -1;
    }

    @Override
    public TimeSeries getPowerTimeSeries(QueryMode mode) {
        TimeSeries ts = new TimeSeries(Policy.REPLACE);
        wattMap.forEach((timestamp, power) -> {
            ts.add(timestamp.toInstant(), Utils.getPowerState(power / 1000.0));
        });
        return ts;
    }

    public double getDayTotal(LocalDate queryDate) {
        if (rawData.isEmpty()) {
            throw new SolarForecastException(this, "No forecast data available");
        }
        JSONObject contentJson = new JSONObject(rawData.get());
        JSONObject resultJson = contentJson.getJSONObject("result");
        JSONObject wattsDay = resultJson.getJSONObject("watt_hours_day");

        if (wattsDay.has(queryDate.toString())) {
            return wattsDay.getDouble(queryDate.toString()) / 1000.0;
        } else {
            throw new SolarForecastException(this,
                    "Day " + queryDate + " not available in forecast. " + getTimeRange());
        }
    }

    public double getRemainingProduction(ZonedDateTime queryDateTime) {
        double daily = getDayTotal(queryDateTime.toLocalDate());
        double actual = getActualEnergyValue(queryDateTime);
        return daily - actual;
    }

    public String getRaw() {
        if (rawData.isPresent()) {
            return rawData.get();
        }
        return "{}";
    }

    public ZoneId getZone() {
        return zone;
    }

    @Override
    public String toString() {
        return "Expiration: " + expirationDateTime + ", Data:" + wattHourMap;
    }

    /**
     * SolarForecast Interface
     */
    @Override
    public QuantityType<Energy> getDay(LocalDate localDate, String... args) throws IllegalArgumentException {
        if (args.length > 0) {
            throw new IllegalArgumentException("ForecastSolar doesn't accept arguments");
        }
        double measure = getDayTotal(localDate);
        return Utils.getEnergyState(measure);
    }

    @Override
    public QuantityType<Energy> getEnergy(Instant start, Instant end, String... args) throws IllegalArgumentException {
        if (args.length > 0) {
            throw new IllegalArgumentException("ForecastSolar doesn't accept arguments");
        }
        LocalDate beginDate = start.atZone(zone).toLocalDate();
        LocalDate endDate = end.atZone(zone).toLocalDate();
        double measure = -1;
        if (beginDate.equals(endDate)) {
            measure = getDayTotal(beginDate) - getActualEnergyValue(start.atZone(zone))
                    - getRemainingProduction(end.atZone(zone));
        } else {
            measure = getRemainingProduction(start.atZone(zone));
            beginDate = beginDate.plusDays(1);
            while (beginDate.isBefore(endDate) && measure >= 0) {
                double day = getDayTotal(beginDate);
                if (day > 0) {
                    measure += day;
                }
                beginDate = beginDate.plusDays(1);
            }
            double lastDay = getActualEnergyValue(end.atZone(zone));
            if (lastDay >= 0) {
                measure += lastDay;
            }
        }
        return Utils.getEnergyState(measure);
    }

    @Override
    public QuantityType<Power> getPower(Instant timestamp, String... args) throws IllegalArgumentException {
        if (args.length > 0) {
            throw new IllegalArgumentException("ForecastSolar doesn't accept arguments");
        }
        double measure = getActualPowerValue(timestamp.atZone(zone));
        return Utils.getPowerState(measure);
    }

    @Override
    public Instant getForecastBegin() {
        if (wattHourMap.isEmpty()) {
            return Instant.MAX;
        }
        ZonedDateTime zdt = wattHourMap.firstEntry().getKey();
        return zdt.toInstant();
    }

    @Override
    public Instant getForecastEnd() {
        if (wattHourMap.isEmpty()) {
            return Instant.MIN;
        }
        ZonedDateTime zdt = wattHourMap.lastEntry().getKey();
        return zdt.toInstant();
    }

    private void throwOutOfRangeException(Instant query) {
        if (getForecastBegin().equals(Instant.MAX) || getForecastEnd().equals(Instant.MIN)) {
            throw new SolarForecastException(this, "Forecast invalid time range");
        }
        if (query.isBefore(getForecastBegin())) {
            throw new SolarForecastException(this,
                    "Query " + dateOutputFormatter.format(query) + " too early. " + getTimeRange());
        } else if (query.isAfter(getForecastEnd())) {
            throw new SolarForecastException(this,
                    "Query " + dateOutputFormatter.format(query) + " too late. " + getTimeRange());
        } else {
            logger.warn("Query {} is fine. {}", dateOutputFormatter.format(query), getTimeRange());
        }
    }

    private String getTimeRange() {
        return "Valid range: " + dateOutputFormatter.format(getForecastBegin()) + " - "
                + dateOutputFormatter.format(getForecastEnd());
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }
}

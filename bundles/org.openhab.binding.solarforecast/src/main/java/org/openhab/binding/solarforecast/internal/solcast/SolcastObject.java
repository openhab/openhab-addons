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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openhab.binding.solarforecast.internal.SolarForecastBindingConstants;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
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
public class SolcastObject {
    private final Logger logger = LoggerFactory.getLogger(SolcastObject.class);
    private static final double UNDEF = -1;
    private final Map<LocalDate, TreeMap<LocalDateTime, Double>> dataMap = new HashMap<LocalDate, TreeMap<LocalDateTime, Double>>();
    private final Map<LocalDate, TreeMap<LocalDateTime, Double>> optimisticDataMap = new HashMap<LocalDate, TreeMap<LocalDateTime, Double>>();
    private final Map<LocalDate, TreeMap<LocalDateTime, Double>> pessimisticDataMap = new HashMap<LocalDate, TreeMap<LocalDateTime, Double>>();
    private Optional<JSONObject> rawData = Optional.of(new JSONObject());
    private LocalDateTime expirationDateTime;
    private boolean valid = false;

    public SolcastObject() {
        // invalid forecast object
        expirationDateTime = LocalDateTime.now();
    }

    public SolcastObject(String content, LocalDateTime ldt) {
        expirationDateTime = ldt;
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
                LocalDate ld = LocalDate.parse(periodEnd.substring(0, periodEnd.indexOf("T")));
                TreeMap<LocalDateTime, Double> forecastMap = dataMap.get(ld);
                if (forecastMap == null) {
                    forecastMap = new TreeMap<LocalDateTime, Double>();
                    LocalDateTime ldt = LocalDateTime.parse(periodEnd.substring(0, periodEnd.lastIndexOf(".")));
                    forecastMap.put(ldt, jo.getDouble("pv_estimate"));
                    dataMap.put(ld, forecastMap);
                } else {
                    LocalDateTime ldt = LocalDateTime.parse(periodEnd.substring(0, periodEnd.lastIndexOf(".")));
                    forecastMap.put(ldt, jo.getDouble("pv_estimate"));
                    dataMap.put(ld, forecastMap);
                }
                if (jo.has("pv_estimate10")) {
                    TreeMap<LocalDateTime, Double> pessimisticForecastMap = pessimisticDataMap.get(ld);
                    if (pessimisticForecastMap == null) {
                        pessimisticForecastMap = new TreeMap<LocalDateTime, Double>();
                        LocalDateTime ldt = LocalDateTime.parse(periodEnd.substring(0, periodEnd.lastIndexOf(".")));
                        pessimisticForecastMap.put(ldt, jo.getDouble("pv_estimate10"));
                        pessimisticDataMap.put(ld, pessimisticForecastMap);
                    } else {
                        LocalDateTime ldt = LocalDateTime.parse(periodEnd.substring(0, periodEnd.lastIndexOf(".")));
                        pessimisticForecastMap.put(ldt, jo.getDouble("pv_estimate10"));
                        pessimisticDataMap.put(ld, pessimisticForecastMap);
                    }
                }
                if (jo.has("pv_estimate90")) {
                    TreeMap<LocalDateTime, Double> optimisticForecastMap = optimisticDataMap.get(ld);
                    if (optimisticForecastMap == null) {
                        optimisticForecastMap = new TreeMap<LocalDateTime, Double>();
                        LocalDateTime ldt = LocalDateTime.parse(periodEnd.substring(0, periodEnd.lastIndexOf(".")));
                        optimisticForecastMap.put(ldt, jo.getDouble("pv_estimate90"));
                        optimisticDataMap.put(ld, optimisticForecastMap);
                    } else {
                        LocalDateTime ldt = LocalDateTime.parse(periodEnd.substring(0, periodEnd.lastIndexOf(".")));
                        optimisticForecastMap.put(ldt, jo.getDouble("pv_estimate90"));
                        optimisticDataMap.put(ld, optimisticForecastMap);
                    }
                }
            }
        }
    }

    public boolean isValid() {
        if (valid) {
            if (!dataMap.isEmpty()) {
                if (expirationDateTime.isAfter(LocalDateTime.now())) {
                    return true;
                } else {
                    logger.info("Forecast data expired");
                }
            } else {
                logger.info("Empty data map");
            }
        } else {
            logger.info("No Forecast data available");
        }
        return false;
    }

    public double getActualValue(LocalDateTime now) {
        if (dataMap.isEmpty()) {
            return UNDEF;
        }
        LocalDate ld = now.toLocalDate();
        TreeMap<LocalDateTime, Double> dtm = dataMap.get(ld);
        if (dtm == null) {
            return UNDEF;
        }
        double forecastValue = 0;
        Set<LocalDateTime> keySet = dtm.keySet();
        for (LocalDateTime key : keySet) {
            if (key.isBefore(now)) {
                // value are reported in PT30M = 30 minutes interval with kw value
                // for kw/h it's half the value
                Double addedValue = dtm.get(key);
                if (addedValue != null) {
                    forecastValue += addedValue.doubleValue() / 2;
                }
            }
        }

        Entry<LocalDateTime, Double> f = dtm.floorEntry(now);
        Entry<LocalDateTime, Double> c = dtm.ceilingEntry(now);
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

    public double getDayTotal(LocalDateTime now, int offset) {
        LocalDate ld = now.plusDays(offset).toLocalDate();
        TreeMap<LocalDateTime, Double> dtm = dataMap.get(ld);
        if (dtm != null) {
            return getTotalValue(dtm);
        } else {
            return -1;
        }
    }

    public double getOptimisticDayTotal(LocalDateTime now, int offset) {
        LocalDate ld = now.plusDays(offset).toLocalDate();
        TreeMap<LocalDateTime, Double> dtm = optimisticDataMap.get(ld);
        if (dtm != null) {
            return getTotalValue(dtm);
        } else {
            return -1;
        }
    }

    public double getPessimisticDayTotal(LocalDateTime now, int offset) {
        LocalDate ld = now.plusDays(offset).toLocalDate();
        TreeMap<LocalDateTime, Double> dtm = pessimisticDataMap.get(ld);
        if (dtm != null) {
            return getTotalValue(dtm);
        } else {
            return 0;
        }
    }

    private double getTotalValue(TreeMap<LocalDateTime, Double> map) {
        double forecastValue = 0;
        Set<LocalDateTime> keySet = map.keySet();
        for (LocalDateTime key : keySet) {
            // value are reported in PT30M = 30 minutes interval with kw value
            // for kw/h it's half the value
            Double addedValue = map.get(key);
            if (addedValue != null) {
                forecastValue += addedValue.doubleValue() / 2;
            }
        }
        return forecastValue;
    }

    public double getRemainingProduction(LocalDateTime now) {
        if (dataMap.isEmpty()) {
            return UNDEF;
        }
        return getDayTotal(now, 0) - getActualValue(now);
    }

    public static State getStateObject(double d) {
        if (d < 0) {
            return UnDefType.UNDEF;
        } else {
            return QuantityType.valueOf(d, Units.KILOWATT_HOUR);
        }
    }

    @Override
    public String toString() {
        return "Expiration: " + expirationDateTime + ", Valid: " + valid + ", Data:" + dataMap;
    }

    public String getRaw() {
        return rawData.get().toString();
    }
}

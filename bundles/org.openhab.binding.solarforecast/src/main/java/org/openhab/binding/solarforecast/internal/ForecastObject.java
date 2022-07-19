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
package org.openhab.binding.solarforecast.internal;

import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.measure.quantity.Energy;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.json.JSONObject;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;

/**
 * The {@link ForecastObject} holds complete data for forecast
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class ForecastObject {
    private final TreeMap<LocalDateTime, Double> dataMap = new TreeMap<LocalDateTime, Double>();
    private boolean valid = false;
    private int constructionHour;

    public ForecastObject(String content, LocalDateTime now) {
        constructionHour = now.getHour();
        JSONObject contentJson = new JSONObject(content);
        JSONObject resultJson = contentJson.getJSONObject("result");
        JSONObject wattsJson = resultJson.getJSONObject("watt_hours");
        Iterator<String> iter = wattsJson.keys();
        // put all values of the current day into sorted tree map
        while (iter.hasNext()) {
            String dateStr = iter.next();
            // convert date time into machine readable format
            LocalDateTime ldt = LocalDateTime.parse(dateStr.replace(" ", "T"));
            if (ldt.getDayOfMonth() == now.getDayOfMonth()) {
                dataMap.put(ldt, wattsJson.getDouble(dateStr));
            }
        }
        valid = true;
    }

    public ForecastObject() {
    }

    public boolean isValid() {
        return valid && constructionHour == LocalDateTime.now().getHour() && !dataMap.isEmpty();
    }

    public QuantityType<Energy> getCurrentValue(LocalDateTime now) {
        Entry<LocalDateTime, Double> f = dataMap.floorEntry(now);
        Entry<LocalDateTime, Double> c = dataMap.ceilingEntry(now);
        if (f != null) {
            if (c != null) {
                // we're during suntime!
                double production = c.getValue() - f.getValue();
                int interpolation = now.getMinute() - f.getKey().getMinute();
                double interpolationProduction = production * interpolation / 60;
                double actualProduction = f.getValue() + interpolationProduction;
                return QuantityType.valueOf(Math.round(actualProduction) / 1000.0, Units.KILOWATT_HOUR);
            } else {
                // sun is down
                return QuantityType.valueOf(Math.round(f.getValue()) / 1000.0, Units.KILOWATT_HOUR);
            }
        } else {
            // no floor - sun not rised yet
            return QuantityType.valueOf(0, Units.KILOWATT_HOUR);
        }
    }

    public QuantityType<Energy> getDayTotal() {
        if (dataMap.isEmpty()) {
            return QuantityType.valueOf(0, Units.KILOWATT_HOUR);
        }
        return QuantityType.valueOf(Math.round(dataMap.lastEntry().getValue()) / 1000.0, Units.KILOWATT_HOUR);
    }

    public QuantityType<Energy> getRemainingProduction(LocalDateTime now) {
        if (dataMap.isEmpty()) {
            return QuantityType.valueOf(0, Units.KILOWATT_HOUR);
        }
        return QuantityType.valueOf(
                Math.round(dataMap.lastEntry().getValue() - getCurrentValue(now).doubleValue()) / 1000.0,
                Units.KILOWATT_HOUR);
    }

    @Override
    public String toString() {
        return "Hour: " + constructionHour + ", Valid: " + valid + ", Data:" + dataMap;
    }
}

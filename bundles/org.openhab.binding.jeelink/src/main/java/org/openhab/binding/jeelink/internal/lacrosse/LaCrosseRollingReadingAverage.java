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
package org.openhab.binding.jeelink.internal.lacrosse;

import org.openhab.binding.jeelink.internal.RollingReadingAverage;

/**
 * Computes a rolling average of readings.
 *
 * @author Volker Bier - Initial contribution
 */
public class LaCrosseRollingReadingAverage extends RollingReadingAverage<LaCrosseTemperatureReading> {
    public LaCrosseRollingReadingAverage(int bufferSize) {
        super(new LaCrosseTemperatureReading[bufferSize]);
    }

    @Override
    protected LaCrosseTemperatureReading add(LaCrosseTemperatureReading value1, LaCrosseTemperatureReading value2) {
        if (value2 != null) {
            return new LaCrosseTemperatureReading(value2.getSensorId(), value2.getSensorType(), value2.getChannel(),
                    value1.getTemperature() + value2.getTemperature(), value1.getHumidity() + value2.getHumidity(),
                    value2.isBatteryNew(), value2.isBatteryLow());
        }

        return new LaCrosseTemperatureReading(value1.getSensorId(), value1.getSensorType(), value1.getChannel(),
                value1.getTemperature(), value1.getHumidity(), value1.isBatteryNew(), value1.isBatteryLow());
    }

    @Override
    protected LaCrosseTemperatureReading substract(LaCrosseTemperatureReading from, LaCrosseTemperatureReading value) {
        float newTemp = from.getTemperature();
        int newHum = from.getHumidity();

        if (value != null) {
            newTemp -= value.getTemperature();
            newHum -= value.getHumidity();
        }

        return new LaCrosseTemperatureReading(from.getSensorId(), from.getSensorType(), from.getChannel(), newTemp,
                newHum, from.isBatteryNew(), from.isBatteryLow());
    }

    @Override
    protected LaCrosseTemperatureReading divide(LaCrosseTemperatureReading value, int number) {
        return new LaCrosseTemperatureReading(value.getSensorId(), value.getSensorType(), value.getChannel(),
                value.getTemperature() / number, value.getHumidity() / number, value.isBatteryNew(),
                value.isBatteryLow());
    }
}

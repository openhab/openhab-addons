/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
            return new LaCrosseTemperatureReading(value2.getSensorId(), value2.getSensorType(),
                    value1.getTemperature() + value2.getTemperature(), value1.getHumidity() + value2.getHumidity(),
                    value2.isBatteryNew(), value2.isBatteryLow());
        }

        return new LaCrosseTemperatureReading(value1.getSensorId(), value1.getSensorType(), value1.getTemperature(),
                value1.getHumidity(), value1.isBatteryNew(), value1.isBatteryLow());
    }

    @Override
    protected LaCrosseTemperatureReading substract(LaCrosseTemperatureReading from, LaCrosseTemperatureReading value) {
        float newTemp = from.getTemperature();
        int newHum = from.getHumidity();

        if (value != null) {
            newTemp -= value.getTemperature();
            newHum -= value.getHumidity();
        }

        return new LaCrosseTemperatureReading(from.getSensorId(), from.getSensorType(), newTemp, newHum,
                from.isBatteryNew(), from.isBatteryLow());
    }

    @Override
    protected LaCrosseTemperatureReading divide(LaCrosseTemperatureReading value, int number) {
        return new LaCrosseTemperatureReading(value.getSensorId(), value.getSensorType(),
                value.getTemperature() / number, value.getHumidity() / number, value.isBatteryNew(),
                value.isBatteryLow());
    }
}

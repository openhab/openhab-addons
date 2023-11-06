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
package org.openhab.binding.jeelink.internal.lacrosse;

/**
 * Reading of a TX22 Temperature/Humidity Sensor.
 *
 * @author Volker Bier - Initial contribution
 */
public class Tx22Reading extends LaCrosseTemperatureReading {
    private Integer rain;
    private Float windDirection;
    private Float windSpeed;
    private Float windGust;
    private Integer pressure;

    public Tx22Reading(int sensorId, int sensorType, int channel, Float temp, Integer humidity, boolean batteryNew,
            boolean batteryLow, Integer rain, Float windDirection, Float windSpeed, Float windGust, Integer pressure) {
        super(String.valueOf(sensorId), sensorType, channel, temp, humidity, batteryNew, batteryLow);

        this.rain = rain;
        this.windDirection = windDirection;
        this.windSpeed = windSpeed;
        this.windGust = windGust;
        this.pressure = pressure;
    }

    public Integer getRain() {
        return rain;
    }

    public Float getWindDirection() {
        return windDirection;
    }

    public Float getWindSpeed() {
        return windSpeed;
    }

    public Float getWindGust() {
        return windGust;
    }

    public Integer getPressure() {
        return pressure;
    }

    public boolean hasWindGust() {
        return windGust != null;
    }

    public boolean hasWindSpeed() {
        return windSpeed != null;
    }

    public boolean hasWindDirection() {
        return windDirection != null;
    }

    public boolean hasPressure() {
        return pressure != null;
    }

    public boolean hasRain() {
        return rain != null;
    }

    public boolean hasHumidity() {
        return getHumidity() != null;
    }

    public boolean hasTemperature() {
        return getTemperature() != null;
    }

    @Override
    public String toString() {
        return super.toString() + " rain=" + rain + " windDirection=" + windDirection + " windSpeed=" + windSpeed
                + " windGust=" + windGust + " pressure=" + pressure;
    }
}

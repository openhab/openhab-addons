/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.jeelink.internal.lacrosse;

/**
 * Reading of a TX22 Temperature/Humidity Sensor.
 *
 * @author Volker Bier - Initial contribution
 */
public class Tx22Reading extends LaCrosseTemperatureReading {
    private int rain;
    private float windDirection;
    private float windSpeed;
    private float windGust;
    private float pressure;

    public Tx22Reading(int sensorId, int sensorType, int channel, float temp, int humidity, boolean batteryNew,
            boolean batteryLow, int rain, float windDirection, float windSpeed, float windGust, float pressure) {
        this(String.valueOf(sensorId), sensorType, channel, temp, humidity, batteryNew, batteryLow, rain, windDirection,
                windSpeed, windGust, pressure);
    }

    public Tx22Reading(String sensorId, int sensorType, int channel, float temp, int humidity, boolean batteryNew,
            boolean batteryLow, int rain, float windDirection, float windSpeed, float windGust, float pressure) {
        super(sensorId, sensorType, channel, temp, humidity, batteryNew, batteryLow);

        this.rain = rain;
        this.windDirection = windDirection;
        this.windSpeed = windSpeed;
        this.windGust = windGust;
        this.pressure = pressure;
    }

    public int getRain() {
        return rain;
    }

    public float getWindDirection() {
        return windDirection;
    }

    public float getWindSpeed() {
        return windSpeed;
    }

    public float getWindGust() {
        return windGust;
    }

    public float getPressure() {
        return pressure;
    }

    public boolean hasWindGust() {
        return windGust != Float.MAX_VALUE;
    }

    public boolean hasWindSpeed() {
        return windSpeed != Float.MAX_VALUE;
    }

    public boolean hasWindDirection() {
        return windDirection != Float.MAX_VALUE;
    }

    public boolean hasPressure() {
        return pressure != Float.MAX_VALUE;
    }

    public boolean hasRain() {
        return rain != Integer.MAX_VALUE;
    }

    public boolean hasHumidity() {
        return getHumidity() != Integer.MAX_VALUE;
    }

    public boolean hasTemperature() {
        return getTemperature() != Float.MAX_VALUE;
    }
}

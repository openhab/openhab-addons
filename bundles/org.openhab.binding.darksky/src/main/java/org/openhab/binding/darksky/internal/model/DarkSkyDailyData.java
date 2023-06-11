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
package org.openhab.binding.darksky.internal.model;

import java.util.List;

import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link DarkSkyDailyData} is the Java class used to map the JSON response to an Dark Sky request.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
public class DarkSkyDailyData {
    private String summary;
    private String icon;
    private @Nullable List<DailyData> data;

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public @Nullable List<DailyData> getData() {
        return data;
    }

    public void setData(@Nullable List<DailyData> data) {
        this.data = data;
    }

    public class DailyData {
        private int time;
        private String summary;
        private String icon;
        private int sunriseTime;
        private int sunsetTime;
        private double moonPhase;
        private double precipIntensity;
        private double precipIntensityMax;
        private int precipIntensityMaxTime;
        private double precipProbability;
        private String precipType;
        private double temperatureHigh;
        private int temperatureHighTime;
        private double temperatureLow;
        private int temperatureLowTime;
        private double apparentTemperatureHigh;
        private int apparentTemperatureHighTime;
        private double apparentTemperatureLow;
        private int apparentTemperatureLowTime;
        private double dewPoint;
        private double humidity;
        private double pressure;
        private double windSpeed;
        private double windGust;
        private int windGustTime;
        private int windBearing;
        private double cloudCover;
        private int uvIndex;
        private int uvIndexTime;
        private double visibility;
        private double ozone;
        private double temperatureMin;
        private int temperatureMinTime;
        private double temperatureMax;
        private int temperatureMaxTime;
        private double apparentTemperatureMin;
        private int apparentTemperatureMinTime;
        private double apparentTemperatureMax;
        private int apparentTemperatureMaxTime;
        private double precipAccumulation;

        public int getTime() {
            return time;
        }

        public void setTime(int time) {
            this.time = time;
        }

        public String getSummary() {
            return summary;
        }

        public void setSummary(String summary) {
            this.summary = summary;
        }

        public String getIcon() {
            return icon;
        }

        public void setIcon(String icon) {
            this.icon = icon;
        }

        public int getSunriseTime() {
            return sunriseTime;
        }

        public void setSunriseTime(int sunriseTime) {
            this.sunriseTime = sunriseTime;
        }

        public int getSunsetTime() {
            return sunsetTime;
        }

        public void setSunsetTime(int sunsetTime) {
            this.sunsetTime = sunsetTime;
        }

        public double getMoonPhase() {
            return moonPhase;
        }

        public void setMoonPhase(double moonPhase) {
            this.moonPhase = moonPhase;
        }

        public double getPrecipIntensity() {
            return precipIntensity;
        }

        public void setPrecipIntensity(double precipIntensity) {
            this.precipIntensity = precipIntensity;
        }

        public double getPrecipIntensityMax() {
            return precipIntensityMax;
        }

        public void setPrecipIntensityMax(double precipIntensityMax) {
            this.precipIntensityMax = precipIntensityMax;
        }

        public int getPrecipIntensityMaxTime() {
            return precipIntensityMaxTime;
        }

        public void setPrecipIntensityMaxTime(int precipIntensityMaxTime) {
            this.precipIntensityMaxTime = precipIntensityMaxTime;
        }

        public double getPrecipProbability() {
            return precipProbability;
        }

        public void setPrecipProbability(double precipProbability) {
            this.precipProbability = precipProbability;
        }

        public String getPrecipType() {
            return precipType;
        }

        public void setPrecipType(String precipType) {
            this.precipType = precipType;
        }

        public double getTemperatureHigh() {
            return temperatureHigh;
        }

        public void setTemperatureHigh(double temperatureHigh) {
            this.temperatureHigh = temperatureHigh;
        }

        public int getTemperatureHighTime() {
            return temperatureHighTime;
        }

        public void setTemperatureHighTime(int temperatureHighTime) {
            this.temperatureHighTime = temperatureHighTime;
        }

        public double getTemperatureLow() {
            return temperatureLow;
        }

        public void setTemperatureLow(double temperatureLow) {
            this.temperatureLow = temperatureLow;
        }

        public int getTemperatureLowTime() {
            return temperatureLowTime;
        }

        public void setTemperatureLowTime(int temperatureLowTime) {
            this.temperatureLowTime = temperatureLowTime;
        }

        public double getApparentTemperatureHigh() {
            return apparentTemperatureHigh;
        }

        public void setApparentTemperatureHigh(double apparentTemperatureHigh) {
            this.apparentTemperatureHigh = apparentTemperatureHigh;
        }

        public int getApparentTemperatureHighTime() {
            return apparentTemperatureHighTime;
        }

        public void setApparentTemperatureHighTime(int apparentTemperatureHighTime) {
            this.apparentTemperatureHighTime = apparentTemperatureHighTime;
        }

        public double getApparentTemperatureLow() {
            return apparentTemperatureLow;
        }

        public void setApparentTemperatureLow(double apparentTemperatureLow) {
            this.apparentTemperatureLow = apparentTemperatureLow;
        }

        public int getApparentTemperatureLowTime() {
            return apparentTemperatureLowTime;
        }

        public void setApparentTemperatureLowTime(int apparentTemperatureLowTime) {
            this.apparentTemperatureLowTime = apparentTemperatureLowTime;
        }

        public double getDewPoint() {
            return dewPoint;
        }

        public void setDewPoint(double dewPoint) {
            this.dewPoint = dewPoint;
        }

        public double getHumidity() {
            return humidity;
        }

        public void setHumidity(double humidity) {
            this.humidity = humidity;
        }

        public double getPressure() {
            return pressure;
        }

        public void setPressure(double pressure) {
            this.pressure = pressure;
        }

        public double getWindSpeed() {
            return windSpeed;
        }

        public void setWindSpeed(double windSpeed) {
            this.windSpeed = windSpeed;
        }

        public double getWindGust() {
            return windGust;
        }

        public void setWindGust(double windGust) {
            this.windGust = windGust;
        }

        public int getWindGustTime() {
            return windGustTime;
        }

        public void setWindGustTime(int windGustTime) {
            this.windGustTime = windGustTime;
        }

        public int getWindBearing() {
            return windBearing;
        }

        public void setWindBearing(int windBearing) {
            this.windBearing = windBearing;
        }

        public double getCloudCover() {
            return cloudCover;
        }

        public void setCloudCover(double cloudCover) {
            this.cloudCover = cloudCover;
        }

        public int getUvIndex() {
            return uvIndex;
        }

        public void setUvIndex(int uvIndex) {
            this.uvIndex = uvIndex;
        }

        public int getUvIndexTime() {
            return uvIndexTime;
        }

        public void setUvIndexTime(int uvIndexTime) {
            this.uvIndexTime = uvIndexTime;
        }

        public double getVisibility() {
            return visibility;
        }

        public void setVisibility(double visibility) {
            this.visibility = visibility;
        }

        public double getOzone() {
            return ozone;
        }

        public void setOzone(double ozone) {
            this.ozone = ozone;
        }

        public double getTemperatureMin() {
            return temperatureMin;
        }

        public void setTemperatureMin(double temperatureMin) {
            this.temperatureMin = temperatureMin;
        }

        public int getTemperatureMinTime() {
            return temperatureMinTime;
        }

        public void setTemperatureMinTime(int temperatureMinTime) {
            this.temperatureMinTime = temperatureMinTime;
        }

        public double getTemperatureMax() {
            return temperatureMax;
        }

        public void setTemperatureMax(double temperatureMax) {
            this.temperatureMax = temperatureMax;
        }

        public int getTemperatureMaxTime() {
            return temperatureMaxTime;
        }

        public void setTemperatureMaxTime(int temperatureMaxTime) {
            this.temperatureMaxTime = temperatureMaxTime;
        }

        public double getApparentTemperatureMin() {
            return apparentTemperatureMin;
        }

        public void setApparentTemperatureMin(double apparentTemperatureMin) {
            this.apparentTemperatureMin = apparentTemperatureMin;
        }

        public int getApparentTemperatureMinTime() {
            return apparentTemperatureMinTime;
        }

        public void setApparentTemperatureMinTime(int apparentTemperatureMinTime) {
            this.apparentTemperatureMinTime = apparentTemperatureMinTime;
        }

        public double getApparentTemperatureMax() {
            return apparentTemperatureMax;
        }

        public void setApparentTemperatureMax(double apparentTemperatureMax) {
            this.apparentTemperatureMax = apparentTemperatureMax;
        }

        public int getApparentTemperatureMaxTime() {
            return apparentTemperatureMaxTime;
        }

        public void setApparentTemperatureMaxTime(int apparentTemperatureMaxTime) {
            this.apparentTemperatureMaxTime = apparentTemperatureMaxTime;
        }

        public double getPrecipAccumulation() {
            return precipAccumulation;
        }

        public void setPrecipAccumulation(double precipAccumulation) {
            this.precipAccumulation = precipAccumulation;
        }
    }
}

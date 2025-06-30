/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.pirateweather.internal.model;

import java.util.List;

import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link PirateWeatherJsonWeatherData} is the Java class used to map the JSON response to an Pirate Weather
 * request.
 *
 * @author Scott Hanson - Pirate Weather convertion
 * @author Christoph Weitkamp - Initial contribution
 */
public class PirateWeatherJsonWeatherData {
    private double latitude;
    private double longitude;
    private String timezone;
    private PirateWeatherCurrentlyData currently;
    private PirateWeatherHourlyData hourly;
    private PirateWeatherDailyData daily;
    private @Nullable List<AlertsData> alerts;
    private int offset;

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public PirateWeatherCurrentlyData getCurrently() {
        return currently;
    }

    public void setCurrently(PirateWeatherCurrentlyData currently) {
        this.currently = currently;
    }

    public PirateWeatherHourlyData getHourly() {
        return hourly;
    }

    public void setHourly(PirateWeatherHourlyData hourly) {
        this.hourly = hourly;
    }

    public PirateWeatherDailyData getDaily() {
        return daily;
    }

    public void setDaily(PirateWeatherDailyData daily) {
        this.daily = daily;
    }

    public @Nullable List<AlertsData> getAlerts() {
        return alerts;
    }

    public void setAlerts(List<AlertsData> alerts) {
        this.alerts = alerts;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public class AlertsData {
        public String title;
        public int time;
        public int expires;
        public String description;
        public String severity;
        public String uri;
        public List<String> regions;
    }
}

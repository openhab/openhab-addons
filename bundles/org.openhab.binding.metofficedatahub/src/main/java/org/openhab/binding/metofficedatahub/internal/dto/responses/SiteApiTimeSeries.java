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
package org.openhab.binding.metofficedatahub.internal.dto.responses;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link SiteApiTimeSeries} is a Java class used as a DTO to hold part of the response to the Site Specific
 * API.
 *
 * @author David Goodyear - Initial contribution
 */
public class SiteApiTimeSeries {

    @SerializedName("time")
    private String time;

    public String getTime() {
        return time;
    }

    /**
     * Fields below relate to the hourly data-model
     */
    @SerializedName("screenTemperature")
    private Double screenTemperature;

    public Double getScreenTemperature() {
        return screenTemperature;
    }

    @SerializedName("maxScreenAirTemp")
    private Double maxScreenAirTemp;

    public Double getMaxScreenTemperature() {
        return maxScreenAirTemp;
    }

    @SerializedName("minScreenAirTemp")
    private Double minScreenAirTemp;

    public Double getMinScreenTemperature() {
        return minScreenAirTemp;
    }

    @SerializedName("screenDewPointTemperature")
    private Double screenDewPointTemperature;

    public Double getScreenDewPointTemperature() {
        return screenDewPointTemperature;
    }

    @SerializedName("feelsLikeTemperature")
    private Double feelsLikeTemperature;

    public Double getFeelsLikeTemperature() {
        return feelsLikeTemperature;
    }

    @SerializedName("windSpeed10m")
    private Double windSpeed10m;

    public Double getWindSpeed10m() {
        return windSpeed10m;
    }

    @SerializedName("windDirectionFrom10m")
    private Double windDirectionFrom10m;

    public Double getWindDirectionFrom10m() {
        return windDirectionFrom10m;
    }

    @SerializedName("max10mWindGust")
    private Double max10mWindGust;

    public Double getMax10mWindGust() {
        return max10mWindGust;
    }

    @SerializedName("windGustSpeed10m")
    private Double windGustSpeed10m;

    public Double getWindGustSpeed10m() {
        return windGustSpeed10m;
    }

    @SerializedName("visibility")
    private Integer visibility;

    public Integer getVisibility() {
        return visibility;
    }

    @SerializedName("screenRelativeHumidity")
    private Double screenRelativeHumidity;

    public Double getScreenRelativeHumidity() {
        return screenRelativeHumidity;
    }

    @SerializedName("mslp")
    private Integer pressure;

    public Integer getPressure() {
        return pressure;
    }

    @SerializedName("uvIndex")
    private Integer uvIndex;

    public Integer getUvIndex() {
        return uvIndex;
    }

    @SerializedName("significantWeatherCode")
    private Integer significantWeatherCode;

    public Integer getSignificantWeatherCode() {
        return significantWeatherCode;
    }

    @SerializedName("precipitationRate")
    private Double precipitationRate;

    public Double getPrecipitationRate() {
        return precipitationRate;
    }

    @SerializedName("totalPrecipAmount")
    private Double totalPrecipAmount;

    public Double getTotalPrecipAmount() {
        return totalPrecipAmount;
    }

    @SerializedName("totalSnowAmount")
    private Double totalSnowAmount;

    public Double getTotalSnowAmount() {
        return totalSnowAmount;
    }

    @SerializedName("probOfPrecipitation")
    private Double probOfPrecipitation;

    public Double getProbOfPrecipitation() {
        return probOfPrecipitation;
    }

    /**
     * Fields below relate to the daily data-model
     */

    @SerializedName("midday10MWindSpeed")
    private Double midday10MWindSpeed;

    public Double getMidday10MWindSpeed() {
        return midday10MWindSpeed;
    }

    @SerializedName("midnight10MWindSpeed")
    private Double midnight10MWindSpeed;

    public Double getMidnight10MWindSpeed() {
        return midnight10MWindSpeed;
    }

    @SerializedName("midday10MWindDirection")
    private Integer midday10MWindDirection;

    public Integer getMidday10MWindDirection() {
        return midday10MWindDirection;
    }

    @SerializedName("midnight10MWindDirection")
    private Integer midnight10MWindDirection;

    public Integer getMidnight10MWindDirection() {
        return midnight10MWindDirection;
    }

    @SerializedName("midday10MWindGust")
    private Double midday10MWindGust;

    public Double getMidday10MWindGust() {
        return midday10MWindGust;
    }

    @SerializedName("midnight10MWindGust")
    private Double midnight10MWindGust;

    public Double getMidnight10MWindGust() {
        return midnight10MWindGust;
    }

    @SerializedName("middayVisibility")
    private Integer middayVisibility;

    public Integer getMiddayVisibility() {
        return middayVisibility;
    }

    @SerializedName("midnightVisibility")
    private Integer midnightVisibility;

    public Integer getMidnightVisibility() {
        return midnightVisibility;
    }

    @SerializedName("middayRelativeHumidity")
    private Double middayRelativeHumidity;

    public Double getMiddayRelativeHumidity() {
        return middayRelativeHumidity;
    }

    @SerializedName("midnightRelativeHumidity")
    private Double midnightRelativeHumidity;

    public Double getMidnightRelativeHumidity() {
        return midnightRelativeHumidity;
    }

    @SerializedName("middayMslp")
    private Integer middayPressure;

    public Integer getMiddayPressure() {
        return middayPressure;
    }

    @SerializedName("midnightMslp")
    private Integer midnightPressure;

    public Integer getMidnightPressure() {
        return midnightPressure;
    }

    @SerializedName("maxUvIndex")
    private Integer maxUvIndex;

    public Integer getMaxUvIndex() {
        return maxUvIndex;
    }

    @SerializedName("daySignificantWeatherCode")
    private Integer daySignificantWeatherCode;

    public Integer getDaySignificantWeatherCode() {
        return daySignificantWeatherCode;
    }

    @SerializedName("nightSignificantWeatherCode")
    private Integer nightSignificantWeatherCode;

    public Integer getNightSignificantWeatherCode() {
        return nightSignificantWeatherCode;
    }

    @SerializedName("dayMaxScreenTemperature")
    private Double dayMaxScreenTemperature;

    public Double getDayMaxScreenTemperature() {
        return dayMaxScreenTemperature;
    }

    @SerializedName("nightMinScreenTemperature")
    private Double nightMinScreenTemperature;

    public Double getNightMinScreenTemperature() {
        return nightMinScreenTemperature;
    }

    @SerializedName("dayUpperBoundMaxTemp")
    private Double dayUpperBoundMaxTemp;

    public Double getDayUpperBoundMaxTemp() {
        return dayUpperBoundMaxTemp;
    }

    @SerializedName("dayUpperBoundMinTemp")
    private Double dayUpperBoundMinTemp;

    public Double getDayUpperBoundMinTemp() {
        return dayUpperBoundMinTemp;
    }

    @SerializedName("nightUpperBoundMinTemp")
    private Double nightUpperBoundMinTemp;

    public Double getNightUpperBoundMinTemp() {
        return nightUpperBoundMinTemp;
    }

    @SerializedName("dayLowerBoundMaxTemp")
    private Double dayLowerBoundMaxTemp;

    public Double getDayLowerBoundMaxTemp() {
        return dayLowerBoundMaxTemp;
    }

    @SerializedName("nightLowerBoundMinTemp")
    private Double nightLowerBoundMinTemp;

    public Double getNightLowerBoundMinTemp() {
        return nightLowerBoundMinTemp;
    }

    @SerializedName("dayMaxFeelsLikeTemp")
    private Double dayMaxFeelsLikeTemp;

    public Double getDayMaxFeelsLikeTemp() {
        return dayMaxFeelsLikeTemp;
    }

    @SerializedName("nightMinFeelsLikeTemp")
    private Double nightMinFeelsLikeTemp;

    public Double getNightMinFeelsLikeTemp() {
        return nightMinFeelsLikeTemp;
    }

    @SerializedName("dayUpperBoundMaxFeelsLikeTemp")
    private Double dayUpperBoundMaxFeelsLikeTemp;

    public Double getDayUpperBoundMaxFeelsLikeTemp() {
        return dayUpperBoundMaxFeelsLikeTemp;
    }

    @SerializedName("nightUpperBoundMinFeelsLikeTemp")
    private Double nightUpperBoundMinFeelsLikeTemp;

    public Double getNightUpperBoundMinFeelsLikeTemp() {
        return nightUpperBoundMinFeelsLikeTemp;
    }

    @SerializedName("dayLowerBoundMaxFeelsLikeTemp")
    private Double dayLowerBoundMaxFeelsLikeTemp;

    public Double getDayLowerBoundMaxFeelsLikeTemp() {
        return dayLowerBoundMaxFeelsLikeTemp;
    }

    @SerializedName("nightLowerBoundMinFeelsLikeTemp")
    private Double nightLowerBoundMinFeelsLikeTemp;

    public Double getNightLowerBoundMinFeelsLikeTemp() {
        return nightLowerBoundMinFeelsLikeTemp;
    }

    @SerializedName("dayProbabilityOfPrecipitation")
    private Double dayProbabilityOfPrecipitation;

    public Double getDayProbabilityOfPrecipitation() {
        return dayProbabilityOfPrecipitation;
    }

    @SerializedName("nightProbabilityOfPrecipitation")
    private Double nightProbabilityOfPrecipitation;

    public Double getNightProbabilityOfPrecipitation() {
        return nightProbabilityOfPrecipitation;
    }

    @SerializedName("dayProbabilityOfSnow")
    private Double dayProbabilityOfSnow;

    public Double getDayProbabilityOfSnow() {
        return dayProbabilityOfSnow;
    }

    @SerializedName("nightProbabilityOfSnow")
    private Double nightProbabilityOfSnow;

    public Double getNightProbabilityOfSnow() {
        return nightProbabilityOfSnow;
    }

    @SerializedName("dayProbabilityOfHeavySnow")
    private Double dayProbabilityOfHeavySnow;

    public Double getDayProbabilityOfHeavySnow() {
        return dayProbabilityOfHeavySnow;
    }

    @SerializedName("nightProbabilityOfHeavySnow")
    private Double nightProbabilityOfHeavySnow;

    public Double getNightProbabilityOfHeavySnow() {
        return nightProbabilityOfHeavySnow;
    }

    @SerializedName("dayProbabilityOfRain")
    private Double dayProbabilityOfRain;

    public Double getDayProbabilityOfRain() {
        return dayProbabilityOfRain;
    }

    @SerializedName("nightProbabilityOfRain")
    private Double nightProbabilityOfRain;

    public Double getNightProbabilityOfRain() {
        return nightProbabilityOfRain;
    }

    @SerializedName("dayProbabilityOfHeavyRain")
    private Double dayProbabilityOfHeavyRain;

    public Double getDayProbabilityOfHeavyRain() {
        return dayProbabilityOfHeavyRain;
    }

    @SerializedName("nightProbabilityOfHeavyRain")
    private Double nightProbabilityOfHeavyRain;

    public Double getNightProbabilityOfHeavyRain() {
        return nightProbabilityOfHeavyRain;
    }

    @SerializedName("dayProbabilityOfHail")
    private Double dayProbabilityOfHail;

    public Double getDayProbabilityOfHail() {
        return dayProbabilityOfHail;
    }

    @SerializedName("nightProbabilityOfHail")
    private Double nightProbabilityOfHail;

    public Double getNightProbabilityOfHail() {
        return nightProbabilityOfHail;
    }

    @SerializedName("dayProbabilityOfSferics")
    private Double dayProbabilityOfSferics;

    public Double getDayProbabilityOfSferics() {
        return dayProbabilityOfSferics;
    }

    @SerializedName("nightProbabilityOfSferics")
    private Double nightProbabilityOfSferics;

    public Double getNightProbabilityOfSferics() {
        return nightProbabilityOfSferics;
    }
}

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
package org.openhab.binding.meteoblue.internal.json;

import com.google.gson.annotations.SerializedName;

/**
 * {@link JsonDataDay} models the 'data_day' portion of the JSON
 * response to a weather request.
 *
 * @author Chris Carman - Initial contribution
 */
public class JsonDataDay {

    private String[] time;
    private Integer[] pictocode;

    @SerializedName("uvindex")
    private Integer[] uvIndex;

    @SerializedName("temperature_max")
    private Double[] temperatureMax;

    @SerializedName("temperature_min")
    private Double[] temperatureMin;

    @SerializedName("temperature_mean")
    private Double[] temperatureMean;

    @SerializedName("felttemperature_max")
    private Double[] feltTemperatureMax;

    @SerializedName("felttemperature_min")
    private Double[] feltTemperatureMin;

    @SerializedName("winddirection")
    private Integer[] windDirection;

    @SerializedName("precipitation_probability")
    private Integer[] precipitationProbability;

    private String[] rainspot;

    @SerializedName("predictability_class")
    private Integer[] predictabilityClass;

    private Integer[] predictability;

    private Double[] precipitation;

    @SerializedName("snowfraction")
    private Double[] snowFraction;

    @SerializedName("sealevelpressure_max")
    private Integer[] seaLevelPressureMax;

    @SerializedName("sealevelpressure_min")
    private Integer[] seaLevelPressureMin;

    @SerializedName("sealevelpressure_mean")
    private Integer[] seaLevelPressureMean;

    @SerializedName("windspeed_max")
    private Double[] windSpeedMax;

    @SerializedName("windspeed_mean")
    private Double[] windSpeedMean;

    @SerializedName("windspeed_min")
    private Double[] windSpeedMin;

    @SerializedName("relativehumidity_max")
    private Integer[] relativeHumidityMax;

    @SerializedName("relativehumidity_min")
    private Integer[] relativeHumidityMin;

    @SerializedName("relativehumidity_mean")
    private Integer[] relativeHumidityMean;

    @SerializedName("convective_precipitation")
    private Double[] convectivePrecipitation;

    @SerializedName("precipitation_hours")
    private Double[] precipitationHours;

    @SerializedName("humiditygreater90_hours")
    private Double[] humidityGreater90Hours;

    public JsonDataDay() {
    }

    public String[] getTime() {
        return time;
    }

    public Integer[] getPictocode() {
        return pictocode;
    }

    public Integer[] getUVIndex() {
        return uvIndex;
    }

    public Double[] getTemperatureMax() {
        return temperatureMax;
    }

    public Double[] getTemperatureMin() {
        return temperatureMin;
    }

    public Double[] getTemperatureMean() {
        return temperatureMean;
    }

    public Double[] getFeltTemperatureMax() {
        return feltTemperatureMax;
    }

    public Double[] getFeltTemperatureMin() {
        return feltTemperatureMin;
    }

    public Integer[] getWindDirection() {
        return windDirection;
    }

    public Integer[] getPrecipitationProbability() {
        return precipitationProbability;
    }

    public String[] getRainspot() {
        return rainspot;
    }

    public Integer[] getPredictabilityClass() {
        return predictabilityClass;
    }

    public Integer[] getPredictability() {
        return predictability;
    }

    public Double[] getPrecipitation() {
        return precipitation;
    }

    public Double[] getSnowFraction() {
        return snowFraction;
    }

    public Integer[] getSeaLevelPressureMax() {
        return seaLevelPressureMax;
    }

    public Integer[] getSeaLevelPressureMin() {
        return seaLevelPressureMin;
    }

    public Integer[] getSeaLevelPressureMean() {
        return seaLevelPressureMean;
    }

    public Double[] getWindSpeedMax() {
        return windSpeedMax;
    }

    public Double[] getWindSpeedMean() {
        return windSpeedMean;
    }

    public Double[] getWindSpeedMin() {
        return windSpeedMin;
    }

    public Integer[] getRelativeHumidityMax() {
        return relativeHumidityMax;
    }

    public Integer[] getRelativeHumidityMin() {
        return relativeHumidityMin;
    }

    public Integer[] getRelativeHumidityMean() {
        return relativeHumidityMean;
    }

    public Double[] getConvectivePrecipitation() {
        return convectivePrecipitation;
    }

    public Double[] getPrecipitationHours() {
        return precipitationHours;
    }

    public Double[] getHumidityGreater90Hours() {
        return humidityGreater90Hours;
    }
}

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
package org.openhab.binding.weathercompany.internal.model;

/**
 * The {@link DayPartDTO} is the JSON object that contains the n-day forecast.
 *
 * The daypart object as well as the temperatureMax field will
 * appear as null in the API after 3:00 pm Local Apparent Time.
 *
 * @author Mark Hilbush - Initial contribution
 */
public class DayPartDTO {
    /*
     * The name of a 12 hour daypart not including day names in the
     * first 48 hours (Today, Tonight)
     */
    public String[] daypartName;

    /*
     * Day or night indicator (D, N)
     */
    public String[] dayOrNight;

    /*
     * The narrative forecast for the daypart period
     */
    public String[] narrative;

    /*
     * Sensible weather phrase
     */
    public String[] wxPhraseLong;

    /*
     * Sensible weather phrase
     */
    public String[] wxPhraseShort;

    /*
     * The maximum temperature between 7am and 7pm for daytime temperature and the minimum temperature
     * between 7pm and 7am for night-time temperature. Minimum temperature also
     */
    public Double[] temperature;

    /*
     * Maximum heat index.
     * An apparent temperature. It represents what the air temperature “feels like” on exposed human skin
     * due to the combined effect of warm temperatures and high humidity.
     * When the temperature is 70°F or higher, the Feels Like value represents the computed Heat Index.
     * For temperatures between 40°F and 70°F, the Feels Like value and Temperature are the same,
     * regardless of wind speed and humidity, so use the Temperature value
     */
    public Double[] temperatureHeatIndex;

    /*
     * Minimum wind chill.
     * An apparent temperature. It represents what the air temperature “feels like” on exposed human skin
     * due to the combined effect of the cold temperatures and wind speed.
     * When the temperature is 61°F or lower the Feels Like value represents the computed Wind Chill so display the
     * Wind Chill value.
     * For temperatures between 61°F and 75°F, the Feels Like value and Temperature are the same, regardless
     * of wind speed and humidity, so display the Temperature value.
     */
    public Double[] temperatureWindChill;

    /*
     * The relative humidity of the air, which is defined as the ratio of the amount of water vapor
     * in the air to the amount of vapor required to bring the air to saturation at a constant
     * temperature. Relative humidity is always expressed as a percentage
     */
    public Double[] relativeHumidity;

    /*
     * Daytime average cloud cover expressed as a percentage
     */
    public Integer[] cloudCover;

    /*
     * The maximum forecasted wind speed.
     * The wind is treated as a vector; hence, winds must have direction and magnitude (speed).
     * The wind information reported in the hourly current conditions corresponds to a 10-minute average
     * called the sustained wind speed. Sudden or brief variations in the wind speed are known
     * as “wind gusts” and are reported in a separate data field. Wind directions are always expressed
     * as "from whence the wind blows" meaning that a North wind blows from North to South. If you face
     * North in a North wind the wind is at your face. Face southward and the North wind is at your back
     */
    public Double[] windSpeed;

    /*
     * Average wind direction in magnetic notation
     */
    public Integer[] windDirection;

    /*
     * Average wind direction in cardinal notation
     * N, NNE, NE, ENE, E, ESE, SE, SSE, S, SSW, SW, WSW, W, WNW, NW, NNW, CALM, VAR
     */
    public String[] windDirectionCardinal;

    /*
     * The phrase that describes the wind direction and speed for a 12 hour daypart
     */
    public String[] windPhrase;

    /*
     * Maximum probability of precipitation
     */
    public Integer[] precipChance;

    /*
     * Type of precipitation to display with the probability of precipitation
     * data element. (rain, snow, precip)
     */
    public String[] precipType;

    /*
     * The description of probability thunderstorm activity in an area for 12 hour daypart
     * 0 = "No thunder"; 1 = "Thunder possible"; 2 = "Thunder expected"; 3 = "Severe thunderstorms possible";
     * 4 = "Severe thunderstorms likely"; 5 = "High risk of severe thunderstorms"
     */
    public String[] thunderCategory;

    /*
     * The enumeration of thunderstorm probability within an area for a 12 hour daypart
     */
    public Integer[] thunderIndex;

    /*
     * The forecasted measurable precipitation (liquid or liquid equivalent) during 12 or 24 hour period
     */
    public Double[] qpf;

    /*
     * The forecasted measurable precipitation as snow during the 12 or 24 hour forecast period
     */
    public Double[] qpfSnow;

    /*
     * A phrase associated to the qualifier code describing special weather criteria
     */
    public String[] qualifierPhrase;

    /*
     * ????
     */
    public String[] qualifierCode;

    /*
     * Snow accumulation amount for the 12 hour forecast period
     */
    public String[] snowRange;

    /*
     * The UV Index Description which complements the UV Index value by providing an associated
     * level of risk of skin damage due to exposure.
     * -2 = Not Available, -1 = No Report, 0 to 2 = Low, 3 to 5 = Moderate,
     * 6 to 7 = High, 8 to 10 = Very High, 11 to 16 = Extreme
     */
    public String[] uvDescription;

    /*
     * Maximum UV index for the 12 hour forecast period
     */
    public Integer[] uvIndex;

    /*
     * This number is the key to the weather icon lookup. The data field shows the icon
     * number that is matched to represent the observed weather conditions
     */
    public Integer[] iconCode;

    /*
     * Code representing full set sensible weather
     */
    public Integer[] iconCodeExtend;
}

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
package org.openhab.binding.weathercompany.internal.model;

/**
 * The {@link ForecastDTO} is the JSON object that contains the n-day forecast.
 *
 * The daypart object as well as the temperatureMax field OUTSIDE of the daypart object will
 * appear as null in the API after 3:00pm Local Apparent Time.
 *
 * Standard HTTP Cache-Control headers are used to define caching length. The TTL value is
 * provided in the HTTP Header as an absolute time value using the “Expires” parameter.
 * Example: “Expires: Fri, 12 Jul 2013 12:00:00 GMT”. The response provides a data element
 * expirationTimeUtc, this should be used to expire and remove a record from your system
 *
 * Translated fields:
 * - dayOfWeek
 * - daypartName
 * - moonPhase
 * - narrative
 * - qualifierPhrase
 * - uvDescription
 * - windDirectionCardinal
 * - windPhrase
 * - wxPhraseLong
 *
 * @author Mark Hilbush - Initial contribution
 */
public class ForecastDTO {
    /*
     * Day of week
     */
    public String[] dayOfWeek;

    /*
     * For the purposes of this product day(D) = 7am to 7pm and night(N) = 7pm to 7am
     */
    public Object daypart;

    /*
     * The narrative forecast for the 24-hour period.
     */
    public String[] narrative;

    /*
     * Daily maximum temperature
     */
    public Double[] temperatureMax;

    /*
     * Daily minimum temperature
     */
    public Double[] temperatureMin;

    /*
     * The forecasted measurable precipitation (liquid or liquid equivalent) during 12 or 24 hour period.
     */
    public Double[] qpf;

    /*
     * The forecasted measurable precipitation as snow during the 12 or 24 hour forecast period.
     */
    public Double[] qpfSnow;

    /*
     * Time forecast is valid in local apparent time.
     * ISO 8601 - YYYY-MM-DDTHH:MM:SS-NNNN; NNNN=GMT offset
     */
    public String[] validTimeLocal;

    /*
     * Time forecast is valid in UNIX seconds
     */
    public Integer[] validTimeUtc;

    /*
     * Expiration time in UNIX seconds
     */
    public Integer[] expirationTimeUtc;

    /*
     * The local time of the sunrise. It reflects any local daylight savings conventions.
     * For a few Arctic and Antarctic regions, the Sunrise and Sunset data values may be the
     * same (each with a value of 12:01am) to reflect conditions where a sunrise or sunset does not occur.
     * ISO 8601 - YYYY-MM-DDTHH:MM:SS-NNNN; NNNN=GMT offset
     */
    public String[] sunriseTimeLocal;

    /*
     * Sunrise time in UNIX epoch value
     */
    public Integer[] sunriseTimeUtc;

    /*
     * The local time of the sunset. It reflects any local daylight savings conventions.
     * For a few Arctic and Antarctic regions, the Sunrise and Sunset data values may be the
     * same (each with a value of 12:01am) to reflect conditions where a sunrise or sunset does not occur.
     * ISO 8601 - YYYY-MM-DDTHH:MM:SS-NNNN; NNNN=GMT offset
     */
    public String[] sunsetTimeLocal;

    /*
     * Sunset time in UNIX epoch value
     */
    public Integer[] sunsetTimeUtc;

    /*
     * Description phrase for the current lunar phase
     */
    public String[] moonPhase;

    /*
     * 3 character short code for lunar phases WNG, WXC, FQ, WNC, LQ, F, WXG, N()
     */
    public String[] moonPhaseCode;

    /*
     * Day number within monthly lunar cycle
     */
    public Integer[] moonPhaseDay;

    /*
     * First moonrise in local time. It reflects daylight savings time conventions
     * ISO 8601 - YYYY-MM-DDTHH:MM:SS-NNNN; NNNN=GMT offset
     */
    public String[] moonriseTimeLocal;

    /*
     * Moonrise time in UNIX epoch value
     */
    public Integer[] moonriseTimeUtc;

    /*
     * First Moonset in local time. It reflects daylight savings time conventions
     * ISO 8601 - YYYY-MM-DDTHH:MM:SS-NNNN; NNNN=GMT offset
     */
    public String[] moonSetTimeLocal;

    /*
     * Moonset time in UNIX epoch value
     */
    public Integer[] moonsetTimeUtc;
}

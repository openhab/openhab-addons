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
package org.openhab.binding.visualcrossing.internal.api;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.visualcrossing.internal.api.dto.CurrentConditions;
import org.openhab.binding.visualcrossing.internal.api.dto.Day;
import org.openhab.binding.visualcrossing.internal.api.dto.Hour;
import org.openhab.binding.visualcrossing.internal.api.dto.WeatherResponse;

/**
 * @author Martin Grześlowski - Initial contribution
 */
@NonNullByDefault
public class VisualCrossingApiTestConst {
    public static final String FULL_JSON = """
            {
              "queryCost": 1,
              "latitude": 51.1082,
              "longitude": 17.0269,
              "resolvedAddress": "Wrocław, Woj. Dolnośląskie, Polska",
              "address": "wrocław,poland",
              "timezone": "Europe/Warsaw",
              "tzoffset": 2.0,
              "description": "Similar temperatures continuing with a chance of rain Wednesday & Thursday.",
              "days": [
                {
                  "datetime": "2024-08-09",
                  "datetimeEpoch": 1723154400,
                  "tempmax": 26.4,
                  "tempmin": 13.0,
                  "temp": 20.3,
                  "feelslikemax": 26.4,
                  "feelslikemin": 13.0,
                  "feelslike": 20.3,
                  "dew": 13.9,
                  "humidity": 70.7,
                  "precip": 0.0,
                  "precipprob": 29.0,
                  "precipcover": 0.0,
                  "preciptype": null,
                  "snow": 0.0,
                  "snowdepth": 0.0,
                  "windgust": 23.8,
                  "windspeed": 11.2,
                  "winddir": 252.4,
                  "pressure": 1016.6,
                  "cloudcover": 59.4,
                  "visibility": 16.3,
                  "solarradiation": 149.8,
                  "solarenergy": 12.9,
                  "uvindex": 8.0,
                  "severerisk": 10.0,
                  "sunrise": "05:29:47",
                  "sunriseEpoch": 1723174187,
                  "sunset": "20:23:59",
                  "sunsetEpoch": 1723227839,
                  "moonphase": 0.16,
                  "conditions": "Partially cloudy",
                  "description": "Partly cloudy throughout the day.",
                  "icon": "partly-cloudy-day",
                  "stations": [
                    "EPWR",
                    "F4529",
                    "remote"
                  ],
                  "source": "comb",
                  "hours": [
                    {
                      "datetime": "00:00:00",
                      "datetimeEpoch": 1723154400,
                      "temp": 16.0,
                      "feelslike": 16.0,
                      "humidity": 93.79,
                      "dew": 15.0,
                      "precip": 0.0,
                      "precipprob": 0.0,
                      "snow": 0.0,
                      "snowdepth": 0.0,
                      "preciptype": null,
                      "windgust": 13.7,
                      "windspeed": 9.4,
                      "winddir": 270.0,
                      "pressure": 1017.0,
                      "visibility": 10.0,
                      "cloudcover": 62.5,
                      "solarradiation": 0.0,
                      "solarenergy": 0.0,
                      "uvindex": 0.0,
                      "severerisk": 10.0,
                      "conditions": "Partially cloudy",
                      "icon": "partly-cloudy-night",
                      "stations": [
                        "EPWR",
                        "F4529"
                      ],
                      "source": "obs"
                    },
                    {
                      "datetime": "01:00:00",
                      "datetimeEpoch": 1723158000,
                      "temp": 16.0,
                      "feelslike": 16.0,
                      "humidity": 100.0,
                      "dew": 16.0,
                      "precip": 0.0,
                      "precipprob": 0.0,
                      "snow": 0.0,
                      "snowdepth": 0.0,
                      "preciptype": null,
                      "windgust": 10.8,
                      "windspeed": 7.6,
                      "winddir": 280.0,
                      "pressure": 1017.0,
                      "visibility": 10.0,
                      "cloudcover": 0.0,
                      "solarradiation": 0.0,
                      "solarenergy": 0.0,
                      "uvindex": 0.0,
                      "severerisk": 10.0,
                      "conditions": "Clear",
                      "icon": "clear-night",
                      "stations": [
                        "EPWR",
                        "F4529"
                      ],
                      "source": "obs"
                    },
                    {
                      "datetime": "02:00:00",
                      "datetimeEpoch": 1723161600,
                      "temp": 15.0,
                      "feelslike": 15.0,
                      "humidity": 93.74,
                      "dew": 14.0,
                      "precip": 0.0,
                      "precipprob": 0.0,
                      "snow": 0.0,
                      "snowdepth": 0.0,
                      "preciptype": null,
                      "windgust": 10.1,
                      "windspeed": 9.4,
                      "winddir": 299.4,
                      "pressure": 1017.0,
                      "visibility": 10.0,
                      "cloudcover": 3.4,
                      "solarradiation": 0.0,
                      "solarenergy": 0.0,
                      "uvindex": 0.0,
                      "severerisk": 10.0,
                      "conditions": "Clear",
                      "icon": "clear-night",
                      "stations": [
                        "EPWR",
                        "F4529"
                      ],
                      "source": "obs"
                    },
                    {
                      "datetime": "03:00:00",
                      "datetimeEpoch": 1723165200,
                      "temp": 14.0,
                      "feelslike": 14.0,
                      "humidity": 100.0,
                      "dew": 14.0,
                      "precip": 0.0,
                      "precipprob": 0.0,
                      "snow": 0.0,
                      "snowdepth": 0.0,
                      "preciptype": null,
                      "windgust": 9.7,
                      "windspeed": 3.6,
                      "winddir": 260.0,
                      "pressure": 1017.0,
                      "visibility": 10.0,
                      "cloudcover": 0.0,
                      "solarradiation": 0.0,
                      "solarenergy": 0.0,
                      "uvindex": 0.0,
                      "severerisk": 10.0,
                      "conditions": "Clear",
                      "icon": "clear-night",
                      "stations": [
                        "EPWR",
                        "F4529"
                      ],
                      "source": "obs"
                    },
                    {
                      "datetime": "04:00:00",
                      "datetimeEpoch": 1723168800,
                      "temp": 14.0,
                      "feelslike": 14.0,
                      "humidity": 100.0,
                      "dew": 14.0,
                      "precip": 0.0,
                      "precipprob": 0.0,
                      "snow": 0.0,
                      "snowdepth": 0.0,
                      "preciptype": null,
                      "windgust": 8.3,
                      "windspeed": 5.4,
                      "winddir": 270.0,
                      "pressure": 1017.0,
                      "visibility": 10.0,
                      "cloudcover": 100.0,
                      "solarradiation": 0.0,
                      "solarenergy": 0.0,
                      "uvindex": 0.0,
                      "severerisk": 10.0,
                      "conditions": "Overcast",
                      "icon": "cloudy",
                      "stations": [
                        "EPWR",
                        "F4529"
                      ],
                      "source": "obs"
                    },
                    {
                      "datetime": "05:00:00",
                      "datetimeEpoch": 1723172400,
                      "temp": 13.0,
                      "feelslike": 13.0,
                      "humidity": 100.0,
                      "dew": 13.0,
                      "precip": 0.0,
                      "precipprob": 0.0,
                      "snow": 0.0,
                      "snowdepth": 0.0,
                      "preciptype": null,
                      "windgust": 7.6,
                      "windspeed": 5.4,
                      "winddir": 270.0,
                      "pressure": 1017.0,
                      "visibility": 10.0,
                      "cloudcover": 0.0,
                      "solarradiation": 0.0,
                      "solarenergy": 0.0,
                      "uvindex": 0.0,
                      "severerisk": 10.0,
                      "conditions": "Clear",
                      "icon": "clear-night",
                      "stations": [
                        "EPWR",
                        "F4529"
                      ],
                      "source": "obs"
                    },
                    {
                      "datetime": "06:00:00",
                      "datetimeEpoch": 1723176000,
                      "temp": 13.0,
                      "feelslike": 13.0,
                      "humidity": 100.0,
                      "dew": 13.0,
                      "precip": 0.0,
                      "precipprob": 0.0,
                      "snow": 0.0,
                      "snowdepth": 0.0,
                      "preciptype": null,
                      "windgust": 7.6,
                      "windspeed": 3.6,
                      "winddir": 260.0,
                      "pressure": 1018.0,
                      "visibility": 6.0,
                      "cloudcover": 0.0,
                      "solarradiation": 8.0,
                      "solarenergy": 0.0,
                      "uvindex": 0.0,
                      "severerisk": 10.0,
                      "conditions": "Clear",
                      "icon": "clear-day",
                      "stations": [
                        "EPWR",
                        "F4529"
                      ],
                      "source": "obs"
                    },
                    {
                      "datetime": "07:00:00",
                      "datetimeEpoch": 1723179600,
                      "temp": 15.0,
                      "feelslike": 15.0,
                      "humidity": 100.0,
                      "dew": 15.0,
                      "precip": 0.0,
                      "precipprob": 0.0,
                      "snow": 0.0,
                      "snowdepth": 0.0,
                      "preciptype": null,
                      "windgust": 7.2,
                      "windspeed": 5.4,
                      "winddir": 280.0,
                      "pressure": 1018.0,
                      "visibility": 10.0,
                      "cloudcover": 0.1,
                      "solarradiation": 18.0,
                      "solarenergy": 0.1,
                      "uvindex": 0.0,
                      "severerisk": 10.0,
                      "conditions": "Clear",
                      "icon": "clear-day",
                      "stations": [
                        "EPWR",
                        "F4529"
                      ],
                      "source": "obs"
                    },
                    {
                      "datetime": "08:00:00",
                      "datetimeEpoch": 1723183200,
                      "temp": 17.0,
                      "feelslike": 17.0,
                      "humidity": 88.01,
                      "dew": 15.0,
                      "precip": 0.0,
                      "precipprob": 0.0,
                      "snow": 0.0,
                      "snowdepth": 0.0,
                      "preciptype": null,
                      "windgust": 10.1,
                      "windspeed": 7.6,
                      "winddir": 260.0,
                      "pressure": 1018.0,
                      "visibility": 10.0,
                      "cloudcover": 0.1,
                      "solarradiation": 22.0,
                      "solarenergy": 0.1,
                      "uvindex": 0.0,
                      "severerisk": 10.0,
                      "conditions": "Clear",
                      "icon": "clear-day",
                      "stations": [
                        "EPWR",
                        "F4529"
                      ],
                      "source": "obs"
                    },
                    {
                      "datetime": "09:00:00",
                      "datetimeEpoch": 1723186800,
                      "temp": 19.0,
                      "feelslike": 19.0,
                      "humidity": 77.61,
                      "dew": 15.0,
                      "precip": 0.0,
                      "precipprob": 0.0,
                      "snow": 0.0,
                      "snowdepth": 0.0,
                      "preciptype": null,
                      "windgust": 12.2,
                      "windspeed": 5.4,
                      "winddir": 190.0,
                      "pressure": 1018.0,
                      "visibility": 10.0,
                      "cloudcover": 0.3,
                      "solarradiation": 27.0,
                      "solarenergy": 0.1,
                      "uvindex": 0.0,
                      "severerisk": 10.0,
                      "conditions": "Clear",
                      "icon": "clear-day",
                      "stations": [
                        "EPWR",
                        "F4529"
                      ],
                      "source": "obs"
                    },
                    {
                      "datetime": "10:00:00",
                      "datetimeEpoch": 1723190400,
                      "temp": 21.0,
                      "feelslike": 21.0,
                      "humidity": 64.28,
                      "dew": 14.0,
                      "precip": 0.0,
                      "precipprob": 0.0,
                      "snow": 0.0,
                      "snowdepth": 0.0,
                      "preciptype": null,
                      "windgust": 11.9,
                      "windspeed": 7.6,
                      "winddir": 190.0,
                      "pressure": 1018.0,
                      "visibility": 10.0,
                      "cloudcover": 100.0,
                      "solarradiation": 33.0,
                      "solarenergy": 0.1,
                      "uvindex": 0.0,
                      "severerisk": 10.0,
                      "conditions": "Overcast",
                      "icon": "cloudy",
                      "stations": [
                        "EPWR",
                        "F4529"
                      ],
                      "source": "obs"
                    },
                    {
                      "datetime": "11:00:00",
                      "datetimeEpoch": 1723194000,
                      "temp": 22.0,
                      "feelslike": 22.0,
                      "humidity": 53.05,
                      "dew": 12.0,
                      "precip": 0.0,
                      "precipprob": 0.0,
                      "snow": 0.0,
                      "snowdepth": 0.0,
                      "preciptype": null,
                      "windgust": 13.0,
                      "windspeed": 5.4,
                      "winddir": 203.2,
                      "pressure": 1018.0,
                      "visibility": 10.0,
                      "cloudcover": 90.9,
                      "solarradiation": 224.0,
                      "solarenergy": 0.8,
                      "uvindex": 2.0,
                      "severerisk": 10.0,
                      "conditions": "Overcast",
                      "icon": "cloudy",
                      "stations": [
                        "EPWR",
                        "F4529"
                      ],
                      "source": "obs"
                    },
                    {
                      "datetime": "12:00:00",
                      "datetimeEpoch": 1723197600,
                      "temp": 23.0,
                      "feelslike": 23.0,
                      "humidity": 56.89,
                      "dew": 14.0,
                      "precip": 0.0,
                      "precipprob": 0.0,
                      "snow": 0.0,
                      "snowdepth": 0.0,
                      "preciptype": null,
                      "windgust": 16.6,
                      "windspeed": 11.2,
                      "winddir": 240.0,
                      "pressure": 1018.0,
                      "visibility": 10.0,
                      "cloudcover": 88.3,
                      "solarradiation": 592.0,
                      "solarenergy": 2.1,
                      "uvindex": 6.0,
                      "severerisk": 10.0,
                      "conditions": "Partially cloudy",
                      "icon": "partly-cloudy-day",
                      "stations": [
                        "EPWR",
                        "F4529"
                      ],
                      "source": "obs"
                    },
                    {
                      "datetime": "13:00:00",
                      "datetimeEpoch": 1723201200,
                      "temp": 24.4,
                      "feelslike": 24.4,
                      "humidity": 49.0,
                      "dew": 13.0,
                      "precip": 0.0,
                      "precipprob": 0.0,
                      "snow": 0.0,
                      "snowdepth": 0.0,
                      "preciptype": null,
                      "windgust": 19.4,
                      "windspeed": 8.3,
                      "winddir": 253.0,
                      "pressure": 1017.0,
                      "visibility": 24.1,
                      "cloudcover": 54.0,
                      "solarradiation": 753.0,
                      "solarenergy": 2.7,
                      "uvindex": 8.0,
                      "severerisk": 10.0,
                      "conditions": "Partially cloudy",
                      "icon": "partly-cloudy-day",
                      "stations": [
                        "remote"
                      ],
                      "source": "obs"
                    },
                    {
                      "datetime": "14:00:00",
                      "datetimeEpoch": 1723204800,
                      "temp": 25.4,
                      "feelslike": 25.4,
                      "humidity": 46.16,
                      "dew": 13.0,
                      "precip": 0.0,
                      "precipprob": 0.0,
                      "snow": 0.0,
                      "snowdepth": 0.0,
                      "preciptype": null,
                      "windgust": 20.9,
                      "windspeed": 8.3,
                      "winddir": 256.5,
                      "pressure": 1017.0,
                      "visibility": 24.1,
                      "cloudcover": 72.2,
                      "solarradiation": 703.0,
                      "solarenergy": 2.5,
                      "uvindex": 7.0,
                      "severerisk": 10.0,
                      "conditions": "Partially cloudy",
                      "icon": "partly-cloudy-day",
                      "stations": null,
                      "source": "fcst"
                    },
                    {
                      "datetime": "15:00:00",
                      "datetimeEpoch": 1723208400,
                      "temp": 26.1,
                      "feelslike": 26.1,
                      "humidity": 42.29,
                      "dew": 12.3,
                      "precip": 0.0,
                      "precipprob": 0.0,
                      "snow": 0.0,
                      "snowdepth": 0.0,
                      "preciptype": null,
                      "windgust": 21.6,
                      "windspeed": 9.7,
                      "winddir": 252.8,
                      "pressure": 1016.0,
                      "visibility": 24.1,
                      "cloudcover": 100.0,
                      "solarradiation": 583.0,
                      "solarenergy": 2.1,
                      "uvindex": 6.0,
                      "severerisk": 10.0,
                      "conditions": "Overcast",
                      "icon": "cloudy",
                      "stations": null,
                      "source": "fcst"
                    },
                    {
                      "datetime": "16:00:00",
                      "datetimeEpoch": 1723212000,
                      "temp": 26.4,
                      "feelslike": 26.4,
                      "humidity": 42.1,
                      "dew": 12.5,
                      "precip": 0.0,
                      "precipprob": 0.0,
                      "snow": 0.0,
                      "snowdepth": 0.0,
                      "preciptype": null,
                      "windgust": 23.8,
                      "windspeed": 10.8,
                      "winddir": 261.6,
                      "pressure": 1015.0,
                      "visibility": 24.1,
                      "cloudcover": 91.8,
                      "solarradiation": 146.0,
                      "solarenergy": 0.5,
                      "uvindex": 1.0,
                      "severerisk": 10.0,
                      "conditions": "Overcast",
                      "icon": "cloudy",
                      "stations": null,
                      "source": "fcst"
                    },
                    {
                      "datetime": "17:00:00",
                      "datetimeEpoch": 1723215600,
                      "temp": 26.3,
                      "feelslike": 26.3,
                      "humidity": 43.19,
                      "dew": 12.8,
                      "precip": 0.0,
                      "precipprob": 0.0,
                      "snow": 0.0,
                      "snowdepth": 0.0,
                      "preciptype": null,
                      "windgust": 23.8,
                      "windspeed": 9.4,
                      "winddir": 258.9,
                      "pressure": 1015.0,
                      "visibility": 24.1,
                      "cloudcover": 99.1,
                      "solarradiation": 161.0,
                      "solarenergy": 0.6,
                      "uvindex": 2.0,
                      "severerisk": 10.0,
                      "conditions": "Overcast",
                      "icon": "cloudy",
                      "stations": null,
                      "source": "fcst"
                    },
                    {
                      "datetime": "18:00:00",
                      "datetimeEpoch": 1723219200,
                      "temp": 26.3,
                      "feelslike": 26.3,
                      "humidity": 43.76,
                      "dew": 13.0,
                      "precip": 0.0,
                      "precipprob": 0.0,
                      "snow": 0.0,
                      "snowdepth": 0.0,
                      "preciptype": null,
                      "windgust": 20.2,
                      "windspeed": 9.0,
                      "winddir": 253.9,
                      "pressure": 1015.0,
                      "visibility": 24.1,
                      "cloudcover": 100.0,
                      "solarradiation": 222.0,
                      "solarenergy": 0.8,
                      "uvindex": 2.0,
                      "severerisk": 10.0,
                      "conditions": "Overcast",
                      "icon": "cloudy",
                      "stations": null,
                      "source": "fcst"
                    },
                    {
                      "datetime": "19:00:00",
                      "datetimeEpoch": 1723222800,
                      "temp": 25.0,
                      "feelslike": 25.0,
                      "humidity": 51.11,
                      "dew": 14.2,
                      "precip": 0.0,
                      "precipprob": 0.0,
                      "snow": 0.0,
                      "snowdepth": 0.0,
                      "preciptype": null,
                      "windgust": 18.4,
                      "windspeed": 5.8,
                      "winddir": 243.6,
                      "pressure": 1015.0,
                      "visibility": 24.1,
                      "cloudcover": 99.7,
                      "solarradiation": 78.0,
                      "solarenergy": 0.3,
                      "uvindex": 1.0,
                      "severerisk": 10.0,
                      "conditions": "Overcast",
                      "icon": "cloudy",
                      "stations": null,
                      "source": "fcst"
                    },
                    {
                      "datetime": "20:00:00",
                      "datetimeEpoch": 1723226400,
                      "temp": 23.7,
                      "feelslike": 23.7,
                      "humidity": 56.7,
                      "dew": 14.6,
                      "precip": 0.0,
                      "precipprob": 0.0,
                      "snow": 0.0,
                      "snowdepth": 0.0,
                      "preciptype": null,
                      "windgust": 11.5,
                      "windspeed": 5.0,
                      "winddir": 249.1,
                      "pressure": 1015.0,
                      "visibility": 24.1,
                      "cloudcover": 79.2,
                      "solarradiation": 25.0,
                      "solarenergy": 0.1,
                      "uvindex": 0.0,
                      "severerisk": 10.0,
                      "conditions": "Partially cloudy",
                      "icon": "partly-cloudy-day",
                      "stations": null,
                      "source": "fcst"
                    },
                    {
                      "datetime": "21:00:00",
                      "datetimeEpoch": 1723230000,
                      "temp": 22.4,
                      "feelslike": 22.4,
                      "humidity": 61.74,
                      "dew": 14.7,
                      "precip": 0.0,
                      "precipprob": 29.0,
                      "snow": 0.0,
                      "snowdepth": 0.0,
                      "preciptype": null,
                      "windgust": 9.7,
                      "windspeed": 4.7,
                      "winddir": 235.7,
                      "pressure": 1015.0,
                      "visibility": 24.1,
                      "cloudcover": 97.5,
                      "solarradiation": 0.0,
                      "solarenergy": 0.0,
                      "uvindex": 0.0,
                      "severerisk": 3.0,
                      "conditions": "Overcast",
                      "icon": "cloudy",
                      "stations": null,
                      "source": "fcst"
                    },
                    {
                      "datetime": "22:00:00",
                      "datetimeEpoch": 1723233600,
                      "temp": 21.9,
                      "feelslike": 21.9,
                      "humidity": 65.31,
                      "dew": 15.1,
                      "precip": 0.0,
                      "precipprob": 29.0,
                      "snow": 0.0,
                      "snowdepth": 0.0,
                      "preciptype": null,
                      "windgust": 9.0,
                      "windspeed": 4.0,
                      "winddir": 217.7,
                      "pressure": 1015.0,
                      "visibility": 24.1,
                      "cloudcover": 92.6,
                      "solarradiation": 0.0,
                      "solarenergy": 0.0,
                      "uvindex": 0.0,
                      "severerisk": 10.0,
                      "conditions": "Overcast",
                      "icon": "cloudy",
                      "stations": null,
                      "source": "fcst"
                    },
                    {
                      "datetime": "23:00:00",
                      "datetimeEpoch": 1723237200,
                      "temp": 21.5,
                      "feelslike": 21.5,
                      "humidity": 67.79,
                      "dew": 15.3,
                      "precip": 0.0,
                      "precipprob": 29.0,
                      "snow": 0.0,
                      "snowdepth": 0.0,
                      "preciptype": null,
                      "windgust": 9.4,
                      "windspeed": 4.7,
                      "winddir": 229.5,
                      "pressure": 1016.0,
                      "visibility": 24.1,
                      "cloudcover": 93.1,
                      "solarradiation": 0.0,
                      "solarenergy": 0.0,
                      "uvindex": 0.0,
                      "severerisk": 10.0,
                      "conditions": "Overcast",
                      "icon": "cloudy",
                      "stations": null,
                      "source": "fcst"
                    }
                  ]
                },
                     {
                       "datetime": "2024-08-10",
                       "datetimeEpoch": 1723240800,
                       "tempmax": 25.9,
                       "tempmin": 19.0,
                       "temp": 22.1,
                       "feelslikemax": 25.9,
                       "feelslikemin": 19.0,
                       "feelslike": 22.1,
                       "dew": 14.9,
                       "humidity": 65.4,
                       "precip": 0.0,
                       "precipprob": 29.0,
                       "precipcover": 0.0,
                       "preciptype": [
                         "rain"
                       ],
                       "snow": 0.0,
                       "snowdepth": 0.0,
                       "windgust": 34.9,
                       "windspeed": 15.5,
                       "winddir": 296.2,
                       "pressure": 1018.9,
                       "cloudcover": 60.6,
                       "visibility": 24.1,
                       "solarradiation": 246.9,
                       "solarenergy": 21.4,
                       "uvindex": 7.0,
                       "severerisk": 10.0,
                       "sunrise": "05:31:19",
                       "sunriseEpoch": 1723260679,
                       "sunset": "20:22:08",
                       "sunsetEpoch": 1723314128,
                       "moonphase": 0.19,
                       "conditions": "Partially cloudy",
                       "description": "Partly cloudy throughout the day.",
                       "icon": "partly-cloudy-day",
                       "stations": null,
                       "source": "fcst",
                       "hours": [
                         {
                           "datetime": "00:00:00",
                           "datetimeEpoch": 1723240800,
                           "temp": 21.1,
                           "feelslike": 21.1,
                           "humidity": 69.47,
                           "dew": 15.3,
                           "precip": 0.0,
                           "precipprob": 29.0,
                           "snow": 0.0,
                           "snowdepth": 0.0,
                           "preciptype": null,
                           "windgust": 10.1,
                           "windspeed": 4.3,
                           "winddir": 294.1,
                           "pressure": 1016.0,
                           "visibility": 24.1,
                           "cloudcover": 99.3,
                           "solarradiation": 0.0,
                           "solarenergy": 0.0,
                           "uvindex": 0.0,
                           "severerisk": 10.0,
                           "conditions": "Overcast",
                           "icon": "cloudy",
                           "stations": null,
                           "source": "fcst"
                         },
                         {
                           "datetime": "01:00:00",
                           "datetimeEpoch": 1723244400,
                           "temp": 20.7,
                           "feelslike": 20.7,
                           "humidity": 72.12,
                           "dew": 15.5,
                           "precip": 0.0,
                           "precipprob": 29.0,
                           "snow": 0.0,
                           "snowdepth": 0.0,
                           "preciptype": null,
                           "windgust": 9.4,
                           "windspeed": 4.3,
                           "winddir": 263.3,
                           "pressure": 1017.0,
                           "visibility": 24.1,
                           "cloudcover": 95.8,
                           "solarradiation": 0.0,
                           "solarenergy": 0.0,
                           "uvindex": 0.0,
                           "severerisk": 10.0,
                           "conditions": "Overcast",
                           "icon": "cloudy",
                           "stations": null,
                           "source": "fcst"
                         },
                         {
                           "datetime": "02:00:00",
                           "datetimeEpoch": 1723248000,
                           "temp": 20.3,
                           "feelslike": 20.3,
                           "humidity": 74.88,
                           "dew": 15.7,
                           "precip": 0.0,
                           "precipprob": 29.0,
                           "snow": 0.0,
                           "snowdepth": 0.0,
                           "preciptype": [
                             "rain"
                           ],
                           "windgust": 9.4,
                           "windspeed": 4.7,
                           "winddir": 251.8,
                           "pressure": 1017.0,
                           "visibility": 24.1,
                           "cloudcover": 92.6,
                           "solarradiation": 0.0,
                           "solarenergy": 0.0,
                           "uvindex": 0.0,
                           "severerisk": 10.0,
                           "conditions": "Overcast",
                           "icon": "cloudy",
                           "stations": null,
                           "source": "fcst"
                         },
                         {
                           "datetime": "03:00:00",
                           "datetimeEpoch": 1723251600,
                           "temp": 20.2,
                           "feelslike": 20.2,
                           "humidity": 76.31,
                           "dew": 15.9,
                           "precip": 0.0,
                           "precipprob": 0.0,
                           "snow": 0.0,
                           "snowdepth": 0.0,
                           "preciptype": null,
                           "windgust": 14.4,
                           "windspeed": 6.8,
                           "winddir": 255.8,
                           "pressure": 1017.0,
                           "visibility": 24.1,
                           "cloudcover": 77.6,
                           "solarradiation": 0.0,
                           "solarenergy": 0.0,
                           "uvindex": 0.0,
                           "severerisk": 10.0,
                           "conditions": "Partially cloudy",
                           "icon": "partly-cloudy-night",
                           "stations": null,
                           "source": "fcst"
                         },
                         {
                           "datetime": "04:00:00",
                           "datetimeEpoch": 1723255200,
                           "temp": 19.8,
                           "feelslike": 19.8,
                           "humidity": 79.23,
                           "dew": 16.1,
                           "precip": 0.0,
                           "precipprob": 0.0,
                           "snow": 0.0,
                           "snowdepth": 0.0,
                           "preciptype": null,
                           "windgust": 16.6,
                           "windspeed": 7.9,
                           "winddir": 267.2,
                           "pressure": 1017.0,
                           "visibility": 24.1,
                           "cloudcover": 98.3,
                           "solarradiation": 0.0,
                           "solarenergy": 0.0,
                           "uvindex": 0.0,
                           "severerisk": 10.0,
                           "conditions": "Overcast",
                           "icon": "cloudy",
                           "stations": null,
                           "source": "fcst"
                         },
                         {
                           "datetime": "05:00:00",
                           "datetimeEpoch": 1723258800,
                           "temp": 19.3,
                           "feelslike": 19.3,
                           "humidity": 83.84,
                           "dew": 16.5,
                           "precip": 0.0,
                           "precipprob": 0.0,
                           "snow": 0.0,
                           "snowdepth": 0.0,
                           "preciptype": null,
                           "windgust": 17.3,
                           "windspeed": 8.3,
                           "winddir": 279.8,
                           "pressure": 1018.0,
                           "visibility": 24.1,
                           "cloudcover": 76.6,
                           "solarradiation": 0.0,
                           "solarenergy": 0.0,
                           "uvindex": 0.0,
                           "severerisk": 10.0,
                           "conditions": "Partially cloudy",
                           "icon": "partly-cloudy-night",
                           "stations": null,
                           "source": "fcst"
                         },
                         {
                           "datetime": "06:00:00",
                           "datetimeEpoch": 1723262400,
                           "temp": 19.0,
                           "feelslike": 19.0,
                           "humidity": 85.97,
                           "dew": 16.6,
                           "precip": 0.0,
                           "precipprob": 0.0,
                           "snow": 0.0,
                           "snowdepth": 0.0,
                           "preciptype": null,
                           "windgust": 16.9,
                           "windspeed": 7.6,
                           "winddir": 284.8,
                           "pressure": 1018.0,
                           "visibility": 24.1,
                           "cloudcover": 70.5,
                           "solarradiation": 4.0,
                           "solarenergy": 0.0,
                           "uvindex": 0.0,
                           "severerisk": 10.0,
                           "conditions": "Partially cloudy",
                           "icon": "partly-cloudy-day",
                           "stations": null,
                           "source": "fcst"
                         },
                         {
                           "datetime": "07:00:00",
                           "datetimeEpoch": 1723266000,
                           "temp": 19.2,
                           "feelslike": 19.2,
                           "humidity": 86.54,
                           "dew": 16.9,
                           "precip": 0.0,
                           "precipprob": 0.0,
                           "snow": 0.0,
                           "snowdepth": 0.0,
                           "preciptype": null,
                           "windgust": 16.6,
                           "windspeed": 7.9,
                           "winddir": 295.6,
                           "pressure": 1019.0,
                           "visibility": 24.1,
                           "cloudcover": 71.8,
                           "solarradiation": 68.0,
                           "solarenergy": 0.2,
                           "uvindex": 1.0,
                           "severerisk": 10.0,
                           "conditions": "Partially cloudy",
                           "icon": "partly-cloudy-day",
                           "stations": null,
                           "source": "fcst"
                         },
                         {
                           "datetime": "08:00:00",
                           "datetimeEpoch": 1723269600,
                           "temp": 20.2,
                           "feelslike": 20.2,
                           "humidity": 82.89,
                           "dew": 17.2,
                           "precip": 0.0,
                           "precipprob": 0.0,
                           "snow": 0.0,
                           "snowdepth": 0.0,
                           "preciptype": null,
                           "windgust": 22.0,
                           "windspeed": 10.4,
                           "winddir": 293.4,
                           "pressure": 1020.0,
                           "visibility": 24.1,
                           "cloudcover": 53.0,
                           "solarradiation": 224.0,
                           "solarenergy": 0.8,
                           "uvindex": 2.0,
                           "severerisk": 10.0,
                           "conditions": "Partially cloudy",
                           "icon": "partly-cloudy-day",
                           "stations": null,
                           "source": "fcst"
                         },
                         {
                           "datetime": "09:00:00",
                           "datetimeEpoch": 1723273200,
                           "temp": 21.2,
                           "feelslike": 21.2,
                           "humidity": 76.47,
                           "dew": 16.9,
                           "precip": 0.0,
                           "precipprob": 12.9,
                           "snow": 0.0,
                           "snowdepth": 0.0,
                           "preciptype": null,
                           "windgust": 27.4,
                           "windspeed": 12.6,
                           "winddir": 298.9,
                           "pressure": 1020.0,
                           "visibility": 24.1,
                           "cloudcover": 63.6,
                           "solarradiation": 383.0,
                           "solarenergy": 1.4,
                           "uvindex": 4.0,
                           "severerisk": 10.0,
                           "conditions": "Partially cloudy",
                           "icon": "partly-cloudy-day",
                           "stations": null,
                           "source": "fcst"
                         },
                         {
                           "datetime": "10:00:00",
                           "datetimeEpoch": 1723276800,
                           "temp": 21.7,
                           "feelslike": 21.7,
                           "humidity": 71.39,
                           "dew": 16.3,
                           "precip": 0.0,
                           "precipprob": 12.9,
                           "snow": 0.0,
                           "snowdepth": 0.0,
                           "preciptype": null,
                           "windgust": 29.2,
                           "windspeed": 13.7,
                           "winddir": 303.3,
                           "pressure": 1020.0,
                           "visibility": 24.1,
                           "cloudcover": 76.8,
                           "solarradiation": 535.0,
                           "solarenergy": 1.9,
                           "uvindex": 5.0,
                           "severerisk": 10.0,
                           "conditions": "Partially cloudy",
                           "icon": "partly-cloudy-day",
                           "stations": null,
                           "source": "fcst"
                         },
                         {
                           "datetime": "11:00:00",
                           "datetimeEpoch": 1723280400,
                           "temp": 22.7,
                           "feelslike": 22.7,
                           "humidity": 63.01,
                           "dew": 15.3,
                           "precip": 0.0,
                           "precipprob": 12.9,
                           "snow": 0.0,
                           "snowdepth": 0.0,
                           "preciptype": [
                             "rain"
                           ],
                           "windgust": 30.2,
                           "windspeed": 14.0,
                           "winddir": 301.1,
                           "pressure": 1020.0,
                           "visibility": 24.1,
                           "cloudcover": 54.8,
                           "solarradiation": 626.0,
                           "solarenergy": 2.3,
                           "uvindex": 6.0,
                           "severerisk": 10.0,
                           "conditions": "Partially cloudy",
                           "icon": "partly-cloudy-day",
                           "stations": null,
                           "source": "fcst"
                         },
                         {
                           "datetime": "12:00:00",
                           "datetimeEpoch": 1723284000,
                           "temp": 23.6,
                           "feelslike": 23.6,
                           "humidity": 57.78,
                           "dew": 14.8,
                           "precip": 0.0,
                           "precipprob": 12.9,
                           "snow": 0.0,
                           "snowdepth": 0.0,
                           "preciptype": [
                             "rain"
                           ],
                           "windgust": 33.8,
                           "windspeed": 14.8,
                           "winddir": 298.6,
                           "pressure": 1020.0,
                           "visibility": 24.1,
                           "cloudcover": 46.0,
                           "solarradiation": 608.0,
                           "solarenergy": 2.2,
                           "uvindex": 6.0,
                           "severerisk": 10.0,
                           "conditions": "Partially cloudy",
                           "icon": "partly-cloudy-day",
                           "stations": null,
                           "source": "fcst"
                         },
                         {
                           "datetime": "13:00:00",
                           "datetimeEpoch": 1723287600,
                           "temp": 24.5,
                           "feelslike": 24.5,
                           "humidity": 53.69,
                           "dew": 14.5,
                           "precip": 0.0,
                           "precipprob": 12.9,
                           "snow": 0.0,
                           "snowdepth": 0.0,
                           "preciptype": null,
                           "windgust": 34.9,
                           "windspeed": 15.1,
                           "winddir": 298.7,
                           "pressure": 1020.0,
                           "visibility": 24.1,
                           "cloudcover": 50.9,
                           "solarradiation": 642.0,
                           "solarenergy": 2.3,
                           "uvindex": 6.0,
                           "severerisk": 10.0,
                           "conditions": "Partially cloudy",
                           "icon": "partly-cloudy-day",
                           "stations": null,
                           "source": "fcst"
                         },
                         {
                           "datetime": "14:00:00",
                           "datetimeEpoch": 1723291200,
                           "temp": 25.3,
                           "feelslike": 25.3,
                           "humidity": 49.88,
                           "dew": 14.1,
                           "precip": 0.0,
                           "precipprob": 12.9,
                           "snow": 0.0,
                           "snowdepth": 0.0,
                           "preciptype": null,
                           "windgust": 33.1,
                           "windspeed": 14.8,
                           "winddir": 296.0,
                           "pressure": 1020.0,
                           "visibility": 24.1,
                           "cloudcover": 58.4,
                           "solarradiation": 693.0,
                           "solarenergy": 2.5,
                           "uvindex": 7.0,
                           "severerisk": 10.0,
                           "conditions": "Partially cloudy",
                           "icon": "partly-cloudy-day",
                           "stations": null,
                           "source": "fcst"
                         },
                         {
                           "datetime": "15:00:00",
                           "datetimeEpoch": 1723294800,
                           "temp": 25.7,
                           "feelslike": 25.7,
                           "humidity": 47.46,
                           "dew": 13.7,
                           "precip": 0.0,
                           "precipprob": 3.2,
                           "snow": 0.0,
                           "snowdepth": 0.0,
                           "preciptype": null,
                           "windgust": 33.1,
                           "windspeed": 15.5,
                           "winddir": 299.5,
                           "pressure": 1019.0,
                           "visibility": 24.1,
                           "cloudcover": 57.7,
                           "solarradiation": 431.0,
                           "solarenergy": 1.6,
                           "uvindex": 4.0,
                           "severerisk": 10.0,
                           "conditions": "Partially cloudy",
                           "icon": "partly-cloudy-day",
                           "stations": null,
                           "source": "fcst"
                         },
                         {
                           "datetime": "16:00:00",
                           "datetimeEpoch": 1723298400,
                           "temp": 25.9,
                           "feelslike": 25.9,
                           "humidity": 45.99,
                           "dew": 13.4,
                           "precip": 0.0,
                           "precipprob": 3.2,
                           "snow": 0.0,
                           "snowdepth": 0.0,
                           "preciptype": null,
                           "windgust": 33.8,
                           "windspeed": 15.1,
                           "winddir": 299.3,
                           "pressure": 1019.0,
                           "visibility": 24.1,
                           "cloudcover": 55.1,
                           "solarradiation": 530.0,
                           "solarenergy": 1.9,
                           "uvindex": 5.0,
                           "severerisk": 10.0,
                           "conditions": "Partially cloudy",
                           "icon": "partly-cloudy-day",
                           "stations": null,
                           "source": "fcst"
                         },
                         {
                           "datetime": "17:00:00",
                           "datetimeEpoch": 1723302000,
                           "temp": 25.6,
                           "feelslike": 25.6,
                           "humidity": 45.91,
                           "dew": 13.1,
                           "precip": 0.0,
                           "precipprob": 3.2,
                           "snow": 0.0,
                           "snowdepth": 0.0,
                           "preciptype": null,
                           "windgust": 32.8,
                           "windspeed": 14.0,
                           "winddir": 300.0,
                           "pressure": 1019.0,
                           "visibility": 24.1,
                           "cloudcover": 62.4,
                           "solarradiation": 526.0,
                           "solarenergy": 1.9,
                           "uvindex": 5.0,
                           "severerisk": 10.0,
                           "conditions": "Partially cloudy",
                           "icon": "partly-cloudy-day",
                           "stations": null,
                           "source": "fcst"
                         },
                         {
                           "datetime": "18:00:00",
                           "datetimeEpoch": 1723305600,
                           "temp": 25.2,
                           "feelslike": 25.2,
                           "humidity": 46.71,
                           "dew": 13.0,
                           "precip": 0.0,
                           "precipprob": 3.2,
                           "snow": 0.0,
                           "snowdepth": 0.0,
                           "preciptype": null,
                           "windgust": 29.9,
                           "windspeed": 11.9,
                           "winddir": 305.5,
                           "pressure": 1019.0,
                           "visibility": 24.1,
                           "cloudcover": 66.8,
                           "solarradiation": 376.0,
                           "solarenergy": 1.4,
                           "uvindex": 4.0,
                           "severerisk": 10.0,
                           "conditions": "Partially cloudy",
                           "icon": "partly-cloudy-day",
                           "stations": null,
                           "source": "fcst"
                         },
                         {
                           "datetime": "19:00:00",
                           "datetimeEpoch": 1723309200,
                           "temp": 24.5,
                           "feelslike": 24.5,
                           "humidity": 49.02,
                           "dew": 13.1,
                           "precip": 0.0,
                           "precipprob": 3.2,
                           "snow": 0.0,
                           "snowdepth": 0.0,
                           "preciptype": null,
                           "windgust": 24.8,
                           "windspeed": 7.6,
                           "winddir": 306.1,
                           "pressure": 1019.0,
                           "visibility": 24.1,
                           "cloudcover": 51.6,
                           "solarradiation": 211.0,
                           "solarenergy": 0.8,
                           "uvindex": 2.0,
                           "severerisk": 10.0,
                           "conditions": "Partially cloudy",
                           "icon": "partly-cloudy-day",
                           "stations": null,
                           "source": "fcst"
                         },
                         {
                           "datetime": "20:00:00",
                           "datetimeEpoch": 1723312800,
                           "temp": 22.9,
                           "feelslike": 22.9,
                           "humidity": 57.61,
                           "dew": 14.1,
                           "precip": 0.0,
                           "precipprob": 3.2,
                           "snow": 0.0,
                           "snowdepth": 0.0,
                           "preciptype": null,
                           "windgust": 15.5,
                           "windspeed": 4.3,
                           "winddir": 341.4,
                           "pressure": 1019.0,
                           "visibility": 24.1,
                           "cloudcover": 34.3,
                           "solarradiation": 69.0,
                           "solarenergy": 0.2,
                           "uvindex": 1.0,
                           "severerisk": 10.0,
                           "conditions": "Partially cloudy",
                           "icon": "partly-cloudy-day",
                           "stations": null,
                           "source": "fcst"
                         },
                         {
                           "datetime": "21:00:00",
                           "datetimeEpoch": 1723316400,
                           "temp": 21.1,
                           "feelslike": 21.1,
                           "humidity": 63.06,
                           "dew": 13.8,
                           "precip": 0.0,
                           "precipprob": 3.2,
                           "snow": 0.0,
                           "snowdepth": 0.0,
                           "preciptype": null,
                           "windgust": 8.3,
                           "windspeed": 3.6,
                           "winddir": 2.0,
                           "pressure": 1020.0,
                           "visibility": 24.1,
                           "cloudcover": 10.9,
                           "solarradiation": 0.0,
                           "solarenergy": 0.0,
                           "uvindex": 0.0,
                           "severerisk": 10.0,
                           "conditions": "Clear",
                           "icon": "clear-night",
                           "stations": null,
                           "source": "fcst"
                         },
                         {
                           "datetime": "22:00:00",
                           "datetimeEpoch": 1723320000,
                           "temp": 20.3,
                           "feelslike": 20.3,
                           "humidity": 64.54,
                           "dew": 13.4,
                           "precip": 0.0,
                           "precipprob": 3.2,
                           "snow": 0.0,
                           "snowdepth": 0.0,
                           "preciptype": null,
                           "windgust": 6.8,
                           "windspeed": 2.5,
                           "winddir": 14.5,
                           "pressure": 1020.0,
                           "visibility": 24.1,
                           "cloudcover": 6.0,
                           "solarradiation": 0.0,
                           "solarenergy": 0.0,
                           "uvindex": 0.0,
                           "severerisk": 10.0,
                           "conditions": "Clear",
                           "icon": "clear-night",
                           "stations": null,
                           "source": "fcst"
                         },
                         {
                           "datetime": "23:00:00",
                           "datetimeEpoch": 1723323600,
                           "temp": 19.8,
                           "feelslike": 19.8,
                           "humidity": 64.86,
                           "dew": 13.0,
                           "precip": 0.0,
                           "precipprob": 3.2,
                           "snow": 0.0,
                           "snowdepth": 0.0,
                           "preciptype": null,
                           "windgust": 3.6,
                           "windspeed": 2.5,
                           "winddir": 138.2,
                           "pressure": 1020.0,
                           "visibility": 24.1,
                           "cloudcover": 24.4,
                           "solarradiation": 0.0,
                           "solarenergy": 0.0,
                           "uvindex": 0.0,
                           "severerisk": 10.0,
                           "conditions": "Partially cloudy",
                           "icon": "partly-cloudy-night",
                           "stations": null,
                           "source": "fcst"
                         }
                       ]
                     }
              ],
              "alerts": [],
              "stations": {
                "F4529": {
                  "distance": 7820.0,
                  "latitude": 51.176,
                  "longitude": 16.999,
                  "useCount": 0,
                  "id": "F4529",
                  "name": "FW4529 Wroclaw PO",
                  "quality": 0,
                  "contribution": 0.0
                },
                "EPWR": {
                  "distance": 10309.0,
                  "latitude": 51.1,
                  "longitude": 16.88,
                  "useCount": 0,
                  "id": "EPWR",
                  "name": "EPWR",
                  "quality": 50,
                  "contribution": 0.0
                },
                "E1158": {
                  "distance": 8755.0,
                  "latitude": 51.087,
                  "longitude": 16.906,
                  "useCount": 0,
                  "id": "E1158",
                  "name": "EW1158 Wrocaw PL",
                  "quality": 0,
                  "contribution": 0.0
                }
              },
              "currentConditions": {
                "datetime": "15:45:00",
                "datetimeEpoch": 1723297500,
                "temp": 79.7,
                "feelslike": 79.7,
                "humidity": 53.2,
                "dew": 61.1,
                "precip": 0.0,
                "precipprob": 0.0,
                "snow": 0.0,
                "snowdepth": 0.0,
                "preciptype": null,
                "windgust": 7.8,
                "windspeed": 6.0,
                "winddir": 16.0,
                "pressure": 1019.0,
                "visibility": 6.2,
                "cloudcover": 68.4,
                "solarradiation": 321.0,
                "solarenergy": 1.2,
                "uvindex": 3.0,
                "conditions": "Częściowe zachmurzenie",
                "icon": "partly-cloudy-day",
                "stations": [
                  "EPWR",
                  "E1158",
                  "F4529"
                ],
                "source": "obs",
                "sunrise": "05:31:19",
                "sunriseEpoch": 1723260679,
                "sunset": "20:22:08",
                "sunsetEpoch": 1723314128,
                "moonphase": 0.19
              }
            }
            """;
    public static final WeatherResponse FULL_JSON_RESPONSE = new WeatherResponse(1, //
            51.1082, //
            17.0269, //
            "Wrocław, Woj. Dolnośląskie, Polska", //
            "wrocław,poland", //
            "Europe/Warsaw", //
            2.0, //
            "Similar temperatures continuing with a chance of rain Wednesday & Thursday.", //
            List.of( //
                    new Day( //
                            "2024-08-09", //
                            1723154400L, //
                            26.4, //
                            13.0, //
                            20.3, //
                            26.4, //
                            13.0, //
                            20.3, //
                            13.9, //
                            70.7, //
                            0.0, //
                            29.0, //
                            0.0, //
                            null, //
                            0.0, //
                            0.0, //
                            23.8, //
                            11.2, //
                            252.4, //
                            1016.6, //
                            59.4, //
                            16.3, //
                            149.8, //
                            12.9, //
                            8.0, //
                            10.0, //
                            "05:29:47", //
                            1723174187L, //
                            "20:23:59", //
                            1723227839L, //
                            0.16, //
                            "Partially cloudy", //
                            "Partly cloudy throughout the day.", //
                            "partly-cloudy-day", //
                            List.of("EPWR", "F4529", "remote"), //
                            "comb", //
                            List.of( //
                                    new Hour( //
                                            "00:00:00", //
                                            1723154400L, //
                                            16.0, //
                                            16.0, //
                                            93.79, //
                                            15.0, //
                                            0.0, //
                                            0.0, //
                                            0.0, //
                                            0.0, //
                                            null, //
                                            13.7, //
                                            9.4, //
                                            270.0, //
                                            1017.0, //
                                            10.0, //
                                            62.5, //
                                            0.0, //
                                            0.0, //
                                            0.0, //
                                            10.0, //
                                            "Partially cloudy", //
                                            "partly-cloudy-night", //
                                            List.of("EPWR", "F4529"), //
                                            "obs" //
                                    ), //
                                    new Hour( //
                                            "01:00:00", //
                                            1723158000L, //
                                            16.0, //
                                            16.0, //
                                            100.0, //
                                            16.0, //
                                            0.0, //
                                            0.0, //
                                            0.0, //
                                            0.0, //
                                            null, //
                                            10.8, //
                                            7.6, //
                                            280.0, //
                                            1017.0, //
                                            10.0, //
                                            0.0, //
                                            0.0, //
                                            0.0, //
                                            0.0, //
                                            10.0, //
                                            "Clear", //
                                            "clear-night", //
                                            List.of("EPWR", "F4529"), //
                                            "obs" //
                                    ), //
                                    new Hour( //
                                            "02:00:00", //
                                            1723161600L, //
                                            15.0, //
                                            15.0, //
                                            93.74, //
                                            14.0, //
                                            0.0, //
                                            0.0, //
                                            0.0, //
                                            0.0, //
                                            null, //
                                            10.1, //
                                            9.4, //
                                            299.4, //
                                            1017.0, //
                                            10.0, //
                                            3.4, //
                                            0.0, //
                                            0.0, //
                                            0.0, //
                                            10.0, //
                                            "Clear", //
                                            "clear-night", //
                                            List.of("EPWR", "F4529"), //
                                            "obs" //
                                    ), //
                                    new Hour( //
                                            "03:00:00", //
                                            1723165200L, //
                                            14.0, //
                                            14.0, //
                                            100.0, //
                                            14.0, //
                                            0.0, //
                                            0.0, //
                                            0.0, //
                                            0.0, //
                                            null, //
                                            9.7, //
                                            3.6, //
                                            260.0, //
                                            1017.0, //
                                            10.0, //
                                            0.0, //
                                            0.0, //
                                            0.0, //
                                            0.0, //
                                            10.0, //
                                            "Clear", //
                                            "clear-night", //
                                            List.of("EPWR", "F4529"), //
                                            "obs" //
                                    ), //
                                    new Hour( //
                                            "04:00:00", //
                                            1723168800L, //
                                            14.0, //
                                            14.0, //
                                            100.0, //
                                            14.0, //
                                            0.0, //
                                            0.0, //
                                            0.0, //
                                            0.0, //
                                            null, //
                                            8.3, //
                                            5.4, //
                                            270.0, //
                                            1017.0, //
                                            10.0, //
                                            100.0, //
                                            0.0, //
                                            0.0, //
                                            0.0, //
                                            10.0, //
                                            "Overcast", //
                                            "cloudy", //
                                            List.of("EPWR", "F4529"), //
                                            "obs" //
                                    ), //
                                    new Hour( //
                                            "05:00:00", //
                                            1723172400L, //
                                            13.0, //
                                            13.0, //
                                            100.0, //
                                            13.0, //
                                            0.0, //
                                            0.0, //
                                            0.0, //
                                            0.0, //
                                            null, //
                                            7.6, //
                                            5.4, //
                                            270.0, //
                                            1017.0, //
                                            10.0, //
                                            0.0, //
                                            0.0, //
                                            0.0, //
                                            0.0, //
                                            10.0, //
                                            "Clear", //
                                            "clear-night", //
                                            List.of("EPWR", "F4529"), //
                                            "obs" //
                                    ), //
                                    new Hour( //
                                            "06:00:00", //
                                            1723176000L, //
                                            13.0, //
                                            13.0, //
                                            100.0, //
                                            13.0, //
                                            0.0, //
                                            0.0, //
                                            0.0, //
                                            0.0, //
                                            null, //
                                            7.6, //
                                            3.6, //
                                            260.0, //
                                            1018.0, //
                                            6.0, //
                                            0.0, //
                                            8.0, //
                                            0.0, //
                                            0.0, //
                                            10.0, //
                                            "Clear", //
                                            "clear-day", //
                                            List.of("EPWR", "F4529"), //
                                            "obs" //
                                    ), //
                                    new Hour( //
                                            "07:00:00", //
                                            1723179600L, //
                                            15.0, //
                                            15.0, //
                                            100.0, //
                                            15.0, //
                                            0.0, //
                                            0.0, //
                                            0.0, //
                                            0.0, //
                                            null, //
                                            7.2, //
                                            5.4, //
                                            280.0, //
                                            1018.0, //
                                            10.0, //
                                            0.1, //
                                            18.0, //
                                            0.1, //
                                            0.0, //
                                            10.0, //
                                            "Clear", //
                                            "clear-day", //
                                            List.of("EPWR", "F4529"), //
                                            "obs" //
                                    ), //
                                    new Hour( //
                                            "08:00:00", //
                                            1723183200L, //
                                            17.0, //
                                            17.0, //
                                            88.01, //
                                            15.0, //
                                            0.0, //
                                            0.0, //
                                            0.0, //
                                            0.0, //
                                            null, //
                                            10.1, //
                                            7.6, //
                                            260.0, //
                                            1018.0, //
                                            10.0, //
                                            0.1, //
                                            22.0, //
                                            0.1, //
                                            0.0, //
                                            10.0, //
                                            "Clear", //
                                            "clear-day", //
                                            List.of("EPWR", "F4529"), //
                                            "obs" //
                                    ), //
                                    new Hour( //
                                            "09:00:00", //
                                            1723186800L, //
                                            19.0, //
                                            19.0, //
                                            77.61, //
                                            15.0, //
                                            0.0, //
                                            0.0, //
                                            0.0, //
                                            0.0, //
                                            null, //
                                            12.2, //
                                            5.4, //
                                            190.0, //
                                            1018.0, //
                                            10.0, //
                                            0.3, //
                                            27.0, //
                                            0.1, //
                                            0.0, //
                                            10.0, //
                                            "Clear", //
                                            "clear-day", //
                                            List.of("EPWR", "F4529"), //
                                            "obs" //
                                    ), //
                                    new Hour( //
                                            "10:00:00", //
                                            1723190400L, //
                                            21.0, //
                                            21.0, //
                                            64.28, //
                                            14.0, //
                                            0.0, //
                                            0.0, //
                                            0.0, //
                                            0.0, //
                                            null, //
                                            11.9, //
                                            7.6, //
                                            190.0, //
                                            1018.0, //
                                            10.0, //
                                            100.0, //
                                            33.0, //
                                            0.1, //
                                            0.0, //
                                            10.0, //
                                            "Overcast", //
                                            "cloudy", //
                                            List.of("EPWR", "F4529"), //
                                            "obs" //
                                    ), //
                                    new Hour( //
                                            "11:00:00", //
                                            1723194000L, //
                                            22.0, //
                                            22.0, //
                                            53.05, //
                                            12.0, //
                                            0.0, //
                                            0.0, //
                                            0.0, //
                                            0.0, //
                                            null, //
                                            13.0, //
                                            5.4, //
                                            203.2, //
                                            1018.0, //
                                            10.0, //
                                            90.9, //
                                            224.0, //
                                            0.8, //
                                            2.0, //
                                            10.0, //
                                            "Overcast", //
                                            "cloudy", //
                                            List.of("EPWR", "F4529"), //
                                            "obs" //
                                    ), //
                                    new Hour( //
                                            "12:00:00", //
                                            1723197600L, //
                                            23.0, //
                                            23.0, //
                                            56.89, //
                                            14.0, //
                                            0.0, //
                                            0.0, //
                                            0.0, //
                                            0.0, //
                                            null, //
                                            16.6, //
                                            11.2, //
                                            240.0, //
                                            1018.0, //
                                            10.0, //
                                            88.3, //
                                            592.0, //
                                            2.1, //
                                            6.0, //
                                            10.0, //
                                            "Partially cloudy", //
                                            "partly-cloudy-day", //
                                            List.of("EPWR", "F4529"), //
                                            "obs" //
                                    ), //
                                    new Hour( //
                                            "13:00:00", //
                                            1723201200L, //
                                            24.4, //
                                            24.4, //
                                            49.0, //
                                            13.0, //
                                            0.0, //
                                            0.0, //
                                            0.0, //
                                            0.0, //
                                            null, //
                                            19.4, //
                                            8.3, //
                                            253.0, //
                                            1017.0, //
                                            24.1, //
                                            54.0, //
                                            753.0, //
                                            2.7, //
                                            8.0, //
                                            10.0, //
                                            "Partially cloudy", //
                                            "partly-cloudy-day", //
                                            List.of("remote"), //
                                            "obs" //
                                    ), //
                                    new Hour( //
                                            "14:00:00", //
                                            1723204800L, //
                                            25.4, //
                                            25.4, //
                                            46.16, //
                                            13.0, //
                                            0.0, //
                                            0.0, //
                                            0.0, //
                                            0.0, //
                                            null, //
                                            20.9, //
                                            8.3, //
                                            256.5, //
                                            1017.0, //
                                            24.1, //
                                            72.2, //
                                            703.0, //
                                            2.5, //
                                            7.0, //
                                            10.0, //
                                            "Partially cloudy", //
                                            "partly-cloudy-day", //
                                            null, //
                                            "fcst" //
                                    ), //
                                    new Hour( //
                                            "15:00:00", //
                                            1723208400L, //
                                            26.1, //
                                            26.1, //
                                            42.29, //
                                            12.3, //
                                            0.0, //
                                            0.0, //
                                            0.0, //
                                            0.0, //
                                            null, //
                                            21.6, //
                                            9.7, //
                                            252.8, //
                                            1016.0, //
                                            24.1, //
                                            100.0, //
                                            583.0, //
                                            2.1, //
                                            6.0, //
                                            10.0, //
                                            "Overcast", //
                                            "cloudy", //
                                            null, //
                                            "fcst" //
                                    ), //
                                    new Hour( //
                                            "16:00:00", //
                                            1723212000L, //
                                            26.4, //
                                            26.4, //
                                            42.1, //
                                            12.5, //
                                            0.0, //
                                            0.0, //
                                            0.0, //
                                            0.0, //
                                            null, //
                                            23.8, //
                                            10.8, //
                                            261.6, //
                                            1015.0, //
                                            24.1, //
                                            91.8, //
                                            146.0, //
                                            0.5, //
                                            1.0, //
                                            10.0, //
                                            "Overcast", //
                                            "cloudy", //
                                            null, //
                                            "fcst" //
                                    ), //
                                    new Hour( //
                                            "17:00:00", //
                                            1723215600L, //
                                            26.3, //
                                            26.3, //
                                            43.19, //
                                            12.8, //
                                            0.0, //
                                            0.0, //
                                            0.0, //
                                            0.0, //
                                            null, //
                                            23.8, //
                                            9.4, //
                                            258.9, //
                                            1015.0, //
                                            24.1, //
                                            99.1, //
                                            161.0, //
                                            0.6, //
                                            2.0, //
                                            10.0, //
                                            "Overcast", //
                                            "cloudy", //
                                            null, //
                                            "fcst" //
                                    ), //
                                    new Hour( //
                                            "18:00:00", //
                                            1723219200L, //
                                            26.3, //
                                            26.3, //
                                            43.76, //
                                            13.0, //
                                            0.0, //
                                            0.0, //
                                            0.0, //
                                            0.0, //
                                            null, //
                                            20.2, //
                                            9.0, //
                                            253.9, //
                                            1015.0, //
                                            24.1, //
                                            100.0, //
                                            222.0, //
                                            0.8, //
                                            2.0, //
                                            10.0, //
                                            "Overcast", //
                                            "cloudy", //
                                            null, //
                                            "fcst" //
                                    ), //
                                    new Hour( //
                                            "19:00:00", //
                                            1723222800L, //
                                            25.0, //
                                            25.0, //
                                            51.11, //
                                            14.2, //
                                            0.0, //
                                            0.0, //
                                            0.0, //
                                            0.0, //
                                            null, //
                                            18.4, //
                                            5.8, //
                                            243.6, //
                                            1015.0, //
                                            24.1, //
                                            99.7, //
                                            78.0, //
                                            0.3, //
                                            1.0, //
                                            10.0, //
                                            "Overcast", //
                                            "cloudy", //
                                            null, //
                                            "fcst" //
                                    ), //
                                    new Hour( //
                                            "20:00:00", //
                                            1723226400L, //
                                            23.7, //
                                            23.7, //
                                            56.7, //
                                            14.6, //
                                            0.0, //
                                            0.0, //
                                            0.0, //
                                            0.0, //
                                            null, //
                                            11.5, //
                                            5.0, //
                                            249.1, //
                                            1015.0, //
                                            24.1, //
                                            79.2, //
                                            25.0, //
                                            0.1, //
                                            0.0, //
                                            10.0, //
                                            "Partially cloudy", //
                                            "partly-cloudy-day", //
                                            null, //
                                            "fcst" //
                                    ), //
                                    new Hour( //
                                            "21:00:00", //
                                            1723230000L, //
                                            22.4, //
                                            22.4, //
                                            61.74, //
                                            14.7, //
                                            0.0, //
                                            29.0, //
                                            0.0, //
                                            0.0, //
                                            null, //
                                            9.7, //
                                            4.7, //
                                            235.7, //
                                            1015.0, //
                                            24.1, //
                                            97.5, //
                                            0.0, //
                                            0.0, //
                                            0.0, //
                                            3.0, //
                                            "Overcast", //
                                            "cloudy", //
                                            null, //
                                            "fcst" //
                                    ), //
                                    new Hour( //
                                            "22:00:00", //
                                            1723233600L, //
                                            21.9, //
                                            21.9, //
                                            65.31, //
                                            15.1, //
                                            0.0, //
                                            29.0, //
                                            0.0, //
                                            0.0, //
                                            null, //
                                            9.0, //
                                            4.0, //
                                            217.7, //
                                            1015.0, //
                                            24.1, //
                                            92.6, //
                                            0.0, //
                                            0.0, //
                                            0.0, //
                                            10.0, //
                                            "Overcast", //
                                            "cloudy", //
                                            null, //
                                            "fcst" //
                                    ), //
                                    new Hour( //
                                            "23:00:00", //
                                            1723237200L, //
                                            21.5, //
                                            21.5, //
                                            67.79, //
                                            15.3, //
                                            0.0, //
                                            29.0, //
                                            0.0, //
                                            0.0, //
                                            null, //
                                            9.4, //
                                            4.7, //
                                            229.5, //
                                            1016.0, //
                                            24.1, //
                                            93.1, //
                                            0.0, //
                                            0.0, //
                                            0.0, //
                                            10.0, //
                                            "Overcast", //
                                            "cloudy", //
                                            null, //
                                            "fcst" //
                                    ) //
                            ) //
                    ), //
                    new Day( //
                            "2024-08-10", //
                            1723240800L, //
                            25.9, //
                            19.0, //
                            22.1, //
                            25.9, //
                            19.0, //
                            22.1, //
                            14.9, //
                            65.4, //
                            0.0, //
                            29.0, //
                            0.0, //
                            List.of("rain"), //
                            0.0, //
                            0.0, //
                            34.9, //
                            15.5, //
                            296.2, //
                            1018.9, //
                            60.6, //
                            24.1, //
                            246.9, //
                            21.4, //
                            7.0, //
                            10.0, //
                            "05:31:19", //
                            1723260679L, //
                            "20:22:08", //
                            1723314128L, //
                            0.19, //
                            "Partially cloudy", //
                            "Partly cloudy throughout the day.", //
                            "partly-cloudy-day", //
                            null, //
                            "fcst", //
                            List.of( //
                                    new Hour( //
                                            "00:00:00", //
                                            1723240800L, //
                                            21.1, //
                                            21.1, //
                                            69.47, //
                                            15.3, //
                                            0.0, //
                                            29.0, //
                                            0.0, //
                                            0.0, //
                                            null, //
                                            10.1, //
                                            4.3, //
                                            294.1, //
                                            1016.0, //
                                            24.1, //
                                            99.3, //
                                            0.0, //
                                            0.0, //
                                            0.0, //
                                            10.0, //
                                            "Overcast", //
                                            "cloudy", //
                                            null, //
                                            "fcst" //
                                    ), //
                                    new Hour( //
                                            "01:00:00", //
                                            1723244400L, //
                                            20.7, //
                                            20.7, //
                                            72.12, //
                                            15.5, //
                                            0.0, //
                                            29.0, //
                                            0.0, //
                                            0.0, //
                                            null, //
                                            9.4, //
                                            4.3, //
                                            263.3, //
                                            1017.0, //
                                            24.1, //
                                            95.8, //
                                            0.0, //
                                            0.0, //
                                            0.0, //
                                            10.0, //
                                            "Overcast", //
                                            "cloudy", //
                                            null, //
                                            "fcst" //
                                    ), //
                                    new Hour( //
                                            "02:00:00", //
                                            1723248000L, //
                                            20.3, //
                                            20.3, //
                                            74.88, //
                                            15.7, //
                                            0.0, //
                                            29.0, //
                                            0.0, //
                                            0.0, //
                                            List.of("rain"), //
                                            9.4, //
                                            4.7, //
                                            251.8, //
                                            1017.0, //
                                            24.1, //
                                            92.6, //
                                            0.0, //
                                            0.0, //
                                            0.0, //
                                            10.0, //
                                            "Overcast", //
                                            "cloudy", //
                                            null, //
                                            "fcst" //
                                    ), //
                                    new Hour( //
                                            "03:00:00", //
                                            1723251600L, //
                                            20.2, //
                                            20.2, //
                                            76.31, //
                                            15.9, //
                                            0.0, //
                                            0.0, //
                                            0.0, //
                                            0.0, //
                                            null, //
                                            14.4, //
                                            6.8, //
                                            255.8, //
                                            1017.0, //
                                            24.1, //
                                            77.6, //
                                            0.0, //
                                            0.0, //
                                            0.0, //
                                            10.0, //
                                            "Partially cloudy", //
                                            "partly-cloudy-night", //
                                            null, //
                                            "fcst" //
                                    ), //
                                    new Hour( //
                                            "04:00:00", //
                                            1723255200L, //
                                            19.8, //
                                            19.8, //
                                            79.23, //
                                            16.1, //
                                            0.0, //
                                            0.0, //
                                            0.0, //
                                            0.0, //
                                            null, //
                                            16.6, //
                                            7.9, //
                                            267.2, //
                                            1017.0, //
                                            24.1, //
                                            98.3, //
                                            0.0, //
                                            0.0, //
                                            0.0, //
                                            10.0, //
                                            "Overcast", //
                                            "cloudy", //
                                            null, //
                                            "fcst" //
                                    ), //
                                    new Hour( //
                                            "05:00:00", //
                                            1723258800L, //
                                            19.3, //
                                            19.3, //
                                            83.84, //
                                            16.5, //
                                            0.0, //
                                            0.0, //
                                            0.0, //
                                            0.0, //
                                            null, //
                                            17.3, //
                                            8.3, //
                                            279.8, //
                                            1018.0, //
                                            24.1, //
                                            76.6, //
                                            0.0, //
                                            0.0, //
                                            0.0, //
                                            10.0, //
                                            "Partially cloudy", //
                                            "partly-cloudy-night", //
                                            null, //
                                            "fcst" //
                                    ), //
                                    new Hour( //
                                            "06:00:00", //
                                            1723262400L, //
                                            19.0, //
                                            19.0, //
                                            85.97, //
                                            16.6, //
                                            0.0, //
                                            0.0, //
                                            0.0, //
                                            0.0, //
                                            null, //
                                            16.9, //
                                            7.6, //
                                            284.8, //
                                            1018.0, //
                                            24.1, //
                                            70.5, //
                                            4.0, //
                                            0.0, //
                                            0.0, //
                                            10.0, //
                                            "Partially cloudy", //
                                            "partly-cloudy-day", //
                                            null, //
                                            "fcst" //
                                    ), //
                                    new Hour( //
                                            "07:00:00", //
                                            1723266000L, //
                                            19.2, //
                                            19.2, //
                                            86.54, //
                                            16.9, //
                                            0.0, //
                                            0.0, //
                                            0.0, //
                                            0.0, //
                                            null, //
                                            16.6, //
                                            7.9, //
                                            295.6, //
                                            1019.0, //
                                            24.1, //
                                            71.8, //
                                            68.0, //
                                            0.2, //
                                            1.0, //
                                            10.0, //
                                            "Partially cloudy", //
                                            "partly-cloudy-day", //
                                            null, //
                                            "fcst" //
                                    ), //
                                    new Hour( //
                                            "08:00:00", //
                                            1723269600L, //
                                            20.2, //
                                            20.2, //
                                            82.89, //
                                            17.2, //
                                            0.0, //
                                            0.0, //
                                            0.0, //
                                            0.0, //
                                            null, //
                                            22.0, //
                                            10.4, //
                                            293.4, //
                                            1020.0, //
                                            24.1, //
                                            53.0, //
                                            224.0, //
                                            0.8, //
                                            2.0, //
                                            10.0, //
                                            "Partially cloudy", //
                                            "partly-cloudy-day", //
                                            null, //
                                            "fcst" //
                                    ), //
                                    new Hour( //
                                            "09:00:00", //
                                            1723273200L, //
                                            21.2, //
                                            21.2, //
                                            76.47, //
                                            16.9, //
                                            0.0, //
                                            12.9, //
                                            0.0, //
                                            0.0, //
                                            null, //
                                            27.4, //
                                            12.6, //
                                            298.9, //
                                            1020.0, //
                                            24.1, //
                                            63.6, //
                                            383.0, //
                                            1.4, //
                                            4.0, //
                                            10.0, //
                                            "Partially cloudy", //
                                            "partly-cloudy-day", //
                                            null, //
                                            "fcst" //
                                    ), //
                                    new Hour( //
                                            "10:00:00", //
                                            1723276800L, //
                                            21.7, //
                                            21.7, //
                                            71.39, //
                                            16.3, //
                                            0.0, //
                                            12.9, //
                                            0.0, //
                                            0.0, //
                                            null, //
                                            29.2, //
                                            13.7, //
                                            303.3, //
                                            1020.0, //
                                            24.1, //
                                            76.8, //
                                            535.0, //
                                            1.9, //
                                            5.0, //
                                            10.0, //
                                            "Partially cloudy", //
                                            "partly-cloudy-day", //
                                            null, //
                                            "fcst" //
                                    ), //
                                    new Hour( //
                                            "11:00:00", //
                                            1723280400L, //
                                            22.7, //
                                            22.7, //
                                            63.01, //
                                            15.3, //
                                            0.0, //
                                            12.9, //
                                            0.0, //
                                            0.0, //
                                            List.of("rain"), //
                                            30.2, //
                                            14.0, //
                                            301.1, //
                                            1020.0, //
                                            24.1, //
                                            54.8, //
                                            626.0, //
                                            2.3, //
                                            6.0, //
                                            10.0, //
                                            "Partially cloudy", //
                                            "partly-cloudy-day", //
                                            null, //
                                            "fcst" //
                                    ), //
                                    new Hour( //
                                            "12:00:00", //
                                            1723284000L, //
                                            23.6, //
                                            23.6, //
                                            57.78, //
                                            14.8, //
                                            0.0, //
                                            12.9, //
                                            0.0, //
                                            0.0, //
                                            List.of("rain"), //
                                            33.8, //
                                            14.8, //
                                            298.6, //
                                            1020.0, //
                                            24.1, //
                                            46.0, //
                                            608.0, //
                                            2.2, //
                                            6.0, //
                                            10.0, //
                                            "Partially cloudy", //
                                            "partly-cloudy-day", //
                                            null, //
                                            "fcst" //
                                    ), //
                                    new Hour( //
                                            "13:00:00", //
                                            1723287600L, //
                                            24.5, //
                                            24.5, //
                                            53.69, //
                                            14.5, //
                                            0.0, //
                                            12.9, //
                                            0.0, //
                                            0.0, //
                                            null, //
                                            34.9, //
                                            15.1, //
                                            298.7, //
                                            1020.0, //
                                            24.1, //
                                            50.9, //
                                            642.0, //
                                            2.3, //
                                            6.0, //
                                            10.0, //
                                            "Partially cloudy", //
                                            "partly-cloudy-day", //
                                            null, //
                                            "fcst" //
                                    ), //
                                    new Hour( //
                                            "14:00:00", //
                                            1723291200L, //
                                            25.3, //
                                            25.3, //
                                            49.88, //
                                            14.1, //
                                            0.0, //
                                            12.9, //
                                            0.0, //
                                            0.0, //
                                            null, //
                                            33.1, //
                                            14.8, //
                                            296.0, //
                                            1020.0, //
                                            24.1, //
                                            58.4, //
                                            693.0, //
                                            2.5, //
                                            7.0, //
                                            10.0, //
                                            "Partially cloudy", //
                                            "partly-cloudy-day", //
                                            null, //
                                            "fcst" //
                                    ), //
                                    new Hour( //
                                            "15:00:00", //
                                            1723294800L, //
                                            25.7, //
                                            25.7, //
                                            47.46, //
                                            13.7, //
                                            0.0, //
                                            3.2, //
                                            0.0, //
                                            0.0, //
                                            null, //
                                            33.1, //
                                            15.5, //
                                            299.5, //
                                            1019.0, //
                                            24.1, //
                                            57.7, //
                                            431.0, //
                                            1.6, //
                                            4.0, //
                                            10.0, //
                                            "Partially cloudy", //
                                            "partly-cloudy-day", //
                                            null, //
                                            "fcst" //
                                    ), //
                                    new Hour( //
                                            "16:00:00", //
                                            1723298400L, //
                                            25.9, //
                                            25.9, //
                                            45.99, //
                                            13.4, //
                                            0.0, //
                                            3.2, //
                                            0.0, //
                                            0.0, //
                                            null, //
                                            33.8, //
                                            15.1, //
                                            299.3, //
                                            1019.0, //
                                            24.1, //
                                            55.1, //
                                            530.0, //
                                            1.9, //
                                            5.0, //
                                            10.0, //
                                            "Partially cloudy", //
                                            "partly-cloudy-day", //
                                            null, //
                                            "fcst" //
                                    ), //
                                    new Hour( //
                                            "17:00:00", //
                                            1723302000L, //
                                            25.6, //
                                            25.6, //
                                            45.91, //
                                            13.1, //
                                            0.0, //
                                            3.2, //
                                            0.0, //
                                            0.0, //
                                            null, //
                                            32.8, //
                                            14.0, //
                                            300.0, //
                                            1019.0, //
                                            24.1, //
                                            62.4, //
                                            526.0, //
                                            1.9, //
                                            5.0, //
                                            10.0, //
                                            "Partially cloudy", //
                                            "partly-cloudy-day", //
                                            null, //
                                            "fcst" //
                                    ), //
                                    new Hour( //
                                            "18:00:00", //
                                            1723305600L, //
                                            25.2, //
                                            25.2, //
                                            46.71, //
                                            13.0, //
                                            0.0, //
                                            3.2, //
                                            0.0, //
                                            0.0, //
                                            null, //
                                            29.9, //
                                            11.9, //
                                            305.5, //
                                            1019.0, //
                                            24.1, //
                                            66.8, //
                                            376.0, //
                                            1.4, //
                                            4.0, //
                                            10.0, //
                                            "Partially cloudy", //
                                            "partly-cloudy-day", //
                                            null, //
                                            "fcst" //
                                    ), //
                                    new Hour( //
                                            "19:00:00", //
                                            1723309200L, //
                                            24.5, //
                                            24.5, //
                                            49.02, //
                                            13.1, //
                                            0.0, //
                                            3.2, //
                                            0.0, //
                                            0.0, //
                                            null, //
                                            24.8, //
                                            7.6, //
                                            306.1, //
                                            1019.0, //
                                            24.1, //
                                            51.6, //
                                            211.0, //
                                            0.8, //
                                            2.0, //
                                            10.0, //
                                            "Partially cloudy", //
                                            "partly-cloudy-day", //
                                            null, //
                                            "fcst" //
                                    ), //
                                    new Hour( //
                                            "20:00:00", //
                                            1723312800L, //
                                            22.9, //
                                            22.9, //
                                            57.61, //
                                            14.1, //
                                            0.0, //
                                            3.2, //
                                            0.0, //
                                            0.0, //
                                            null, //
                                            15.5, //
                                            4.3, //
                                            341.4, //
                                            1019.0, //
                                            24.1, //
                                            34.3, //
                                            69.0, //
                                            0.2, //
                                            1.0, //
                                            10.0, //
                                            "Partially cloudy", //
                                            "partly-cloudy-day", //
                                            null, //
                                            "fcst" //
                                    ), //
                                    new Hour( //
                                            "21:00:00", //
                                            1723316400L, //
                                            21.1, //
                                            21.1, //
                                            63.06, //
                                            13.8, //
                                            0.0, //
                                            3.2, //
                                            0.0, //
                                            0.0, //
                                            null, //
                                            8.3, //
                                            3.6, //
                                            2.0, //
                                            1020.0, //
                                            24.1, //
                                            10.9, //
                                            0.0, //
                                            0.0, //
                                            0.0, //
                                            10.0, //
                                            "Clear", //
                                            "clear-night", //
                                            null, //
                                            "fcst" //
                                    ), //
                                    new Hour( //
                                            "22:00:00", //
                                            1723320000L, //
                                            20.3, //
                                            20.3, //
                                            64.54, //
                                            13.4, //
                                            0.0, //
                                            3.2, //
                                            0.0, //
                                            0.0, //
                                            null, //
                                            6.8, //
                                            2.5, //
                                            14.5, //
                                            1020.0, //
                                            24.1, //
                                            6.0, //
                                            0.0, //
                                            0.0, //
                                            0.0, //
                                            10.0, //
                                            "Clear", //
                                            "clear-night", //
                                            null, //
                                            "fcst" //
                                    ), //
                                    new Hour( //
                                            "23:00:00", //
                                            1723323600L, //
                                            19.8, //
                                            19.8, //
                                            64.86, //
                                            13.0, //
                                            0.0, //
                                            3.2, //
                                            0.0, //
                                            0.0, //
                                            null, //
                                            3.6, //
                                            2.5, //
                                            138.2, //
                                            1020.0, //
                                            24.1, //
                                            24.4, //
                                            0.0, //
                                            0.0, //
                                            0.0, //
                                            10.0, //
                                            "Partially cloudy", //
                                            "partly-cloudy-night", //
                                            null, //
                                            "fcst" //
                                    ) //
                            ) //
                    ) //
            ), new CurrentConditions(//
                    "15:45:00", //
                    1723297500L, //
                    79.7, //
                    79.7, //
                    53.2, //
                    61.1, //
                    0.0, //
                    0.0, //
                    0.0, //
                    0.0, //
                    null, //
                    7.8, //
                    6.0, //
                    16.0, //
                    1019.0, //
                    6.2, //
                    68.4, //
                    321.0, //
                    1.2, //
                    3.0, //
                    "Częściowe zachmurzenie", //
                    "partly-cloudy-day", //
                    List.of("EPWR", "E1158", "F4529"), //
                    "obs", //
                    "05:31:19", //
                    1723260679L, //
                    "20:22:08", //
                    1723314128L, //
                    0.19//
            )//
    ); //
}

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
package org.openhab.binding.metofficedatahub.internal.dto.responses;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.metofficedatahub.internal.MetOfficeDataHubBindingConstants;

/**
 * The {@link SiteApiFeatureCollectionTest} class implements unit test case for {@link SiteApiFeatureCollection}
 *
 * @author David Goodyear - Initial contribution
 */
@NonNullByDefault
public class SiteApiFeatureCollectionTest {

    public final static String siteApiDailyResponse = "{\n" +
            "    \"type\": \"FeatureCollection\",\n" +
            "    \"features\": [\n" +
            "        {\n" +
            "            \"type\": \"Feature\",\n" +
            "            \"geometry\": {\n" +
            "                \"type\": \"Point\",\n" +
            "                \"coordinates\": [\n" +
            "                    -0.32430000000000003,\n" +
            "                    51.0624,\n" +
            "                    50.0\n" +
            "                ]\n" +
            "            },\n" +
            "            \"properties\": {\n" +
            "                \"location\": {\n" +
            "                    \"name\": \"Horsham\"\n" +
            "                },\n" +
            "                \"requestPointDistance\": 0.1508,\n" +
            "                \"modelRunDate\": \"2022-09-19T21:00Z\",\n" +
            "                \"timeSeries\": [\n" +
            "                    {\n" +
            "                        \"time\": \"2022-09-18T00:00Z\",\n" +
            "                        \"midnight10MWindSpeed\": 1.39,\n" +
            "                        \"midnight10MWindDirection\": 31,\n" +
            "                        \"midnight10MWindGust\": 5.66,\n" +
            "                        \"midnightVisibility\": 8776,\n" +
            "                        \"midnightRelativeHumidity\": 91.42,\n" +
            "                        \"midnightMslp\": 102310,\n" +
            "                        \"nightSignificantWeatherCode\": 2,\n" +
            "                        \"nightMinScreenTemperature\": 8.05,\n" +
            "                        \"nightUpperBoundMinTemp\": 12.79,\n" +
            "                        \"nightLowerBoundMinTemp\": 5.51,\n" +
            "                        \"nightMinFeelsLikeTemp\": 7.35,\n" +
            "                        \"nightUpperBoundMinFeelsLikeTemp\": 12.83,\n" +
            "                        \"nightLowerBoundMinFeelsLikeTemp\": 7.33,\n" +
            "                        \"nightProbabilityOfPrecipitation\": 5,\n" +
            "                        \"nightProbabilityOfSnow\": 0,\n" +
            "                        \"nightProbabilityOfHeavySnow\": 1,\n" +
            "                        \"nightProbabilityOfRain\": 5,\n" +
            "                        \"nightProbabilityOfHeavyRain\": 3,\n" +
            "                        \"nightProbabilityOfHail\": 4,\n" +
            "                        \"nightProbabilityOfSferics\": 6\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"time\": \"2022-09-19T00:00Z\",\n" +
            "                        \"midday10MWindSpeed\": 1.03,\n" +
            "                        \"midnight10MWindSpeed\": 1.81,\n" +
            "                        \"midday10MWindDirection\": 299,\n" +
            "                        \"midnight10MWindDirection\": 323,\n" +
            "                        \"midday10MWindGust\": 2.06,\n" +
            "                        \"midnight10MWindGust\": 4.05,\n" +
            "                        \"middayVisibility\": 28034,\n" +
            "                        \"midnightVisibility\": 20277,\n" +
            "                        \"middayRelativeHumidity\": 63.54,\n" +
            "                        \"midnightRelativeHumidity\": 80.83,\n" +
            "                        \"middayMslp\": 102519,\n" +
            "                        \"midnightMslp\": 102541,\n" +
            "                        \"maxUvIndex\": 3,\n" +
            "                        \"daySignificantWeatherCode\": 7,\n" +
            "                        \"nightSignificantWeatherCode\": 7,\n" +
            "                        \"dayMaxScreenTemperature\": 17.64,\n" +
            "                        \"nightMinScreenTemperature\": 10.93,\n" +
            "                        \"dayUpperBoundMaxTemp\": 18.51,\n" +
            "                        \"nightUpperBoundMinTemp\": 13.74,\n" +
            "                        \"dayLowerBoundMaxTemp\": 14.22,\n" +
            "                        \"nightLowerBoundMinTemp\": 6.96,\n" +
            "                        \"dayMaxFeelsLikeTemp\": 17.06,\n" +
            "                        \"nightMinFeelsLikeTemp\": 11.12,\n" +
            "                        \"dayUpperBoundMaxFeelsLikeTemp\": 17.87,\n" +
            "                        \"nightUpperBoundMinFeelsLikeTemp\": 13.75,\n" +
            "                        \"dayLowerBoundMaxFeelsLikeTemp\": 14.57,\n" +
            "                        \"nightLowerBoundMinFeelsLikeTemp\": 9.81,\n" +
            "                        \"dayProbabilityOfPrecipitation\": 9,\n" +
            "                        \"nightProbabilityOfPrecipitation\": 6,\n" +
            "                        \"dayProbabilityOfSnow\": 0,\n" +
            "                        \"nightProbabilityOfSnow\": 0,\n" +
            "                        \"dayProbabilityOfHeavySnow\": 0,\n" +
            "                        \"nightProbabilityOfHeavySnow\": 0,\n" +
            "                        \"dayProbabilityOfRain\": 9,\n" +
            "                        \"nightProbabilityOfRain\": 6,\n" +
            "                        \"dayProbabilityOfHeavyRain\": 0,\n" +
            "                        \"nightProbabilityOfHeavyRain\": 1,\n" +
            "                        \"dayProbabilityOfHail\": 0,\n" +
            "                        \"nightProbabilityOfHail\": 0,\n" +
            "                        \"dayProbabilityOfSferics\": 0,\n" +
            "                        \"nightProbabilityOfSferics\": 0\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"time\": \"2022-09-20T00:00Z\",\n" +
            "                        \"midday10MWindSpeed\": 1.55,\n" +
            "                        \"midnight10MWindSpeed\": 1.02,\n" +
            "                        \"midday10MWindDirection\": 10,\n" +
            "                        \"midnight10MWindDirection\": 304,\n" +
            "                        \"midday10MWindGust\": 3.11,\n" +
            "                        \"midnight10MWindGust\": 2.33,\n" +
            "                        \"middayVisibility\": 28838,\n" +
            "                        \"midnightVisibility\": 16314,\n" +
            "                        \"middayRelativeHumidity\": 51.07,\n" +
            "                        \"midnightRelativeHumidity\": 83.33,\n" +
            "                        \"middayMslp\": 102680,\n" +
            "                        \"midnightMslp\": 102713,\n" +
            "                        \"maxUvIndex\": 3,\n" +
            "                        \"daySignificantWeatherCode\": 7,\n" +
            "                        \"nightSignificantWeatherCode\": 2,\n" +
            "                        \"dayMaxScreenTemperature\": 18.09,\n" +
            "                        \"nightMinScreenTemperature\": 9.92,\n" +
            "                        \"dayUpperBoundMaxTemp\": 19.61,\n" +
            "                        \"nightUpperBoundMinTemp\": 14.7,\n" +
            "                        \"dayLowerBoundMaxTemp\": 16.79,\n" +
            "                        \"nightLowerBoundMinTemp\": 5.69,\n" +
            "                        \"dayMaxFeelsLikeTemp\": 16.98,\n" +
            "                        \"nightMinFeelsLikeTemp\": 9.91,\n" +
            "                        \"dayUpperBoundMaxFeelsLikeTemp\": 18.95,\n" +
            "                        \"nightUpperBoundMinFeelsLikeTemp\": 14.99,\n" +
            "                        \"dayLowerBoundMaxFeelsLikeTemp\": 15.98,\n" +
            "                        \"nightLowerBoundMinFeelsLikeTemp\": 8.69,\n" +
            "                        \"dayProbabilityOfPrecipitation\": 5,\n" +
            "                        \"nightProbabilityOfPrecipitation\": 4,\n" +
            "                        \"dayProbabilityOfSnow\": 0,\n" +
            "                        \"nightProbabilityOfSnow\": 0,\n" +
            "                        \"dayProbabilityOfHeavySnow\": 0,\n" +
            "                        \"nightProbabilityOfHeavySnow\": 0,\n" +
            "                        \"dayProbabilityOfRain\": 5,\n" +
            "                        \"nightProbabilityOfRain\": 4,\n" +
            "                        \"dayProbabilityOfHeavyRain\": 0,\n" +
            "                        \"nightProbabilityOfHeavyRain\": 0,\n" +
            "                        \"dayProbabilityOfHail\": 0,\n" +
            "                        \"nightProbabilityOfHail\": 0,\n" +
            "                        \"dayProbabilityOfSferics\": 0,\n" +
            "                        \"nightProbabilityOfSferics\": 0\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"time\": \"2022-09-21T00:00Z\",\n" +
            "                        \"midday10MWindSpeed\": 1.05,\n" +
            "                        \"midnight10MWindSpeed\": 1.6,\n" +
            "                        \"midday10MWindDirection\": 201,\n" +
            "                        \"midnight10MWindDirection\": 133,\n" +
            "                        \"midday10MWindGust\": 2.85,\n" +
            "                        \"midnight10MWindGust\": 2.7,\n" +
            "                        \"middayVisibility\": 28467,\n" +
            "                        \"midnightVisibility\": 20397,\n" +
            "                        \"middayRelativeHumidity\": 51.33,\n" +
            "                        \"midnightRelativeHumidity\": 90.57,\n" +
            "                        \"middayMslp\": 102640,\n" +
            "                        \"midnightMslp\": 102440,\n" +
            "                        \"maxUvIndex\": 4,\n" +
            "                        \"daySignificantWeatherCode\": 1,\n" +
            "                        \"nightSignificantWeatherCode\": 2,\n" +
            "                        \"dayMaxScreenTemperature\": 19.75,\n" +
            "                        \"nightMinScreenTemperature\": 9.04,\n" +
            "                        \"dayUpperBoundMaxTemp\": 20.85,\n" +
            "                        \"nightUpperBoundMinTemp\": 12.07,\n" +
            "                        \"dayLowerBoundMaxTemp\": 17.71,\n" +
            "                        \"nightLowerBoundMinTemp\": 5.93,\n" +
            "                        \"dayMaxFeelsLikeTemp\": 18.82,\n" +
            "                        \"nightMinFeelsLikeTemp\": 8.75,\n" +
            "                        \"dayUpperBoundMaxFeelsLikeTemp\": 20.07,\n" +
            "                        \"nightUpperBoundMinFeelsLikeTemp\": 11.3,\n" +
            "                        \"dayLowerBoundMaxFeelsLikeTemp\": 17.23,\n" +
            "                        \"nightLowerBoundMinFeelsLikeTemp\": 5.31,\n" +
            "                        \"dayProbabilityOfPrecipitation\": 1,\n" +
            "                        \"nightProbabilityOfPrecipitation\": 2,\n" +
            "                        \"dayProbabilityOfSnow\": 0,\n" +
            "                        \"nightProbabilityOfSnow\": 0,\n" +
            "                        \"dayProbabilityOfHeavySnow\": 0,\n" +
            "                        \"nightProbabilityOfHeavySnow\": 0,\n" +
            "                        \"dayProbabilityOfRain\": 1,\n" +
            "                        \"nightProbabilityOfRain\": 2,\n" +
            "                        \"dayProbabilityOfHeavyRain\": 0,\n" +
            "                        \"nightProbabilityOfHeavyRain\": 0,\n" +
            "                        \"dayProbabilityOfHail\": 0,\n" +
            "                        \"nightProbabilityOfHail\": 0,\n" +
            "                        \"dayProbabilityOfSferics\": 0,\n" +
            "                        \"nightProbabilityOfSferics\": 0\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"time\": \"2022-09-22T00:00Z\",\n" +
            "                        \"midday10MWindSpeed\": 3.87,\n" +
            "                        \"midnight10MWindSpeed\": 2.21,\n" +
            "                        \"midday10MWindDirection\": 192,\n" +
            "                        \"midnight10MWindDirection\": 167,\n" +
            "                        \"midday10MWindGust\": 8.14,\n" +
            "                        \"midnight10MWindGust\": 3.35,\n" +
            "                        \"middayVisibility\": 32675,\n" +
            "                        \"midnightVisibility\": 25210,\n" +
            "                        \"middayRelativeHumidity\": 61.39,\n" +
            "                        \"midnightRelativeHumidity\": 88.29,\n" +
            "                        \"middayMslp\": 102164,\n" +
            "                        \"midnightMslp\": 101816,\n" +
            "                        \"maxUvIndex\": 3,\n" +
            "                        \"daySignificantWeatherCode\": 7,\n" +
            "                        \"nightSignificantWeatherCode\": 7,\n" +
            "                        \"dayMaxScreenTemperature\": 18.71,\n" +
            "                        \"nightMinScreenTemperature\": 10.89,\n" +
            "                        \"dayUpperBoundMaxTemp\": 21.93,\n" +
            "                        \"nightUpperBoundMinTemp\": 14.6,\n" +
            "                        \"dayLowerBoundMaxTemp\": 17.77,\n" +
            "                        \"nightLowerBoundMinTemp\": 7.75,\n" +
            "                        \"dayMaxFeelsLikeTemp\": 16.78,\n" +
            "                        \"nightMinFeelsLikeTemp\": 10.78,\n" +
            "                        \"dayUpperBoundMaxFeelsLikeTemp\": 19.63,\n" +
            "                        \"nightUpperBoundMinFeelsLikeTemp\": 13.97,\n" +
            "                        \"dayLowerBoundMaxFeelsLikeTemp\": 16.54,\n" +
            "                        \"nightLowerBoundMinFeelsLikeTemp\": 7.46,\n" +
            "                        \"dayProbabilityOfPrecipitation\": 5,\n" +
            "                        \"nightProbabilityOfPrecipitation\": 9,\n" +
            "                        \"dayProbabilityOfSnow\": 0,\n" +
            "                        \"nightProbabilityOfSnow\": 0,\n" +
            "                        \"dayProbabilityOfHeavySnow\": 0,\n" +
            "                        \"nightProbabilityOfHeavySnow\": 0,\n" +
            "                        \"dayProbabilityOfRain\": 5,\n" +
            "                        \"nightProbabilityOfRain\": 9,\n" +
            "                        \"dayProbabilityOfHeavyRain\": 0,\n" +
            "                        \"nightProbabilityOfHeavyRain\": 4,\n" +
            "                        \"dayProbabilityOfHail\": 0,\n" +
            "                        \"nightProbabilityOfHail\": 1,\n" +
            "                        \"dayProbabilityOfSferics\": 0,\n" +
            "                        \"nightProbabilityOfSferics\": 1\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"time\": \"2022-09-23T00:00Z\",\n" +
            "                        \"midday10MWindSpeed\": 2.42,\n" +
            "                        \"midnight10MWindSpeed\": 1.73,\n" +
            "                        \"midday10MWindDirection\": 209,\n" +
            "                        \"midnight10MWindDirection\": 306,\n" +
            "                        \"midday10MWindGust\": 5.59,\n" +
            "                        \"midnight10MWindGust\": 3.31,\n" +
            "                        \"middayVisibility\": 30194,\n" +
            "                        \"midnightVisibility\": 9598,\n" +
            "                        \"middayRelativeHumidity\": 63.53,\n" +
            "                        \"midnightRelativeHumidity\": 92.99,\n" +
            "                        \"middayMslp\": 101628,\n" +
            "                        \"midnightMslp\": 101583,\n" +
            "                        \"maxUvIndex\": 3,\n" +
            "                        \"daySignificantWeatherCode\": 7,\n" +
            "                        \"nightSignificantWeatherCode\": 12,\n" +
            "                        \"dayMaxScreenTemperature\": 18.76,\n" +
            "                        \"nightMinScreenTemperature\": 11.6,\n" +
            "                        \"dayUpperBoundMaxTemp\": 21.29,\n" +
            "                        \"nightUpperBoundMinTemp\": 13.67,\n" +
            "                        \"dayLowerBoundMaxTemp\": 14.51,\n" +
            "                        \"nightLowerBoundMinTemp\": 6.44,\n" +
            "                        \"dayMaxFeelsLikeTemp\": 17.49,\n" +
            "                        \"nightMinFeelsLikeTemp\": 11.38,\n" +
            "                        \"dayUpperBoundMaxFeelsLikeTemp\": 20.03,\n" +
            "                        \"nightUpperBoundMinFeelsLikeTemp\": 13.12,\n" +
            "                        \"dayLowerBoundMaxFeelsLikeTemp\": 14.39,\n" +
            "                        \"nightLowerBoundMinFeelsLikeTemp\": 5.45,\n" +
            "                        \"dayProbabilityOfPrecipitation\": 43,\n" +
            "                        \"nightProbabilityOfPrecipitation\": 45,\n" +
            "                        \"dayProbabilityOfSnow\": 0,\n" +
            "                        \"nightProbabilityOfSnow\": 0,\n" +
            "                        \"dayProbabilityOfHeavySnow\": 0,\n" +
            "                        \"nightProbabilityOfHeavySnow\": 0,\n" +
            "                        \"dayProbabilityOfRain\": 43,\n" +
            "                        \"nightProbabilityOfRain\": 45,\n" +
            "                        \"dayProbabilityOfHeavyRain\": 22,\n" +
            "                        \"nightProbabilityOfHeavyRain\": 26,\n" +
            "                        \"dayProbabilityOfHail\": 1,\n" +
            "                        \"nightProbabilityOfHail\": 1,\n" +
            "                        \"dayProbabilityOfSferics\": 2,\n" +
            "                        \"nightProbabilityOfSferics\": 2\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"time\": \"2022-09-24T00:00Z\",\n" +
            "                        \"midday10MWindSpeed\": 3.95,\n" +
            "                        \"midnight10MWindSpeed\": 4.28,\n" +
            "                        \"midday10MWindDirection\": 43,\n" +
            "                        \"midnight10MWindDirection\": 6,\n" +
            "                        \"midday10MWindGust\": 8.46,\n" +
            "                        \"midnight10MWindGust\": 8.19,\n" +
            "                        \"middayVisibility\": 17042,\n" +
            "                        \"midnightVisibility\": 26745,\n" +
            "                        \"middayRelativeHumidity\": 74.83,\n" +
            "                        \"midnightRelativeHumidity\": 81.26,\n" +
            "                        \"middayMslp\": 101763,\n" +
            "                        \"midnightMslp\": 101959,\n" +
            "                        \"maxUvIndex\": 3,\n" +
            "                        \"daySignificantWeatherCode\": 7,\n" +
            "                        \"nightSignificantWeatherCode\": 0,\n" +
            "                        \"dayMaxScreenTemperature\": 17.4,\n" +
            "                        \"nightMinScreenTemperature\": 11.09,\n" +
            "                        \"dayUpperBoundMaxTemp\": 22.92,\n" +
            "                        \"nightUpperBoundMinTemp\": 13.81,\n" +
            "                        \"dayLowerBoundMaxTemp\": 13.72,\n" +
            "                        \"nightLowerBoundMinTemp\": 4.24,\n" +
            "                        \"dayMaxFeelsLikeTemp\": 15.15,\n" +
            "                        \"nightMinFeelsLikeTemp\": 9.74,\n" +
            "                        \"dayUpperBoundMaxFeelsLikeTemp\": 21.94,\n" +
            "                        \"nightUpperBoundMinFeelsLikeTemp\": 13.29,\n" +
            "                        \"dayLowerBoundMaxFeelsLikeTemp\": 11.64,\n" +
            "                        \"nightLowerBoundMinFeelsLikeTemp\": 3.44,\n" +
            "                        \"dayProbabilityOfPrecipitation\": 32,\n" +
            "                        \"nightProbabilityOfPrecipitation\": 10,\n" +
            "                        \"dayProbabilityOfSnow\": 0,\n" +
            "                        \"nightProbabilityOfSnow\": 0,\n" +
            "                        \"dayProbabilityOfHeavySnow\": 0,\n" +
            "                        \"nightProbabilityOfHeavySnow\": 0,\n" +
            "                        \"dayProbabilityOfRain\": 32,\n" +
            "                        \"nightProbabilityOfRain\": 10,\n" +
            "                        \"dayProbabilityOfHeavyRain\": 17,\n" +
            "                        \"nightProbabilityOfHeavyRain\": 6,\n" +
            "                        \"dayProbabilityOfHail\": 1,\n" +
            "                        \"nightProbabilityOfHail\": 0,\n" +
            "                        \"dayProbabilityOfSferics\": 4,\n" +
            "                        \"nightProbabilityOfSferics\": 1\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"time\": \"2022-09-25T00:00Z\",\n" +
            "                        \"midday10MWindSpeed\": 4.36,\n" +
            "                        \"midnight10MWindSpeed\": 2.77,\n" +
            "                        \"midday10MWindDirection\": 32,\n" +
            "                        \"midnight10MWindDirection\": 269,\n" +
            "                        \"midday10MWindGust\": 9.45,\n" +
            "                        \"midnight10MWindGust\": 4.63,\n" +
            "                        \"middayVisibility\": 31004,\n" +
            "                        \"midnightVisibility\": 24820,\n" +
            "                        \"middayRelativeHumidity\": 60.24,\n" +
            "                        \"midnightRelativeHumidity\": 88.87,\n" +
            "                        \"middayMslp\": 102022,\n" +
            "                        \"midnightMslp\": 101654,\n" +
            "                        \"maxUvIndex\": 3,\n" +
            "                        \"daySignificantWeatherCode\": 3,\n" +
            "                        \"nightSignificantWeatherCode\": 2,\n" +
            "                        \"dayMaxScreenTemperature\": 17.65,\n" +
            "                        \"nightMinScreenTemperature\": 8.73,\n" +
            "                        \"dayUpperBoundMaxTemp\": 22.27,\n" +
            "                        \"nightUpperBoundMinTemp\": 13.54,\n" +
            "                        \"dayLowerBoundMaxTemp\": 11.66,\n" +
            "                        \"nightLowerBoundMinTemp\": 4.02,\n" +
            "                        \"dayMaxFeelsLikeTemp\": 15.0,\n" +
            "                        \"nightMinFeelsLikeTemp\": 7.98,\n" +
            "                        \"dayUpperBoundMaxFeelsLikeTemp\": 21.24,\n" +
            "                        \"nightUpperBoundMinFeelsLikeTemp\": 12.32,\n" +
            "                        \"dayLowerBoundMaxFeelsLikeTemp\": 11.09,\n" +
            "                        \"nightLowerBoundMinFeelsLikeTemp\": 3.6,\n" +
            "                        \"dayProbabilityOfPrecipitation\": 9,\n" +
            "                        \"nightProbabilityOfPrecipitation\": 6,\n" +
            "                        \"dayProbabilityOfSnow\": 0,\n" +
            "                        \"nightProbabilityOfSnow\": 0,\n" +
            "                        \"dayProbabilityOfHeavySnow\": 0,\n" +
            "                        \"nightProbabilityOfHeavySnow\": 0,\n" +
            "                        \"dayProbabilityOfRain\": 9,\n" +
            "                        \"nightProbabilityOfRain\": 6,\n" +
            "                        \"dayProbabilityOfHeavyRain\": 3,\n" +
            "                        \"nightProbabilityOfHeavyRain\": 4,\n" +
            "                        \"dayProbabilityOfHail\": 0,\n" +
            "                        \"nightProbabilityOfHail\": 1,\n" +
            "                        \"dayProbabilityOfSferics\": 1,\n" +
            "                        \"nightProbabilityOfSferics\": 1\n" +
            "                    }\n" +
            "                ]\n" +
            "            }\n" +
            "        }\n" +
            "    ],\n" +
            "    \"parameters\": [\n" +
            "        {\n" +
            "            \"daySignificantWeatherCode\": {\n" +
            "                \"type\": \"Parameter\",\n" +
            "                \"description\": \"Day Significant Weather Code\",\n" +
            "                \"unit\": {\n" +
            "                    \"label\": \"dimensionless\",\n" +
            "                    \"symbol\": {\n" +
            "                        \"value\": \"https://metoffice.apiconnect.ibmcloud.com/metoffice/production/\",\n" +
            "                        \"type\": \"1\"\n" +
            "                    }\n" +
            "                }\n" +
            "            },\n" +
            "            \"midnightRelativeHumidity\": {\n" +
            "                \"type\": \"Parameter\",\n" +
            "                \"description\": \"Relative Humidity at Local Midnight\",\n" +
            "                \"unit\": {\n" +
            "                    \"label\": \"percentage\",\n" +
            "                    \"symbol\": {\n" +
            "                        \"value\": \"http://www.opengis.net/def/uom/UCUM/\",\n" +
            "                        \"type\": \"%\"\n" +
            "                    }\n" +
            "                }\n" +
            "            },\n" +
            "            \"nightProbabilityOfHeavyRain\": {\n" +
            "                \"type\": \"Parameter\",\n" +
            "                \"description\": \"Probability of Heavy Rain During The Night\",\n" +
            "                \"unit\": {\n" +
            "                    \"label\": \"percentage\",\n" +
            "                    \"symbol\": {\n" +
            "                        \"value\": \"http://www.opengis.net/def/uom/UCUM/\",\n" +
            "                        \"type\": \"%\"\n" +
            "                    }\n" +
            "                }\n" +
            "            },\n" +
            "            \"midnight10MWindSpeed\": {\n" +
            "                \"type\": \"Parameter\",\n" +
            "                \"description\": \"10m Wind Speed at Local Midnight\",\n" +
            "                \"unit\": {\n" +
            "                    \"label\": \"metres per second\",\n" +
            "                    \"symbol\": {\n" +
            "                        \"value\": \"http://www.opengis.net/def/uom/UCUM/\",\n" +
            "                        \"type\": \"m/s\"\n" +
            "                    }\n" +
            "                }\n" +
            "            },\n" +
            "            \"nightUpperBoundMinFeelsLikeTemp\": {\n" +
            "                \"type\": \"Parameter\",\n" +
            "                \"description\": \"Upper Bound on Night Minimum Feels Like Air Temperature\",\n" +
            "                \"unit\": {\n" +
            "                    \"label\": \"degrees Celsius\",\n" +
            "                    \"symbol\": {\n" +
            "                        \"value\": \"http://www.opengis.net/def/uom/UCUM/\",\n" +
            "                        \"type\": \"Cel\"\n" +
            "                    }\n" +
            "                }\n" +
            "            },\n" +
            "            \"nightUpperBoundMinTemp\": {\n" +
            "                \"type\": \"Parameter\",\n" +
            "                \"description\": \"Upper Bound on Night Minimum Screen Air Temperature\",\n" +
            "                \"unit\": {\n" +
            "                    \"label\": \"degrees Celsius\",\n" +
            "                    \"symbol\": {\n" +
            "                        \"value\": \"http://www.opengis.net/def/uom/UCUM/\",\n" +
            "                        \"type\": \"Cel\"\n" +
            "                    }\n" +
            "                }\n" +
            "            },\n" +
            "            \"midnightVisibility\": {\n" +
            "                \"type\": \"Parameter\",\n" +
            "                \"description\": \"Visibility at Local Midnight\",\n" +
            "                \"unit\": {\n" +
            "                    \"label\": \"metres\",\n" +
            "                    \"symbol\": {\n" +
            "                        \"value\": \"http://www.opengis.net/def/uom/UCUM/\",\n" +
            "                        \"type\": \"m\"\n" +
            "                    }\n" +
            "                }\n" +
            "            },\n" +
            "            \"dayUpperBoundMaxFeelsLikeTemp\": {\n" +
            "                \"type\": \"Parameter\",\n" +
            "                \"description\": \"Upper Bound on Day Maximum Feels Like Air Temperature\",\n" +
            "                \"unit\": {\n" +
            "                    \"label\": \"degrees Celsius\",\n" +
            "                    \"symbol\": {\n" +
            "                        \"value\": \"http://www.opengis.net/def/uom/UCUM/\",\n" +
            "                        \"type\": \"Cel\"\n" +
            "                    }\n" +
            "                }\n" +
            "            },\n" +
            "            \"nightProbabilityOfRain\": {\n" +
            "                \"type\": \"Parameter\",\n" +
            "                \"description\": \"Probability of Rain During The Night\",\n" +
            "                \"unit\": {\n" +
            "                    \"label\": \"percentage\",\n" +
            "                    \"symbol\": {\n" +
            "                        \"value\": \"http://www.opengis.net/def/uom/UCUM/\",\n" +
            "                        \"type\": \"%\"\n" +
            "                    }\n" +
            "                }\n" +
            "            },\n" +
            "            \"midday10MWindDirection\": {\n" +
            "                \"type\": \"Parameter\",\n" +
            "                \"description\": \"10m Wind Direction at Local Midday\",\n" +
            "                \"unit\": {\n" +
            "                    \"label\": \"degrees\",\n" +
            "                    \"symbol\": {\n" +
            "                        \"value\": \"http://www.opengis.net/def/uom/UCUM/\",\n" +
            "                        \"type\": \"deg\"\n" +
            "                    }\n" +
            "                }\n" +
            "            },\n" +
            "            \"nightLowerBoundMinFeelsLikeTemp\": {\n" +
            "                \"type\": \"Parameter\",\n" +
            "                \"description\": \"Lower Bound on Night Minimum Feels Like Air Temperature\",\n" +
            "                \"unit\": {\n" +
            "                    \"label\": \"degrees Celsius\",\n" +
            "                    \"symbol\": {\n" +
            "                        \"value\": \"http://www.opengis.net/def/uom/UCUM/\",\n" +
            "                        \"type\": \"Cel\"\n" +
            "                    }\n" +
            "                }\n" +
            "            },\n" +
            "            \"nightProbabilityOfHail\": {\n" +
            "                \"type\": \"Parameter\",\n" +
            "                \"description\": \"Probability of Hail During The Night\",\n" +
            "                \"unit\": {\n" +
            "                    \"label\": \"percentage\",\n" +
            "                    \"symbol\": {\n" +
            "                        \"value\": \"http://www.opengis.net/def/uom/UCUM/\",\n" +
            "                        \"type\": \"%\"\n" +
            "                    }\n" +
            "                }\n" +
            "            },\n" +
            "            \"middayMslp\": {\n" +
            "                \"type\": \"Parameter\",\n" +
            "                \"description\": \"Mean Sea Level Pressure at Local Midday\",\n" +
            "                \"unit\": {\n" +
            "                    \"label\": \"pascals\",\n" +
            "                    \"symbol\": {\n" +
            "                        \"value\": \"http://www.opengis.net/def/uom/UCUM/\",\n" +
            "                        \"type\": \"Pa\"\n" +
            "                    }\n" +
            "                }\n" +
            "            },\n" +
            "            \"dayProbabilityOfHeavySnow\": {\n" +
            "                \"type\": \"Parameter\",\n" +
            "                \"description\": \"Probability of Heavy Snow During The Day\",\n" +
            "                \"unit\": {\n" +
            "                    \"label\": \"percentage\",\n" +
            "                    \"symbol\": {\n" +
            "                        \"value\": \"http://www.opengis.net/def/uom/UCUM/\",\n" +
            "                        \"type\": \"%\"\n" +
            "                    }\n" +
            "                }\n" +
            "            },\n" +
            "            \"nightProbabilityOfPrecipitation\": {\n" +
            "                \"type\": \"Parameter\",\n" +
            "                \"description\": \"Probability of Precipitation During The Night\",\n" +
            "                \"unit\": {\n" +
            "                    \"label\": \"percentage\",\n" +
            "                    \"symbol\": {\n" +
            "                        \"value\": \"http://www.opengis.net/def/uom/UCUM/\",\n" +
            "                        \"type\": \"%\"\n" +
            "                    }\n" +
            "                }\n" +
            "            },\n" +
            "            \"dayProbabilityOfHail\": {\n" +
            "                \"type\": \"Parameter\",\n" +
            "                \"description\": \"Probability of Hail During The Day\",\n" +
            "                \"unit\": {\n" +
            "                    \"label\": \"percentage\",\n" +
            "                    \"symbol\": {\n" +
            "                        \"value\": \"http://www.opengis.net/def/uom/UCUM/\",\n" +
            "                        \"type\": \"%\"\n" +
            "                    }\n" +
            "                }\n" +
            "            },\n" +
            "            \"dayProbabilityOfRain\": {\n" +
            "                \"type\": \"Parameter\",\n" +
            "                \"description\": \"Probability of Rain During The Day\",\n" +
            "                \"unit\": {\n" +
            "                    \"label\": \"percentage\",\n" +
            "                    \"symbol\": {\n" +
            "                        \"value\": \"http://www.opengis.net/def/uom/UCUM/\",\n" +
            "                        \"type\": \"%\"\n" +
            "                    }\n" +
            "                }\n" +
            "            },\n" +
            "            \"midday10MWindSpeed\": {\n" +
            "                \"type\": \"Parameter\",\n" +
            "                \"description\": \"10m Wind Speed at Local Midday\",\n" +
            "                \"unit\": {\n" +
            "                    \"label\": \"metres per second\",\n" +
            "                    \"symbol\": {\n" +
            "                        \"value\": \"http://www.opengis.net/def/uom/UCUM/\",\n" +
            "                        \"type\": \"m/s\"\n" +
            "                    }\n" +
            "                }\n" +
            "            },\n" +
            "            \"midday10MWindGust\": {\n" +
            "                \"type\": \"Parameter\",\n" +
            "                \"description\": \"10m Wind Gust Speed at Local Midday\",\n" +
            "                \"unit\": {\n" +
            "                    \"label\": \"metres per second\",\n" +
            "                    \"symbol\": {\n" +
            "                        \"value\": \"http://www.opengis.net/def/uom/UCUM/\",\n" +
            "                        \"type\": \"m/s\"\n" +
            "                    }\n" +
            "                }\n" +
            "            },\n" +
            "            \"middayVisibility\": {\n" +
            "                \"type\": \"Parameter\",\n" +
            "                \"description\": \"Visibility at Local Midday\",\n" +
            "                \"unit\": {\n" +
            "                    \"label\": \"metres\",\n" +
            "                    \"symbol\": {\n" +
            "                        \"value\": \"http://www.opengis.net/def/uom/UCUM/\",\n" +
            "                        \"type\": \"m\"\n" +
            "                    }\n" +
            "                }\n" +
            "            },\n" +
            "            \"midnight10MWindGust\": {\n" +
            "                \"type\": \"Parameter\",\n" +
            "                \"description\": \"10m Wind Gust Speed at Local Midnight\",\n" +
            "                \"unit\": {\n" +
            "                    \"label\": \"metres per second\",\n" +
            "                    \"symbol\": {\n" +
            "                        \"value\": \"http://www.opengis.net/def/uom/UCUM/\",\n" +
            "                        \"type\": \"m/s\"\n" +
            "                    }\n" +
            "                }\n" +
            "            },\n" +
            "            \"midnightMslp\": {\n" +
            "                \"type\": \"Parameter\",\n" +
            "                \"description\": \"Mean Sea Level Pressure at Local Midnight\",\n" +
            "                \"unit\": {\n" +
            "                    \"label\": \"pascals\",\n" +
            "                    \"symbol\": {\n" +
            "                        \"value\": \"http://www.opengis.net/def/uom/UCUM/\",\n" +
            "                        \"type\": \"Pa\"\n" +
            "                    }\n" +
            "                }\n" +
            "            },\n" +
            "            \"dayProbabilityOfSferics\": {\n" +
            "                \"type\": \"Parameter\",\n" +
            "                \"description\": \"Probability of Sferics During The Day\",\n" +
            "                \"unit\": {\n" +
            "                    \"label\": \"percentage\",\n" +
            "                    \"symbol\": {\n" +
            "                        \"value\": \"http://www.opengis.net/def/uom/UCUM/\",\n" +
            "                        \"type\": \"%\"\n" +
            "                    }\n" +
            "                }\n" +
            "            },\n" +
            "            \"nightSignificantWeatherCode\": {\n" +
            "                \"type\": \"Parameter\",\n" +
            "                \"description\": \"Night Significant Weather Code\",\n" +
            "                \"unit\": {\n" +
            "                    \"label\": \"dimensionless\",\n" +
            "                    \"symbol\": {\n" +
            "                        \"value\": \"https://metoffice.apiconnect.ibmcloud.com/metoffice/production/\",\n" +
            "                        \"type\": \"1\"\n" +
            "                    }\n" +
            "                }\n" +
            "            },\n" +
            "            \"dayProbabilityOfPrecipitation\": {\n" +
            "                \"type\": \"Parameter\",\n" +
            "                \"description\": \"Probability of Precipitation During The Day\",\n" +
            "                \"unit\": {\n" +
            "                    \"label\": \"percentage\",\n" +
            "                    \"symbol\": {\n" +
            "                        \"value\": \"http://www.opengis.net/def/uom/UCUM/\",\n" +
            "                        \"type\": \"%\"\n" +
            "                    }\n" +
            "                }\n" +
            "            },\n" +
            "            \"dayProbabilityOfHeavyRain\": {\n" +
            "                \"type\": \"Parameter\",\n" +
            "                \"description\": \"Probability of Heavy Rain During The Day\",\n" +
            "                \"unit\": {\n" +
            "                    \"label\": \"percentage\",\n" +
            "                    \"symbol\": {\n" +
            "                        \"value\": \"http://www.opengis.net/def/uom/UCUM/\",\n" +
            "                        \"type\": \"%\"\n" +
            "                    }\n" +
            "                }\n" +
            "            },\n" +
            "            \"dayMaxScreenTemperature\": {\n" +
            "                \"type\": \"Parameter\",\n" +
            "                \"description\": \"Day Maximum Screen Air Temperature\",\n" +
            "                \"unit\": {\n" +
            "                    \"label\": \"degrees Celsius\",\n" +
            "                    \"symbol\": {\n" +
            "                        \"value\": \"http://www.opengis.net/def/uom/UCUM/\",\n" +
            "                        \"type\": \"Cel\"\n" +
            "                    }\n" +
            "                }\n" +
            "            },\n" +
            "            \"nightMinScreenTemperature\": {\n" +
            "                \"type\": \"Parameter\",\n" +
            "                \"description\": \"Night Minimum Screen Air Temperature\",\n" +
            "                \"unit\": {\n" +
            "                    \"label\": \"degrees Celsius\",\n" +
            "                    \"symbol\": {\n" +
            "                        \"value\": \"http://www.opengis.net/def/uom/UCUM/\",\n" +
            "                        \"type\": \"Cel\"\n" +
            "                    }\n" +
            "                }\n" +
            "            },\n" +
            "            \"midnight10MWindDirection\": {\n" +
            "                \"type\": \"Parameter\",\n" +
            "                \"description\": \"10m Wind Direction at Local Midnight\",\n" +
            "                \"unit\": {\n" +
            "                    \"label\": \"degrees\",\n" +
            "                    \"symbol\": {\n" +
            "                        \"value\": \"http://www.opengis.net/def/uom/UCUM/\",\n" +
            "                        \"type\": \"deg\"\n" +
            "                    }\n" +
            "                }\n" +
            "            },\n" +
            "            \"maxUvIndex\": {\n" +
            "                \"type\": \"Parameter\",\n" +
            "                \"description\": \"Day Maximum UV Index\",\n" +
            "                \"unit\": {\n" +
            "                    \"label\": \"dimensionless\",\n" +
            "                    \"symbol\": {\n" +
            "                        \"value\": \"http://www.opengis.net/def/uom/UCUM/\",\n" +
            "                        \"type\": \"1\"\n" +
            "                    }\n" +
            "                }\n" +
            "            },\n" +
            "            \"dayProbabilityOfSnow\": {\n" +
            "                \"type\": \"Parameter\",\n" +
            "                \"description\": \"Probability of Snow During The Day\",\n" +
            "                \"unit\": {\n" +
            "                    \"label\": \"percentage\",\n" +
            "                    \"symbol\": {\n" +
            "                        \"value\": \"http://www.opengis.net/def/uom/UCUM/\",\n" +
            "                        \"type\": \"%\"\n" +
            "                    }\n" +
            "                }\n" +
            "            },\n" +
            "            \"nightProbabilityOfSnow\": {\n" +
            "                \"type\": \"Parameter\",\n" +
            "                \"description\": \"Probability of Snow During The Night\",\n" +
            "                \"unit\": {\n" +
            "                    \"label\": \"percentage\",\n" +
            "                    \"symbol\": {\n" +
            "                        \"value\": \"http://www.opengis.net/def/uom/UCUM/\",\n" +
            "                        \"type\": \"%\"\n" +
            "                    }\n" +
            "                }\n" +
            "            },\n" +
            "            \"dayLowerBoundMaxTemp\": {\n" +
            "                \"type\": \"Parameter\",\n" +
            "                \"description\": \"Lower Bound on Day Maximum Screen Air Temperature\",\n" +
            "                \"unit\": {\n" +
            "                    \"label\": \"degrees Celsius\",\n" +
            "                    \"symbol\": {\n" +
            "                        \"value\": \"http://www.opengis.net/def/uom/UCUM/\",\n" +
            "                        \"type\": \"Cel\"\n" +
            "                    }\n" +
            "                }\n" +
            "            },\n" +
            "            \"nightProbabilityOfHeavySnow\": {\n" +
            "                \"type\": \"Parameter\",\n" +
            "                \"description\": \"Probability of Heavy Snow During The Night\",\n" +
            "                \"unit\": {\n" +
            "                    \"label\": \"percentage\",\n" +
            "                    \"symbol\": {\n" +
            "                        \"value\": \"http://www.opengis.net/def/uom/UCUM/\",\n" +
            "                        \"type\": \"%\"\n" +
            "                    }\n" +
            "                }\n" +
            "            },\n" +
            "            \"dayLowerBoundMaxFeelsLikeTemp\": {\n" +
            "                \"type\": \"Parameter\",\n" +
            "                \"description\": \"Lower Bound on Day Maximum Feels Like Air Temperature\",\n" +
            "                \"unit\": {\n" +
            "                    \"label\": \"degrees Celsius\",\n" +
            "                    \"symbol\": {\n" +
            "                        \"value\": \"http://www.opengis.net/def/uom/UCUM/\",\n" +
            "                        \"type\": \"Cel\"\n" +
            "                    }\n" +
            "                }\n" +
            "            },\n" +
            "            \"dayUpperBoundMaxTemp\": {\n" +
            "                \"type\": \"Parameter\",\n" +
            "                \"description\": \"Upper Bound on Day Maximum Screen Air Temperature\",\n" +
            "                \"unit\": {\n" +
            "                    \"label\": \"degrees Celsius\",\n" +
            "                    \"symbol\": {\n" +
            "                        \"value\": \"http://www.opengis.net/def/uom/UCUM/\",\n" +
            "                        \"type\": \"Cel\"\n" +
            "                    }\n" +
            "                }\n" +
            "            },\n" +
            "            \"dayMaxFeelsLikeTemp\": {\n" +
            "                \"type\": \"Parameter\",\n" +
            "                \"description\": \"Day Maximum Feels Like Air Temperature\",\n" +
            "                \"unit\": {\n" +
            "                    \"label\": \"degrees Celsius\",\n" +
            "                    \"symbol\": {\n" +
            "                        \"value\": \"http://www.opengis.net/def/uom/UCUM/\",\n" +
            "                        \"type\": \"Cel\"\n" +
            "                    }\n" +
            "                }\n" +
            "            },\n" +
            "            \"middayRelativeHumidity\": {\n" +
            "                \"type\": \"Parameter\",\n" +
            "                \"description\": \"Relative Humidity at Local Midday\",\n" +
            "                \"unit\": {\n" +
            "                    \"label\": \"percentage\",\n" +
            "                    \"symbol\": {\n" +
            "                        \"value\": \"http://www.opengis.net/def/uom/UCUM/\",\n" +
            "                        \"type\": \"%\"\n" +
            "                    }\n" +
            "                }\n" +
            "            },\n" +
            "            \"nightLowerBoundMinTemp\": {\n" +
            "                \"type\": \"Parameter\",\n" +
            "                \"description\": \"Lower Bound on Night Minimum Screen Air Temperature\",\n" +
            "                \"unit\": {\n" +
            "                    \"label\": \"degrees Celsius\",\n" +
            "                    \"symbol\": {\n" +
            "                        \"value\": \"http://www.opengis.net/def/uom/UCUM/\",\n" +
            "                        \"type\": \"Cel\"\n" +
            "                    }\n" +
            "                }\n" +
            "            },\n" +
            "            \"nightMinFeelsLikeTemp\": {\n" +
            "                \"type\": \"Parameter\",\n" +
            "                \"description\": \"Night Minimum Feels Like Air Temperature\",\n" +
            "                \"unit\": {\n" +
            "                    \"label\": \"degrees Celsius\",\n" +
            "                    \"symbol\": {\n" +
            "                        \"value\": \"http://www.opengis.net/def/uom/UCUM/\",\n" +
            "                        \"type\": \"Cel\"\n" +
            "                    }\n" +
            "                }\n" +
            "            },\n" +
            "            \"nightProbabilityOfSferics\": {\n" +
            "                \"type\": \"Parameter\",\n" +
            "                \"description\": \"Probability of Sferics During The Night\",\n" +
            "                \"unit\": {\n" +
            "                    \"label\": \"percentage\",\n" +
            "                    \"symbol\": {\n" +
            "                        \"value\": \"http://www.opengis.net/def/uom/UCUM/\",\n" +
            "                        \"type\": \"%\"\n" +
            "                    }\n" +
            "                }\n" +
            "            }\n" +
            "        }\n" +
            "    ]\n" +
            "}";

    public final static String siteApiHourlyResponse = "{\n" +
            "    \"type\": \"FeatureCollection\",\n" +
            "    \"features\": [\n" +
            "        {\n" +
            "            \"type\": \"Feature\",\n" +
            "            \"geometry\": {\n" +
            "                \"type\": \"Point\",\n" +
            "                \"coordinates\": [\n" +
            "                    -0.32430000000000003,\n" +
            "                    51.0624,\n" +
            "                    50.0\n" +
            "                ]\n" +
            "            },\n" +
            "            \"properties\": {\n" +
            "                \"location\": {\n" +
            "                    \"name\": \"Horsham\"\n" +
            "                },\n" +
            "                \"requestPointDistance\": 0.1508,\n" +
            "                \"modelRunDate\": \"2022-09-17T20:00Z\",\n" +
            "                \"timeSeries\": [\n" +
            "                    {\n" +
            "                        \"time\": \"2022-09-17T20:00Z\",\n" +
            "                        \"screenTemperature\": 8.55,\n" +
            "                        \"maxScreenAirTemp\": 10.36,\n" +
            "                        \"minScreenAirTemp\": 8.54,\n" +
            "                        \"screenDewPointTemperature\": 4.67,\n" +
            "                        \"feelsLikeTemperature\": 8.18,\n" +
            "                        \"windSpeed10m\": 0.46,\n" +
            "                        \"windDirectionFrom10m\": 297,\n" +
            "                        \"windGustSpeed10m\": 4.63,\n" +
            "                        \"max10mWindGust\": 6.43,\n" +
            "                        \"visibility\": 19040,\n" +
            "                        \"screenRelativeHumidity\": 76.51,\n" +
            "                        \"mslp\": 102230,\n" +
            "                        \"uvIndex\": 1,\n" +
            "                        \"significantWeatherCode\": 2,\n" +
            "                        \"precipitationRate\": 3,\n" +
            "                        \"totalPrecipAmount\": 4,\n" +
            "                        \"totalSnowAmount\": 5,\n" +
            "                        \"probOfPrecipitation\": 60\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"time\": \"2022-09-17T21:00Z\",\n" +
            "                        \"screenTemperature\": 10.09,\n" +
            "                        \"maxScreenAirTemp\": 10.09,\n" +
            "                        \"minScreenAirTemp\": 8.55,\n" +
            "                        \"screenDewPointTemperature\": 4.6,\n" +
            "                        \"feelsLikeTemperature\": 9.77,\n" +
            "                        \"windSpeed10m\": 1.3,\n" +
            "                        \"windDirectionFrom10m\": 336,\n" +
            "                        \"windGustSpeed10m\": 7.55,\n" +
            "                        \"max10mWindGust\": 7.65,\n" +
            "                        \"visibility\": 20405,\n" +
            "                        \"screenRelativeHumidity\": 68.6,\n" +
            "                        \"mslp\": 102283,\n" +
            "                        \"uvIndex\": 0,\n" +
            "                        \"significantWeatherCode\": 2,\n" +
            "                        \"precipitationRate\": 0,\n" +
            "                        \"totalPrecipAmount\": 0,\n" +
            "                        \"totalSnowAmount\": 0,\n" +
            "                        \"probOfPrecipitation\": 0\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"time\": \"2022-09-17T22:00Z\",\n" +
            "                        \"screenTemperature\": 10.18,\n" +
            "                        \"maxScreenAirTemp\": 10.18,\n" +
            "                        \"minScreenAirTemp\": 10.09,\n" +
            "                        \"screenDewPointTemperature\": 4.81,\n" +
            "                        \"feelsLikeTemperature\": 9.93,\n" +
            "                        \"windSpeed10m\": 1.14,\n" +
            "                        \"windDirectionFrom10m\": 318,\n" +
            "                        \"windGustSpeed10m\": 6.53,\n" +
            "                        \"max10mWindGust\": 6.87,\n" +
            "                        \"visibility\": 18826,\n" +
            "                        \"screenRelativeHumidity\": 69.1,\n" +
            "                        \"mslp\": 102260,\n" +
            "                        \"uvIndex\": 0,\n" +
            "                        \"significantWeatherCode\": 0,\n" +
            "                        \"precipitationRate\": 0,\n" +
            "                        \"totalPrecipAmount\": 0,\n" +
            "                        \"totalSnowAmount\": 0,\n" +
            "                        \"probOfPrecipitation\": 0\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"time\": \"2022-09-17T23:00Z\",\n" +
            "                        \"screenTemperature\": 10.05,\n" +
            "                        \"maxScreenAirTemp\": 10.18,\n" +
            "                        \"minScreenAirTemp\": 10.01,\n" +
            "                        \"screenDewPointTemperature\": 4.69,\n" +
            "                        \"feelsLikeTemperature\": 9.35,\n" +
            "                        \"windSpeed10m\": 1.86,\n" +
            "                        \"windDirectionFrom10m\": 309,\n" +
            "                        \"windGustSpeed10m\": 7.16,\n" +
            "                        \"max10mWindGust\": 7.33,\n" +
            "                        \"visibility\": 18786,\n" +
            "                        \"screenRelativeHumidity\": 69.11,\n" +
            "                        \"mslp\": 102269,\n" +
            "                        \"uvIndex\": 0,\n" +
            "                        \"significantWeatherCode\": 0,\n" +
            "                        \"precipitationRate\": 0,\n" +
            "                        \"totalPrecipAmount\": 0,\n" +
            "                        \"totalSnowAmount\": 0,\n" +
            "                        \"probOfPrecipitation\": 0\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"time\": \"2022-09-18T00:00Z\",\n" +
            "                        \"screenTemperature\": 9.52,\n" +
            "                        \"maxScreenAirTemp\": 10.05,\n" +
            "                        \"minScreenAirTemp\": 9.49,\n" +
            "                        \"screenDewPointTemperature\": 4.89,\n" +
            "                        \"feelsLikeTemperature\": 8.81,\n" +
            "                        \"windSpeed10m\": 1.8,\n" +
            "                        \"windDirectionFrom10m\": 306,\n" +
            "                        \"windGustSpeed10m\": 6.41,\n" +
            "                        \"max10mWindGust\": 7.28,\n" +
            "                        \"visibility\": 18868,\n" +
            "                        \"screenRelativeHumidity\": 72.56,\n" +
            "                        \"mslp\": 102250,\n" +
            "                        \"uvIndex\": 0,\n" +
            "                        \"significantWeatherCode\": 0,\n" +
            "                        \"precipitationRate\": 0,\n" +
            "                        \"totalPrecipAmount\": 0,\n" +
            "                        \"totalSnowAmount\": 0,\n" +
            "                        \"probOfPrecipitation\": 0\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"time\": \"2022-09-18T01:00Z\",\n" +
            "                        \"screenTemperature\": 9.23,\n" +
            "                        \"maxScreenAirTemp\": 9.52,\n" +
            "                        \"minScreenAirTemp\": 9.19,\n" +
            "                        \"screenDewPointTemperature\": 5.01,\n" +
            "                        \"feelsLikeTemperature\": 8.19,\n" +
            "                        \"windSpeed10m\": 2.12,\n" +
            "                        \"windDirectionFrom10m\": 312,\n" +
            "                        \"windGustSpeed10m\": 7.34,\n" +
            "                        \"max10mWindGust\": 7.64,\n" +
            "                        \"visibility\": 19284,\n" +
            "                        \"screenRelativeHumidity\": 74.68,\n" +
            "                        \"mslp\": 102250,\n" +
            "                        \"uvIndex\": 0,\n" +
            "                        \"significantWeatherCode\": 0,\n" +
            "                        \"precipitationRate\": 0,\n" +
            "                        \"totalPrecipAmount\": 0,\n" +
            "                        \"totalSnowAmount\": 0,\n" +
            "                        \"probOfPrecipitation\": 0\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"time\": \"2022-09-18T02:00Z\",\n" +
            "                        \"screenTemperature\": 8.9,\n" +
            "                        \"maxScreenAirTemp\": 9.23,\n" +
            "                        \"minScreenAirTemp\": 8.88,\n" +
            "                        \"screenDewPointTemperature\": 5.08,\n" +
            "                        \"feelsLikeTemperature\": 7.67,\n" +
            "                        \"windSpeed10m\": 2.33,\n" +
            "                        \"windDirectionFrom10m\": 307,\n" +
            "                        \"windGustSpeed10m\": 7.6,\n" +
            "                        \"max10mWindGust\": 8.06,\n" +
            "                        \"visibility\": 19338,\n" +
            "                        \"screenRelativeHumidity\": 76.68,\n" +
            "                        \"mslp\": 102230,\n" +
            "                        \"uvIndex\": 0,\n" +
            "                        \"significantWeatherCode\": 0,\n" +
            "                        \"precipitationRate\": 0,\n" +
            "                        \"totalPrecipAmount\": 0,\n" +
            "                        \"totalSnowAmount\": 0,\n" +
            "                        \"probOfPrecipitation\": 0\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"time\": \"2022-09-18T03:00Z\",\n" +
            "                        \"screenTemperature\": 8.47,\n" +
            "                        \"maxScreenAirTemp\": 8.9,\n" +
            "                        \"minScreenAirTemp\": 8.45,\n" +
            "                        \"screenDewPointTemperature\": 5.35,\n" +
            "                        \"feelsLikeTemperature\": 7.21,\n" +
            "                        \"windSpeed10m\": 2.24,\n" +
            "                        \"windDirectionFrom10m\": 303,\n" +
            "                        \"windGustSpeed10m\": 7.79,\n" +
            "                        \"max10mWindGust\": 8.33,\n" +
            "                        \"visibility\": 18227,\n" +
            "                        \"screenRelativeHumidity\": 80.5,\n" +
            "                        \"mslp\": 102192,\n" +
            "                        \"uvIndex\": 0,\n" +
            "                        \"significantWeatherCode\": 0,\n" +
            "                        \"precipitationRate\": 0,\n" +
            "                        \"totalPrecipAmount\": 0,\n" +
            "                        \"totalSnowAmount\": 0,\n" +
            "                        \"probOfPrecipitation\": 0\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"time\": \"2022-09-18T04:00Z\",\n" +
            "                        \"screenTemperature\": 8.31,\n" +
            "                        \"maxScreenAirTemp\": 8.47,\n" +
            "                        \"minScreenAirTemp\": 8.29,\n" +
            "                        \"screenDewPointTemperature\": 5.52,\n" +
            "                        \"feelsLikeTemperature\": 6.88,\n" +
            "                        \"windSpeed10m\": 2.42,\n" +
            "                        \"windDirectionFrom10m\": 302,\n" +
            "                        \"windGustSpeed10m\": 8.17,\n" +
            "                        \"max10mWindGust\": 8.65,\n" +
            "                        \"visibility\": 18424,\n" +
            "                        \"screenRelativeHumidity\": 82.46,\n" +
            "                        \"mslp\": 102180,\n" +
            "                        \"uvIndex\": 0,\n" +
            "                        \"significantWeatherCode\": 2,\n" +
            "                        \"precipitationRate\": 0,\n" +
            "                        \"totalPrecipAmount\": 0,\n" +
            "                        \"totalSnowAmount\": 0,\n" +
            "                        \"probOfPrecipitation\": 0\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"time\": \"2022-09-18T05:00Z\",\n" +
            "                        \"screenTemperature\": 7.96,\n" +
            "                        \"maxScreenAirTemp\": 8.31,\n" +
            "                        \"minScreenAirTemp\": 7.95,\n" +
            "                        \"screenDewPointTemperature\": 5.54,\n" +
            "                        \"feelsLikeTemperature\": 6.5,\n" +
            "                        \"windSpeed10m\": 2.4,\n" +
            "                        \"windDirectionFrom10m\": 294,\n" +
            "                        \"windGustSpeed10m\": 7.48,\n" +
            "                        \"max10mWindGust\": 8.31,\n" +
            "                        \"visibility\": 18185,\n" +
            "                        \"screenRelativeHumidity\": 84.47,\n" +
            "                        \"mslp\": 102180,\n" +
            "                        \"uvIndex\": 0,\n" +
            "                        \"significantWeatherCode\": 0,\n" +
            "                        \"precipitationRate\": 0,\n" +
            "                        \"totalPrecipAmount\": 0,\n" +
            "                        \"totalSnowAmount\": 0,\n" +
            "                        \"probOfPrecipitation\": 0\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"time\": \"2022-09-18T06:00Z\",\n" +
            "                        \"screenTemperature\": 7.87,\n" +
            "                        \"maxScreenAirTemp\": 7.96,\n" +
            "                        \"minScreenAirTemp\": 7.8,\n" +
            "                        \"screenDewPointTemperature\": 5.62,\n" +
            "                        \"feelsLikeTemperature\": 6.32,\n" +
            "                        \"windSpeed10m\": 2.46,\n" +
            "                        \"windDirectionFrom10m\": 297,\n" +
            "                        \"windGustSpeed10m\": 7.7,\n" +
            "                        \"max10mWindGust\": 8.38,\n" +
            "                        \"visibility\": 17945,\n" +
            "                        \"screenRelativeHumidity\": 85.7,\n" +
            "                        \"mslp\": 102202,\n" +
            "                        \"uvIndex\": 1,\n" +
            "                        \"significantWeatherCode\": 1,\n" +
            "                        \"precipitationRate\": 0,\n" +
            "                        \"totalPrecipAmount\": 0,\n" +
            "                        \"totalSnowAmount\": 0,\n" +
            "                        \"probOfPrecipitation\": 0\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"time\": \"2022-09-18T07:00Z\",\n" +
            "                        \"screenTemperature\": 9.16,\n" +
            "                        \"maxScreenAirTemp\": 9.17,\n" +
            "                        \"minScreenAirTemp\": 7.87,\n" +
            "                        \"screenDewPointTemperature\": 6.1,\n" +
            "                        \"feelsLikeTemperature\": 7.44,\n" +
            "                        \"windSpeed10m\": 3.06,\n" +
            "                        \"windDirectionFrom10m\": 302,\n" +
            "                        \"windGustSpeed10m\": 8.12,\n" +
            "                        \"max10mWindGust\": 8.82,\n" +
            "                        \"visibility\": 19171,\n" +
            "                        \"screenRelativeHumidity\": 81.36,\n" +
            "                        \"mslp\": 102214,\n" +
            "                        \"uvIndex\": 1,\n" +
            "                        \"significantWeatherCode\": 1,\n" +
            "                        \"precipitationRate\": 0,\n" +
            "                        \"totalPrecipAmount\": 0,\n" +
            "                        \"totalSnowAmount\": 0,\n" +
            "                        \"probOfPrecipitation\": 0\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"time\": \"2022-09-18T08:00Z\",\n" +
            "                        \"screenTemperature\": 10.87,\n" +
            "                        \"maxScreenAirTemp\": 10.88,\n" +
            "                        \"minScreenAirTemp\": 9.16,\n" +
            "                        \"screenDewPointTemperature\": 6.56,\n" +
            "                        \"feelsLikeTemperature\": 9.2,\n" +
            "                        \"windSpeed10m\": 3.45,\n" +
            "                        \"windDirectionFrom10m\": 298,\n" +
            "                        \"windGustSpeed10m\": 6.33,\n" +
            "                        \"max10mWindGust\": 8.01,\n" +
            "                        \"visibility\": 22152,\n" +
            "                        \"screenRelativeHumidity\": 74.86,\n" +
            "                        \"mslp\": 102215,\n" +
            "                        \"uvIndex\": 2,\n" +
            "                        \"significantWeatherCode\": 1,\n" +
            "                        \"precipitationRate\": 0,\n" +
            "                        \"totalPrecipAmount\": 0,\n" +
            "                        \"totalSnowAmount\": 0,\n" +
            "                        \"probOfPrecipitation\": 0\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"time\": \"2022-09-18T09:00Z\",\n" +
            "                        \"screenTemperature\": 12.85,\n" +
            "                        \"maxScreenAirTemp\": 12.87,\n" +
            "                        \"minScreenAirTemp\": 10.87,\n" +
            "                        \"screenDewPointTemperature\": 6.89,\n" +
            "                        \"feelsLikeTemperature\": 11.14,\n" +
            "                        \"windSpeed10m\": 3.85,\n" +
            "                        \"windDirectionFrom10m\": 297,\n" +
            "                        \"windGustSpeed10m\": 6.6,\n" +
            "                        \"max10mWindGust\": 6.6,\n" +
            "                        \"visibility\": 25612,\n" +
            "                        \"screenRelativeHumidity\": 67.1,\n" +
            "                        \"mslp\": 102205,\n" +
            "                        \"uvIndex\": 2,\n" +
            "                        \"significantWeatherCode\": 1,\n" +
            "                        \"precipitationRate\": 0,\n" +
            "                        \"totalPrecipAmount\": 0,\n" +
            "                        \"totalSnowAmount\": 0,\n" +
            "                        \"probOfPrecipitation\": 0\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"time\": \"2022-09-18T10:00Z\",\n" +
            "                        \"screenTemperature\": 14.25,\n" +
            "                        \"maxScreenAirTemp\": 14.25,\n" +
            "                        \"minScreenAirTemp\": 12.85,\n" +
            "                        \"screenDewPointTemperature\": 6.55,\n" +
            "                        \"feelsLikeTemperature\": 12.04,\n" +
            "                        \"windSpeed10m\": 4.84,\n" +
            "                        \"windDirectionFrom10m\": 310,\n" +
            "                        \"windGustSpeed10m\": 8.48,\n" +
            "                        \"max10mWindGust\": 8.48,\n" +
            "                        \"visibility\": 30820,\n" +
            "                        \"screenRelativeHumidity\": 60.11,\n" +
            "                        \"mslp\": 102205,\n" +
            "                        \"uvIndex\": 3,\n" +
            "                        \"significantWeatherCode\": 3,\n" +
            "                        \"precipitationRate\": 0,\n" +
            "                        \"totalPrecipAmount\": 0,\n" +
            "                        \"totalSnowAmount\": 0,\n" +
            "                        \"probOfPrecipitation\": 0\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"time\": \"2022-09-18T11:00Z\",\n" +
            "                        \"screenTemperature\": 15.21,\n" +
            "                        \"maxScreenAirTemp\": 15.22,\n" +
            "                        \"minScreenAirTemp\": 14.25,\n" +
            "                        \"screenDewPointTemperature\": 6.41,\n" +
            "                        \"feelsLikeTemperature\": 12.94,\n" +
            "                        \"windSpeed10m\": 4.84,\n" +
            "                        \"windDirectionFrom10m\": 315,\n" +
            "                        \"windGustSpeed10m\": 8.58,\n" +
            "                        \"max10mWindGust\": 8.58,\n" +
            "                        \"visibility\": 30123,\n" +
            "                        \"screenRelativeHumidity\": 55.92,\n" +
            "                        \"mslp\": 102186,\n" +
            "                        \"uvIndex\": 3,\n" +
            "                        \"significantWeatherCode\": 3,\n" +
            "                        \"precipitationRate\": 0,\n" +
            "                        \"totalPrecipAmount\": 0,\n" +
            "                        \"totalSnowAmount\": 0,\n" +
            "                        \"probOfPrecipitation\": 0\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"time\": \"2022-09-18T12:00Z\",\n" +
            "                        \"screenTemperature\": 16.17,\n" +
            "                        \"maxScreenAirTemp\": 16.19,\n" +
            "                        \"minScreenAirTemp\": 15.21,\n" +
            "                        \"screenDewPointTemperature\": 5.87,\n" +
            "                        \"feelsLikeTemperature\": 13.74,\n" +
            "                        \"windSpeed10m\": 4.94,\n" +
            "                        \"windDirectionFrom10m\": 319,\n" +
            "                        \"windGustSpeed10m\": 8.77,\n" +
            "                        \"max10mWindGust\": 8.77,\n" +
            "                        \"visibility\": 31664,\n" +
            "                        \"screenRelativeHumidity\": 50.49,\n" +
            "                        \"mslp\": 102175,\n" +
            "                        \"uvIndex\": 4,\n" +
            "                        \"significantWeatherCode\": 3,\n" +
            "                        \"precipitationRate\": 0,\n" +
            "                        \"totalPrecipAmount\": 0,\n" +
            "                        \"totalSnowAmount\": 0,\n" +
            "                        \"probOfPrecipitation\": 1\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"time\": \"2022-09-18T13:00Z\",\n" +
            "                        \"screenTemperature\": 16.85,\n" +
            "                        \"maxScreenAirTemp\": 16.86,\n" +
            "                        \"minScreenAirTemp\": 16.17,\n" +
            "                        \"screenDewPointTemperature\": 5.38,\n" +
            "                        \"feelsLikeTemperature\": 14.38,\n" +
            "                        \"windSpeed10m\": 4.71,\n" +
            "                        \"windDirectionFrom10m\": 321,\n" +
            "                        \"windGustSpeed10m\": 8.46,\n" +
            "                        \"max10mWindGust\": 8.46,\n" +
            "                        \"visibility\": 33113,\n" +
            "                        \"screenRelativeHumidity\": 46.86,\n" +
            "                        \"mslp\": 102155,\n" +
            "                        \"uvIndex\": 3,\n" +
            "                        \"significantWeatherCode\": 7,\n" +
            "                        \"precipitationRate\": 0,\n" +
            "                        \"totalPrecipAmount\": 0,\n" +
            "                        \"totalSnowAmount\": 0,\n" +
            "                        \"probOfPrecipitation\": 4\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"time\": \"2022-09-18T14:00Z\",\n" +
            "                        \"screenTemperature\": 17.04,\n" +
            "                        \"maxScreenAirTemp\": 17.11,\n" +
            "                        \"minScreenAirTemp\": 16.85,\n" +
            "                        \"screenDewPointTemperature\": 5.42,\n" +
            "                        \"feelsLikeTemperature\": 14.55,\n" +
            "                        \"windSpeed10m\": 4.7,\n" +
            "                        \"windDirectionFrom10m\": 322,\n" +
            "                        \"windGustSpeed10m\": 8.45,\n" +
            "                        \"max10mWindGust\": 8.45,\n" +
            "                        \"visibility\": 34423,\n" +
            "                        \"screenRelativeHumidity\": 46.39,\n" +
            "                        \"mslp\": 102134,\n" +
            "                        \"uvIndex\": 2,\n" +
            "                        \"significantWeatherCode\": 7,\n" +
            "                        \"precipitationRate\": 0,\n" +
            "                        \"totalPrecipAmount\": 0,\n" +
            "                        \"totalSnowAmount\": 0,\n" +
            "                        \"probOfPrecipitation\": 4\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"time\": \"2022-09-18T15:00Z\",\n" +
            "                        \"screenTemperature\": 17.01,\n" +
            "                        \"maxScreenAirTemp\": 17.09,\n" +
            "                        \"minScreenAirTemp\": 16.98,\n" +
            "                        \"screenDewPointTemperature\": 5.55,\n" +
            "                        \"feelsLikeTemperature\": 14.74,\n" +
            "                        \"windSpeed10m\": 4.35,\n" +
            "                        \"windDirectionFrom10m\": 327,\n" +
            "                        \"windGustSpeed10m\": 7.96,\n" +
            "                        \"max10mWindGust\": 7.96,\n" +
            "                        \"visibility\": 33040,\n" +
            "                        \"screenRelativeHumidity\": 46.86,\n" +
            "                        \"mslp\": 102115,\n" +
            "                        \"uvIndex\": 1,\n" +
            "                        \"significantWeatherCode\": 7,\n" +
            "                        \"precipitationRate\": 0,\n" +
            "                        \"totalPrecipAmount\": 0,\n" +
            "                        \"totalSnowAmount\": 0,\n" +
            "                        \"probOfPrecipitation\": 4\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"time\": \"2022-09-18T16:00Z\",\n" +
            "                        \"screenTemperature\": 16.66,\n" +
            "                        \"maxScreenAirTemp\": 17.01,\n" +
            "                        \"minScreenAirTemp\": 16.62,\n" +
            "                        \"screenDewPointTemperature\": 5.97,\n" +
            "                        \"feelsLikeTemperature\": 14.58,\n" +
            "                        \"windSpeed10m\": 4.1,\n" +
            "                        \"windDirectionFrom10m\": 333,\n" +
            "                        \"windGustSpeed10m\": 7.59,\n" +
            "                        \"max10mWindGust\": 7.73,\n" +
            "                        \"visibility\": 31599,\n" +
            "                        \"screenRelativeHumidity\": 49.45,\n" +
            "                        \"mslp\": 102125,\n" +
            "                        \"uvIndex\": 1,\n" +
            "                        \"significantWeatherCode\": 7,\n" +
            "                        \"precipitationRate\": 0,\n" +
            "                        \"totalPrecipAmount\": 0,\n" +
            "                        \"totalSnowAmount\": 0,\n" +
            "                        \"probOfPrecipitation\": 4\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"time\": \"2022-09-18T17:00Z\",\n" +
            "                        \"screenTemperature\": 15.95,\n" +
            "                        \"maxScreenAirTemp\": 16.66,\n" +
            "                        \"minScreenAirTemp\": 15.93,\n" +
            "                        \"screenDewPointTemperature\": 6.7,\n" +
            "                        \"feelsLikeTemperature\": 14.31,\n" +
            "                        \"windSpeed10m\": 3.51,\n" +
            "                        \"windDirectionFrom10m\": 338,\n" +
            "                        \"windGustSpeed10m\": 6.76,\n" +
            "                        \"max10mWindGust\": 7.83,\n" +
            "                        \"visibility\": 29182,\n" +
            "                        \"screenRelativeHumidity\": 54.37,\n" +
            "                        \"mslp\": 102145,\n" +
            "                        \"uvIndex\": 1,\n" +
            "                        \"significantWeatherCode\": 7,\n" +
            "                        \"precipitationRate\": 0,\n" +
            "                        \"totalPrecipAmount\": 0,\n" +
            "                        \"totalSnowAmount\": 0,\n" +
            "                        \"probOfPrecipitation\": 4\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"time\": \"2022-09-18T18:00Z\",\n" +
            "                        \"screenTemperature\": 15.15,\n" +
            "                        \"maxScreenAirTemp\": 15.95,\n" +
            "                        \"minScreenAirTemp\": 15.14,\n" +
            "                        \"screenDewPointTemperature\": 7.3,\n" +
            "                        \"feelsLikeTemperature\": 14.12,\n" +
            "                        \"windSpeed10m\": 2.58,\n" +
            "                        \"windDirectionFrom10m\": 334,\n" +
            "                        \"windGustSpeed10m\": 5.79,\n" +
            "                        \"max10mWindGust\": 6.81,\n" +
            "                        \"visibility\": 26430,\n" +
            "                        \"screenRelativeHumidity\": 59.57,\n" +
            "                        \"mslp\": 102166,\n" +
            "                        \"uvIndex\": 1,\n" +
            "                        \"significantWeatherCode\": 7,\n" +
            "                        \"precipitationRate\": 0,\n" +
            "                        \"totalPrecipAmount\": 0,\n" +
            "                        \"totalSnowAmount\": 0,\n" +
            "                        \"probOfPrecipitation\": 4\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"time\": \"2022-09-18T19:00Z\",\n" +
            "                        \"screenTemperature\": 14.5,\n" +
            "                        \"maxScreenAirTemp\": 15.15,\n" +
            "                        \"minScreenAirTemp\": 14.48,\n" +
            "                        \"screenDewPointTemperature\": 7.6,\n" +
            "                        \"feelsLikeTemperature\": 13.52,\n" +
            "                        \"windSpeed10m\": 2.56,\n" +
            "                        \"windDirectionFrom10m\": 336,\n" +
            "                        \"windGustSpeed10m\": 6.1,\n" +
            "                        \"max10mWindGust\": 6.66,\n" +
            "                        \"visibility\": 24900,\n" +
            "                        \"screenRelativeHumidity\": 63.38,\n" +
            "                        \"mslp\": 102216,\n" +
            "                        \"uvIndex\": 0,\n" +
            "                        \"significantWeatherCode\": 7,\n" +
            "                        \"precipitationRate\": 0,\n" +
            "                        \"totalPrecipAmount\": 0,\n" +
            "                        \"totalSnowAmount\": 0,\n" +
            "                        \"probOfPrecipitation\": 4\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"time\": \"2022-09-18T20:00Z\",\n" +
            "                        \"screenTemperature\": 13.86,\n" +
            "                        \"maxScreenAirTemp\": 14.5,\n" +
            "                        \"minScreenAirTemp\": 13.83,\n" +
            "                        \"screenDewPointTemperature\": 7.93,\n" +
            "                        \"feelsLikeTemperature\": 12.99,\n" +
            "                        \"windSpeed10m\": 2.39,\n" +
            "                        \"windDirectionFrom10m\": 334,\n" +
            "                        \"windGustSpeed10m\": 6.05,\n" +
            "                        \"max10mWindGust\": 6.63,\n" +
            "                        \"visibility\": 23605,\n" +
            "                        \"screenRelativeHumidity\": 67.63,\n" +
            "                        \"mslp\": 102257,\n" +
            "                        \"uvIndex\": 0,\n" +
            "                        \"significantWeatherCode\": 2,\n" +
            "                        \"precipitationRate\": 0,\n" +
            "                        \"totalPrecipAmount\": 0,\n" +
            "                        \"totalSnowAmount\": 0,\n" +
            "                        \"probOfPrecipitation\": 1\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"time\": \"2022-09-18T21:00Z\",\n" +
            "                        \"screenTemperature\": 13.38,\n" +
            "                        \"maxScreenAirTemp\": 13.86,\n" +
            "                        \"minScreenAirTemp\": 13.35,\n" +
            "                        \"screenDewPointTemperature\": 8.24,\n" +
            "                        \"feelsLikeTemperature\": 12.52,\n" +
            "                        \"windSpeed10m\": 2.42,\n" +
            "                        \"windDirectionFrom10m\": 341,\n" +
            "                        \"windGustSpeed10m\": 6.28,\n" +
            "                        \"max10mWindGust\": 6.82,\n" +
            "                        \"visibility\": 23266,\n" +
            "                        \"screenRelativeHumidity\": 71.15,\n" +
            "                        \"mslp\": 102288,\n" +
            "                        \"uvIndex\": 0,\n" +
            "                        \"significantWeatherCode\": 2,\n" +
            "                        \"precipitationRate\": 0,\n" +
            "                        \"totalPrecipAmount\": 0,\n" +
            "                        \"totalSnowAmount\": 0,\n" +
            "                        \"probOfPrecipitation\": 1\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"time\": \"2022-09-18T22:00Z\",\n" +
            "                        \"screenTemperature\": 12.68,\n" +
            "                        \"maxScreenAirTemp\": 13.38,\n" +
            "                        \"minScreenAirTemp\": 12.65,\n" +
            "                        \"screenDewPointTemperature\": 8.4,\n" +
            "                        \"feelsLikeTemperature\": 11.86,\n" +
            "                        \"windSpeed10m\": 2.32,\n" +
            "                        \"windDirectionFrom10m\": 340,\n" +
            "                        \"windGustSpeed10m\": 6.03,\n" +
            "                        \"max10mWindGust\": 7.12,\n" +
            "                        \"visibility\": 22500,\n" +
            "                        \"screenRelativeHumidity\": 75.51,\n" +
            "                        \"mslp\": 102308,\n" +
            "                        \"uvIndex\": 0,\n" +
            "                        \"significantWeatherCode\": 2,\n" +
            "                        \"precipitationRate\": 0,\n" +
            "                        \"totalPrecipAmount\": 0,\n" +
            "                        \"totalSnowAmount\": 0,\n" +
            "                        \"probOfPrecipitation\": 1\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"time\": \"2022-09-18T23:00Z\",\n" +
            "                        \"screenTemperature\": 12.1,\n" +
            "                        \"maxScreenAirTemp\": 12.68,\n" +
            "                        \"minScreenAirTemp\": 12.06,\n" +
            "                        \"screenDewPointTemperature\": 8.61,\n" +
            "                        \"feelsLikeTemperature\": 11.46,\n" +
            "                        \"windSpeed10m\": 2.01,\n" +
            "                        \"windDirectionFrom10m\": 338,\n" +
            "                        \"windGustSpeed10m\": 5.07,\n" +
            "                        \"max10mWindGust\": 6.35,\n" +
            "                        \"visibility\": 20354,\n" +
            "                        \"screenRelativeHumidity\": 79.47,\n" +
            "                        \"mslp\": 102336,\n" +
            "                        \"uvIndex\": 0,\n" +
            "                        \"significantWeatherCode\": 2,\n" +
            "                        \"precipitationRate\": 0,\n" +
            "                        \"totalPrecipAmount\": 0,\n" +
            "                        \"totalSnowAmount\": 0,\n" +
            "                        \"probOfPrecipitation\": 1\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"time\": \"2022-09-19T00:00Z\",\n" +
            "                        \"screenTemperature\": 11.7,\n" +
            "                        \"maxScreenAirTemp\": 12.1,\n" +
            "                        \"minScreenAirTemp\": 11.61,\n" +
            "                        \"screenDewPointTemperature\": 8.7,\n" +
            "                        \"feelsLikeTemperature\": 11.07,\n" +
            "                        \"windSpeed10m\": 1.94,\n" +
            "                        \"windDirectionFrom10m\": 351,\n" +
            "                        \"windGustSpeed10m\": 4.7,\n" +
            "                        \"max10mWindGust\": 5.32,\n" +
            "                        \"visibility\": 20170,\n" +
            "                        \"screenRelativeHumidity\": 82.06,\n" +
            "                        \"mslp\": 102346,\n" +
            "                        \"uvIndex\": 0,\n" +
            "                        \"significantWeatherCode\": 2,\n" +
            "                        \"precipitationRate\": 0,\n" +
            "                        \"totalPrecipAmount\": 0,\n" +
            "                        \"totalSnowAmount\": 0,\n" +
            "                        \"probOfPrecipitation\": 1\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"time\": \"2022-09-19T01:00Z\",\n" +
            "                        \"screenTemperature\": 11.18,\n" +
            "                        \"maxScreenAirTemp\": 11.7,\n" +
            "                        \"minScreenAirTemp\": 11.15,\n" +
            "                        \"screenDewPointTemperature\": 8.61,\n" +
            "                        \"feelsLikeTemperature\": 10.57,\n" +
            "                        \"windSpeed10m\": 1.83,\n" +
            "                        \"windDirectionFrom10m\": 337,\n" +
            "                        \"windGustSpeed10m\": 4.23,\n" +
            "                        \"max10mWindGust\": 5.05,\n" +
            "                        \"visibility\": 19729,\n" +
            "                        \"screenRelativeHumidity\": 84.51,\n" +
            "                        \"mslp\": 102347,\n" +
            "                        \"uvIndex\": 0,\n" +
            "                        \"significantWeatherCode\": 2,\n" +
            "                        \"precipitationRate\": 0,\n" +
            "                        \"totalPrecipAmount\": 0,\n" +
            "                        \"totalSnowAmount\": 0,\n" +
            "                        \"probOfPrecipitation\": 1\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"time\": \"2022-09-19T02:00Z\",\n" +
            "                        \"screenTemperature\": 10.72,\n" +
            "                        \"maxScreenAirTemp\": 11.18,\n" +
            "                        \"minScreenAirTemp\": 10.67,\n" +
            "                        \"screenDewPointTemperature\": 8.38,\n" +
            "                        \"feelsLikeTemperature\": 10.21,\n" +
            "                        \"windSpeed10m\": 1.66,\n" +
            "                        \"windDirectionFrom10m\": 335,\n" +
            "                        \"windGustSpeed10m\": 3.89,\n" +
            "                        \"max10mWindGust\": 4.44,\n" +
            "                        \"visibility\": 17930,\n" +
            "                        \"screenRelativeHumidity\": 85.7,\n" +
            "                        \"mslp\": 102347,\n" +
            "                        \"uvIndex\": 0,\n" +
            "                        \"significantWeatherCode\": 2,\n" +
            "                        \"precipitationRate\": 0,\n" +
            "                        \"totalPrecipAmount\": 0,\n" +
            "                        \"totalSnowAmount\": 0,\n" +
            "                        \"probOfPrecipitation\": 1\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"time\": \"2022-09-19T03:00Z\",\n" +
            "                        \"screenTemperature\": 10.43,\n" +
            "                        \"maxScreenAirTemp\": 10.72,\n" +
            "                        \"minScreenAirTemp\": 10.26,\n" +
            "                        \"screenDewPointTemperature\": 8.45,\n" +
            "                        \"feelsLikeTemperature\": 10.05,\n" +
            "                        \"windSpeed10m\": 1.35,\n" +
            "                        \"windDirectionFrom10m\": 313,\n" +
            "                        \"windGustSpeed10m\": 3.14,\n" +
            "                        \"max10mWindGust\": 4.07,\n" +
            "                        \"visibility\": 16819,\n" +
            "                        \"screenRelativeHumidity\": 87.85,\n" +
            "                        \"mslp\": 102348,\n" +
            "                        \"uvIndex\": 0,\n" +
            "                        \"significantWeatherCode\": 2,\n" +
            "                        \"precipitationRate\": 0,\n" +
            "                        \"totalPrecipAmount\": 0,\n" +
            "                        \"totalSnowAmount\": 0,\n" +
            "                        \"probOfPrecipitation\": 1\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"time\": \"2022-09-19T04:00Z\",\n" +
            "                        \"screenTemperature\": 10.18,\n" +
            "                        \"maxScreenAirTemp\": 10.43,\n" +
            "                        \"minScreenAirTemp\": 10.12,\n" +
            "                        \"screenDewPointTemperature\": 8.2,\n" +
            "                        \"feelsLikeTemperature\": 9.77,\n" +
            "                        \"windSpeed10m\": 1.36,\n" +
            "                        \"windDirectionFrom10m\": 328,\n" +
            "                        \"windGustSpeed10m\": 2.99,\n" +
            "                        \"max10mWindGust\": 3.74,\n" +
            "                        \"visibility\": 16296,\n" +
            "                        \"screenRelativeHumidity\": 87.89,\n" +
            "                        \"mslp\": 102374,\n" +
            "                        \"uvIndex\": 0,\n" +
            "                        \"significantWeatherCode\": 2,\n" +
            "                        \"precipitationRate\": 0,\n" +
            "                        \"totalPrecipAmount\": 0,\n" +
            "                        \"totalSnowAmount\": 0,\n" +
            "                        \"probOfPrecipitation\": 1\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"time\": \"2022-09-19T05:00Z\",\n" +
            "                        \"screenTemperature\": 9.98,\n" +
            "                        \"maxScreenAirTemp\": 10.18,\n" +
            "                        \"minScreenAirTemp\": 9.89,\n" +
            "                        \"screenDewPointTemperature\": 8.28,\n" +
            "                        \"feelsLikeTemperature\": 9.55,\n" +
            "                        \"windSpeed10m\": 1.45,\n" +
            "                        \"windDirectionFrom10m\": 312,\n" +
            "                        \"windGustSpeed10m\": 3.02,\n" +
            "                        \"max10mWindGust\": 3.92,\n" +
            "                        \"visibility\": 13673,\n" +
            "                        \"screenRelativeHumidity\": 89.56,\n" +
            "                        \"mslp\": 102404,\n" +
            "                        \"uvIndex\": 0,\n" +
            "                        \"significantWeatherCode\": 2,\n" +
            "                        \"precipitationRate\": 0,\n" +
            "                        \"totalPrecipAmount\": 0,\n" +
            "                        \"totalSnowAmount\": 0,\n" +
            "                        \"probOfPrecipitation\": 2\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"time\": \"2022-09-19T06:00Z\",\n" +
            "                        \"screenTemperature\": 9.79,\n" +
            "                        \"maxScreenAirTemp\": 10.08,\n" +
            "                        \"minScreenAirTemp\": 9.58,\n" +
            "                        \"screenDewPointTemperature\": 8.33,\n" +
            "                        \"feelsLikeTemperature\": 9.37,\n" +
            "                        \"windSpeed10m\": 1.33,\n" +
            "                        \"windDirectionFrom10m\": 326,\n" +
            "                        \"windGustSpeed10m\": 3.32,\n" +
            "                        \"max10mWindGust\": 3.79,\n" +
            "                        \"visibility\": 12471,\n" +
            "                        \"screenRelativeHumidity\": 91.04,\n" +
            "                        \"mslp\": 102436,\n" +
            "                        \"uvIndex\": 1,\n" +
            "                        \"significantWeatherCode\": 3,\n" +
            "                        \"precipitationRate\": 0,\n" +
            "                        \"totalPrecipAmount\": 0,\n" +
            "                        \"totalSnowAmount\": 0,\n" +
            "                        \"probOfPrecipitation\": 1\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"time\": \"2022-09-19T07:00Z\",\n" +
            "                        \"screenTemperature\": 10.9,\n" +
            "                        \"maxScreenAirTemp\": 10.91,\n" +
            "                        \"minScreenAirTemp\": 9.79,\n" +
            "                        \"screenDewPointTemperature\": 8.79,\n" +
            "                        \"feelsLikeTemperature\": 10.53,\n" +
            "                        \"windSpeed10m\": 1.45,\n" +
            "                        \"windDirectionFrom10m\": 327,\n" +
            "                        \"windGustSpeed10m\": 3.43,\n" +
            "                        \"max10mWindGust\": 4.07,\n" +
            "                        \"visibility\": 12829,\n" +
            "                        \"screenRelativeHumidity\": 87.3,\n" +
            "                        \"mslp\": 102466,\n" +
            "                        \"uvIndex\": 1,\n" +
            "                        \"significantWeatherCode\": 3,\n" +
            "                        \"precipitationRate\": 0,\n" +
            "                        \"totalPrecipAmount\": 0,\n" +
            "                        \"totalSnowAmount\": 0,\n" +
            "                        \"probOfPrecipitation\": 1\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"time\": \"2022-09-19T08:00Z\",\n" +
            "                        \"screenTemperature\": 12.01,\n" +
            "                        \"maxScreenAirTemp\": 12.01,\n" +
            "                        \"minScreenAirTemp\": 10.9,\n" +
            "                        \"screenDewPointTemperature\": 9.21,\n" +
            "                        \"feelsLikeTemperature\": 11.72,\n" +
            "                        \"windSpeed10m\": 1.37,\n" +
            "                        \"windDirectionFrom10m\": 275,\n" +
            "                        \"windGustSpeed10m\": 2.93,\n" +
            "                        \"max10mWindGust\": 3.5,\n" +
            "                        \"visibility\": 17147,\n" +
            "                        \"screenRelativeHumidity\": 83.49,\n" +
            "                        \"mslp\": 102486,\n" +
            "                        \"uvIndex\": 2,\n" +
            "                        \"significantWeatherCode\": 7,\n" +
            "                        \"precipitationRate\": 0,\n" +
            "                        \"totalPrecipAmount\": 0,\n" +
            "                        \"totalSnowAmount\": 0,\n" +
            "                        \"probOfPrecipitation\": 5\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"time\": \"2022-09-19T09:00Z\",\n" +
            "                        \"screenTemperature\": 13.35,\n" +
            "                        \"maxScreenAirTemp\": 13.35,\n" +
            "                        \"minScreenAirTemp\": 12.01,\n" +
            "                        \"screenDewPointTemperature\": 9.36,\n" +
            "                        \"feelsLikeTemperature\": 13.07,\n" +
            "                        \"windSpeed10m\": 1.47,\n" +
            "                        \"windDirectionFrom10m\": 311,\n" +
            "                        \"windGustSpeed10m\": 2.94,\n" +
            "                        \"max10mWindGust\": 2.94,\n" +
            "                        \"visibility\": 20347,\n" +
            "                        \"screenRelativeHumidity\": 77.29,\n" +
            "                        \"mslp\": 102497,\n" +
            "                        \"uvIndex\": 2,\n" +
            "                        \"significantWeatherCode\": 7,\n" +
            "                        \"precipitationRate\": 0,\n" +
            "                        \"totalPrecipAmount\": 0,\n" +
            "                        \"totalSnowAmount\": 0,\n" +
            "                        \"probOfPrecipitation\": 4\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"time\": \"2022-09-19T10:00Z\",\n" +
            "                        \"screenTemperature\": 14.36,\n" +
            "                        \"maxScreenAirTemp\": 14.37,\n" +
            "                        \"minScreenAirTemp\": 13.35,\n" +
            "                        \"screenDewPointTemperature\": 9.14,\n" +
            "                        \"feelsLikeTemperature\": 13.87,\n" +
            "                        \"windSpeed10m\": 1.85,\n" +
            "                        \"windDirectionFrom10m\": 296,\n" +
            "                        \"windGustSpeed10m\": 3.71,\n" +
            "                        \"max10mWindGust\": 3.71,\n" +
            "                        \"visibility\": 23360,\n" +
            "                        \"screenRelativeHumidity\": 71.6,\n" +
            "                        \"mslp\": 102515,\n" +
            "                        \"uvIndex\": 2,\n" +
            "                        \"significantWeatherCode\": 7,\n" +
            "                        \"precipitationRate\": 0,\n" +
            "                        \"totalPrecipAmount\": 0,\n" +
            "                        \"totalSnowAmount\": 0,\n" +
            "                        \"probOfPrecipitation\": 4\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"time\": \"2022-09-19T11:00Z\",\n" +
            "                        \"screenTemperature\": 15.28,\n" +
            "                        \"maxScreenAirTemp\": 15.31,\n" +
            "                        \"minScreenAirTemp\": 14.36,\n" +
            "                        \"screenDewPointTemperature\": 8.96,\n" +
            "                        \"feelsLikeTemperature\": 14.54,\n" +
            "                        \"windSpeed10m\": 2.29,\n" +
            "                        \"windDirectionFrom10m\": 295,\n" +
            "                        \"windGustSpeed10m\": 4.55,\n" +
            "                        \"max10mWindGust\": 4.55,\n" +
            "                        \"visibility\": 25109,\n" +
            "                        \"screenRelativeHumidity\": 66.86,\n" +
            "                        \"mslp\": 102505,\n" +
            "                        \"uvIndex\": 3,\n" +
            "                        \"significantWeatherCode\": 7,\n" +
            "                        \"precipitationRate\": 0,\n" +
            "                        \"totalPrecipAmount\": 0,\n" +
            "                        \"totalSnowAmount\": 0,\n" +
            "                        \"probOfPrecipitation\": 5\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"time\": \"2022-09-19T12:00Z\",\n" +
            "                        \"screenTemperature\": 16.01,\n" +
            "                        \"maxScreenAirTemp\": 16.03,\n" +
            "                        \"minScreenAirTemp\": 15.28,\n" +
            "                        \"screenDewPointTemperature\": 8.73,\n" +
            "                        \"feelsLikeTemperature\": 15.04,\n" +
            "                        \"windSpeed10m\": 2.63,\n" +
            "                        \"windDirectionFrom10m\": 297,\n" +
            "                        \"windGustSpeed10m\": 5.3,\n" +
            "                        \"max10mWindGust\": 5.3,\n" +
            "                        \"visibility\": 26780,\n" +
            "                        \"screenRelativeHumidity\": 62.73,\n" +
            "                        \"mslp\": 102494,\n" +
            "                        \"uvIndex\": 3,\n" +
            "                        \"significantWeatherCode\": 7,\n" +
            "                        \"precipitationRate\": 0,\n" +
            "                        \"totalPrecipAmount\": 0,\n" +
            "                        \"totalSnowAmount\": 0,\n" +
            "                        \"probOfPrecipitation\": 5\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"time\": \"2022-09-19T13:00Z\",\n" +
            "                        \"screenTemperature\": 16.37,\n" +
            "                        \"maxScreenAirTemp\": 16.4,\n" +
            "                        \"minScreenAirTemp\": 16.01,\n" +
            "                        \"screenDewPointTemperature\": 8.74,\n" +
            "                        \"feelsLikeTemperature\": 15.37,\n" +
            "                        \"windSpeed10m\": 2.66,\n" +
            "                        \"windDirectionFrom10m\": 304,\n" +
            "                        \"windGustSpeed10m\": 5.43,\n" +
            "                        \"max10mWindGust\": 5.43,\n" +
            "                        \"visibility\": 26670,\n" +
            "                        \"screenRelativeHumidity\": 61.18,\n" +
            "                        \"mslp\": 102473,\n" +
            "                        \"uvIndex\": 3,\n" +
            "                        \"significantWeatherCode\": 7,\n" +
            "                        \"precipitationRate\": 0,\n" +
            "                        \"totalPrecipAmount\": 0,\n" +
            "                        \"totalSnowAmount\": 0,\n" +
            "                        \"probOfPrecipitation\": 5\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"time\": \"2022-09-19T14:00Z\",\n" +
            "                        \"screenTemperature\": 16.68,\n" +
            "                        \"maxScreenAirTemp\": 16.97,\n" +
            "                        \"minScreenAirTemp\": 16.37,\n" +
            "                        \"screenDewPointTemperature\": 8.62,\n" +
            "                        \"feelsLikeTemperature\": 15.53,\n" +
            "                        \"windSpeed10m\": 2.85,\n" +
            "                        \"windDirectionFrom10m\": 306,\n" +
            "                        \"windGustSpeed10m\": 5.68,\n" +
            "                        \"max10mWindGust\": 5.68,\n" +
            "                        \"visibility\": 28030,\n" +
            "                        \"screenRelativeHumidity\": 59.53,\n" +
            "                        \"mslp\": 102452,\n" +
            "                        \"uvIndex\": 2,\n" +
            "                        \"significantWeatherCode\": 7,\n" +
            "                        \"precipitationRate\": 0,\n" +
            "                        \"totalPrecipAmount\": 0,\n" +
            "                        \"totalSnowAmount\": 0,\n" +
            "                        \"probOfPrecipitation\": 5\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"time\": \"2022-09-19T15:00Z\",\n" +
            "                        \"screenTemperature\": 16.93,\n" +
            "                        \"maxScreenAirTemp\": 17.02,\n" +
            "                        \"minScreenAirTemp\": 16.68,\n" +
            "                        \"screenDewPointTemperature\": 8.63,\n" +
            "                        \"feelsLikeTemperature\": 15.72,\n" +
            "                        \"windSpeed10m\": 2.96,\n" +
            "                        \"windDirectionFrom10m\": 315,\n" +
            "                        \"windGustSpeed10m\": 5.76,\n" +
            "                        \"max10mWindGust\": 5.76,\n" +
            "                        \"visibility\": 28413,\n" +
            "                        \"screenRelativeHumidity\": 58.57,\n" +
            "                        \"mslp\": 102416,\n" +
            "                        \"uvIndex\": 1,\n" +
            "                        \"significantWeatherCode\": 7,\n" +
            "                        \"precipitationRate\": 0,\n" +
            "                        \"totalPrecipAmount\": 0,\n" +
            "                        \"totalSnowAmount\": 0,\n" +
            "                        \"probOfPrecipitation\": 4\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"time\": \"2022-09-19T16:00Z\",\n" +
            "                        \"screenTemperature\": 16.11,\n" +
            "                        \"maxScreenAirTemp\": 16.79,\n" +
            "                        \"minScreenAirTemp\": 15.97,\n" +
            "                        \"screenDewPointTemperature\": 9.0,\n" +
            "                        \"feelsLikeTemperature\": 15.35,\n" +
            "                        \"windSpeed10m\": 2.28,\n" +
            "                        \"windDirectionFrom10m\": 321,\n" +
            "                        \"windGustSpeed10m\": 4.56,\n" +
            "                        \"max10mWindGust\": 4.56,\n" +
            "                        \"visibility\": 27394,\n" +
            "                        \"screenRelativeHumidity\": 63.61,\n" +
            "                        \"mslp\": 102400,\n" +
            "                        \"uvIndex\": 1,\n" +
            "                        \"significantWeatherCode\": 7,\n" +
            "                        \"precipitationRate\": 0,\n" +
            "                        \"totalPrecipAmount\": 0,\n" +
            "                        \"totalSnowAmount\": 0,\n" +
            "                        \"probOfPrecipitation\": 5\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"time\": \"2022-09-19T17:00Z\",\n" +
            "                        \"screenTemperature\": 15.32,\n" +
            "                        \"maxScreenAirTemp\": 16.11,\n" +
            "                        \"minScreenAirTemp\": 15.32,\n" +
            "                        \"screenDewPointTemperature\": 9.35,\n" +
            "                        \"feelsLikeTemperature\": 14.76,\n" +
            "                        \"windSpeed10m\": 2.02,\n" +
            "                        \"windDirectionFrom10m\": 314,\n" +
            "                        \"windGustSpeed10m\": 3.82,\n" +
            "                        \"max10mWindGust\": 3.82,\n" +
            "                        \"visibility\": 25612,\n" +
            "                        \"screenRelativeHumidity\": 68.54,\n" +
            "                        \"mslp\": 102398,\n" +
            "                        \"uvIndex\": 1,\n" +
            "                        \"significantWeatherCode\": 7,\n" +
            "                        \"precipitationRate\": 0,\n" +
            "                        \"totalPrecipAmount\": 0,\n" +
            "                        \"totalSnowAmount\": 0,\n" +
            "                        \"probOfPrecipitation\": 4\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"time\": \"2022-09-19T18:00Z\",\n" +
            "                        \"screenTemperature\": 14.38,\n" +
            "                        \"screenDewPointTemperature\": 10.03,\n" +
            "                        \"feelsLikeTemperature\": 14.09,\n" +
            "                        \"windSpeed10m\": 1.7,\n" +
            "                        \"windDirectionFrom10m\": 310,\n" +
            "                        \"windGustSpeed10m\": 3.05,\n" +
            "                        \"visibility\": 24267,\n" +
            "                        \"screenRelativeHumidity\": 75.61,\n" +
            "                        \"mslp\": 102388,\n" +
            "                        \"uvIndex\": 0,\n" +
            "                        \"significantWeatherCode\": 7,\n" +
            "                        \"precipitationRate\": 0,\n" +
            "                        \"probOfPrecipitation\": 4\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"time\": \"2022-09-19T19:00Z\",\n" +
            "                        \"screenTemperature\": 13.51,\n" +
            "                        \"screenDewPointTemperature\": 9.89,\n" +
            "                        \"feelsLikeTemperature\": 13.18,\n" +
            "                        \"windSpeed10m\": 1.75,\n" +
            "                        \"windDirectionFrom10m\": 302,\n" +
            "                        \"windGustSpeed10m\": 3.09,\n" +
            "                        \"visibility\": 24262,\n" +
            "                        \"screenRelativeHumidity\": 79.35,\n" +
            "                        \"mslp\": 102415,\n" +
            "                        \"uvIndex\": 0,\n" +
            "                        \"significantWeatherCode\": 2,\n" +
            "                        \"precipitationRate\": 0,\n" +
            "                        \"probOfPrecipitation\": 1\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"time\": \"2022-09-19T20:00Z\",\n" +
            "                        \"screenTemperature\": 12.91,\n" +
            "                        \"screenDewPointTemperature\": 9.79,\n" +
            "                        \"feelsLikeTemperature\": 12.56,\n" +
            "                        \"windSpeed10m\": 1.7,\n" +
            "                        \"windDirectionFrom10m\": 295,\n" +
            "                        \"windGustSpeed10m\": 3.04,\n" +
            "                        \"visibility\": 21268,\n" +
            "                        \"screenRelativeHumidity\": 82.04,\n" +
            "                        \"mslp\": 102435,\n" +
            "                        \"uvIndex\": 0,\n" +
            "                        \"significantWeatherCode\": 2,\n" +
            "                        \"precipitationRate\": 0,\n" +
            "                        \"probOfPrecipitation\": 1\n" +
            "                    }\n" +
            "                ]\n" +
            "            }\n" +
            "        }\n" +
            "    ],\n" +
            "    \"parameters\": [\n" +
            "        {\n" +
            "            \"totalSnowAmount\": {\n" +
            "                \"type\": \"Parameter\",\n" +
            "                \"description\": \"Total Snow Amount Over Previous Hour\",\n" +
            "                \"unit\": {\n" +
            "                    \"label\": \"millimetres\",\n" +
            "                    \"symbol\": {\n" +
            "                        \"value\": \"http://www.opengis.net/def/uom/UCUM/\",\n" +
            "                        \"type\": \"mm\"\n" +
            "                    }\n" +
            "                }\n" +
            "            },\n" +
            "            \"screenTemperature\": {\n" +
            "                \"type\": \"Parameter\",\n" +
            "                \"description\": \"Screen Air Temperature\",\n" +
            "                \"unit\": {\n" +
            "                    \"label\": \"degrees Celsius\",\n" +
            "                    \"symbol\": {\n" +
            "                        \"value\": \"http://www.opengis.net/def/uom/UCUM/\",\n" +
            "                        \"type\": \"Cel\"\n" +
            "                    }\n" +
            "                }\n" +
            "            },\n" +
            "            \"visibility\": {\n" +
            "                \"type\": \"Parameter\",\n" +
            "                \"description\": \"Visibility\",\n" +
            "                \"unit\": {\n" +
            "                    \"label\": \"metres\",\n" +
            "                    \"symbol\": {\n" +
            "                        \"value\": \"http://www.opengis.net/def/uom/UCUM/\",\n" +
            "                        \"type\": \"m\"\n" +
            "                    }\n" +
            "                }\n" +
            "            },\n" +
            "            \"windDirectionFrom10m\": {\n" +
            "                \"type\": \"Parameter\",\n" +
            "                \"description\": \"10m Wind From Direction\",\n" +
            "                \"unit\": {\n" +
            "                    \"label\": \"degrees\",\n" +
            "                    \"symbol\": {\n" +
            "                        \"value\": \"http://www.opengis.net/def/uom/UCUM/\",\n" +
            "                        \"type\": \"deg\"\n" +
            "                    }\n" +
            "                }\n" +
            "            },\n" +
            "            \"precipitationRate\": {\n" +
            "                \"type\": \"Parameter\",\n" +
            "                \"description\": \"Precipitation Rate\",\n" +
            "                \"unit\": {\n" +
            "                    \"label\": \"millimetres per hour\",\n" +
            "                    \"symbol\": {\n" +
            "                        \"value\": \"http://www.opengis.net/def/uom/UCUM/\",\n" +
            "                        \"type\": \"mm/h\"\n" +
            "                    }\n" +
            "                }\n" +
            "            },\n" +
            "            \"maxScreenAirTemp\": {\n" +
            "                \"type\": \"Parameter\",\n" +
            "                \"description\": \"Maximum Screen Air Temperature Over Previous Hour\",\n" +
            "                \"unit\": {\n" +
            "                    \"label\": \"degrees Celsius\",\n" +
            "                    \"symbol\": {\n" +
            "                        \"value\": \"http://www.opengis.net/def/uom/UCUM/\",\n" +
            "                        \"type\": \"Cel\"\n" +
            "                    }\n" +
            "                }\n" +
            "            },\n" +
            "            \"feelsLikeTemperature\": {\n" +
            "                \"type\": \"Parameter\",\n" +
            "                \"description\": \"Feels Like Temperature\",\n" +
            "                \"unit\": {\n" +
            "                    \"label\": \"degrees Celsius\",\n" +
            "                    \"symbol\": {\n" +
            "                        \"value\": \"http://www.opengis.net/def/uom/UCUM/\",\n" +
            "                        \"type\": \"Cel\"\n" +
            "                    }\n" +
            "                }\n" +
            "            },\n" +
            "            \"screenDewPointTemperature\": {\n" +
            "                \"type\": \"Parameter\",\n" +
            "                \"description\": \"Screen Dew Point Temperature\",\n" +
            "                \"unit\": {\n" +
            "                    \"label\": \"degrees Celsius\",\n" +
            "                    \"symbol\": {\n" +
            "                        \"value\": \"http://www.opengis.net/def/uom/UCUM/\",\n" +
            "                        \"type\": \"Cel\"\n" +
            "                    }\n" +
            "                }\n" +
            "            },\n" +
            "            \"screenRelativeHumidity\": {\n" +
            "                \"type\": \"Parameter\",\n" +
            "                \"description\": \"Screen Relative Humidity\",\n" +
            "                \"unit\": {\n" +
            "                    \"label\": \"percentage\",\n" +
            "                    \"symbol\": {\n" +
            "                        \"value\": \"http://www.opengis.net/def/uom/UCUM/\",\n" +
            "                        \"type\": \"%\"\n" +
            "                    }\n" +
            "                }\n" +
            "            },\n" +
            "            \"windSpeed10m\": {\n" +
            "                \"type\": \"Parameter\",\n" +
            "                \"description\": \"10m Wind Speed\",\n" +
            "                \"unit\": {\n" +
            "                    \"label\": \"metres per second\",\n" +
            "                    \"symbol\": {\n" +
            "                        \"value\": \"http://www.opengis.net/def/uom/UCUM/\",\n" +
            "                        \"type\": \"m/s\"\n" +
            "                    }\n" +
            "                }\n" +
            "            },\n" +
            "            \"probOfPrecipitation\": {\n" +
            "                \"type\": \"Parameter\",\n" +
            "                \"description\": \"Probability of Precipitation\",\n" +
            "                \"unit\": {\n" +
            "                    \"label\": \"percentage\",\n" +
            "                    \"symbol\": {\n" +
            "                        \"value\": \"http://www.opengis.net/def/uom/UCUM/\",\n" +
            "                        \"type\": \"%\"\n" +
            "                    }\n" +
            "                }\n" +
            "            },\n" +
            "            \"max10mWindGust\": {\n" +
            "                \"type\": \"Parameter\",\n" +
            "                \"description\": \"Maximum 10m Wind Gust Speed Over Previous Hour\",\n" +
            "                \"unit\": {\n" +
            "                    \"label\": \"metres per second\",\n" +
            "                    \"symbol\": {\n" +
            "                        \"value\": \"http://www.opengis.net/def/uom/UCUM/\",\n" +
            "                        \"type\": \"m/s\"\n" +
            "                    }\n" +
            "                }\n" +
            "            },\n" +
            "            \"significantWeatherCode\": {\n" +
            "                \"type\": \"Parameter\",\n" +
            "                \"description\": \"Significant Weather Code\",\n" +
            "                \"unit\": {\n" +
            "                    \"label\": \"dimensionless\",\n" +
            "                    \"symbol\": {\n" +
            "                        \"value\": \"https://metoffice.apiconnect.ibmcloud.com/metoffice/production/\",\n" +
            "                        \"type\": \"1\"\n" +
            "                    }\n" +
            "                }\n" +
            "            },\n" +
            "            \"minScreenAirTemp\": {\n" +
            "                \"type\": \"Parameter\",\n" +
            "                \"description\": \"Minimum Screen Air Temperature Over Previous Hour\",\n" +
            "                \"unit\": {\n" +
            "                    \"label\": \"degrees Celsius\",\n" +
            "                    \"symbol\": {\n" +
            "                        \"value\": \"http://www.opengis.net/def/uom/UCUM/\",\n" +
            "                        \"type\": \"Cel\"\n" +
            "                    }\n" +
            "                }\n" +
            "            },\n" +
            "            \"totalPrecipAmount\": {\n" +
            "                \"type\": \"Parameter\",\n" +
            "                \"description\": \"Total Precipitation Amount Over Previous Hour\",\n" +
            "                \"unit\": {\n" +
            "                    \"label\": \"millimetres\",\n" +
            "                    \"symbol\": {\n" +
            "                        \"value\": \"http://www.opengis.net/def/uom/UCUM/\",\n" +
            "                        \"type\": \"mm\"\n" +
            "                    }\n" +
            "                }\n" +
            "            },\n" +
            "            \"mslp\": {\n" +
            "                \"type\": \"Parameter\",\n" +
            "                \"description\": \"Mean Sea Level Pressure\",\n" +
            "                \"unit\": {\n" +
            "                    \"label\": \"pascals\",\n" +
            "                    \"symbol\": {\n" +
            "                        \"value\": \"http://www.opengis.net/def/uom/UCUM/\",\n" +
            "                        \"type\": \"Pa\"\n" +
            "                    }\n" +
            "                }\n" +
            "            },\n" +
            "            \"windGustSpeed10m\": {\n" +
            "                \"type\": \"Parameter\",\n" +
            "                \"description\": \"10m Wind Gust Speed\",\n" +
            "                \"unit\": {\n" +
            "                    \"label\": \"metres per second\",\n" +
            "                    \"symbol\": {\n" +
            "                        \"value\": \"http://www.opengis.net/def/uom/UCUM/\",\n" +
            "                        \"type\": \"m/s\"\n" +
            "                    }\n" +
            "                }\n" +
            "            },\n" +
            "            \"uvIndex\": {\n" +
            "                \"type\": \"Parameter\",\n" +
            "                \"description\": \"UV Index\",\n" +
            "                \"unit\": {\n" +
            "                    \"label\": \"dimensionless\",\n" +
            "                    \"symbol\": {\n" +
            "                        \"value\": \"http://www.opengis.net/def/uom/UCUM/\",\n" +
            "                        \"type\": \"1\"\n" +
            "                    }\n" +
            "                }\n" +
            "            }\n" +
            "        }\n" +
            "    ]\n" +
            "}";

    @Test
    public void testSiteApiFeatureCollectionHourly() {
      SiteApiFeatureCollection response = MetOfficeDataHubBindingConstants.GSON.fromJson(siteApiHourlyResponse,
                SiteApiFeatureCollection.class);
        if (response != null) {
            assertEquals(SiteApiFeatureCollection.TYPE_SITE_API_FEATURE_COLLECTION, response.getType());
        } else {
            fail("GSON returned null");
        }
    }

    @Test
    public void testSiteApiFeatureCollectionDaily() {
        SiteApiFeatureCollection response = MetOfficeDataHubBindingConstants.GSON.fromJson(siteApiDailyResponse,
                SiteApiFeatureCollection.class);
        if (response != null) {
            assertEquals(SiteApiFeatureCollection.TYPE_SITE_API_FEATURE_COLLECTION, response.getType());
        } else {
            fail("GSON returned null");
        }
    }

    @Test
    public void testSiteApiFeatureHourly() {
        SiteApiFeatureCollection response = MetOfficeDataHubBindingConstants.GSON.fromJson(siteApiHourlyResponse,
                SiteApiFeatureCollection.class);

        assertEquals(1,response.getFeature().length);
        assertEquals(SiteApiFeature.TYPE_SITE_API_FEATURE, response.getFeature()[0].getType());
    }

    @Test
    public void testSiteApiFeatureDaily() {
        SiteApiFeatureCollection response = MetOfficeDataHubBindingConstants.GSON.fromJson(siteApiDailyResponse,
                SiteApiFeatureCollection.class);

        assertEquals(1,response.getFeature().length);
        assertEquals(SiteApiFeature.TYPE_SITE_API_FEATURE, response.getFeature()[0].getType());
    }

    @Test
    public void testSiteApiFeatureGeometryHourly() {
        SiteApiFeatureCollection response = MetOfficeDataHubBindingConstants.GSON.fromJson(siteApiHourlyResponse,
                SiteApiFeatureCollection.class);

        assertEquals(SiteApiFeaturePoint.TYPE_SITE_API_FEATURE, response.getFeature()[0].getGeometry().getType());
        assertEquals(-0.32430000000000003,response.getFeature()[0].getGeometry().getLongitude());
        assertEquals(51.0624,response.getFeature()[0].getGeometry().getLatitude());
        assertEquals(50.0,response.getFeature()[0].getGeometry().getElevation());
    }

    @Test
    public void testSiteApiFeatureGeometryDaily() {
        SiteApiFeatureCollection response = MetOfficeDataHubBindingConstants.GSON.fromJson(siteApiDailyResponse,
                SiteApiFeatureCollection.class);

        assertEquals(SiteApiFeaturePoint.TYPE_SITE_API_FEATURE, response.getFeature()[0].getGeometry().getType());
        assertEquals(-0.32430000000000003,response.getFeature()[0].getGeometry().getLongitude());
        assertEquals(51.0624,response.getFeature()[0].getGeometry().getLatitude());
        assertEquals(50.0,response.getFeature()[0].getGeometry().getElevation());
    }

    @Test
    public void testSiteApiFeatureProperitiesHourly() {
        SiteApiFeatureCollection response = MetOfficeDataHubBindingConstants.GSON.fromJson(siteApiHourlyResponse,
                SiteApiFeatureCollection.class);

        assertEquals("Horsham",response.getFeature()[0].getProperities().getLocation().getName());
        assertEquals(0.1508,response.getFeature()[0].getProperities().getRequestPointDistance());
        assertEquals("2022-09-17T20:00Z",response.getFeature()[0].getProperities().getModelRunDate());
    }

    @Test
    public void testSiteApiFeatureProperitiesDaily() {
        SiteApiFeatureCollection response = MetOfficeDataHubBindingConstants.GSON.fromJson(siteApiDailyResponse,
                SiteApiFeatureCollection.class);

        assertEquals("Horsham",response.getFeature()[0].getProperities().getLocation().getName());
        assertEquals(0.1508,response.getFeature()[0].getProperities().getRequestPointDistance());
        assertEquals("2022-09-19T21:00Z",response.getFeature()[0].getProperities().getModelRunDate());
    }

    @Test
    public void testSiteApiFeatureProperitiesTimeSeries0Hourly() {
        SiteApiFeatureCollection response = MetOfficeDataHubBindingConstants.GSON.fromJson(siteApiHourlyResponse,
                SiteApiFeatureCollection.class);

        assertEquals("2022-09-17T20:00Z",response.getFeature()[0].getProperities().getTimeSeries()[0].getTime());
        assertEquals( 8.55,response.getFeature()[0].getProperities().getTimeSeries()[0].getScreenTemperature());
        assertEquals( 10.36,response.getFeature()[0].getProperities().getTimeSeries()[0].getMaxScreenTemperature());
        assertEquals( 8.54,response.getFeature()[0].getProperities().getTimeSeries()[0].getMinScreenTemperature());
        assertEquals( 4.67,response.getFeature()[0].getProperities().getTimeSeries()[0].getScreenDewPointTemperature());
        assertEquals( 8.18,response.getFeature()[0].getProperities().getTimeSeries()[0].getFeelsLikeTemperature());
        assertEquals( 0.46,response.getFeature()[0].getProperities().getTimeSeries()[0].getWindSpeed10m());
        assertEquals( 297,response.getFeature()[0].getProperities().getTimeSeries()[0].getWindDirectionFrom10m());
        assertEquals( 4.63,response.getFeature()[0].getProperities().getTimeSeries()[0].getWindGustSpeed10m());
        assertEquals( 6.43,response.getFeature()[0].getProperities().getTimeSeries()[0].getMax10mWindGust());
        assertEquals( 19040,response.getFeature()[0].getProperities().getTimeSeries()[0].getVisibility());
        assertEquals( 76.51,response.getFeature()[0].getProperities().getTimeSeries()[0].getScreenRelativeHumidity());
        assertEquals( 102230,response.getFeature()[0].getProperities().getTimeSeries()[0].getMslp());
        assertEquals( 1,response.getFeature()[0].getProperities().getTimeSeries()[0].getUvIndex());
        assertEquals( 2,response.getFeature()[0].getProperities().getTimeSeries()[0].getSignificantWeatherCode());
        assertEquals( 3,response.getFeature()[0].getProperities().getTimeSeries()[0].getPrecipitationRate());
        assertEquals( 4,response.getFeature()[0].getProperities().getTimeSeries()[0].getTotalPrecipAmount());
        assertEquals( 5,response.getFeature()[0].getProperities().getTimeSeries()[0].getTotalSnowAmount());
        assertEquals( 60,response.getFeature()[0].getProperities().getTimeSeries()[0].getProbOfPrecipitation());
    }

    @Test
    public void testSiteApiFeatureProperitiesTimeSeries0Daily() {
        SiteApiFeatureCollection response = MetOfficeDataHubBindingConstants.GSON.fromJson(siteApiDailyResponse,
                SiteApiFeatureCollection.class);

        assertEquals("2022-09-18T00:00Z",response.getFeature()[0].getProperities().getTimeSeries()[0].getTime());
        assertEquals( 1.39,response.getFeature()[0].getProperities().getTimeSeries()[0].getMidnight10MWindSpeed());
        assertEquals( 31,response.getFeature()[0].getProperities().getTimeSeries()[0].getMidnight10MWindDirection());
        assertEquals( 5.66,response.getFeature()[0].getProperities().getTimeSeries()[0].getMidnight10MWindGust());
        assertEquals( 8776,response.getFeature()[0].getProperities().getTimeSeries()[0].getMidnightVisibility());
        assertEquals( 91.42,response.getFeature()[0].getProperities().getTimeSeries()[0].getMidnightRelativeHumidity());
        assertEquals( 102310,response.getFeature()[0].getProperities().getTimeSeries()[0].getMidnightMslp());
        assertEquals( 2,response.getFeature()[0].getProperities().getTimeSeries()[0].getNightSignificantWeatherCode());
        assertEquals( 8.05,response.getFeature()[0].getProperities().getTimeSeries()[0].getNightMinScreenTemperature());
        assertEquals( 12.79,response.getFeature()[0].getProperities().getTimeSeries()[0].getNightUpperBoundMinTemp());
        assertEquals( 5.51,response.getFeature()[0].getProperities().getTimeSeries()[0].getNightLowerBoundMinTemp());
        assertEquals( 7.35,response.getFeature()[0].getProperities().getTimeSeries()[0].getNightMinFeelsLikeTemp());
        assertEquals( 12.83,response.getFeature()[0].getProperities().getTimeSeries()[0].getNightUpperBoundMinFeelsLikeTemp());
        assertEquals( 7.33,response.getFeature()[0].getProperities().getTimeSeries()[0].getNightLowerBoundMinFeelsLikeTemp());
        assertEquals( 5,response.getFeature()[0].getProperities().getTimeSeries()[0].getNightProbabilityOfPrecipitation());
        assertEquals( 0,response.getFeature()[0].getProperities().getTimeSeries()[0].getNightProbabilityOfSnow());
        assertEquals( 1,response.getFeature()[0].getProperities().getTimeSeries()[0].getNightProbabilityOfHeavySnow());
        assertEquals( 5,response.getFeature()[0].getProperities().getTimeSeries()[0].getNightProbabilityOfRain());
        assertEquals( 3,response.getFeature()[0].getProperities().getTimeSeries()[0].getNightProbabilityOfHeavyRain());
        assertEquals( 4,response.getFeature()[0].getProperities().getTimeSeries()[0].getNightProbabilityOfHail());
        assertEquals( 6,response.getFeature()[0].getProperities().getTimeSeries()[0].getNightProbabilityOfSferics());
    }

    @Test
    public void testGetTimeSeriesForCurrentHour() {
        SiteApiFeatureCollection response = MetOfficeDataHubBindingConstants.GSON.fromJson(siteApiHourlyResponse,
                SiteApiFeatureCollection.class);

        assertEquals( 0,response.getFeature()[0].getProperities().getHourlyTimeSeriesPositionForCurrentHour("2022-09-17T20:00Z"));
        assertEquals( 1,response.getFeature()[0].getProperities().getHourlyTimeSeriesPositionForCurrentHour("2022-09-17T21:00Z"));
        assertEquals( 2,response.getFeature()[0].getProperities().getHourlyTimeSeriesPositionForCurrentHour("2022-09-17T22:00Z"));
        assertEquals( 3,response.getFeature()[0].getProperities().getHourlyTimeSeriesPositionForCurrentHour("2022-09-17T23:00Z"));
        assertEquals( 24,response.getFeature()[0].getProperities().getHourlyTimeSeriesPositionForCurrentHour("2022-09-18T20:00Z"));
        assertEquals( 48,response.getFeature()[0].getProperities().getHourlyTimeSeriesPositionForCurrentHour("2022-09-19T20:00Z"));
    }

}

/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.ems.internal.handler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.ems.dto.DailyForecast;
import org.openhab.binding.ems.dto.HourForecast;
import org.openhab.binding.ems.dto.OnecallWeather;
import org.openhab.binding.ems.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link WeatherForecast} handles forecast data
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class WeatherForecast {

    private final Logger logger = LoggerFactory.getLogger(WeatherForecast.class);
    private Optional<OnecallWeather> ocw = Optional.empty();

    public WeatherForecast(double lat, double lon, String apiKey) {
        try {
            URL weatherApi = new URL(
                    "https://api.openweathermap.org/data/2.5/onecall?lat=" + lat + "&lon=" + lon + "&appid=" + apiKey);
            HttpURLConnection con = (HttpURLConnection) weatherApi.openConnection();
            con.setRequestMethod("GET");
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer content = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();
            System.out.println(content.toString());
            OnecallWeather check = Constants.GSON.fromJson(content.toString(), OnecallWeather.class);
            if (check != null) {
                ocw = Optional.of(check);
            }
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public int getCloudiness(int day, int hour) {
        if (ocw.isPresent()) {
            // check if hourly forecast is present
            for (Iterator iterator = ocw.get().hourly.iterator(); iterator.hasNext();) {
                HourForecast hf = (HourForecast) iterator.next();
                Calendar c = Calendar.getInstance();
                c.setTimeInMillis((long) hf.dt * 1000);
                if (c.get(Calendar.DAY_OF_MONTH) == day && c.get(Calendar.HOUR_OF_DAY) == hour) {
                    return hf.clouds;
                }
            }
            // else return daily
            for (Iterator iterator = ocw.get().daily.iterator(); iterator.hasNext();) {
                DailyForecast df = (DailyForecast) iterator.next();
                Calendar c = Calendar.getInstance();
                c.setTimeInMillis((long) df.dt * 1000);
                if (c.get(Calendar.DAY_OF_MONTH) == day) {
                    return df.clouds;
                }
            }
        }
        // logger.info("No Cloudiness found for day {} hour {}", day, hour);
        return -1;
    }
}

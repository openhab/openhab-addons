/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.icloud.internal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import org.eclipse.smarthome.core.library.types.PointType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Lookup addresses from given coordinates via Google API.
 *
 * @author Patrik Gfeller - Initial Contribution
 *
 */
public class AddressLookup {
    final String baseURL = "https://maps.googleapis.com/maps/api/geocode/json";
    private final Logger logger = LoggerFactory.getLogger(AddressLookup.class);

    private String key;

    public AddressLookup(String googleAPIKey) {
        key = googleAPIKey;
    }

    public String getAddressJSON(PointType location) {
        String json = null;
        String url = baseURL + "?latlng=" + location.toString() + "&key=" + key;
        ;

        URL requestURL;
        try {
            requestURL = new URL(url);
            HttpsURLConnection connection;
            connection = (HttpsURLConnection) requestURL.openConnection();
            connection.setRequestMethod("GET");
            setRequestProperties(connection);

            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = bufferedReader.readLine()) != null) {
                    response.append(inputLine);
                }
                bufferedReader.close();
                json = response.toString();
            }
        } catch (MalformedURLException e) {
            logger.warn("Invalid Google API request URL: [{}]", url, e);
        } catch (IOException e) {
            logger.warn("Unable to communicate with Google to get human readable address.", e);
        }

        return json;
    }

    private void setRequestProperties(HttpsURLConnection connection) {
        connection.setRequestProperty("User-Agent", "Mozilla/5.0");
    }
}

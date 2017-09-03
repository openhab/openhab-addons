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
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import org.eclipse.smarthome.core.library.types.PointType;

/**
 * Lookup addresses from given coordinates.
 *
 * @author Patrik Gfeller
 *
 */
public class AddressLookup {
    final String baseURL = "https://maps.googleapis.com/maps/api/geocode/json";

    public String getAddressJSON(PointType location) throws Exception {
        String json = null;
        URL requestURL = new URL(baseURL + "?latlng=" + location.toString());

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

        return json;
    }

    private void setRequestProperties(HttpsURLConnection connection) {
        connection.setRequestProperty("User-Agent", "Mozilla/5.0");
    }
}

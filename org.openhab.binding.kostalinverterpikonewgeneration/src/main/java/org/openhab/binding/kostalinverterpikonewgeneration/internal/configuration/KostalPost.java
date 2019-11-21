/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

package org.openhab.binding.kostalinverterpikonewgeneration.internal.configuration;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 *
 * @author Örjan Backsell - Initial contribution
 */

public class KostalPost {
    static final String USER_AGENT = "Mozilla/5.0";

    // HTTP Sending Get Login Request
    static int postValue(String url, String sessionId, String dxsId, String value) throws Exception {
        URL obj = new URL(url);

        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("User-Agent", USER_AGENT);
        con.setRequestProperty("Accept-Language", "en-US,en;q=0.9,sv;q=0.8");
        con.setRequestProperty("Content-Type", "application/json");

        String postJsonData = "";

        if (dxsId.contentEquals("16777984")) {
            // There must be an " around the value regarding inverterName
            postJsonData = "{\"dxsEntries\":[{\"dxsId\":" + dxsId + ",\"value\":\"" + value + "\"}]}";
        } else {
            // This is for all other values
            postJsonData = "{\"dxsEntries\":[{\"dxsId\":" + dxsId + ",\"value\":" + value + "}]}";
        }
        // Send post request
        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(postJsonData);
        wr.flush();
        wr.close();

        int responseCode = con.getResponseCode();

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String output;
        StringBuffer response = new StringBuffer();

        while ((output = in.readLine()) != null) {
            response.append(output);
        }
        in.close();

        return responseCode;
    }
}

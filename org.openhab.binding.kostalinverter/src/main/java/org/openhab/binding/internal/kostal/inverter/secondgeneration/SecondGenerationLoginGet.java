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

package org.openhab.binding.internal.kostal.inverter.secondgeneration;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * The {@link SecondGenerationLoginGet} is responsible for access to the inverter, regarded Get of changeable values,
 * which are used in the second generation part of the binding.
 *
 *
 * @author Örjan Backsell - Initial contribution Piko1020, Piko New Generation
 */

public class SecondGenerationLoginGet {
    static final String USER_AGENT = "Mozilla/5.0";

    // HTTP Sending Get Login Request
    public static String[] loginGet(String url, String username, String password) throws Exception {
        String code = "";
        String salt = "";
        String sessionId = "";
        String[] getResponse = new String[2];

        URL obj = new URL(url);

        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", USER_AGENT);
        con.setRequestProperty("Accept-Language", "en-US,en;q=0.9,sv;q=0.8");
        con.setRequestProperty("Content-Type", "text/plain");
        con.connect();

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String output;
        StringBuffer response = new StringBuffer();

        while ((output = in.readLine()) != null) {
            response.append(output);
        }
        in.close();

        int sessionStart = response.indexOf("session");
        response.insert(sessionStart + 9, '[');
        int sessionEnd = response.indexOf("roleId");
        response.insert(sessionEnd + 10, ']');

        int statusStart = response.indexOf("status");
        response.insert(statusStart + 8, '[');
        int statusEnd = response.indexOf("code");
        response.insert(statusEnd + 8, ']');

        JsonObject jsonObject1 = new JsonParser().parse(response.toString()).getAsJsonObject();

        JsonArray arrStatus = jsonObject1.getAsJsonArray("status");
        for (int i = 0; i < arrStatus.size(); i++) {
            code = arrStatus.get(i).getAsJsonObject().get("code").getAsString();
        }

        if (code.contentEquals("0")) {
            salt = jsonObject1.get("salt").getAsString();

            JsonArray arrSession = jsonObject1.getAsJsonArray("session");
            for (int i = 0; i < arrSession.size(); i++) {
                sessionId = arrSession.get(i).getAsJsonObject().get("sessionId").getAsString();
            }

            getResponse[0] = salt;
            getResponse[1] = sessionId;

            return getResponse;
        } else {
            getResponse[0] = code;
            return getResponse;
        }
    }
}

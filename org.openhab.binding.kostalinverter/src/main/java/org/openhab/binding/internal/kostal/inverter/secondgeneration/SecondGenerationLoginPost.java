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
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.util.Base64;

/**
 *
 * @author Örjan Backsell - Initial contribution Piko1020, Piko New Generation
 */

public class SecondGenerationLoginPost {
    static final String USER_AGENT = "Mozilla/5.0";

    // HTTP Sending Post Request
    static int loginPost(String url, String username, String password, String salt) throws Exception {
        URL objLoginPost = new URL(url);
        HttpURLConnection conLoginPost = (HttpURLConnection) objLoginPost.openConnection();
        conLoginPost.setRequestMethod("POST");
        conLoginPost.setRequestProperty("User-Agent", USER_AGENT);
        conLoginPost.setRequestProperty("Accept-Language", "en-US,en;q=0.9,sv;q=0.8");
        conLoginPost.setRequestProperty("Content-Type", "text/plain");

        String input = new StringBuffer(password).append(salt).toString();
        MessageDigest mDigest = MessageDigest.getInstance("SHA1");

        byte[] result1 = mDigest.digest(input.getBytes());
        StringBuffer sb1 = new StringBuffer();
        for (int i = 0; i < result1.length; i++) {
            sb1.append(Integer.toString((result1[i] & 0xff) + 0x100, 16).substring(1));
        }
        String pwh = Base64.getEncoder().encodeToString(mDigest.digest(input.getBytes()));

        String postJsonData = "{\"mode\":1,\"userId\":\"" + username + "\",\"pwh\":\"" + pwh + "\"}";

        // Send post request
        conLoginPost.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(conLoginPost.getOutputStream());
        wr.writeBytes(postJsonData);
        wr.flush();
        wr.close();

        int responseCode = conLoginPost.getResponseCode();

        BufferedReader in = new BufferedReader(new InputStreamReader(conLoginPost.getInputStream()));
        String output;
        StringBuffer response = new StringBuffer();

        while ((output = in.readLine()) != null) {
            response.append(output);
        }
        in.close();

        return responseCode;
    }
}

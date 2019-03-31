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
package org.openhab.binding.opensprinkler.internal.util;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * The {@link Http} class contains static methods for communicating HTTP GET
 * and HTTP POST requests.
 *
 * @author Chris Graham - Initial contribution
 */
public class Http {
    private static final String HTTP_GET = "GET";
    private static final String HTTP_POST = "POST";
    private static final int HTTP_OK_CODE = 200;
    private static final String USER_AGENT = "Mozilla/5.0";

    /**
     * Given a URL and a set parameters, send a HTTP GET request to the URL location created by the URL and parameters.
     *
     * @param url The URL to send a GET request to.
     * @param urlParameters List of parameters to use in the URL for the GET request. Null if no parameters.
     * @return String contents of the response for the GET request.
     * @throws Exception
     */
    public static String sendHttpGet(String url, String urlParameters) throws Exception {
        URL location = null;

        if (urlParameters != null) {
            location = new URL(url + "?" + urlParameters);
        } else {
            location = new URL(url);
        }

        HttpURLConnection connection = (HttpURLConnection) location.openConnection();

        connection.setRequestMethod(HTTP_GET);
        connection.setRequestProperty("User-Agent", USER_AGENT);

        int responseCode = connection.getResponseCode();

        if (responseCode != HTTP_OK_CODE) {
            throw new Exception("Error sending HTTP GET request to " + url + ". Got response code: " + responseCode);
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }

        in.close();

        return response.toString();
    }

    /**
     * Given a URL and a set parameters, send a HTTP POST request to the URL location created by the URL and parameters.
     *
     * @param url The URL to send a POST request to.
     * @param urlParameters List of parameters to use in the URL for the POST request. Null if no parameters.
     * @return String contents of the response for the POST request.
     * @throws Exception
     */
    public static String sendHttpPost(String url, String urlParameters) throws Exception {
        URL location = new URL(url);
        HttpURLConnection connection = (HttpsURLConnection) location.openConnection();

        connection.setRequestMethod(HTTP_POST);
        connection.setRequestProperty("User-Agent", USER_AGENT);

        // Send post request
        connection.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
        wr.writeBytes(urlParameters);
        wr.flush();
        wr.close();

        int responseCode = connection.getResponseCode();

        if (responseCode != HTTP_OK_CODE) {
            throw new Exception("Error sending HTTP POST request to " + url + ". Got responce code: " + responseCode);
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }

        in.close();

        return response.toString();
    }
}

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
package org.openhab.binding.infokeydinrail.internal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Themistoklis Anastasopoulos - Initial contribution
 */
public class NetClient {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public NetClient() {
    }

    public String get(String callString) {

        String output = "";

        try {

            URL url = new URL(callString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            output = org.apache.commons.io.IOUtils.toString(br);

            // logger.debug("Output from Server : {}", output);

            /*
             * while ((output = br.readLine()) != null) {
             * logger.debug("{}", output);
             * }
             */

            conn.disconnect();

        } catch (MalformedURLException e) {

            logger.debug("Ops!", e);

        } catch (IOException e) {

            logger.debug("Ops!", e);

        }

        return output;
    }

    public String post(String callString, String jsonInputString) {

        String output = "";

        try {

            URL url = new URL(callString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; utf-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode() + " message : "
                        + conn.getResponseMessage());
            }

            // BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));

            output = org.apache.commons.io.IOUtils.toString(br);

            // logger.debug("Output from Server : {}", output);

            /*
             * while ((output = br.readLine()) != null) {
             * logger.debug("{}", output);
             * }
             */

            conn.disconnect();

        } catch (MalformedURLException e) {

            logger.debug("Ops!", e);

        } catch (IOException e) {

            logger.debug("Ops!", e);

        }

        return output;
    }
}

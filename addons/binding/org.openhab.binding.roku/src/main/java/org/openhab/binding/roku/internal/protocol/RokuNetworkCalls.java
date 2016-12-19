/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.roku.internal.protocol;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link RokuBindingDiscovery} class wraps network calls to each individual roku
 * device on your network
 *
 * @author Jarod Peters - Initial contribution
 */
public class RokuNetworkCalls {

    private final Logger logger = LoggerFactory.getLogger(RokuNetworkCalls.class);
    private String baseUrl;

    public RokuNetworkCalls(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getResult(String content, String method) throws IOException {
        StringBuilder result = new StringBuilder();
        URL url = new URL("http://" + baseUrl + content);
        logger.debug("Requested URL: " + url.toString());
        HttpURLConnection conn = null;
        conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(method);
        BufferedReader rd = null;
        rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String line;
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }
        rd.close();
        logger.debug(result.toString());
        return result.toString();
    }

    public void postMethod(String content) throws IOException {
        StringBuilder result = new StringBuilder();
        URL url = new URL("http://" + baseUrl + content);
        logger.debug("Requested URL: " + url.toString());
        HttpURLConnection conn = null;
        conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        BufferedReader rd = null;
        rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String line;
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }
        rd.close();
        logger.debug(result.toString());
    }

    public Matcher processResult(String pattern, String result) {
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(result);
        return m;
    }

}

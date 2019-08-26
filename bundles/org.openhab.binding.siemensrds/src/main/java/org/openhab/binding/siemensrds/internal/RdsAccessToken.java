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
package org.openhab.binding.siemensrds.internal;

import static org.openhab.binding.siemensrds.internal.RdsBindingConstants.*;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.net.ssl.HttpsURLConnection;

import org.eclipse.jdt.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;

/**
 * Interface to the Access Token for a particular User
 * 
 * @author Andrew Fiddian-Green - Initial contribution
 * 
 */
class RdsAccessToken {

    /*
     * NOTE: requires a static logger because the class has static methods
     */
    protected static final Logger LOGGER = LoggerFactory.getLogger(RdsAccessToken.class);

    private static final Gson GSON = new Gson();

    @SerializedName("access_token")
    private String accessToken;
    @SerializedName(".expires")
    private String expires;

    private Date expDate = null;

    /*
     * private method: execute the HTTP POST on the server
     */
    private static String httpGetTokenJson(String apiKey, String user, String password)
            throws RdsCloudException, IOException {
        /*
         * NOTE: this class uses JAVAX HttpsURLConnection library instead of the
         * preferred JETTY library; the reason is that JETTY does not allow sending the
         * square brackets characters "[]" verbatim over HTTP connections
         */
        URL url = new URL(URL_TOKEN);

        HttpsURLConnection https = (HttpsURLConnection) url.openConnection();

        https.setRequestMethod(HTTP_POST);

        https.setRequestProperty(USER_AGENT, MOZILLA);
        https.setRequestProperty(ACCEPT, TEXT_PLAIN);
        https.setRequestProperty(CONTENT_TYPE, TEXT_PLAIN);
        https.setRequestProperty(SUBSCRIPTION_KEY, apiKey);

        https.setDoOutput(true);

        try (OutputStream outputStream = https.getOutputStream();
                DataOutputStream dataOutputStream = new DataOutputStream(outputStream);) {
            dataOutputStream.writeBytes(String.format(TOKEN_REQUEST, user, password));
            dataOutputStream.flush();
        }

        if (https.getResponseCode() != HttpURLConnection.HTTP_OK) {
            throw new RdsCloudException("invalid HTTP response");
        }

        try (InputStream inputStream = https.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
                BufferedReader reader = new BufferedReader(inputStreamReader);) {
            String inputString;
            StringBuffer response = new StringBuffer();
            while ((inputString = reader.readLine()) != null) {
                response.append(inputString);
            }
            return response.toString();
        }
    }

    /*
     * public method: execute a POST on the cloud server, parse the JSON, and create
     * a class that encapsulates the data
     */
    public static @Nullable RdsAccessToken create(String apiKey, String user, String password) {
        try {
            String json = httpGetTokenJson(apiKey, user, password);
            return GSON.fromJson(json, RdsAccessToken.class);
        } catch (JsonSyntaxException | RdsCloudException | IOException e) {
            LOGGER.warn("token creation error \"{}\", cause \"{}\"", e.getMessage(), e.getCause());
            return null;
        }
    }

    /*
     * public method: return the access token
     */
    public String getToken() {
        return accessToken;
    }

    /*
     * public method: check if the token has expired
     */
    public Boolean isExpired() {
        if (expDate == null) {
            try {
                expDate = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z").parse(expires);
            } catch (ParseException e) {
                LOGGER.debug("isExpired: expiry date parsing exception");

                Calendar calendar = Calendar.getInstance();
                calendar.setTime(new Date());
                calendar.add(Calendar.DAY_OF_YEAR, 1);
                expDate = calendar.getTime();
            }
        }
        return (expDate == null || expDate.before(new Date()));
    }

}

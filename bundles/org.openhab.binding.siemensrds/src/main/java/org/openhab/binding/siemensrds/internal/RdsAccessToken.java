/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
import java.util.Locale;

import javax.net.ssl.HttpsURLConnection;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

/**
 * Interface to the Access Token for a particular User
 *
 * @author Andrew Fiddian-Green - Initial contribution
 *
 */
@NonNullByDefault
public class RdsAccessToken {

    /*
     * NOTE: requires a static logger because the class has static methods
     */
    protected final Logger logger = LoggerFactory.getLogger(RdsAccessToken.class);

    private static final Gson GSON = new Gson();

    @SerializedName("access_token")
    private @Nullable String accessToken;
    @SerializedName(".expires")
    private @Nullable String expires;

    private @Nullable Date expDate = null;

    /**
     * public static method: execute the HTTP POST on the server
     */
    public static String httpGetTokenJson(String apiKey, String payload) throws RdsCloudException, IOException {
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
                DataOutputStream dataOutputStream = new DataOutputStream(outputStream)) {
            dataOutputStream.writeBytes(payload);
            dataOutputStream.flush();
        }

        if (https.getResponseCode() != HttpURLConnection.HTTP_OK) {
            throw new IOException(https.getResponseMessage());
        }

        try (InputStream inputStream = https.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
                BufferedReader reader = new BufferedReader(inputStreamReader)) {
            String inputString;
            StringBuffer response = new StringBuffer();
            while ((inputString = reader.readLine()) != null) {
                response.append(inputString);
            }
            return response.toString();
        }
    }

    /**
     * public method: parse the JSON, and create a class that encapsulates the data
     */
    public static @Nullable RdsAccessToken createFromJson(String json) {
        return GSON.fromJson(json, RdsAccessToken.class);
    }

    /**
     * public method: return the access token
     */
    public String getToken() throws RdsCloudException {
        String accessToken = this.accessToken;
        if (accessToken != null) {
            return accessToken;
        }
        throw new RdsCloudException("no access token");
    }

    /**
     * public method: check if the token has expired
     */
    public boolean isExpired() {
        Date expDate = this.expDate;
        if (expDate == null) {
            try {
                expDate = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH).parse(expires);
            } catch (ParseException e) {
                logger.debug("isExpired: expiry date parsing exception");

                Calendar calendar = Calendar.getInstance();
                calendar.setTime(new Date());
                calendar.add(Calendar.DAY_OF_YEAR, 1);
                expDate = calendar.getTime();
            }
        }
        return (expDate == null || expDate.before(new Date()));
    }
}

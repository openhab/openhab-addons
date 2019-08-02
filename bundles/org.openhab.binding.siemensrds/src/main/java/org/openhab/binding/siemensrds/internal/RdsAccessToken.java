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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.util.ssl.SslContextFactory;
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
class RdsAccessToken {

    /*
     * NOTE: requires a static logger because the class has static methods
     */
    protected static final Logger LOGGER = LoggerFactory.getLogger(RdsAccessToken.class);

    @SerializedName("access_token")
    private String accessToken;
    @SerializedName(".expires")
    private String expires;

    private Date expDate = null;

    /*
     * private method: execute the HTTP POST on the server
     */
    private static String httpGetTokenJson(String apiKey, String user, String password) {
        String result = "";

        SslContextFactory sslCtx = new SslContextFactory();
        HttpClient https = new HttpClient(sslCtx);

        try {
            https.start();
            try {
                Request req = https.newRequest(URL_TOKEN);

                req.method(HttpMethod.POST);
                req.agent(VAL_USER_AGENT);
                req.header(HttpHeader.ACCEPT, VAL_ACCEPT);
                req.header(HDR_SUB_KEY, apiKey);
                req.header(HttpHeader.CONTENT_TYPE, VAL_CONT_PLAIN);

                req.content(new StringContentProvider(String.format(TOKEN_REQ, user, password), VAL_CONT_PLAIN));

                ContentResponse resp = req.send();

                int responseCode = resp.getStatus();
                if (responseCode == HttpStatus.OK_200) {
                    result = resp.getContentAsString();
                } else {
                    LOGGER.error("httpGetPointListJson: http error={}", responseCode);
                }
            } finally {
                https.stop();
            }
        } catch (Exception e) {
            LOGGER.error("httpGetPointListJson: exception={}", e.getMessage());
        }
        return result;
    }

    /*
     * public method: execute a POST on the cloud server, parse the JSON, and create
     * a class that encapsulates the data
     */
    @Nullable
    public static RdsAccessToken create(String apiKey, String user, String password) {
        String json = httpGetTokenJson(apiKey, user, password);

        try {
            if (!json.equals("")) {
                Gson gson = new Gson();

                return gson.fromJson(json, RdsAccessToken.class);
            }
        } catch (Exception e) {
            LOGGER.debug("create: exception={}", e.getMessage());
        }
        return null;
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
                LOGGER.debug("isExpired: exception={}", e.getMessage());

                Calendar calendar = Calendar.getInstance();
                calendar.setTime(new Date());
                calendar.add(Calendar.DAY_OF_YEAR, 1);
                expDate = calendar.getTime();
            }
        }
        return (expDate == null || expDate.before(new Date()));
    }

}

/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.openhab.binding.smappee.service;

import java.io.IOException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.io.IOUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link SamppeeService} handles the connection to the Smappee API
 *
 * @author Niko Tanghe
 */

public class SmappeeService {

    private static Logger logger = LoggerFactory.getLogger(SmappeeService.class);

    private long refreshInterval;
    private int retry;

    private String client_id;
    private String client_secret;
    private String username;
    private String password;
    private String serviceLocationName;

    private String serviceLocationId;

    private boolean isInitialized;

    private final Gson gson;

    private String accessToken;
    private DateTime accessTokenValidity;
    private String refreshToken;

    public SmappeeService(String client_id, String client_secret, String username, String password,
            String serviceLocationName) {
        this.serviceLocationName = serviceLocationName;
        this.client_id = client_id;
        this.client_secret = client_secret;
        this.username = username;
        this.password = password;

        this.retry = 1;
        this.refreshInterval = 120000;

        this.gson = new Gson();

        this.isInitialized = false;
    }

    public void startAutomaticRefresh(ScheduledExecutorService scheduledExecutorService,
            final ReadingsUpdate readingsUpdate) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    SmappeeDeviceReading readings = getDeviceReadings();
                    if (readings.consumptions.length > 0) {
                        readingsUpdate.newState(readings);
                    }
                } catch (InvalidConfigurationException e) {
                    readingsUpdate.invalidConfig();
                }
            }
        };

        scheduledJob = scheduledExecutorService.scheduleAtFixedRate(runnable, 0, this.refreshInterval,
                TimeUnit.MILLISECONDS);
    }

    ScheduledFuture<?> scheduledJob;

    public void stopAutomaticRefresh() {
        scheduledJob.cancel(true);
    }

    public SmappeeDeviceReading getDeviceReadings() throws InvalidConfigurationException {
        int currentTry = 0;
        do {
            try {
                DateTime nowUtc = DateTime.now(DateTimeZone.UTC).minusMinutes(1);
                DateTime nowUtcMinus20Min = DateTime.now(DateTimeZone.UTC).minusMinutes(20);

                String nowUtcMillis = String.valueOf(nowUtc.getMillis());
                String nowUtcMinus20MinMillis = String.valueOf(nowUtcMinus20Min.getMillis());

                // https://app1pub.smappee.net/dev/v1/servicelocation/123/consumption?aggregation=1&from=1388534400000&to=1391212800000

                String responseReadings = GetData("/dev/v1/servicelocation/" + this.serviceLocationId
                        + "/consumption?aggregation=1&from=" + nowUtcMinus20MinMillis + "&to=" + nowUtcMillis);

                SmappeeDeviceReading readings = gson.fromJson(responseReadings, SmappeeDeviceReading.class);

                // Sum of 5 minutes in Wh so average power is 60/5 x

                for (SmappeeDeviceReadingConsumption consumption : readings.consumptions) {
                    consumption.consumption = consumption.consumption * 12;
                    consumption.solar = consumption.solar * 12;
                }

                logger.debug("smappee'{}' read", new Object[] { this.serviceLocationId });

                return readings;

            } catch (Exception se) {
                logger.debug("failed to read smappee '{}'", new Object[] { se.getMessage() });
            }
        } while (currentTry++ < this.retry);

        return null;

    }

    public boolean initialize() {
        // get service locations
        String response = GetData("/dev/v1/servicelocation");

        if (response.isEmpty()) {
            return false;
        }

        SmappeeServiceLocationResponse smappeeServiceLocationResponse = gson.fromJson(response,
                SmappeeServiceLocationResponse.class);

        if (smappeeServiceLocationResponse == null) {
            return false;
        }

        for (SmappeeServiceLocation smappeeServiceLocation : smappeeServiceLocationResponse.serviceLocations) {
            if (smappeeServiceLocation.name.equals(this.serviceLocationName)) {
                this.serviceLocationId = Integer.toString(smappeeServiceLocation.serviceLocationId);
                isInitialized = true;

                return true;

            }
        }
        return false;
    }

    public boolean isInitialized() {
        return isInitialized;
    }

    private String GetData(String request) {
        String url = "https://app1pub.smappee.net" + request;

        HttpClient getClient = new HttpClient();

        GetMethod getMethod = new GetMethod(url);
        getMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(3, false));
        getMethod.getParams().setParameter(HttpMethodParams.USER_AGENT,
                "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.2; .NET CLR 1.0.3705;)");
        getMethod.addRequestHeader("Accept", "application/json");

        String accessTokenToInclude = GetAccessToken();
        if (accessTokenToInclude.isEmpty()) {
            logger.error("Could not get access token");
            return "";
        }

        getMethod.addRequestHeader("Authorization", "Bearer " + accessTokenToInclude); // add the authorization header
                                                                                       // to
        // the request

        try {
            int statusCode = getClient.executeMethod(getMethod);
            if (statusCode != HttpStatus.SC_OK) {
                logger.error("Method failed: {}", getMethod.getStatusLine());
                return "";
            }

            return IOUtils.toString(getMethod.getResponseBodyAsStream());
        } catch (HttpException e) {
            logger.error("Fatal protocol violation: {}", e.toString());
            return "";
        } catch (IOException e) {
            logger.error("Fatal transport error: {}", e.toString());
            return "";
        } finally {
            getMethod.releaseConnection();
        }
    }

    private String GetAccessToken() {
        if (accessToken != null && !accessToken.isEmpty() && accessTokenValidity != null
                && accessTokenValidity.isBeforeNow()) {
            return accessToken;
        }

        HttpClient postClient = new HttpClient();

        if (accessTokenValidity != null) {
            // get new accesstoken by using the refreshToken
            PostMethod postMethod = new PostMethod("https://app1pub.smappee.net/dev/v1/oauth2/token");
            postMethod.addRequestHeader("Accept", "application/json");

            postMethod.addParameter("grant_type", "refresh_token");
            postMethod.addParameter("refresh_token", refreshToken);
            postMethod.addParameter("client_id", client_id);
            postMethod.addParameter("client_secret", client_secret);

            try {
                int statusCode = postClient.executeMethod(postMethod);
                if (statusCode != HttpStatus.SC_OK) {
                    logger.error("Method failed: {}", postMethod.getStatusLine());
                    return "";
                }

                String result = IOUtils.toString(postMethod.getResponseBodyAsStream());
                SmappeeAccessTokenResponse accessTokenResponse = gson.fromJson(result,
                        SmappeeAccessTokenResponse.class);

                accessToken = accessTokenResponse.access_token;
                refreshToken = accessTokenResponse.refresh_token;
                accessTokenValidity = DateTime.now().plusSeconds(accessTokenResponse.expires_in);

                return accessToken;

            } catch (HttpException e) {
                logger.error("GetAccessToken on credentials : Fatal protocol violation: {}", e.toString());
                return "";
            } catch (IOException e) {
                logger.error("GetAccessToken on credentials : Fatal transport error: {}", e.toString());
                return "";
            } finally {
                postMethod.releaseConnection();
            }
        }

        // get new accesstoken by using the credentials

        PostMethod postMethod = new PostMethod("https://app1pub.smappee.net/dev/v1/oauth2/token");
        postMethod.addRequestHeader("Accept", "application/json");

        postMethod.addParameter("grant_type", "password");
        postMethod.addParameter("client_id", client_id);
        postMethod.addParameter("client_secret", client_secret);
        postMethod.addParameter("username", username);
        postMethod.addParameter("password", password);

        try {
            int statusCode = postClient.executeMethod(postMethod);
            if (statusCode != HttpStatus.SC_OK) {
                logger.error("Method failed: {}", postMethod.getStatusLine());
                return "";
            }

            String result = IOUtils.toString(postMethod.getResponseBodyAsStream());
            SmappeeAccessTokenResponse accessTokenResponse = gson.fromJson(result, SmappeeAccessTokenResponse.class);

            accessToken = accessTokenResponse.access_token;
            refreshToken = accessTokenResponse.refresh_token;
            accessTokenValidity = DateTime.now().plusSeconds(accessTokenResponse.expires_in);

            return accessToken;

        } catch (HttpException e) {
            logger.error("GetAccessToken on credentials : Fatal protocol violation: {}", e.toString());
            return "";
        } catch (IOException e) {
            logger.error("GetAccessToken on credentials : Fatal transport error: {}", e.toString());
            return "";
        } finally {
            postMethod.releaseConnection();
        }
    }
}

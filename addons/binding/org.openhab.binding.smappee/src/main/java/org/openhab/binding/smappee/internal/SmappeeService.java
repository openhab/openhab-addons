/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.openhab.binding.smappee.internal;

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
 * @author Niko Tanghe - Initial contribution
 */

public class SmappeeService {

    private final Logger logger = LoggerFactory.getLogger(SmappeeService.class);

    ScheduledFuture<?> scheduledJob;

    public long pollTime;
    private int retry;

    private String clientId;
    private String clientSecret;
    private String username;
    private String password;
    private String serviceLocationName;

    private String serviceLocationId;

    private boolean initialized;

    private final Gson gson;

    private String accessToken;
    private DateTime accessTokenValidity;
    private String refreshToken;

    public SmappeeService(String clientId, String clientSecret, String username, String password,
            String serviceLocationName, long pollTime) {
        this.serviceLocationName = serviceLocationName;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.username = username;
        this.password = password;

        this.retry = 1;
        this.pollTime = pollTime;

        this.gson = new Gson();

        this.initialized = false;
    }

    public void startAutomaticRefresh(ScheduledExecutorService scheduledExecutorService,
            final ReadingsUpdate readingsUpdate) {
        Runnable runnable = () -> {
            SmappeeDeviceReading readings = getDeviceReadings();
            if (readings.consumptions.length > 0) {
                readingsUpdate.newState(readings);
            }
        };

        scheduledJob = scheduledExecutorService.scheduleAtFixedRate(runnable, 0, this.pollTime, TimeUnit.MILLISECONDS);
    }

    public void stopAutomaticRefresh() {
        scheduledJob.cancel(true);
    }

    /**
     * Gets the smappee energy consumption readings.
     *
     * @return the device readings
     */
    public SmappeeDeviceReading getDeviceReadings() {
        int currentTry = 0;
        do {
            try {
                DateTime nowUtc = DateTime.now(DateTimeZone.UTC).minusMinutes(1);
                DateTime nowUtcMinus20Min = DateTime.now(DateTimeZone.UTC).minusMinutes(20);

                String nowUtcMillis = String.valueOf(nowUtc.getMillis());
                String nowUtcMinus20MinMillis = String.valueOf(nowUtcMinus20Min.getMillis());

                // sample API method to call :
                // https://app1pub.smappee.net/dev/v1/servicelocation/123/consumption?aggregation=1&from=1388534400000&to=1391212800000

                String responseReadings = GetData("/dev/v1/servicelocation/" + this.serviceLocationId
                        + "/consumption?aggregation=1&from=" + nowUtcMinus20MinMillis + "&to=" + nowUtcMillis);

                SmappeeDeviceReading readings = gson.fromJson(responseReadings, SmappeeDeviceReading.class);

                // Sum of 5 minutes in Wh so average power is 60/5 x

                for (SmappeeDeviceReadingConsumption consumption : readings.consumptions) {
                    consumption.consumption = consumption.consumption * 12;
                    consumption.solar = consumption.solar * 12;
                }

                logger.debug("smappee'{}' read", this.serviceLocationId);

                return readings;

            } catch (Exception se) {
                logger.error("failed to read smappee '{}'", se.getMessage());
            }
        } while (currentTry++ < this.retry);

        return null;
    }

    public SmappeeServiceLocationInfo getServiceLocationInfo() {
        int currentTry = 0;
        do {
            try {
                // sample API method to call :
                // https://app1pub.smappee.net/dev/v1/servicelocation/123/info

                String responseReadings = GetData("/dev/v1/servicelocation/" + this.serviceLocationId + "/info");

                SmappeeServiceLocationInfo readings = gson.fromJson(responseReadings, SmappeeServiceLocationInfo.class);

                logger.debug("servicelocationinfo '{}' read", this.serviceLocationId);

                return readings;

            } catch (Exception se) {
                logger.error("failed to read servicelocationinfo '{}'", se.getMessage());
            }
        } while (currentTry++ < this.retry);

        return null;
    }

    /**
     * Gets the smappee energy consumption readings for a specific appliance.
     *
     * @return the device readings
     */
    public SmappeeApplianceEvent getLatestApplianceReading(String applianceId) {
        int currentTry = 0;
        do {
            try {
                DateTime nowUtc = DateTime.now(DateTimeZone.UTC).minusMinutes(1);
                DateTime nowUtcMinus1Year = DateTime.now(DateTimeZone.UTC).minusYears(1);

                String nowUtcMillis = String.valueOf(nowUtc.getMillis());
                String nowUtcMinus1YearMillis = String.valueOf(nowUtcMinus1Year.getMillis());

                // sample API method to call :
                // https://app1pub.smappee.net/dev/v1/servicelocation/123123/events?
                // applianceId=1&applianceId=2&maxNumber=100&from=1388534400000&to=1391212800000

                String responseReadings = GetData(
                        "/dev/v1/servicelocation/" + this.serviceLocationId + "/events?applianceId=" + applianceId
                                + "&maxNumber=1&from=" + nowUtcMinus1YearMillis + "&to=" + nowUtcMillis);

                SmappeeApplianceEvent[] readings = gson.fromJson(responseReadings, SmappeeApplianceEvent[].class);

                logger.debug("smappee '{}' - appliance '{}' read", this.serviceLocationId, applianceId);

                if (readings != null && readings.length == 1) {
                    return readings[0];
                }
                return null;

            } catch (Exception se) {
                logger.error("failed to read smappee '{}' - appliance '{}' : {}", this.serviceLocationId, applianceId,
                        se.getMessage());
            }
        } while (currentTry++ < this.retry);

        return null;
    }

    /**
     * Gets the smappee energy consumption readings for a specific sensor.
     *
     * @return the device readings
     */
    public SmappeeSensorConsumptionRecord getLatestSensorConsumption(String sensorId) {
        int currentTry = 0;
        do {
            try {
                DateTime nowUtc = DateTime.now(DateTimeZone.UTC).minusMinutes(1);
                DateTime nowUtcMinus1Year = DateTime.now(DateTimeZone.UTC).minusYears(1);

                String nowUtcMillis = String.valueOf(nowUtc.getMillis());
                String nowUtcMinus1YearMillis = String.valueOf(nowUtcMinus1Year.getMillis());

                // sample API method to call :
                // https://app1pub.smappee.net/dev/v1/servicelocation/1/sensor/4/consumption?
                // from=1457597400000&to=1458666049000&aggregation=1
                String responseReadings = GetData(
                        "/dev/v1/servicelocation/" + this.serviceLocationId + "/sensor" + sensorId + "/consumption?"
                                + "aggregation=1&from=" + nowUtcMinus1YearMillis + "&to=" + nowUtcMillis);

                SmappeeSensorConsumption readings = gson.fromJson(responseReadings, SmappeeSensorConsumption.class);

                logger.debug("smappee '{}' - sensor '{}' read", this.serviceLocationId, sensorId);

                if (readings != null && readings.records != null && readings.records.length > 0) {
                    return readings.records[0];
                }
                return null;

            } catch (Exception se) {
                logger.error("failed to read smappee '{}' - sensorId '{}' : {}", this.serviceLocationId, sensorId,
                        se.getMessage());
            }
        } while (currentTry++ < this.retry);

        return null;
    }

    /**
     * Put a plug at a specific location on or off.
     *
     * @param serviceLocationID the service location ID
     * @param actuatorID the actuator ID
     * @param turnOn turn on / off
     */
    public void putPlugOnOff(String actuatorID, boolean turnOn) {
        // duration is not specified, so smappee will turn on/off the plug or an
        // undetermined period of time

        // sample API method to call :
        // https://app1pub.smappee.net/dev/v1/servicelocation/[SERVICELOCATIONID]/actuator/[ACTUATORID]/on

        if (turnOn) {
            SetData("/dev/v1/servicelocation/" + this.serviceLocationId + "/actuator/" + actuatorID + "/on");
        } else {
            SetData("/dev/v1/servicelocation/" + this.serviceLocationId + "/actuator/" + actuatorID + "/off");
        }
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
                initialized = true;

                return true;

            }
        }
        return false;
    }

    public boolean isInitialized() {
        return initialized;
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
            logger.warn("Could not get access token");
            return "";
        }

        getMethod.addRequestHeader("Authorization", "Bearer " + accessTokenToInclude); // add the authorization header
                                                                                       // to the request

        try {
            int statusCode = getClient.executeMethod(getMethod);
            if (statusCode != HttpStatus.SC_OK) {
                logger.warn("Get readings method failed: {}", getMethod.getStatusLine());
                return "";
            }

            return IOUtils.toString(getMethod.getResponseBodyAsStream());
        } catch (HttpException e) {
            logger.warn("Fatal protocol violation: {}", e.toString());
            return "";
        } catch (IOException e) {
            logger.warn("Fatal transport error: {}", e.toString());
            return "";
        } finally {
            getMethod.releaseConnection();
        }
    }

    private void SetData(String request) {
        String url = "https://app1pub.smappee.net" + request;

        HttpClient getClient = new HttpClient();

        PostMethod postMethod = new PostMethod(url);
        postMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
                new DefaultHttpMethodRetryHandler(3, false));
        postMethod.getParams().setParameter(HttpMethodParams.USER_AGENT,
                "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.2; .NET CLR 1.0.3705;)");
        postMethod.addRequestHeader("Accept", "application/json");

        String accessTokenToInclude = GetAccessToken();
        if (accessTokenToInclude.isEmpty()) {
            logger.warn("Could not get access token");
            return;
        }

        postMethod.addRequestHeader("Authorization", "Bearer " + accessTokenToInclude); // add the authorization header
                                                                                        // to the request

        try {
            int statusCode = getClient.executeMethod(postMethod);
            if (statusCode != HttpStatus.SC_OK) {
                logger.warn("Post method failed: {}", postMethod.getStatusLine());
                return;
            }

            return;
        } catch (HttpException e) {
            logger.warn("Fatal protocol violation: {}", e.toString());
            return;
        } catch (IOException e) {
            logger.warn("Fatal transport error: {}", e.toString());
            return;
        } finally {
            postMethod.releaseConnection();
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
            postMethod.addParameter("client_id", clientId);
            postMethod.addParameter("client_secret", clientSecret);

            try {
                int statusCode = postClient.executeMethod(postMethod);
                if (statusCode != HttpStatus.SC_OK) {
                    logger.warn("Get Access Token failed: {}", postMethod.getStatusLine());
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
                logger.warn("GetAccessToken on credentials : Fatal protocol violation: {}", e.toString());
                return "";
            } catch (IOException e) {
                logger.warn("GetAccessToken on credentials : Fatal transport error: {}", e.toString());
                return "";
            } catch (Exception e) {
                logger.error("Failed to parse access token (from refreshtoken request) : {}", e.toString());
                return "";
            } finally {
                postMethod.releaseConnection();
            }
        }

        // get new accesstoken by using the credentials

        PostMethod postMethod = new PostMethod("https://app1pub.smappee.net/dev/v1/oauth2/token");
        postMethod.addRequestHeader("Accept", "application/json");

        postMethod.addParameter("grant_type", "password");
        postMethod.addParameter("client_id", clientId);
        postMethod.addParameter("client_secret", clientSecret);
        postMethod.addParameter("username", username);
        postMethod.addParameter("password", password);

        try {
            int statusCode = postClient.executeMethod(postMethod);
            if (statusCode != HttpStatus.SC_OK) {
                logger.warn("Get token method failed: {}", postMethod.getStatusLine());
                return "";
            }

            String result = IOUtils.toString(postMethod.getResponseBodyAsStream());
            SmappeeAccessTokenResponse accessTokenResponse = gson.fromJson(result, SmappeeAccessTokenResponse.class);

            accessToken = accessTokenResponse.access_token;
            refreshToken = accessTokenResponse.refresh_token;
            accessTokenValidity = DateTime.now().plusSeconds(accessTokenResponse.expires_in);

            return accessToken;

        } catch (HttpException e) {
            logger.warn("GetAccessToken on credentials : Fatal protocol violation: {}", e.toString());
            return "";
        } catch (IOException e) {
            logger.warn("GetAccessToken on credentials : Fatal transport error: {}", e.toString());
            return "";
        } catch (Exception e) {
            logger.error("Failed to parse access token : {}", e.toString());
            return "";
        } finally {
            postMethod.releaseConnection();
        }
    }
}

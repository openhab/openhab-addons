/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.smappee.internal;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.FormContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.util.Fields;
import org.eclipse.jetty.util.ssl.SslContextFactory;
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

    private int retry;

    public SmappeeConfigurationParameters config;

    private String serviceLocationId;

    private boolean initialized;

    private final Gson gson;

    private String accessToken;
    private DateTime accessTokenValidity;
    private String refreshToken;

    private static HttpClient httpClient = new HttpClient(new SslContextFactory());

    public SmappeeService(SmappeeConfigurationParameters config) {
        this.config = config;

        this.retry = 1;

        this.gson = new Gson();

        this.initialized = false;

        if (!httpClient.isStarted()) {
            try {
                httpClient.setFollowRedirects(false);
                httpClient.start();
            } catch (Exception e) {
                logger.warn("Cannot start HttpClient!", e);
            }
        }

    }

    public void startAutomaticRefresh(ScheduledExecutorService scheduledExecutorService,
            final ReadingsUpdate readingsUpdate) {
        Runnable runnable = () -> {
            SmappeeDeviceReading readings = getDeviceReadings();
            if (readings.consumptions.length > 0) {
                readingsUpdate.newState(readings);
            }
        };

        scheduledJob = scheduledExecutorService.scheduleWithFixedDelay(runnable, 0, config.poll_time, TimeUnit.MINUTES);
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

                String responseReadings = getData("/dev/v1/servicelocation/" + this.serviceLocationId
                        + "/consumption?aggregation=1&from=" + nowUtcMinus20MinMillis + "&to=" + nowUtcMillis);

                if (responseReadings.isEmpty()) {
                    return null;
                }

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

                String responseReadings = getData("/dev/v1/servicelocation/" + this.serviceLocationId + "/info");

                if (responseReadings.isEmpty()) {
                    return null;
                }

                SmappeeServiceLocationInfo readings = gson.fromJson(responseReadings, SmappeeServiceLocationInfo.class);

                logger.debug("servicelocationinfo '{}' read", this.serviceLocationId);

                return readings;

            } catch (Exception se) {
                logger.warn("failed to read servicelocationinfo '{}'", se.getMessage());
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

                String responseReadings = getData(
                        "/dev/v1/servicelocation/" + this.serviceLocationId + "/events?applianceId=" + applianceId
                                + "&maxNumber=1&from=" + nowUtcMinus1YearMillis + "&to=" + nowUtcMillis);

                if (responseReadings.isEmpty()) {
                    return null;
                }

                SmappeeApplianceEvent[] readings = gson.fromJson(responseReadings, SmappeeApplianceEvent[].class);

                logger.debug("smappee '{}' - appliance '{}' read", this.serviceLocationId, applianceId);

                if (readings != null && readings.length == 1) {
                    return readings[0];
                }
                return null;

            } catch (Exception se) {
                logger.warn("failed to read smappee '{}' - appliance '{}' : {}", this.serviceLocationId, applianceId,
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
                String responseReadings = getData(
                        "/dev/v1/servicelocation/" + this.serviceLocationId + "/sensor" + sensorId + "/consumption?"
                                + "aggregation=1&from=" + nowUtcMinus1YearMillis + "&to=" + nowUtcMillis);

                if (responseReadings.isEmpty()) {
                    return null;
                }

                SmappeeSensorConsumption readings = gson.fromJson(responseReadings, SmappeeSensorConsumption.class);

                logger.debug("smappee '{}' - sensor '{}' read", this.serviceLocationId, sensorId);

                if (readings != null && readings.records != null && readings.records.length > 0) {
                    return readings.records[0];
                }
                return null;

            } catch (Exception se) {
                logger.warn("failed to read smappee '{}' - sensorId '{}' : {}", this.serviceLocationId, sensorId,
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
            setData("/dev/v1/servicelocation/" + this.serviceLocationId + "/actuator/" + actuatorID + "/on");
        } else {
            setData("/dev/v1/servicelocation/" + this.serviceLocationId + "/actuator/" + actuatorID + "/off");
        }
    }

    public boolean initialize() {
        // get service locations
        String response = getData("/dev/v1/servicelocation");

        if (response.isEmpty()) {
            return false;
        }

        SmappeeServiceLocationResponse smappeeServiceLocationResponse = gson.fromJson(response,
                SmappeeServiceLocationResponse.class);

        if (smappeeServiceLocationResponse == null) {
            return false;
        }

        for (SmappeeServiceLocation smappeeServiceLocation : smappeeServiceLocationResponse.serviceLocations) {
            if (smappeeServiceLocation.name.equals(config.service_location_name)) {
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

    private String getData(String request) {
        String url = "https://app1pub.smappee.net" + request;

        Request getMethod = httpClient.newRequest(url);
        // getMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(3,
        // false));
        getMethod.agent("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.2; .NET CLR 1.0.3705;)");
        getMethod.accept("application/json");

        String accessTokenToInclude = getAccessToken();
        if (accessTokenToInclude.isEmpty()) {
            logger.warn("Could not get access token");
            return "";
        }

        getMethod.header("Authorization", "Bearer " + accessTokenToInclude);

        try {
            ContentResponse response = getMethod.send();
            int statusCode = response.getStatus();
            if (statusCode != HttpStatus.OK_200) {
                logger.warn("Get readings method failed: {}", response.getReason());
                return "";
            }

            return response.getContentAsString();
        } catch (InterruptedException e) {
            logger.warn("Request aborted", e);
            return "";
        } catch (TimeoutException e) {
            logger.warn("Timeout error", e);
            return "";
        } catch (ExecutionException e) {
            logger.warn("Communication error", e.getCause());
            return "";
        } catch (Exception e) {
            logger.warn("Error occured", e);
            return "";
        }
    }

    private void setData(String request) {
        String url = "https://app1pub.smappee.net" + request;

        Request postMethod = httpClient.newRequest(url);
        // postMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(3,
        // false));
        postMethod.method(HttpMethod.POST);
        postMethod.agent("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.2; .NET CLR 1.0.3705;)");
        postMethod.accept("application/json");

        String accessTokenToInclude = getAccessToken();
        if (accessTokenToInclude.isEmpty()) {
            logger.warn("Could not get access token");
            return;
        }

        postMethod.header("Authorization", "Bearer " + accessTokenToInclude);

        try {
            ContentResponse response = postMethod.send();
            if (response.getStatus() != HttpStatus.OK_200) {
                logger.warn("Post method failed: {}", response.getReason());
                return;
            }

            return;
        } catch (InterruptedException e) {
            logger.warn("Request aborted", e);
            return;
        } catch (TimeoutException e) {
            logger.warn("Timeout error", e);
            return;
        } catch (ExecutionException e) {
            logger.warn("Communication error", e.getCause());
            return;
        } catch (Exception e) {
            logger.warn("Error occured", e);
            return;
        }
    }

    private String getAccessToken() {
        if (accessToken != null && !accessToken.isEmpty() && accessTokenValidity != null
                && accessTokenValidity.isBeforeNow()) {
            return accessToken;
        }

        if (accessTokenValidity != null) {
            // get new accesstoken by using the refreshToken
            Request postMethod = httpClient.newRequest("https://app1pub.smappee.net/dev/v1/oauth2/token");
            postMethod.method(HttpMethod.POST);
            postMethod.accept("application/json");

            Fields params = new Fields();
            params.add("grant_type", "refresh_token");
            params.add("refresh_token", refreshToken);
            params.add("client_id", config.client_id);
            params.add("client_secret", config.client_secret);

            postMethod.content(new FormContentProvider(params));

            try {
                ContentResponse response = postMethod.send();
                if (response.getStatus() != HttpStatus.OK_200) {
                    logger.warn("Refresh Access Token failed: {}", response.getReason());
                    return "";
                }

                String result = response.getContentAsString();
                SmappeeAccessTokenResponse accessTokenResponse = gson.fromJson(result,
                        SmappeeAccessTokenResponse.class);

                accessToken = accessTokenResponse.access_token;
                refreshToken = accessTokenResponse.refresh_token;
                accessTokenValidity = DateTime.now().plusSeconds(accessTokenResponse.expires_in);

                return accessToken;

            } catch (InterruptedException e) {
                logger.warn("Request aborted", e);
                return "";
            } catch (TimeoutException e) {
                logger.warn("Timeout error", e);
                return "";
            } catch (ExecutionException e) {
                logger.warn("Communication error", e.getCause());
                return "";
            } catch (Exception e) {
                logger.warn("Error occured", e);
                return "";
            }
        }

        // get new accesstoken by using the credentials

        Request postMethod = httpClient.newRequest("https://app1pub.smappee.net/dev/v1/oauth2/token");
        postMethod.method(HttpMethod.POST);
        postMethod.accept("application/json");

        Fields params = new Fields();
        params.add("grant_type", "password");
        params.add("client_id", config.client_id);
        params.add("client_secret", config.client_secret);
        params.add("username", config.username);
        params.add("password", config.password);

        postMethod.content(new FormContentProvider(params));

        try {
            ContentResponse response = postMethod.send();
            if (response.getStatus() != HttpStatus.OK_200) {
                logger.warn("Get Access token failed: {}", response.getReason());
                return "";
            }

            String result = response.getContentAsString();
            SmappeeAccessTokenResponse accessTokenResponse = gson.fromJson(result, SmappeeAccessTokenResponse.class);

            accessToken = accessTokenResponse.access_token;
            refreshToken = accessTokenResponse.refresh_token;
            accessTokenValidity = DateTime.now().plusSeconds(accessTokenResponse.expires_in);

            return accessToken;
        } catch (InterruptedException e) {
            logger.warn("Request aborted", e);
            return "";
        } catch (TimeoutException e) {
            logger.warn("Timeout error", e);
            return "";
        } catch (ExecutionException e) {
            logger.warn("Communication error", e.getCause());
            return "";
        } catch (Exception e) {
            logger.warn("Error occured", e);
            return "";
        }
    }
}

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
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.util.Fields;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link SamppeeService} handles the connection to the Smappee API
 *
 * @author Niko Tanghe - Initial contribution
 */
public class SmappeeService {

    private final Logger logger = LoggerFactory.getLogger(SmappeeService.class);

    private ScheduledFuture<?> scheduledJob;

    private int retry;

    private SmappeeConfigurationParameters config;

    private String serviceLocationId;

    private boolean initialized;

    private final Gson gson;

    private String accessToken;
    private DateTime accessTokenValidity;
    private String refreshToken;

    private static HttpClient httpClient = new HttpClient(new SslContextFactory());

    private final String apiRoot = "https://app1pub.smappee.net";

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

    public SmappeeConfigurationParameters getConfig() {
        return config;
    }

    public void startAutomaticRefresh(ScheduledExecutorService scheduledExecutorService,
            final ReadingsUpdate readingsUpdate) {
        scheduledJob = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            SmappeeDeviceReading readings = getDeviceReadings();

            // sometimes smappee returns reading without aggregated consumptions
            if (readings != null && readings.consumptions.length > 0) {
                readingsUpdate.newState(readings);
            }
        }, 0, config.pollingInterval, TimeUnit.MINUTES);
    }

    public void stopAutomaticRefresh() {
        if (scheduledJob != null) {
            scheduledJob.cancel(true);
        }
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

            } catch (CommunicationException se) {
                logger.debug("failed to read smappee '{}'", se.getMessage());
            } catch (JsonSyntaxException pe) {
                logger.warn("failed to read response from smappee : {}", pe.getMessage());
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

            } catch (CommunicationException se) {
                logger.warn("failed to read servicelocationinfo '{}'", se.getMessage());
            } catch (JsonSyntaxException pe) {
                logger.warn("failed to read response from smappee : {}", pe.getMessage());
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

                if (readings.length == 1) {
                    return readings[0];
                }
                return null;

            } catch (CommunicationException se) {
                logger.warn("failed to read smappee '{}' - appliance '{}' : {}", this.serviceLocationId, applianceId,
                        se.getMessage());
            } catch (JsonSyntaxException pe) {
                logger.warn("failed to read response from smappee '{}' - appliance '{}' : {}", this.serviceLocationId,
                        applianceId, pe.getMessage());
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

                if (readings.records != null && readings.records.length > 0) {
                    return readings.records[0];
                }
                return null;

            } catch (CommunicationException ce) {
                logger.warn("failed to read smappee '{}' - sensorId '{}' : {}, Retry ({}/{})", this.serviceLocationId,
                        sensorId, ce.getMessage(), currentTry + 1, this.retry);
            } catch (JsonSyntaxException pe) {
                logger.warn("failed to read response from smappee '{}' - sensorId '{}' : {}", this.serviceLocationId,
                        sensorId, pe.getMessage());
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

        int currentTry = 0;
        do {
            try {
                if (turnOn) {
                    setData("/dev/v1/servicelocation/" + this.serviceLocationId + "/actuator/" + actuatorID + "/on",
                            "{}");
                } else {
                    setData("/dev/v1/servicelocation/" + this.serviceLocationId + "/actuator/" + actuatorID + "/off",
                            "{}");
                }
                return;
            } catch (CommunicationException se) {
                logger.warn("failed to set smappee plug '{}' - sensorId '{}' : {}, Retry ({}/{})",
                        this.serviceLocationId, actuatorID, se.getMessage(), currentTry + 1, this.retry);
            }
        } while (currentTry++ < this.retry);
    }

    public void initialize() throws CommunicationException, InvalidConfigurationException {
        // get service locations
        String response = getData("/dev/v1/servicelocation");

        try {
            SmappeeServiceLocationResponse smappeeServiceLocationResponse = gson.fromJson(response,
                    SmappeeServiceLocationResponse.class);

            for (SmappeeServiceLocation smappeeServiceLocation : smappeeServiceLocationResponse.serviceLocations) {
                if (smappeeServiceLocation.name.equals(config.serviceLocationName)) {
                    this.serviceLocationId = Integer.toString(smappeeServiceLocation.serviceLocationId);
                    initialized = true;
                    return;
                }
            }

            throw new InvalidConfigurationException("Could not find a valid servicelotion for "
                    + config.serviceLocationName + ", check binding configuration");
        } catch (JsonSyntaxException pe) {
            throw new CommunicationException("Failed to parse servicelocation response", pe);
        }
    }

    public boolean isInitialized() {
        return initialized;
    }

    private String getData(String request) throws CommunicationException {
        String url = apiRoot + request;

        Request getMethod = httpClient.newRequest(url);
        getMethod.agent("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.2; .NET CLR 1.0.3705;)");
        getMethod.accept("application/json");

        String accessTokenToInclude = getAccessToken();

        getMethod.header("Authorization", "Bearer " + accessTokenToInclude);

        try {
            ContentResponse response = getMethod.send();
            if (response.getStatus() != HttpStatus.OK_200) {
                throw new CommunicationException("Get data method failed : " + response.getReason());
            }

            return response.getContentAsString();
        } catch (InterruptedException e) {
            throw new CommunicationException("Request aborted", e);
        } catch (TimeoutException e) {
            throw new CommunicationException("Timeout error", e);
        } catch (ExecutionException e) {
            throw new CommunicationException("Communication error", e.getCause());
        }
    }

    private void setData(String request, String jsonContent) throws CommunicationException {
        String url = apiRoot + request;

        Request postMethod = httpClient.newRequest(url);
        postMethod.method(HttpMethod.POST);
        postMethod.agent("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.2; .NET CLR 1.0.3705;)");
        postMethod.accept("application/json");
        postMethod.content(new StringContentProvider(jsonContent));

        String accessTokenToInclude = getAccessToken();

        postMethod.header("Authorization", "Bearer " + accessTokenToInclude);

        try {
            ContentResponse response = postMethod.send();
            if (response.getStatus() != HttpStatus.OK_200) {
                throw new CommunicationException("Post data method failed : " + response.getReason());
            }
            return;
        } catch (InterruptedException e) {
            throw new CommunicationException("Request aborted", e);
        } catch (TimeoutException e) {
            throw new CommunicationException("Timeout error", e);
        } catch (ExecutionException e) {
            throw new CommunicationException("Communication error", e.getCause());
        }
    }

    private String getAccessToken() throws CommunicationException {

        // current access token is still valid ?
        if (accessToken != null && !accessToken.isEmpty() && accessTokenValidity != null
                && accessTokenValidity.isBeforeNow()) {
            return accessToken;
        }

        if (accessTokenValidity != null) {
            // get new access token by using the refreshToken
            Request postMethod = httpClient.newRequest("https://app1pub.smappee.net/dev/v1/oauth2/token");
            postMethod.method(HttpMethod.POST);
            postMethod.accept("application/json");

            Fields params = new Fields();
            params.add("grant_type", "refresh_token");
            params.add("refresh_token", refreshToken);
            params.add("client_id", config.clientId);
            params.add("client_secret", config.clientSecret);

            postMethod.content(new FormContentProvider(params));

            try {
                ContentResponse response = postMethod.send();
                if (response.getStatus() != HttpStatus.OK_200) {
                    throw new CommunicationException("Refresh Access Token failed");
                }

                String result = response.getContentAsString();
                SmappeeAccessTokenResponse accessTokenResponse = gson.fromJson(result,
                        SmappeeAccessTokenResponse.class);

                accessToken = accessTokenResponse.access_token;
                refreshToken = accessTokenResponse.refresh_token;
                accessTokenValidity = DateTime.now().plusSeconds(accessTokenResponse.expires_in);

                return accessToken;

            } catch (InterruptedException e) {
                throw new CommunicationException("Request aborted", e);
            } catch (TimeoutException e) {
                throw new CommunicationException("Timeout error", e);
            } catch (ExecutionException e) {
                throw new CommunicationException("Communication error", e.getCause());
            } catch (JsonSyntaxException e) {
                throw new CommunicationException("Response parsing error occured", e);
            }
        }

        // get new access token by using the credentials
        Request postMethod = httpClient.newRequest("https://app1pub.smappee.net/dev/v1/oauth2/token");
        postMethod.method(HttpMethod.POST);
        postMethod.accept("application/json");

        Fields params = new Fields();
        params.add("grant_type", "password");
        params.add("client_id", config.clientId);
        params.add("client_secret", config.clientSecret);
        params.add("username", config.username);
        params.add("password", config.password);

        postMethod.content(new FormContentProvider(params));

        try {
            ContentResponse response = postMethod.send();
            if (response.getStatus() != HttpStatus.OK_200) {
                throw new CommunicationException("Get Access token failed:");
            }

            String result = response.getContentAsString();
            SmappeeAccessTokenResponse accessTokenResponse = gson.fromJson(result, SmappeeAccessTokenResponse.class);

            accessToken = accessTokenResponse.access_token;
            refreshToken = accessTokenResponse.refresh_token;
            accessTokenValidity = DateTime.now().plusSeconds(accessTokenResponse.expires_in);

            return accessToken;
        } catch (InterruptedException e) {
            throw new CommunicationException("Request aborted", e);
        } catch (TimeoutException e) {
            throw new CommunicationException("Timeout error", e);
        } catch (ExecutionException e) {
            throw new CommunicationException("Communication error", e.getCause());
        } catch (JsonSyntaxException e) {
            throw new CommunicationException("Response parsing error occured", e);
        }
    }
}

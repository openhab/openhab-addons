/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.myenergi.internal;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Authentication;
import org.eclipse.jetty.client.api.AuthenticationStore;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.DigestAuthentication;
import org.eclipse.jetty.http.HttpField;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.myenergi.internal.dto.CommandStatus;
import org.openhab.binding.myenergi.internal.dto.DeviceSummary;
import org.openhab.binding.myenergi.internal.dto.DeviceSummaryList;
import org.openhab.binding.myenergi.internal.dto.EddiSummary;
import org.openhab.binding.myenergi.internal.dto.HarviSummary;
import org.openhab.binding.myenergi.internal.dto.MyenergiData;
import org.openhab.binding.myenergi.internal.dto.ZappiBoostTimeSlot;
import org.openhab.binding.myenergi.internal.dto.ZappiBoostTimes;
import org.openhab.binding.myenergi.internal.dto.ZappiHourlyHistory;
import org.openhab.binding.myenergi.internal.dto.ZappiMinuteHistory;
import org.openhab.binding.myenergi.internal.dto.ZappiSummary;
import org.openhab.binding.myenergi.internal.exception.ApiException;
import org.openhab.binding.myenergi.internal.exception.AuthenticationException;
import org.openhab.binding.myenergi.internal.exception.RecordNotFoundException;
import org.openhab.binding.myenergi.internal.util.ZappiBoostMode;
import org.openhab.binding.myenergi.internal.util.ZappiChargingMode;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonSyntaxException;

/**
 * The {@link MyenergiApiClient} is a helper class to abstract the myenergi API.
 * It handles authentication and
 * all JSON API calls. If an API call fails it automatically refreshes the
 * authentication token and retries.
 *
 * @author Rene Scherer - Initial contribution
 * @author Stephen Cook - Eddi Support
 */
@NonNullByDefault
public class MyenergiApiClient {

    private static final int SLEEP_BEFORE_REINIT_MS = 3000;

    private static final String API_USER_AGENT = "Mozilla/5.0 (Linux; Android 7.0; SM-G930F Build/NRD90M; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/64.0.3282.137 Mobile Safari/537.36";

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    private final Logger logger = LoggerFactory.getLogger(MyenergiApiClient.class);

    private MyenergiData data = new MyenergiData();

    private @Nullable HttpClientFactory httpClientFactory;
    private @Nullable HttpClient httpClient;

    // API
    private String host = "";
    private @Nullable URI baseURI;
    private String username = "";
    private String password = "";

    /**
     * Sets the httpClientFactory object to be used to get httpClients.
     *
     * @param httpClientFactory the client to be used.
     */
    public void setHttpClientFactory(@Nullable HttpClientFactory httpClientFactory) {
        this.httpClientFactory = httpClientFactory;
    }

    /**
     * Sets the credentials (username/password) to be used for API calls.
     *
     * @param hubSerialNumber the serial number of the myenergi hub
     * @param password the password for this hub in the myenergi mobile app.
     * @throws MyEnergiApiException
     */
    public void initialize(final String hubSerialNumber, final String password) throws ApiException {
        this.username = hubSerialNumber;
        this.password = password;
        HttpClientFactory factory = httpClientFactory;
        if (factory == null) {
            throw new ApiException("No HttpClientFactory provided");
        } else {
            HttpClient client = this.httpClient;
            // close down existing client
            stop();

            // create a new httpClient, so that we can add our own digest authentication
            client = factory.createHttpClient(MyenergiApiClient.class.getSimpleName());
            AuthenticationStore auth = client.getAuthenticationStore();
            auth.clearAuthentications();
            auth.clearAuthenticationResults();
            if ("".equals(host)) {
                host = new MyenergiGetHostFromDirector().getHostName(client, hubSerialNumber);
            }
            try {
                baseURI = new URI("https", host, "/", null);
                URI uri = baseURI;
                if (uri == null) {
                    throw new ApiException("No base URI could be constructed");
                }
                logger.debug("API base URI: {}", uri.toString());
                client.getAuthenticationStore().addAuthentication(
                        new DigestAuthentication(uri, Authentication.ANY_REALM, hubSerialNumber, password));
                logger.debug("Digest authentication added: {}", hubSerialNumber);
                if (!client.isStarted()) {
                    client.start();
                }
                httpClient = client;
            } catch (MalformedURLException | URISyntaxException e) {
                throw new ApiException("Invalid URL for API call", e);
            } catch (Exception e) {
                throw new ApiException("Could not start httpClient", e);
            }
        }
    }

    public void stop() {
        HttpClient client = httpClient;
        if (client != null) {
            try {
                client.stop();
            } catch (Exception e) {
                logger.debug("Existing httpClient could not be stopped", e);
            }
            httpClient = null;
        }
    }

    public MyenergiData getData() {
        return data;
    }

    public void updateTopologyCache() throws ApiException {
        data.clear();
        for (DeviceSummary summary : getDeviceSummaryList()) {
            if (summary.activeServer != null) {
                data.setActiveServer(summary.activeServer);
                data.setFirmwareVersion(summary.firmwareVersion);
                host = summary.activeServer;
            }
            data.addAllHarvis(summary.harvis);
            data.addAllZappis(summary.zappis);
            data.addAllEddis(summary.eddis);
        }
    }

    public ZappiSummary updateZappiSummary(long serialNumber) throws ApiException, RecordNotFoundException {
        String response = executeApiCall("/cgi-jstatus-Z" + serialNumber);
        try {
            DeviceSummary ds = MyenergiBindingConstants.GSON.fromJson(response, DeviceSummary.class);
            if (ds == null) {
                throw new ApiException("Unexpected JSON response: " + response);
            } else if (ds.zappis.isEmpty()) {
                throw new RecordNotFoundException("No Zappi with serial number: " + serialNumber);
            } else {
                ZappiSummary sum = ds.zappis.get(0);
                data.updateZappi(sum);
                return sum;
            }
        } catch (JsonSyntaxException e) {
            throw new ApiException("Unable to deserialize JSON response: " + response, e);
        }
    }

    public HarviSummary updateHarviSummary(long serialNumber) throws ApiException, RecordNotFoundException {
        String response = executeApiCall("/cgi-jstatus-H" + serialNumber);
        try {
            DeviceSummary ds = MyenergiBindingConstants.GSON.fromJson(response, DeviceSummary.class);
            if (ds == null) {
                throw new ApiException("Unexpected JSON response: " + response);
            } else if (ds.harvis.isEmpty()) {
                throw new RecordNotFoundException("No Harvi with serial number: " + serialNumber);
            } else {
                HarviSummary sum = ds.harvis.get(0);
                data.updateHarvi(sum);
                return sum;
            }
        } catch (JsonSyntaxException e) {
            throw new ApiException("Unable to deserialize JSON response: " + response, e);
        }
    }

    public EddiSummary updateEddiSummary(long serialNumber) throws ApiException, RecordNotFoundException {
        String response = executeApiCall("/cgi-jstatus-E" + serialNumber);
        try {
            DeviceSummary ds = MyenergiBindingConstants.GSON.fromJson(response, DeviceSummary.class);
            if (ds == null) {
                throw new ApiException("Unexpected JSON response: " + response);
            } else if (ds.eddis.isEmpty()) {
                throw new RecordNotFoundException("No Eddi with serial number: " + serialNumber);
            } else {
                EddiSummary sum = ds.eddis.get(0);
                data.updateEddi(sum);
                return sum;
            }
        } catch (JsonSyntaxException e) {
            throw new ApiException("Unable to deserialize JSON response: " + response, e);
        }
    }

    public DeviceSummaryList getDeviceSummaryList() throws ApiException {
        String response = executeApiCall("/cgi-jstatus-*");
        try {
            DeviceSummaryList summaryList = MyenergiBindingConstants.GSON.fromJson(response, DeviceSummaryList.class);
            if (summaryList != null) {
                logger.trace("getDeviceSummaryList - summaryList: {} - {}", summaryList.size(), summaryList.toString());
                return summaryList;
            } else {
                return new DeviceSummaryList();
            }
        } catch (JsonSyntaxException e) {
            throw new ApiException("Unable to deserialize JSON response: " + response, e);
        }
    }

    public CommandStatus setEddiBoost(long eddiSerialNumber, int heater, int duration) throws ApiException {
        String response = executeApiCall(
                "/cgi-eddi-boost-E" + eddiSerialNumber + "-" + "10" + "-" + heater + "-" + duration);
        try {
            CommandStatus status = MyenergiBindingConstants.GSON.fromJson(response, CommandStatus.class);
            if (status != null) {
                return status;
            } else {
                throw new ApiException("Unexpected JSON response: " + response);
            }
        } catch (JsonSyntaxException e) {
            throw new ApiException("Unable to deserialize JSON response: " + response, e);
        }
    }

    public CommandStatus cancelEddiBoost(long eddiSerialNumber, int heater) throws ApiException {
        String response = executeApiCall("/cgi-eddi-boost-E" + eddiSerialNumber + "-" + "10" + "-" + heater + "-0");
        try {
            CommandStatus status = MyenergiBindingConstants.GSON.fromJson(response, CommandStatus.class);
            if (status != null) {
                return status;
            } else {
                throw new ApiException("Unexpected JSON response: " + response);
            }
        } catch (JsonSyntaxException e) {
            throw new ApiException("Unable to deserialize JSON response: " + response, e);
        }
    }

    public CommandStatus setEddiPriority(long eddiSerialNumber, int heater) throws ApiException {
        String response = executeApiCall("/cgi-eddi-boost-E" + eddiSerialNumber + "-" + "10" + "-" + heater + "-0");
        try {
            CommandStatus status = MyenergiBindingConstants.GSON.fromJson(response, CommandStatus.class);
            if (status != null) {
                return status;
            } else {
                throw new ApiException("Unexpected JSON response: " + response);
            }
        } catch (JsonSyntaxException e) {
            throw new ApiException("Unable to deserialize JSON response: " + response, e);
        }
    }

    public CommandStatus setEddiHeaterPriority(long eddiSerialNumber, int heater) throws ApiException {
        String response = executeApiCall("/cgi-set-heater-priority-E" + eddiSerialNumber + "-" + heater);
        try {
            CommandStatus status = MyenergiBindingConstants.GSON.fromJson(response, CommandStatus.class);
            if (status != null) {
                return status;
            } else {
                throw new ApiException("Unexpected JSON response: " + response);
            }
        } catch (JsonSyntaxException e) {
            throw new ApiException("Unable to deserialize JSON response: " + response, e);
        }
    }

    public ZappiHourlyHistory getZappiHistoryByHour(long zappiSerialNumber, ZonedDateTime date) throws ApiException {
        String response = executeApiCall("/cgi-jdayhour-Z" + zappiSerialNumber + "-" + DATE_FORMATTER.format(date));
        try {
            ZappiHourlyHistory history = MyenergiBindingConstants.GSON.fromJson(response, ZappiHourlyHistory.class);
            if (history != null) {
                return history;
            } else {
                throw new ApiException("Unexpected JSON response: " + response);
            }
        } catch (JsonSyntaxException e) {
            throw new ApiException("Unable to deserialize JSON response: " + response, e);
        }
    }

    public ZappiMinuteHistory getZappiHistoryByMinute(long zappiSerialNumber, ZonedDateTime date) throws ApiException {
        String response = executeApiCall("/cgi-jday-Z" + zappiSerialNumber + "-" + DATE_FORMATTER.format(date));
        try {
            ZappiMinuteHistory history = MyenergiBindingConstants.GSON.fromJson(response, ZappiMinuteHistory.class);
            if (history != null) {
                return history;
            } else {
                throw new ApiException("Unexpected JSON response: " + response);
            }
        } catch (JsonSyntaxException e) {
            throw new ApiException("Unable to deserialize JSON response: " + response, e);
        }
    }

    public CommandStatus setZappiChargingMode(long zappiSerialNumber, ZappiChargingMode mode) throws ApiException {
        String response = executeApiCall(
                "/cgi-zappi-mode-Z" + zappiSerialNumber + "-" + mode.getIntValue() + "-0-0-0000");
        try {
            CommandStatus status = MyenergiBindingConstants.GSON.fromJson(response, CommandStatus.class);
            if (status != null) {
                return status;
            } else {
                throw new ApiException("Unexpected JSON response: " + response);
            }
        } catch (JsonSyntaxException e) {
            throw new ApiException("Unable to deserialize JSON response: " + response, e);
        }
    }

    public ZappiBoostTimes getZappiBoostTimes(long zappiSerialNumber) throws ApiException {
        String response = executeApiCall("/cgi-boost-time-Z" + zappiSerialNumber);
        try {
            ZappiBoostTimes result = MyenergiBindingConstants.GSON.fromJson(response, ZappiBoostTimes.class);
            if (result != null) {
                return result;
            } else {
                throw new ApiException("Unexpected JSON response: " + response);
            }
        } catch (JsonSyntaxException e) {
            throw new ApiException("Unable to deserialize JSON response: " + response, e);
        }
    }

    // cgi-boost-time-Z???-{slot}-{bsh}-{bdh}-{bdd}
    // Slot is one of 11,12,13,14
    // Start time is in 24 hour clock, 15 minute intervals.
    // Duration is hoursminutes and is less than 10 hours.

    public ZappiBoostTimes setZappiBoostTimes(long zappiSerialNumber, ZappiBoostTimeSlot slot) throws ApiException {
        if (slot.durationHour >= 8) {
            slot.durationHour = 8;
            slot.durationMinute = 0;
        }
        String uri = String.format("/cgi-boost-time-Z%s-%d-%02d%02d-%1d%02d-%s", zappiSerialNumber, slot.slotId,
                slot.startHour, slot.startMinute, slot.durationHour, slot.durationMinute, slot.daysOfTheWeekMap);
        String response = executeApiCall(uri);
        try {
            ZappiBoostTimes result = MyenergiBindingConstants.GSON.fromJson(response, ZappiBoostTimes.class);
            if (result != null) {
                return result;
            } else {
                throw new ApiException("Unexpected JSON response: " + response);
            }
        } catch (JsonSyntaxException e) {
            throw new ApiException("Unable to deserialize JSON response: " + response, e);
        }
    }

    public CommandStatus setZappiBoostMode(long serialNumber, ZappiBoostMode mode, int energyKiloWattHours,
            @Nullable String departureTime) throws ApiException {
        StringBuilder uriStr = new StringBuilder("/cgi-zappi-mode-Z");
        uriStr.append(serialNumber);
        uriStr.append('-');
        uriStr.append(ZappiChargingMode.BOOST.getIntValue());
        uriStr.append('-');
        uriStr.append(mode.getIntValue());
        uriStr.append('-');
        uriStr.append(energyKiloWattHours);
        if (departureTime == null) {
            uriStr.append("-0000");
        } else {
            uriStr.append('-');
            uriStr.append(departureTime);
        }
        String response = executeApiCall(uriStr.toString());
        try {
            CommandStatus status = MyenergiBindingConstants.GSON.fromJson(response, CommandStatus.class);
            if (status != null) {
                return status;
            } else {
                throw new ApiException("Unexpected JSON response: " + response);
            }
        } catch (JsonSyntaxException e) {
            throw new ApiException("Unable to deserialize JSON response: " + response, e);
        }
    }

    private String executeApiCall(String path) throws ApiException {
        String result = "";
        try {
            URI uri = this.baseURI;
            if (uri == null) {
                throw new ApiException("No base URI available for API call");
            }
            URL url = uri.resolve(path).toURL();
            logger.debug("executeApiCall - url: {}", url.toString());
            result = executeApiCallHttpClient(url);
        } catch (MalformedURLException e) {
            throw new ApiException("Invalid URL", e);
        }
        return result;
    }

    private synchronized String executeApiCallHttpClient(URL url) throws ApiException {
        HttpClient client = httpClient;
        if (client != null) {
            try {
                int lastResponseStatus = 0;
                String lastResponseReason = "";
                int outerLoop = 0;
                while (outerLoop < 3) {
                    outerLoop++;
                    try {
                        int innerLoop = 0;
                        while ((innerLoop < 2) && !client.isStopped()) {
                            innerLoop++;
                            Request request = client.newRequest(url.toString()).method(HttpMethod.GET);
                            request.header(HttpHeader.ACCEPT, "application/json, text/plain, */*");
                            request.header(HttpHeader.ACCEPT_ENCODING, "gzip, deflate");
                            request.header(HttpHeader.CONNECTION, "keep-alive");
                            request.header(HttpHeader.CONTENT_TYPE, "application/json; utf-8");
                            request.header(HttpHeader.USER_AGENT, API_USER_AGENT);

                            logger.debug("sending API request attempt# {}: {}", innerLoop, url.toString());

                            ContentResponse response = request.send();
                            lastResponseStatus = response.getStatus();
                            lastResponseReason = response.getReason();
                            logger.debug("HTTP response code: {}, reason: {}", lastResponseStatus, lastResponseReason);
                            if (logger.isTraceEnabled()) {
                                for (HttpField field : response.getHeaders()) {
                                    logger.trace("HTTP header: {}", field.toString());
                                }
                            }
                            if ((lastResponseStatus == HttpURLConnection.HTTP_OK)
                                    || (lastResponseStatus == HttpURLConnection.HTTP_CREATED)) {
                                String apiResponse = response.getContentAsString();
                                logger.debug("Api response: {}", apiResponse);
                                return apiResponse;
                            } else {
                                if (lastResponseStatus == HttpURLConnection.HTTP_UNAUTHORIZED) {
                                    throw new AuthenticationException(
                                            "Http error: " + response.getStatus() + " - " + response.getReason());
                                } else {
                                    logger.debug("Retrying Api request after code: {}, reason: {}", lastResponseStatus,
                                            lastResponseReason);
                                }
                            }
                        }
                        logger.info("Re-initializing Api connection after code: {}, reason: {}", lastResponseStatus,
                                lastResponseReason);
                    } catch (ExecutionException e) {
                        logger.info("Re-initializing Api connection after exception caught", e);
                    }
                    // reset connection and try again
                    Thread.sleep(SLEEP_BEFORE_REINIT_MS);
                    initialize(username, password);
                }
                throw new ApiException(
                        "Http error after several attemps: " + lastResponseStatus + " - " + lastResponseReason);
            } catch (InterruptedException | TimeoutException e) {
                throw new ApiException("Exception caught during API execution" + e);
            }
        } else {
            throw new ApiException("httpClient is null");
        }
    }
}

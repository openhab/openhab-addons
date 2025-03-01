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
package org.openhab.binding.senseenergy.internal.api;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAmount;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.FormContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.util.Fields;
import org.openhab.binding.senseenergy.internal.api.dto.SenseEnergyApiAuthenticate;
import org.openhab.binding.senseenergy.internal.api.dto.SenseEnergyApiDevice;
import org.openhab.binding.senseenergy.internal.api.dto.SenseEnergyApiGetTrends;
import org.openhab.binding.senseenergy.internal.api.dto.SenseEnergyApiMonitor;
import org.openhab.binding.senseenergy.internal.api.dto.SenseEnergyApiMonitorStatus;
import org.openhab.binding.senseenergy.internal.api.dto.SenseEnergyApiRefreshToken;
import org.openhab.binding.senseenergy.utils.InstantSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * {@link SenseEnergyApi} implements the api for sense energy cloud service. This is highly leveraged from the python
 * implementation here: https://github.com/scottbonline/sense
 *
 * @author Jeff James - Initial contribution
 *
 */
@NonNullByDefault
public class SenseEnergyApi {
    private final Logger logger = LoggerFactory.getLogger(SenseEnergyApi.class);

    private static final String APIURL_BASE = "https://api.sense.com/apiservice/api/v1/";
    private static final String APIURL_AUTHENTICATE = APIURL_BASE + "authenticate";
    private static final String APIURL_RENEW = APIURL_BASE + "renew";
    private static final String APIURL_MONITOR_STATUS = APIURL_BASE + "app/monitors/%s/status";
    private static final String APIURL_MONITOR_OVERVIEW = APIURL_BASE + "app/monitors/%s/overview";
    private static final String APIURL_GET_TRENDS = APIURL_BASE + "app/history/trends?monitor_id=%s&scale=%s&start=%s";
    private static final String APIURL_GET_DEVICES = APIURL_BASE + "app/monitors/%s/devices";
    private static final String APIURL_LOGOUT = APIURL_BASE + "logout";

    private static final int API_TIMEOUT = 15;

    private final Gson gson;

    private String accessToken = "";
    // by experiment, the refresh period is 24 hours for initial authentication, on refresh, there is an expire field
    private static final TemporalAmount REFRESH_TOKEN_DURATION = Duration.ofMinutes(24 * 60 * 2 / 3);
    private String refreshToken = "";
    private long userID;
    private Instant tokenExpiresAt = Instant.MIN;

    private HttpClient httpClient;

    public enum TrendScale {
        DAY,
        WEEK,
        MONTH,
        YEAR;

        public static boolean contains(String value) {
            return Arrays.stream(values()).anyMatch(t -> t.name().equals(value));
        }
    }

    public SenseEnergyApi(final HttpClient httpClient) {
        this.httpClient = httpClient;
        this.gson = new GsonBuilder().registerTypeAdapter(Instant.class, new InstantSerializer()).create();
    }

    public String getAccessToken() {
        return this.accessToken;
    }

    /*
     * authenticates with the cloud api
     *
     * @param email
     * 
     * @param password
     * 
     * @return a set of IDs for all the monitors associated with this account
     * 
     * @throws SenseEnergyApiException on authentication error
     * 
     * @throws InterruptedException
     * 
     * @throws TimeoutException
     * 
     * @throws ExecutionException
     */
    public Set<Long> initialize(String email, String password)
            throws InterruptedException, TimeoutException, ExecutionException, SenseEnergyApiException {
        Fields fields = new Fields();
        fields.put("email", email);
        fields.put("password", password);

        Request request = httpClient.newRequest(APIURL_AUTHENTICATE).method(HttpMethod.POST)
                .content(new FormContentProvider(fields));

        ContentResponse response = sendRequest(request, false);

        final SenseEnergyApiAuthenticate data = gson.fromJson(response.getContentAsString(),
                SenseEnergyApiAuthenticate.class);

        if (data == null) {
            throw new SenseEnergyApiException("@text/api.response-invalid", false);
        }

        accessToken = data.accessToken;
        refreshToken = data.refreshToken;
        userID = data.userID;
        tokenExpiresAt = Instant.now().plus(REFRESH_TOKEN_DURATION); // there is no expire field on the intial
                                                                     // authentication

        return Stream.of(data.monitors).map(m -> m.id).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    /*
     * renew authentication credentials. Timeout of credentials is ~24 hours.
     *
     * @throws InterruptedException
     * 
     * @throws TimeoutException
     * 
     * @throws ExecutionException
     * 
     * @throws SenseEnergyApiException
     */
    public void refreshToken()
            throws InterruptedException, TimeoutException, ExecutionException, SenseEnergyApiException {
        Fields fields = new Fields();
        fields.add("user_id", Long.toString(this.userID));
        fields.add("refresh_token", this.refreshToken);

        Request request = httpClient.newRequest(APIURL_RENEW).method(HttpMethod.POST)
                .content(new FormContentProvider(fields));

        ContentResponse response = sendRequest(request, false);

        final SenseEnergyApiRefreshToken data = gson.fromJson(response.getContentAsString(),
                SenseEnergyApiRefreshToken.class);

        if (data == null) {
            throw new SenseEnergyApiException("@text/api.response-invalid", false);
        }

        logger.debug("Successful refreshToken {}", data.accessToken);

        accessToken = data.accessToken;
        refreshToken = data.refreshToken;
        userID = data.userId;
        tokenExpiresAt = data.expires.minus(1, ChronoUnit.HOURS); // refresh an hour before token expires
    }

    public void logout() throws InterruptedException, TimeoutException, ExecutionException, SenseEnergyApiException {
        Request request = httpClient.newRequest(APIURL_LOGOUT).method(HttpMethod.GET);

        sendRequest(request);
    }

    /*
     * get overview of Monitor
     *
     * @param id of the monitor
     * 
     * @return dto structure containing monitor info
     *
     * @throws InterruptedException
     * 
     * @throws TimeoutException
     * 
     * @throws ExecutionException
     * 
     * @throws SenseEnergyApiException
     */
    @Nullable
    public SenseEnergyApiMonitor getMonitorOverview(long id)
            throws InterruptedException, TimeoutException, ExecutionException, SenseEnergyApiException {
        String url = String.format(APIURL_MONITOR_OVERVIEW, id);
        Request request = httpClient.newRequest(url).method(HttpMethod.GET);

        ContentResponse response = sendRequest(request);

        JsonObject jsonResponse = JsonParser.parseString(response.getContentAsString()).getAsJsonObject();

        SenseEnergyApiMonitor monitor = gson.fromJson(
                jsonResponse.getAsJsonObject("monitor_overview").getAsJsonObject("monitor"),
                SenseEnergyApiMonitor.class);

        return monitor;
    }

    /*
     * get monitor status
     *
     * @param id - id of monitor
     * 
     * @return dto structure containing monitor status
     * 
     * @throws InterruptedException
     * 
     * @throws TimeoutException
     * 
     * @throws ExecutionException
     * 
     * @throws SenseEnergyApiException
     */
    @Nullable
    public SenseEnergyApiMonitorStatus getMonitorStatus(long id)
            throws InterruptedException, TimeoutException, ExecutionException, SenseEnergyApiException {
        String url = String.format(APIURL_MONITOR_STATUS, id);
        Request request = httpClient.newRequest(url).method(HttpMethod.GET);

        ContentResponse response = sendRequest(request);

        final SenseEnergyApiMonitorStatus data = gson.fromJson(response.getContentAsString(),
                SenseEnergyApiMonitorStatus.class);

        return data;
    }

    @Nullable
    public SenseEnergyApiGetTrends getTrendData(long id, TrendScale trendScale)
            throws InterruptedException, TimeoutException, ExecutionException, SenseEnergyApiException {
        return getTrendData(id, trendScale, Instant.now());
    }

    /*
     * get trend totals over a specific period
     *
     * @param id of monitor
     * 
     * @param trendScale period of time over which to query data
     * 
     * @param datetime a datetime within the scale of which to receive data. Does not need to be the start or end .
     * 
     * @return
     * 
     * @throws InterruptedException
     * 
     * @throws TimeoutException
     * 
     * @throws ExecutionException
     * 
     * @throws SenseEnergyApiException
     */
    @Nullable
    public SenseEnergyApiGetTrends getTrendData(long id, TrendScale trendScale, Instant datetime)
            throws InterruptedException, TimeoutException, ExecutionException, SenseEnergyApiException {
        String url = String.format(APIURL_GET_TRENDS, id, trendScale.toString(), datetime.toString());
        Request request = httpClient.newRequest(url).method(HttpMethod.GET);

        ContentResponse response = sendRequest(request);

        final SenseEnergyApiGetTrends data = gson.fromJson(response.getContentAsString(),
                SenseEnergyApiGetTrends.class);

        return data;
    }

    /*
     * Helper function to map jsonElement in stream. For some reason putting the gson conversion inline with the map
     * function yielded a @Nullable mismatch when build with maven
     *
     * @param jsonElement
     * 
     * @return dto object extracted from the json
     */
    @Nullable
    public SenseEnergyApiDevice jsonToSenseEnergyDevice(JsonElement jsonElement) {
        return gson.fromJson(jsonElement, SenseEnergyApiDevice.class);
    }

    /*
     * retrieves a Map of discovered devices
     *
     * @param id of the monitor device
     * 
     * @return Map of discovered devices with the ID of the device as key and the dto object SenseEnergyApiDevice
     * 
     * @throws InterruptedException
     * 
     * @throws TimeoutException
     * 
     * @throws ExecutionException
     * 
     * @throws SenseEnergyApiException
     */
    public Map<String, SenseEnergyApiDevice> getDevices(long id)
            throws InterruptedException, TimeoutException, ExecutionException, SenseEnergyApiException {
        String url = String.format(APIURL_GET_DEVICES, id);
        Request request = httpClient.newRequest(url).method(HttpMethod.GET);

        ContentResponse response = sendRequest(request);

        JsonArray jsonDevices = JsonParser.parseString(response.getContentAsString()).getAsJsonArray();

        @SuppressWarnings("null") // prevent this warning on d.tags - [WARNING] Potential null pointer access: this
                                  // expression has
                                  // a '@Nullable' type
        // @formatter:off
        Map<String, SenseEnergyApiDevice> mapDevices = StreamSupport.stream(jsonDevices.spliterator(), false)
                .map(j -> jsonToSenseEnergyDevice(j))
                .filter(Objects::nonNull)
                .filter(d -> (d.tags == null || !d.tags.userDeleted))
                .collect(Collectors.toMap(d -> Objects.requireNonNull(d.id), d -> Objects.requireNonNull(d)));
        // @formatter:on

        return mapDevices;
    }

    private Request setHeaders(Request request) {
        request.header(HttpHeader.HOST, "api.sense.com");
        if (!accessToken.isEmpty()) {
            request.header(HttpHeader.AUTHORIZATION, "Bearer " + accessToken);
        }
        request.timeout(API_TIMEOUT, TimeUnit.SECONDS);
        return request;
    }

    public void verifyToken()
            throws InterruptedException, TimeoutException, ExecutionException, SenseEnergyApiException {
        if (tokenExpiresAt.isBefore(Instant.now())) {
            refreshToken();
        }
    }

    ContentResponse sendRequest(Request request)
            throws InterruptedException, TimeoutException, ExecutionException, SenseEnergyApiException {
        return sendRequest(request, true);
    }

    ContentResponse sendRequest(Request request, boolean verifyToken)
            throws InterruptedException, TimeoutException, ExecutionException, SenseEnergyApiException {
        if (verifyToken) {
            verifyToken();
        }

        setHeaders(request);

        logger.trace("REQUEST: {}", request.toString());
        ContentResponse response = request.send();
        logger.trace("RESPONSE: {}", response.getContentAsString());

        switch (response.getStatus()) {
            case 200:
                break;
            case 400:
                throw new SenseEnergyApiException("@text/api-bad-request [\"" + response.getReason() + "\"]", false);
            case 401:
                throw new SenseEnergyApiException(
                        "@text/api.invalid-user-credentials [\"" + response.getReason() + "\"]", true);
            case 429:
                throw new SenseEnergyApiException("@text/api.rate-limit-exceeded", false);
            default:
                throw new SenseEnergyApiException(response.getReason(), false);
        }

        return response;
    }
}

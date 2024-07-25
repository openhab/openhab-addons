/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.flume.internal.api;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.flume.internal.api.dto.FlumeApiCurrentFlowRate;
import org.openhab.binding.flume.internal.api.dto.FlumeApiDevice;
import org.openhab.binding.flume.internal.api.dto.FlumeApiGetToken;
import org.openhab.binding.flume.internal.api.dto.FlumeApiQueryBucket;
import org.openhab.binding.flume.internal.api.dto.FlumeApiQueryWaterUsage;
import org.openhab.binding.flume.internal.api.dto.FlumeApiRefreshToken;
import org.openhab.binding.flume.internal.api.dto.FlumeApiToken;
import org.openhab.binding.flume.internal.api.dto.FlumeApiTokenPayload;
import org.openhab.binding.flume.internal.api.dto.FlumeApiUsageAlert;
import org.openhab.binding.flume.utils.JsonLocalDateTimeSerializer;
import org.openhab.binding.flume.utils.JsonZonedDateTimeSerializer;
import org.openhab.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

/**
 * The {@link FlumeApi} implements the interface to the Flume cloud service (using http). The documentation for the API
 * is located here: https://flumetech.readme.io/reference
 *
 * @author Jeff James - Initial contribution
 */
@NonNullByDefault
public class FlumeApi {
    private final Logger logger = LoggerFactory.getLogger(FlumeApi.class);

    // --------------- Flume Cloud API
    public static final String APIURL_BASE = "https://api.flumewater.com/";

    public static final String APIURL_TOKEN = "oauth/token";
    public static final String APIURL_GETUSERSDEVICES = "users/%s/devices?user=%s&location=%s";
    public static final String APIURL_GETDEVICEINFO = "users/%s/devices/%s";
    public static final String APIURL_QUERYUSAGE = "users/%s/devices/%s/query";
    public static final String APIURL_FETCHUSAGEALERTS = "users/%s/usage-alerts?device_id=%s&limit=%d&sort_field=%s&sort_direction=%s";
    public static final String APIURL_FETCHNOTIFICATIONS = "users/%s/notifications?device_id=%s&limit=%d&sort_field=%s&sort_direction=%s";
    public static final String APIURL_GETCURRENTFLOWRATE = "users/%s/devices/%s/query/active";

    // Constants used in queries
    public static final String API_UNITS_IMPERIAL = "GALLONS";
    public static final String API_UNITS_METRIC = "LITERS";

    // @formatter:off
    public enum OperationType {
        SUM, AVG, MIN, MAX, CNT;

        public static boolean contains(String value) {
            return Arrays.stream(values()).anyMatch((t) -> t.name().equals(value));
        }
    }

    public enum BucketType {
        YR, MON, DAY, HR, MIN;

        public static boolean contains(String value) {
            return Arrays.stream(values()).anyMatch((t) -> t.name().equals(value));
        }
    }

    public enum SortDirectionType {
        ASC, DESC
    }
    // @formatter:on

    protected String clientId = "";
    protected String clientSecret = "";
    protected String username = "";
    protected String password = "";
    protected Gson gson;

    private String accessToken = "";
    private String refreshToken = "";
    private int userId;
    private LocalDateTime tokenExpiresAt = LocalDateTime.now();

    private HttpClient httpClient;

    public FlumeApi(HttpClient httpClient) {
        this.httpClient = httpClient;
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new JsonLocalDateTimeSerializer("yyyy-MM-dd HH:mm:ss")) // 2022-07-13
                                                                                                                  // 20:14:00
                .registerTypeAdapter(ZonedDateTime.class, new JsonZonedDateTimeSerializer()) // 2022-07-14T03:13:00.000Z
                .create();
    }

    public String getClientId() {
        return clientId;
    }

    public void initialize(String clientId, String clientSecret, String username, String password, ThingUID bridgeUID)
            throws FlumeApiException, IOException, InterruptedException, TimeoutException, ExecutionException {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.username = username;
        this.password = password;

        getToken();
    }

    private void getToken() throws FlumeApiException, IOException, InterruptedException, TimeoutException,
            ExecutionException, NullPointerException {
        FlumeApiGetToken getToken = new FlumeApiGetToken();

        getToken.clientId = clientId;
        getToken.clientSecret = clientSecret;
        getToken.username = username;
        getToken.password = password;

        String url = APIURL_BASE + APIURL_TOKEN;
        Request request = httpClient.newRequest(url).method(HttpMethod.POST)
                .header(HttpHeader.CONTENT_TYPE, "application/json")
                .content(new StringContentProvider(gson.toJson(getToken)), "application/json");

        logger.trace("POST: {}", url);
        ContentResponse response = request.send();

        if (response.getStatus() == 400) {
            throw new FlumeApiException("@text/api.invalid-user-credentials [\"" + response.getReason() + "\"]",
                    response.getStatus(), true);
        }

        JsonObject jsonResponse = JsonParser.parseString(response.getContentAsString()).getAsJsonObject();
        boolean success = jsonResponse.get("success").getAsBoolean();

        if (!success) {
            String message = jsonResponse.get("message").getAsString();
            throw new FlumeApiException("@text/api.query-fail [\"" + message + "\"]",
                    jsonResponse.get("code").getAsInt(), false);
        }

        final FlumeApiToken[] data = gson.fromJson(jsonResponse.get("data").getAsJsonArray(), FlumeApiToken[].class);

        if (data == null) {
            throw new FlumeApiException("@text/api.response-invalid", jsonResponse.get("code").getAsInt(), true);
        }

        processToken(data[0]);
    }

    private void refreshToken()
            throws IOException, InterruptedException, TimeoutException, ExecutionException, FlumeApiException {
        FlumeApiRefreshToken token = new FlumeApiRefreshToken();

        token.clientId = clientId;
        token.clientSecret = clientSecret;
        token.refeshToken = refreshToken;

        String url = APIURL_BASE + APIURL_TOKEN;
        Request request = httpClient.newRequest(url).method(HttpMethod.POST)
                .header(HttpHeader.CONTENT_TYPE, "application/json").header("Authorization", "Bearer " + accessToken)
                .content(new StringContentProvider(gson.toJson(token)), "application/json");

        logger.trace("POST: {}", url);
        ContentResponse response = request.send();
        JsonObject jsonResponse = validateResponse(response);

        final FlumeApiToken[] data = gson.fromJson(jsonResponse.get("data").getAsJsonArray(), FlumeApiToken[].class);

        if (data == null) {
            throw new FlumeApiException("@text/api.response-invalid", jsonResponse.get("code").getAsInt(), true);
        }

        processToken(data[0]);
    }

    private void processToken(FlumeApiToken token) throws FlumeApiException {
        accessToken = token.accessToken;

        // access_token contains 3 parts: header, payload, signature - decode the payload portion
        String accessTokenPayload[] = accessToken.split("\\.");
        byte decoded[] = Base64.getDecoder().decode(accessTokenPayload[1]);

        String jsonPayload = new String(decoded);

        FlumeApiTokenPayload payload = gson.fromJson(jsonPayload, FlumeApiTokenPayload.class);

        if (payload == null) {
            throw new FlumeApiException("@text/api.response-invalid", 0, true);
        }

        userId = payload.userId;

        refreshToken = token.refreshToken;
        tokenExpiresAt = LocalDateTime.now().plusSeconds(token.expiresIn);

        logger.debug("Token expires in: {}", tokenExpiresAt);
    }

    public void verifyToken()
            throws FlumeApiException, IOException, InterruptedException, TimeoutException, ExecutionException {
        // refresh token 4 hours before it expires
        if (tokenExpiresAt.isBefore(LocalDateTime.now().minusHours(4))) {
            refreshToken();
        }
    }

    public List<FlumeApiDevice> getDeviceList()
            throws FlumeApiException, IOException, InterruptedException, TimeoutException, ExecutionException {
        verifyToken();

        String url = APIURL_BASE + String.format(APIURL_GETUSERSDEVICES, this.userId, false, false);
        Request request = httpClient.newRequest(url).method(HttpMethod.GET)
                .header("Authorization", "Bearer " + accessToken).header(HttpHeader.CONTENT_TYPE, "application/json");

        logger.trace("GET: {}", url);
        ContentResponse response = request.send();
        JsonObject jsonResponse = validateResponse(response);

        final FlumeApiDevice[] listDevices = gson.fromJson(jsonResponse.get("data").getAsJsonArray(),
                FlumeApiDevice[].class);

        return Arrays.asList(listDevices);
    }

    public @Nullable FlumeApiDevice getDeviceInfo(String deviceId)
            throws FlumeApiException, IOException, InterruptedException, TimeoutException, ExecutionException {
        verifyToken();

        String url = APIURL_BASE + String.format(APIURL_GETDEVICEINFO, this.userId, deviceId);
        Request request = httpClient.newRequest(url).method(HttpMethod.GET).header("Authorization",
                "Bearer " + accessToken);

        logger.trace("GET: {}", url);
        ContentResponse response = request.send();
        JsonObject jsonResponse = validateResponse(response);

        final FlumeApiDevice[] apiDevices = gson.fromJson(jsonResponse.get("data").getAsJsonArray(),
                FlumeApiDevice[].class);

        if (apiDevices == null) {
            return null;
        }

        return apiDevices[0];
    }

    public @Nullable List<HashMap<String, List<FlumeApiQueryBucket>>> queryUsage(String deviceID,
            List<FlumeApiQueryWaterUsage> listQuery)
            throws FlumeApiException, IOException, InterruptedException, TimeoutException, ExecutionException {
        verifyToken();

        if (listQuery.isEmpty()) {
            return null;
        }

        String jsonQuery = "{\"queries\":" + gson.toJson(listQuery) + "}";

        String url = APIURL_BASE + String.format(APIURL_QUERYUSAGE, this.userId, deviceID);
        Request request = httpClient.newRequest(url).method(HttpMethod.POST)
                .header("Authorization", "Bearer " + accessToken).header(HttpHeader.CONTENT_TYPE, "application/json")
                .content(new StringContentProvider(jsonQuery), "application/json");

        logger.trace("POST: {}", url);
        logger.trace("json: {}", jsonQuery);
        ContentResponse response = request.send();
        JsonObject jsonResponse = validateResponse(response);

        final Type queryResultType = new TypeToken<List<HashMap<String, List<FlumeApiQueryBucket>>>>() {
        }.getType();

        List<HashMap<String, List<FlumeApiQueryBucket>>> listQueryResult = gson.fromJson(jsonResponse.get("data"),
                queryResultType);

        return listQueryResult;
    }

    public @Nullable Float queryUsage(String device, FlumeApiQueryWaterUsage query)
            throws FlumeApiException, IOException, InterruptedException, TimeoutException, ExecutionException {
        List<FlumeApiQueryWaterUsage> listQuery = new ArrayList<FlumeApiQueryWaterUsage>();
        List<HashMap<String, List<FlumeApiQueryBucket>>> queryData;

        listQuery.add(query);

        queryData = queryUsage(device, listQuery);
        if (queryData == null) {
            return null;
        }

        Map<String, List<FlumeApiQueryBucket>> queryBuckets = queryData.get(0);

        List<FlumeApiQueryBucket> queryBucket = queryBuckets.get(query.requestId);
        if (queryBucket == null) {
            return null;
        }

        float queryUsageResult = queryBucket.get(0).value;

        return queryUsageResult;
    }

    public @Nullable FlumeApiCurrentFlowRate getCurrentFlowRate(String deviceId)
            throws FlumeApiException, IOException, InterruptedException, TimeoutException, ExecutionException {
        verifyToken();

        String url = APIURL_BASE + String.format(APIURL_GETCURRENTFLOWRATE, this.userId, deviceId);
        Request request = httpClient.newRequest(url).method(HttpMethod.GET)
                .header("Authorization", "Bearer " + accessToken).header("accept", "application/json");

        logger.trace("GET: {}", url);
        ContentResponse response = request.send();
        JsonObject jsonResponse = validateResponse(response);

        final FlumeApiCurrentFlowRate[] currentFlowRates = gson.fromJson(jsonResponse.get("data").getAsJsonArray(),
                FlumeApiCurrentFlowRate[].class);

        if (currentFlowRates == null) {
            return null;
        }
        if (currentFlowRates.length < 1) {
            return null;
        }

        return currentFlowRates[0];
    }

    public List<FlumeApiUsageAlert> fetchUsageAlerts(String deviceId, int limit)
            throws FlumeApiException, IOException, InterruptedException, TimeoutException, ExecutionException {
        verifyToken();

        String url = APIURL_BASE
                + String.format(APIURL_FETCHUSAGEALERTS, userId, deviceId, limit, "triggered_datetime", "DESC");
        Request request = httpClient.newRequest(url).method(HttpMethod.GET)
                .header("Authorization", "Bearer " + accessToken).header("accept", "application/json");

        logger.trace("GET: {}", url);
        ContentResponse response = request.send();
        JsonObject jsonResponse = validateResponse(response);

        final FlumeApiUsageAlert[] listUsageAlerts = gson.fromJson(jsonResponse.get("data").getAsJsonArray(),
                FlumeApiUsageAlert[].class);

        return Arrays.asList(listUsageAlerts);
    }

    public List<FlumeApiUsageAlert> fetchNotificatinos(String deviceId, int limit)
            throws FlumeApiException, IOException, InterruptedException, TimeoutException, ExecutionException {
        verifyToken();

        String url = APIURL_BASE
                + String.format(APIURL_FETCHNOTIFICATIONS, userId, deviceId, limit, "triggered_datetime", "DEC");
        Request request = httpClient.newRequest(url).method(HttpMethod.GET)
                .header("Authorization", "Bearer " + accessToken).header("accept", "application/json");

        logger.trace("GET: {}", url);
        ContentResponse response = request.send();
        JsonObject jsonResponse = validateResponse(response);

        final FlumeApiUsageAlert[] listUsageAlerts = gson.fromJson(jsonResponse.get("data").getAsJsonArray(),
                FlumeApiUsageAlert[].class);

        return Arrays.asList(listUsageAlerts);
    }

    private JsonObject validateResponse(ContentResponse response) throws FlumeApiException {
        logger.trace("Response: {}", response.getContentAsString());

        if (response.getStatus() == 429) {
            throw new FlumeApiException("@text/api.rate-limit-exceeded", 429, false);
        }

        JsonObject jsonResponse = JsonParser.parseString(response.getContentAsString()).getAsJsonObject();
        boolean success = jsonResponse.get("success").getAsBoolean();

        if (!success) {
            String message = jsonResponse.get("message").getAsString();
            throw new FlumeApiException("@text/api.query-fail [\"" + message + "\"]", response.getStatus(), false);
        }

        return jsonResponse;
    }
}

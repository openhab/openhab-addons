/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.eyeonwater.internal.api;

import static org.eclipse.jetty.http.HttpHeader.*;
import static org.eclipse.jetty.http.HttpMethod.*;
import static org.eclipse.jetty.http.HttpStatus.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Service to interact with the EyeOnWater REST API.
 * Uses Jetty HttpClient to persist login sessions via managed cookies.
 *
 * @author Richard Koshak - Initial contribution
 */
@NonNullByDefault
@SuppressWarnings("null")
public class EyeOnWaterClient {

    private final Logger logger = LoggerFactory.getLogger(EyeOnWaterClient.class);

    private final String hostname;
    private final String username;
    private final String password;
    private final HttpClient httpClient;

    private boolean authenticated = false;

    public EyeOnWaterClient(String hostname, String username, String password, HttpClient httpClient) {
        this.hostname = hostname.isBlank() ? "eyeonwater.com" : hostname.trim();
        this.username = username;
        this.password = password;
        this.httpClient = httpClient;
    }

    private String buildUrl(String path) {
        return "https://" + hostname + "/" + path.replaceAll("^/+", "");
    }

    /**
     * Authenticate with the EyeOnWater service.
     */
    public synchronized void authenticate() throws IOException, InterruptedException {
        if (authenticated) {
            return;
        }

        logger.debug("Attempting to authenticate with EyeOnWater at {}", hostname);
        String loginUrl = buildUrl("account/signin");

        Map<Object, Object> formData = new HashMap<>();
        formData.put("username", username);
        formData.put("password", password);
        String formDataString = ofFormData(formData);

        Request request = httpClient.newRequest(loginUrl).method(POST).timeout(15, TimeUnit.SECONDS)
                .header(CONTENT_TYPE, "application/x-www-form-urlencoded")
                .header(USER_AGENT, "openHAB-EyeOnWater-Addon/5.3.0").content(new StringContentProvider(
                        "application/x-www-form-urlencoded", formDataString, StandardCharsets.UTF_8));

        ContentResponse response;
        try {
            response = request.send();
        } catch (ExecutionException | TimeoutException e) {
            throw new IOException("Authentication execution failed: " + e.getMessage(), e);
        }

        if (response.getStatus() != OK_200) {
            throw new IOException("Failed to authenticate: HTTP " + response.getStatus());
        }

        String body = response.getContentAsString();
        if (body.contains("account/signin")
                && (body.contains("Invalid username") || body.contains("password") && body.contains("form"))) {
            throw new IOException("Authentication rejected: Invalid username or password.");
        }

        authenticated = true;
    }

    /**
     * Sends request and automatically re-authenticates if we receive 401 Unauthorized.
     */
    private String sendRequestWithReauth(String url, HttpMethod method, @Nullable String contentType,
            @Nullable String contentPayload) throws IOException, InterruptedException {
        ContentResponse response = sendRequest(url, method, contentType, contentPayload);

        if (response.getStatus() == UNAUTHORIZED_401) {
            logger.debug("Session expired (401). Attempting automatic re-authentication...");
            synchronized (this) {
                authenticated = false;
                authenticate();
            }

            response = sendRequest(url, method, contentType, contentPayload);
        }

        if (response.getStatus() != OK_200) {
            throw new IOException("API request failed with HTTP " + response.getStatus());
        }

        return response.getContentAsString();
    }

    private ContentResponse sendRequest(String url, HttpMethod method, @Nullable String contentType,
            @Nullable String contentPayload) throws IOException, InterruptedException {
        Request request = httpClient.newRequest(url).method(method).timeout(15, TimeUnit.SECONDS).header(USER_AGENT,
                "openHAB-EyeOnWater-Addon/5.3.0");

        if (contentPayload != null) {
            String type = contentType != null ? contentType : "application/json";
            request.content(new StringContentProvider(type, contentPayload, StandardCharsets.UTF_8));
        }

        try {
            return request.send();
        } catch (ExecutionException | TimeoutException e) {
            throw new IOException("HTTP request execution failed: " + e.getMessage(), e);
        }
    }

    private static String ofFormData(Map<Object, Object> data) {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<Object, Object> entry : data.entrySet()) {
            if (builder.length() > 0) {
                builder.append("&");
            }
            builder.append(URLEncoder.encode(entry.getKey().toString(), StandardCharsets.UTF_8));
            builder.append("=");
            builder.append(URLEncoder.encode(entry.getValue().toString(), StandardCharsets.UTF_8));
        }
        return builder.toString();
    }

    /**
     * Discover all physical water meters associated with the EyeOnWater account.
     */
    public List<EyeOnWaterMeterData> discoverMeters(boolean preferNewSearch) throws IOException, InterruptedException {
        authenticate();

        if (preferNewSearch) {
            logger.debug("Discovering meters using new_search API...");
            try {
                return fetchMetersNewSearch();
            } catch (IOException e) {
                logger.debug("new_search API discovery failed, falling back to legacy dashboard scrape: {}",
                        e.getMessage(), e);
                return fetchMetersDashboardScrape();
            }
        } else {
            logger.debug("Discovering meters using legacy dashboard scrape...");
            try {
                return fetchMetersDashboardScrape();
            } catch (IOException e) {
                logger.debug("Legacy dashboard discovery failed, falling back to new_search API: {}", e.getMessage(),
                        e);
                return fetchMetersNewSearch();
            }
        }
    }

    private List<EyeOnWaterMeterData> fetchMetersNewSearch() throws IOException, InterruptedException {
        String searchUrl = buildUrl("api/2/residential/new_search");
        String jsonPayload = "{\"query\":{\"match_all\":{}}}";

        String responseBody = sendRequestWithReauth(searchUrl, POST, "application/json", jsonPayload);

        List<EyeOnWaterMeterData> meters = new ArrayList<>();
        JsonObject payload = JsonParser.parseString(responseBody).getAsJsonObject();
        JsonObject elasticResults = payload.getAsJsonObject("elastic_results");
        if (elasticResults != null) {
            JsonObject hitsWrapper = elasticResults.getAsJsonObject("hits");
            if (hitsWrapper != null) {
                JsonArray hits = hitsWrapper.getAsJsonArray("hits");
                if (hits != null) {
                    for (JsonElement hitElement : hits) {
                        JsonObject hit = hitElement.getAsJsonObject();
                        JsonObject source = hit.getAsJsonObject("_source");
                        if (source != null) {
                            JsonObject meterObj = source.getAsJsonObject("meter");
                            String meterUuid = null;
                            String meterId = null;
                            if (meterObj != null) {
                                if (meterObj.has("meter_uuid")) {
                                    meterUuid = meterObj.get("meter_uuid").getAsString();
                                }
                                if (meterObj.has("meter_id")) {
                                    meterId = meterObj.get("meter_id").getAsString();
                                }
                            }
                            if (meterUuid == null && source.has("meter_uuid")) {
                                meterUuid = source.get("meter_uuid").getAsString();
                            }
                            if (meterId == null && source.has("meter_id")) {
                                meterId = source.get("meter_id").getAsString();
                            }

                            if (meterUuid != null && meterId != null) {
                                meters.add(new EyeOnWaterMeterData(meterUuid, meterId));
                            }
                        }
                    }
                }
            }
        }
        return meters;
    }

    private List<EyeOnWaterMeterData> fetchMetersDashboardScrape() throws IOException, InterruptedException {
        String encodedUser = URLEncoder.encode(username, StandardCharsets.UTF_8);
        String dashboardUrl = buildUrl("dashboard/" + encodedUser);

        String responseBody = sendRequestWithReauth(dashboardUrl, GET, null, null);
        return parseMetersFromDashboard(responseBody);
    }

    List<EyeOnWaterMeterData> parseMetersFromDashboard(String htmlBody) {
        List<EyeOnWaterMeterData> meters = new ArrayList<>();
        Pattern pattern = Pattern.compile("AQ\\.Views\\.MeterPicker\\.meters\\s*=\\s*(\\[.*?\\])\\s*;", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(htmlBody);

        if (matcher.find()) {
            String jsonPart = matcher.group(1);
            JsonArray meterInfos = JsonParser.parseString(jsonPart).getAsJsonArray();
            for (JsonElement element : meterInfos) {
                JsonObject meterInfo = element.getAsJsonObject();
                if (meterInfo.has("meter_uuid") && !meterInfo.get("meter_uuid").isJsonNull()
                        && meterInfo.has("meter_id") && !meterInfo.get("meter_id").isJsonNull()) {
                    String uuid = meterInfo.get("meter_uuid").getAsString();
                    String id = meterInfo.get("meter_id").getAsString();
                    meters.add(new EyeOnWaterMeterData(uuid, id));
                }
            }
        }
        return meters;
    }

    /**
     * Poll the current data for a specific meter.
     */
    public EyeOnWaterMeterData pollMeter(String meterUuid, String meterId) throws IOException, InterruptedException {
        authenticate();

        String searchUrl = buildUrl("api/2/residential/new_search");
        String jsonPayload = "{\"query\":{\"terms\":{\"meter.meter_uuid\":[\"" + meterUuid + "\"]}}}";

        String responseBody = sendRequestWithReauth(searchUrl, POST, "application/json", jsonPayload);

        JsonObject payload = JsonParser.parseString(responseBody).getAsJsonObject();
        JsonObject elasticResults = payload.getAsJsonObject("elastic_results");
        if (elasticResults == null) {
            throw new IOException("Search response did not contain 'elastic_results'");
        }
        JsonObject hitsWrapper = elasticResults.getAsJsonObject("hits");
        if (hitsWrapper == null) {
            throw new IOException("Search response did not contain hits wrapper");
        }
        JsonArray hits = hitsWrapper.getAsJsonArray("hits");
        if (hits == null || hits.size() == 0) {
            throw new IOException("Meter UUID " + meterUuid + " not found on account.");
        }

        JsonObject source = hits.get(0).getAsJsonObject().getAsJsonObject("_source");
        if (source == null) {
            throw new IOException("Meter source payload was null");
        }

        JsonObject register = source.getAsJsonObject("register_0");
        if (register == null) {
            throw new IOException("Meter source is missing register_0 data");
        }

        JsonObject latestRead = register.getAsJsonObject("latest_read");
        if (latestRead == null) {
            throw new IOException("Meter register_0 is missing latest_read");
        }

        if (!latestRead.has("full_read") || latestRead.get("full_read").isJsonNull() || !latestRead.has("units")
                || latestRead.get("units").isJsonNull() || !latestRead.has("read_time")
                || latestRead.get("read_time").isJsonNull()) {
            throw new IOException("Meter register_0 latest_read has incomplete data");
        }

        double readingValue = latestRead.get("full_read").getAsDouble();
        String readingUnit = latestRead.get("units").getAsString();
        String readTimeStr = latestRead.get("read_time").getAsString();

        EyeOnWaterMeterData meterData = new EyeOnWaterMeterData(meterUuid, meterId);
        meterData.setReadingValue(readingValue);
        meterData.setReadingUnit(readingUnit);
        meterData.setReadTime(readTimeStr);

        // Fetch Alert Flags
        if (register.has("flags") && !register.get("flags").isJsonNull()) {
            JsonObject flags = register.getAsJsonObject("flags");
            if (flags.has("Leak") && !flags.get("Leak").isJsonNull()) {
                meterData.setLeakAlert(flags.get("Leak").getAsBoolean());
            }
            if (flags.has("LowBattery") && !flags.get("LowBattery").isJsonNull()) {
                meterData.setLowBatteryAlert(flags.get("LowBattery").getAsBoolean());
            }
            if (flags.has("ReverseFlow") && !flags.get("ReverseFlow").isJsonNull()) {
                meterData.setReverseFlowAlert(flags.get("ReverseFlow").getAsBoolean());
            }
        }

        // Fetch Leak Flow rate
        if (register.has("leak") && !register.get("leak").isJsonNull()) {
            JsonObject leak = register.getAsJsonObject("leak");
            if (leak.has("rate") && !leak.get("rate").isJsonNull()) {
                meterData.setLeakRate(leak.get("rate").getAsDouble());
            }
        }

        return meterData;
    }

    /**
     * DTO representing a physical meter's details and active state.
     */
    public static class EyeOnWaterMeterData {
        private final String meterUuid;
        private final String meterId;

        private double readingValue = 0.0;
        private String readingUnit = "GAL";
        private String readTime = "";
        private double leakRate = -1.0;
        private boolean leakAlert = false;
        private boolean lowBatteryAlert = false;
        private boolean reverseFlowAlert = false;

        public EyeOnWaterMeterData(String meterUuid, String meterId) {
            this.meterUuid = meterUuid;
            this.meterId = meterId;
        }

        public String getMeterUuid() {
            return meterUuid;
        }

        public String getMeterId() {
            return meterId;
        }

        public double getReadingValue() {
            return readingValue;
        }

        public void setReadingValue(double readingValue) {
            this.readingValue = readingValue;
        }

        public String getReadingUnit() {
            return readingUnit;
        }

        public void setReadingUnit(String readingUnit) {
            this.readingUnit = readingUnit;
        }

        public String getReadTime() {
            return readTime;
        }

        public void setReadTime(String readTime) {
            this.readTime = readTime;
        }

        public double getLeakRate() {
            return leakRate;
        }

        public void setLeakRate(double leakRate) {
            this.leakRate = leakRate;
        }

        public boolean isLeakAlert() {
            return leakAlert;
        }

        public void setLeakAlert(boolean leakAlert) {
            this.leakAlert = leakAlert;
        }

        public boolean isLowBatteryAlert() {
            return lowBatteryAlert;
        }

        public void setLowBatteryAlert(boolean lowBatteryAlert) {
            this.lowBatteryAlert = lowBatteryAlert;
        }

        public boolean isReverseFlowAlert() {
            return reverseFlowAlert;
        }

        public void setReverseFlowAlert(boolean reverseFlowAlert) {
            this.reverseFlowAlert = reverseFlowAlert;
        }
    }
}

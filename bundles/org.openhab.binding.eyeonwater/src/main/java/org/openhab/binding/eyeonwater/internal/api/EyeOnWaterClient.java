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

import com.google.gson.Gson;
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
public class EyeOnWaterClient {

    private static final String USER_AGENT_VALUE = "openHAB-EyeOnWater-Addon/5.3.0";

    private final Logger logger = LoggerFactory.getLogger(EyeOnWaterClient.class);

    private final Gson gson = new Gson();

    private final String hostname;
    private final String username;
    private final String password;
    private final HttpClient httpClient;

    private boolean authenticated = false;

    public EyeOnWaterClient(String hostname, String username, String password, HttpClient httpClient) {
        this.hostname = hostname;
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
                .header(CONTENT_TYPE, "application/x-www-form-urlencoded").header(USER_AGENT, USER_AGENT_VALUE)
                .followRedirects(false).content(new StringContentProvider("application/x-www-form-urlencoded",
                        formDataString, StandardCharsets.UTF_8));

        ContentResponse response;
        try {
            response = request.send();
        } catch (ExecutionException | TimeoutException e) {
            throw new IOException("Authentication execution failed: " + e.getMessage(), e);
        }

        if (response.getStatus() == FOUND_302 || response.getStatus() == SEE_OTHER_303) {
            authenticated = true;
        } else if (response.getStatus() == OK_200) {
            throw new EyeOnWaterAuthenticationException("Authentication rejected: Invalid username or password.");
        } else {
            throw new IOException("Failed to authenticate: HTTP " + response.getStatus());
        }
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
                USER_AGENT_VALUE);

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
        try {
            SearchResponseDTO response = gson.fromJson(responseBody, SearchResponseDTO.class);
            if (response == null || response.elasticResults == null) {
                throw new IOException("Missing expected 'elastic_results' element.");
            }
            ElasticResultsDTO elasticResults = response.elasticResults;
            if (elasticResults.hits == null) {
                throw new IOException("Missing expected 'hits' element.");
            }
            HitsWrapperDTO hitsWrapper = elasticResults.hits;
            if (hitsWrapper.hits == null) {
                throw new IOException("Missing expected 'hits' array.");
            }
            List<HitDTO> hits = hitsWrapper.hits;
            for (HitDTO hit : hits) {
                if (hit != null && hit.source != null) {
                    SourceDTO source = hit.source;
                    String meterUuid = null;
                    String meterId = null;
                    if (source.meter != null) {
                        meterUuid = source.meter.meterUuid;
                        meterId = source.meter.meterId;
                    }
                    if (meterUuid == null) {
                        meterUuid = source.meterUuid;
                    }
                    if (meterId == null) {
                        meterId = source.meterId;
                    }

                    if (meterUuid != null && meterId != null) {
                        meters.add(new EyeOnWaterMeterData(meterUuid, meterId));
                    }
                }
            }
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException("Failed to parse or validate new_search response: " + e.getMessage(), e);
        }
        return meters;
    }

    private List<EyeOnWaterMeterData> fetchMetersDashboardScrape() throws IOException, InterruptedException {
        String encodedUser = URLEncoder.encode(username, StandardCharsets.UTF_8);
        String dashboardUrl = buildUrl("dashboard/" + encodedUser);

        String responseBody = sendRequestWithReauth(dashboardUrl, GET, null, null);
        return parseMetersFromDashboard(responseBody);
    }

    List<EyeOnWaterMeterData> parseMetersFromDashboard(String htmlBody) throws IOException {
        List<EyeOnWaterMeterData> meters = new ArrayList<>();
        Pattern pattern = Pattern.compile("AQ\\.Views\\.MeterPicker\\.meters\\s*=\\s*(\\[.*?\\])\\s*;", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(htmlBody);

        if (!matcher.find()) {
            throw new IOException(
                    "Failed to scrape meters: 'AQ.Views.MeterPicker.meters' script assignment not found in dashboard HTML.");
        }

        try {
            String jsonPart = matcher.group(1);
            JsonElement parsedElement = JsonParser.parseString(jsonPart);
            if (parsedElement == null || !parsedElement.isJsonArray()) {
                throw new IOException("Invalid or non-array JSON for MeterPicker.meters.");
            }
            JsonArray meterInfos = parsedElement.getAsJsonArray();
            for (JsonElement element : meterInfos) {
                if (element != null && element.isJsonObject()) {
                    JsonObject meterInfo = element.getAsJsonObject();
                    if (meterInfo.has("meter_uuid") && meterInfo.has("meter_id")) {
                        JsonElement uuidEl = meterInfo.get("meter_uuid");
                        JsonElement idEl = meterInfo.get("meter_id");
                        if (uuidEl != null && !uuidEl.isJsonNull() && idEl != null && !idEl.isJsonNull()) {
                            String uuid = uuidEl.getAsString();
                            String id = idEl.getAsString();
                            meters.add(new EyeOnWaterMeterData(uuid, id));
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException("Failed to parse scraped meters JSON: " + e.getMessage(), e);
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

        SearchResponseDTO response = gson.fromJson(responseBody, SearchResponseDTO.class);
        if (response == null || response.elasticResults == null) {
            throw new IOException("Search response did not contain 'elastic_results'");
        }
        ElasticResultsDTO elasticResults = response.elasticResults;
        if (elasticResults.hits == null) {
            throw new IOException("Search response did not contain hits wrapper");
        }
        HitsWrapperDTO hitsWrapper = elasticResults.hits;
        if (hitsWrapper.hits == null) {
            throw new IOException("Search response did not contain hits array");
        }
        List<HitDTO> hits = hitsWrapper.hits;
        if (hits.isEmpty()) {
            throw new IOException("Meter UUID " + meterUuid + " not found on account.");
        }

        HitDTO firstHit = hits.get(0);
        if (firstHit == null || firstHit.source == null) {
            throw new IOException("Meter source payload was null or invalid");
        }
        SourceDTO source = firstHit.source;

        if (source.register == null) {
            throw new IOException("Meter source is missing register_0 data");
        }
        RegisterDTO register = source.register;

        if (register.latestRead == null) {
            throw new IOException("Meter register_0 is missing latest_read");
        }
        LatestReadDTO latestRead = register.latestRead;

        @Nullable
        Double fullRead = latestRead.fullRead;
        @Nullable
        String units = latestRead.units;
        @Nullable
        String readTimeStr = latestRead.readTime;

        if (fullRead == null || units == null || readTimeStr == null) {
            throw new IOException("Meter register_0 latest_read has incomplete data");
        }

        double readingValue = fullRead;
        String readingUnit = units;
        String readTime = readTimeStr;

        EyeOnWaterMeterData meterData = new EyeOnWaterMeterData(meterUuid, meterId);
        meterData.setReadingValue(readingValue);
        meterData.setReadingUnit(readingUnit);
        meterData.setReadTime(readTime);

        // Fetch Alert Flags
        if (register.flags != null) {
            FlagsDTO flags = register.flags;
            if (flags.leak != null) {
                meterData.setLeakAlert(flags.leak);
            }
            if (flags.lowBattery != null) {
                meterData.setLowBatteryAlert(flags.lowBattery);
            }
            if (flags.reverseFlow != null) {
                meterData.setReverseFlowAlert(flags.reverseFlow);
            }
        }

        // Fetch Leak Flow rate
        if (register.leak != null) {
            LeakDTO leak = register.leak;
            if (leak.rate != null) {
                meterData.setLeakRate(leak.rate);
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
        private @Nullable Boolean leakAlert;
        private @Nullable Boolean lowBatteryAlert;
        private @Nullable Boolean reverseFlowAlert;

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

        public @Nullable Boolean getLeakAlert() {
            return leakAlert;
        }

        public void setLeakAlert(boolean leakAlert) {
            this.leakAlert = leakAlert;
        }

        public @Nullable Boolean getLowBatteryAlert() {
            return lowBatteryAlert;
        }

        public void setLowBatteryAlert(boolean lowBatteryAlert) {
            this.lowBatteryAlert = lowBatteryAlert;
        }

        public @Nullable Boolean getReverseFlowAlert() {
            return reverseFlowAlert;
        }

        public void setReverseFlowAlert(boolean reverseFlowAlert) {
            this.reverseFlowAlert = reverseFlowAlert;
        }
    }

    public static class EyeOnWaterAuthenticationException extends IOException {
        private static final long serialVersionUID = 1L;

        public EyeOnWaterAuthenticationException(String message) {
            super(message);
        }

        public EyeOnWaterAuthenticationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}

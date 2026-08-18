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
package org.openhab.binding.melcloud.internal.home.api;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.melcloud.internal.exceptions.MelCloudCommException;
import org.openhab.binding.melcloud.internal.home.api.dto.MelCloudHomeAtaControlRequest;
import org.openhab.binding.melcloud.internal.home.api.dto.MelCloudHomeAtwControlRequest;
import org.openhab.binding.melcloud.internal.home.api.dto.MelCloudHomeAtwScheduleWriteRequest;
import org.openhab.binding.melcloud.internal.home.api.dto.MelCloudHomeTelemetryResponse;
import org.openhab.binding.melcloud.internal.home.api.dto.MelCloudHomeTrendSummaryReport;
import org.openhab.binding.melcloud.internal.home.api.dto.MelCloudHomeUserContext;
import org.openhab.binding.melcloud.internal.logging.SensitiveDataMasker;
import org.openhab.core.io.net.http.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

/**
 * Client for the authenticated MELCloud Home mobile BFF API: reading the full account context, and
 * reading/writing ATA/ATW unit state.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class MelCloudHomeApiClient {

    private static final String BFF_BASE_URL = "https://mobile.bff.melcloudhome.com";
    private static final String CONTEXT_URL = BFF_BASE_URL + "/context";
    private static final String ATA_CONTROL_URL_TEMPLATE = BFF_BASE_URL + "/monitor/ataunit/%s";
    private static final String ATW_CONTROL_URL_TEMPLATE = BFF_BASE_URL + "/monitor/atwunit/%s";
    // Provisional (ADR-012): path and single-endpoint-serves-create-and-update shape are not independently
    // confirmed against real ATW traffic — see docs/changes/add-melcloud-home-schedule-management/proposal.md.
    private static final String ATW_SCHEDULE_URL_TEMPLATE = BFF_BASE_URL + "/monitor/atwcloudschedule/%s";
    private static final String ATW_SCHEDULE_ENABLED_URL_TEMPLATE = BFF_BASE_URL
            + "/monitor/atwcloudschedule/%s/enabled";
    private static final String ATW_SCHEDULE_DELETE_URL_TEMPLATE = BFF_BASE_URL + "/monitor/atwcloudschedule/%s/%s";
    private static final String TELEMETRY_ENERGY_URL_TEMPLATE = BFF_BASE_URL + "/telemetry/telemetry/energy/%s";
    private static final String TRENDSUMMARY_URL = BFF_BASE_URL + "/report/v1/trendsummary";

    // The WebSocket credential ("hash") is issued by a fixed AWS Lambda Function URL, not the mobile BFF itself,
    // authenticated with the same mobile-BFF Bearer access token. Confirmed against the working reference
    // implementation (andrew-blake/melcloudhome); see ADR-007.
    private static final String WEBSOCKET_HASH_URL = "https://6x2dgdulg7omjsxalnhmo4ynba0dcgwk.lambda-url.eu-west-1.on.aws/";
    private static final String WEBSOCKET_HOST = "wss://ws.melcloudhome.com";

    private static final int TIMEOUT_MILLISECONDS = 10000;

    private static final DateTimeFormatter ENERGY_TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
            .withZone(ZoneOffset.UTC);
    // The trend summary endpoint expects a literal 7-digit fractional-second suffix; the reference implementation
    // always sends ".0000000" rather than the instant's real sub-second precision.
    private static final DateTimeFormatter TREND_TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
            .withZone(ZoneOffset.UTC);
    private static final String TREND_TIMESTAMP_SUFFIX = ".0000000";

    private static final Type TREND_SUMMARY_REPORT_LIST_TYPE = new TypeToken<List<MelCloudHomeTrendSummaryReport>>() {
    }.getType();

    private final Logger logger = LoggerFactory.getLogger(MelCloudHomeApiClient.class);
    private final Gson gson = new Gson();
    // A separate instance: control request bodies must serialize unset fields as JSON null rather than omitting
    // them (the server requires every control field to be present on every call).
    private final Gson controlGson = new GsonBuilder().serializeNulls().create();

    /**
     * Fetches every building (owned and guest) and their ATA/ATW units in one call.
     *
     * @param accessToken a Bearer access token; never logged
     * @return the parsed user context
     * @throws MelCloudCommException if the request fails or the response cannot be parsed
     */
    public MelCloudHomeUserContext fetchUserContext(String accessToken) throws MelCloudCommException {
        String body = get(CONTEXT_URL, accessToken);
        return parseJson(body, MelCloudHomeUserContext.class, "user context");
    }

    /**
     * Sends a control update for one Air-to-Air unit. Fields left {@code null} on {@code request} are serialized as
     * JSON {@code null}, per the server's full-payload contract.
     *
     * @throws MelCloudCommException if the request fails
     */
    public void controlAtaUnit(String accessToken, String unitId, MelCloudHomeAtaControlRequest request)
            throws MelCloudCommException {
        put(ATA_CONTROL_URL_TEMPLATE.formatted(unitId), accessToken, controlGson.toJson(request));
    }

    /**
     * Sends a control update for one Air-to-Water unit. Same full-payload-with-nulls contract as
     * {@link #controlAtaUnit(String, String, MelCloudHomeAtaControlRequest)}.
     *
     * @throws MelCloudCommException if the request fails
     */
    public void controlAtwUnit(String accessToken, String unitId, MelCloudHomeAtwControlRequest request)
            throws MelCloudCommException {
        put(ATW_CONTROL_URL_TEMPLATE.formatted(unitId), accessToken, controlGson.toJson(request));
    }

    /**
     * Creates a new ATW cloud schedule entry, or updates an existing one if {@code request.id} matches one already
     * on the unit — the community {@code melcloudhome} reference documents a single shared endpoint for both.
     *
     * <p>
     * <b>Provisional (ADR-012):</b> this endpoint's path, and the create-and-update-share-one-endpoint shape, are
     * not independently confirmed for ATW; see this class's {@code ATW_SCHEDULE_URL_TEMPLATE} and ADR-012.
     *
     * @throws MelCloudCommException if the request fails
     */
    public void createOrUpdateAtwSchedule(String accessToken, String unitId,
            MelCloudHomeAtwScheduleWriteRequest request) throws MelCloudCommException {
        post(ATW_SCHEDULE_URL_TEMPLATE.formatted(unitId), accessToken, controlGson.toJson(request));
    }

    /**
     * Deletes one ATW cloud schedule entry by id.
     *
     * @throws MelCloudCommException if the request fails
     */
    public void deleteAtwSchedule(String accessToken, String unitId, String scheduleId) throws MelCloudCommException {
        delete(ATW_SCHEDULE_DELETE_URL_TEMPLATE.formatted(unitId, scheduleId), accessToken);
    }

    /**
     * Enables or disables all of an ATW unit's cloud schedules at once, independent of any individual entry's own
     * state.
     *
     * @throws MelCloudCommException if the request fails
     */
    public void setAtwScheduleEnabled(String accessToken, String unitId, boolean enabled) throws MelCloudCommException {
        put(ATW_SCHEDULE_ENABLED_URL_TEMPLATE.formatted(unitId), accessToken, gson.toJson(Map.of("enabled", enabled)));
    }

    /**
     * Fetches the most recent cumulative energy value (in Wh) for a unit over the given time window.
     *
     * @param measure the BFF measure name, e.g. {@code cumulative_energy_consumed_since_last_upload} (ATA),
     *            {@code interval_energy_consumed}, or {@code interval_energy_produced} (ATW)
     * @return the latest value in Wh, if any data was available
     * @throws MelCloudCommException if the request fails or the response cannot be parsed
     */
    public Optional<Double> fetchLatestEnergyWh(String accessToken, String unitId, Instant from, Instant to,
            String measure) throws MelCloudCommException {
        String url = TELEMETRY_ENERGY_URL_TEMPLATE.formatted(unitId) + "?from=" + urlEncode(from) + "&to="
                + urlEncode(to) + "&interval=Hour&measure=" + urlEncode(measure);
        String body = get(url, accessToken);
        if (body.isBlank()) {
            // 304 Not Modified is surfaced by HttpUtil as an empty body — no new data, not an error.
            return Optional.empty();
        }
        return parseJson(body, MelCloudHomeTelemetryResponse.class, "energy telemetry response").getLatestValueWh();
    }

    /**
     * Fetches the most recent outdoor temperature datapoint for an ATA unit over the given time window. ATW units
     * report outdoor temperature directly in their {@code settings} instead — see
     * {@link org.openhab.binding.melcloud.internal.home.api.dto.MelCloudHomeAtwUnit#getOutdoorTemperature()}.
     *
     * <p>
     * Queries with {@code period=Hourly}, not {@code Daily}: {@code Daily} labels are 30-minute bucket aggregates
     * whose timestamps are not real reading times and can diverge from the actual latest reading, whereas
     * {@code Hourly} datapoints carry the unit's genuine upload timestamp. This mirrors the fix already applied to
     * the reference Home Assistant MELCloud Home integration (issues #152/#111 in {@code andrew-blake/melcloudhome});
     * see {@code MelCloudHomeTrendSummaryReport#getLatestOutdoorTemperature()} for the synthetic-datapoint filtering
     * that {@code Hourly} then requires.
     *
     * @return the latest outdoor temperature in Celsius, if any data was available
     * @throws MelCloudCommException if the request fails or the response cannot be parsed
     */
    public Optional<Double> fetchLatestOutdoorTemperature(String accessToken, String unitId, Instant from, Instant to)
            throws MelCloudCommException {
        // "to" is truncated to a whole minute so its query-echo datapoint lands on an exact-second boundary and is
        // therefore recognized as synthetic by the genuine-reading filter (see getLatestOutdoorTemperature()).
        Instant queryTo = to.truncatedTo(ChronoUnit.MINUTES);
        String url = TRENDSUMMARY_URL + "?unitId=" + urlEncode(unitId) + "&period=Hourly&from=" + urlEncodeTrend(from)
                + "&to=" + urlEncodeTrend(queryTo);
        String body = get(url, accessToken);
        if (body.isBlank()) {
            return Optional.empty();
        }
        List<MelCloudHomeTrendSummaryReport> reports = parseJsonList(body);
        return reports.stream().flatMap(report -> report.getLatestOutdoorTemperature().stream())
                .reduce((first, second) -> second);
    }

    /**
     * Fetches a short-lived WebSocket credential ("hash") for this account, exchanging the mobile-BFF Bearer access
     * token at the fixed Lambda token endpoint, mirroring what the official app does (ADR-007).
     *
     * @return the {@code hash} used to open {@link #buildWebSocketUri(String)}
     * @throws MelCloudCommException if the request fails, is rejected, or the response is missing {@code hash}
     */
    public String fetchWebSocketHash(String accessToken) throws MelCloudCommException {
        String body = get(WEBSOCKET_HASH_URL, accessToken);
        MelCloudHomeWebSocketHashResponse response = parseJson(body, MelCloudHomeWebSocketHashResponse.class,
                "WebSocket hash response");
        String hash = response.hash;
        if (hash == null || hash.isBlank()) {
            throw new MelCloudCommException("WebSocket hash response did not include a hash");
        }
        return hash;
    }

    /**
     * @param hash a credential obtained from {@link #fetchWebSocketHash(String)}
     * @return the URI to open the MELCloud Home realtime push connection at
     */
    public URI buildWebSocketUri(String hash) {
        return URI.create(WEBSOCKET_HOST + "/?hash=" + hash);
    }

    private String get(String url, String accessToken) throws MelCloudCommException {
        Properties headers = new Properties();
        headers.put("Authorization", "Bearer " + accessToken);
        try {
            String response = HttpUtil.executeUrl("GET", url, headers, null, null, TIMEOUT_MILLISECONDS);
            logger.trace("MELCloud Home BFF GET {} -> {}", SensitiveDataMasker.maskGuidsInUrl(url),
                    response == null ? "" : SensitiveDataMasker.maskJson(response));
            return response == null ? "" : response;
        } catch (IOException e) {
            throw new MelCloudCommException("Error occurred while calling " + url, e);
        }
    }

    private void put(String url, String accessToken, String jsonBody) throws MelCloudCommException {
        Properties headers = new Properties();
        headers.put("Authorization", "Bearer " + accessToken);
        try (InputStream content = new ByteArrayInputStream(jsonBody.getBytes(StandardCharsets.UTF_8))) {
            logger.trace("MELCloud Home BFF PUT {} body={}", SensitiveDataMasker.maskGuidsInUrl(url),
                    SensitiveDataMasker.maskJson(jsonBody));
            HttpUtil.executeUrl("PUT", url, headers, content, "application/json", TIMEOUT_MILLISECONDS);
        } catch (IOException e) {
            throw new MelCloudCommException("Error occurred while calling " + url, e);
        }
    }

    private void post(String url, String accessToken, String jsonBody) throws MelCloudCommException {
        Properties headers = new Properties();
        headers.put("Authorization", "Bearer " + accessToken);
        try (InputStream content = new ByteArrayInputStream(jsonBody.getBytes(StandardCharsets.UTF_8))) {
            logger.trace("MELCloud Home BFF POST {} body={}", SensitiveDataMasker.maskGuidsInUrl(url),
                    SensitiveDataMasker.maskJson(jsonBody));
            HttpUtil.executeUrl("POST", url, headers, content, "application/json", TIMEOUT_MILLISECONDS);
        } catch (IOException e) {
            throw new MelCloudCommException("Error occurred while calling " + url, e);
        }
    }

    private void delete(String url, String accessToken) throws MelCloudCommException {
        Properties headers = new Properties();
        headers.put("Authorization", "Bearer " + accessToken);
        try {
            logger.trace("MELCloud Home BFF DELETE {}", SensitiveDataMasker.maskGuidsInUrl(url));
            HttpUtil.executeUrl("DELETE", url, headers, null, null, TIMEOUT_MILLISECONDS);
        } catch (IOException e) {
            throw new MelCloudCommException("Error occurred while calling " + url, e);
        }
    }

    private <T> T parseJson(String body, Class<T> type, String context) throws MelCloudCommException {
        try {
            @Nullable
            T parsed = gson.fromJson(body, type);
            if (parsed == null) {
                throw new MelCloudCommException("Received an empty " + context);
            }
            return parsed;
        } catch (JsonSyntaxException e) {
            throw new MelCloudCommException("Failed to parse " + context, e);
        }
    }

    private List<MelCloudHomeTrendSummaryReport> parseJsonList(String body) throws MelCloudCommException {
        try {
            List<MelCloudHomeTrendSummaryReport> parsed = gson.fromJson(body, TREND_SUMMARY_REPORT_LIST_TYPE);
            return parsed == null ? List.of() : parsed;
        } catch (JsonSyntaxException e) {
            throw new MelCloudCommException("Failed to parse trend summary response", e);
        }
    }

    private static String urlEncode(Instant instant) {
        return URLEncoder.encode(ENERGY_TIMESTAMP_FORMAT.format(instant), StandardCharsets.UTF_8);
    }

    private static String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static String urlEncodeTrend(Instant instant) {
        return URLEncoder.encode(TREND_TIMESTAMP_FORMAT.format(instant) + TREND_TIMESTAMP_SUFFIX,
                StandardCharsets.UTF_8);
    }
}

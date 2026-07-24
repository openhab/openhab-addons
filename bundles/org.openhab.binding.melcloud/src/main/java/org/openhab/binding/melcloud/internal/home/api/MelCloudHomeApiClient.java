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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.melcloud.internal.exceptions.MelCloudCommException;
import org.openhab.binding.melcloud.internal.home.api.dto.MelCloudHomeAtaControlRequest;
import org.openhab.binding.melcloud.internal.home.api.dto.MelCloudHomeAtwControlRequest;
import org.openhab.binding.melcloud.internal.home.api.dto.MelCloudHomeTelemetryResponse;
import org.openhab.binding.melcloud.internal.home.api.dto.MelCloudHomeTrendSummaryReport;
import org.openhab.binding.melcloud.internal.home.api.dto.MelCloudHomeUserContext;
import org.openhab.core.io.net.http.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

/**
 * Client for the authenticated MELCloud Home mobile BFF API (see ADR-003): reading the full account context, and
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
    private static final String TELEMETRY_ENERGY_URL_TEMPLATE = BFF_BASE_URL + "/telemetry/telemetry/energy/%s";
    private static final String TRENDSUMMARY_URL = BFF_BASE_URL + "/report/v1/trendsummary";

    private static final int TIMEOUT_MILLISECONDS = 10000;

    private static final DateTimeFormatter ENERGY_TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
            .withZone(ZoneOffset.UTC);
    // The trend summary endpoint expects a literal 7-digit fractional-second suffix; the reference implementation
    // this ADR is based on always sends ".0000000" rather than the instant's real sub-second precision.
    private static final DateTimeFormatter TREND_TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
            .withZone(ZoneOffset.UTC);
    private static final String TREND_TIMESTAMP_SUFFIX = ".0000000";

    private static final Type TREND_SUMMARY_REPORT_LIST_TYPE = new TypeToken<List<MelCloudHomeTrendSummaryReport>>() {
    }.getType();

    private final Logger logger = LoggerFactory.getLogger(MelCloudHomeApiClient.class);
    private final Gson gson = new Gson();
    // A separate instance: control request bodies must serialize unset fields as JSON null rather than omitting
    // them (the server requires every control field to be present on every call, see ADR-003).
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
     * @return the latest outdoor temperature in Celsius, if any data was available
     * @throws MelCloudCommException if the request fails or the response cannot be parsed
     */
    public Optional<Double> fetchLatestOutdoorTemperature(String accessToken, String unitId, Instant from, Instant to)
            throws MelCloudCommException {
        String url = TRENDSUMMARY_URL + "?unitId=" + urlEncode(unitId) + "&period=Daily&from=" + urlEncodeTrend(from)
                + "&to=" + urlEncodeTrend(to);
        String body = get(url, accessToken);
        if (body.isBlank()) {
            return Optional.empty();
        }
        List<MelCloudHomeTrendSummaryReport> reports = parseJsonList(body);
        return reports.stream().flatMap(report -> report.getLatestOutdoorTemperature().stream())
                .reduce((first, second) -> second);
    }

    private String get(String url, String accessToken) throws MelCloudCommException {
        Properties headers = new Properties();
        headers.put("Authorization", "Bearer " + accessToken);
        try {
            String response = HttpUtil.executeUrl("GET", url, headers, null, null, TIMEOUT_MILLISECONDS);
            logger.trace("MELCloud Home BFF GET {} -> {}", url, response);
            return response == null ? "" : response;
        } catch (IOException e) {
            throw new MelCloudCommException("Error occurred while calling " + url, e);
        }
    }

    private void put(String url, String accessToken, String jsonBody) throws MelCloudCommException {
        Properties headers = new Properties();
        headers.put("Authorization", "Bearer " + accessToken);
        try (InputStream content = new ByteArrayInputStream(jsonBody.getBytes(StandardCharsets.UTF_8))) {
            logger.trace("MELCloud Home BFF PUT {} body={}", url, jsonBody);
            HttpUtil.executeUrl("PUT", url, headers, content, "application/json", TIMEOUT_MILLISECONDS);
        } catch (IOException e) {
            throw new MelCloudCommException("Error occurred while calling " + url, e);
        }
    }

    private <T> T parseJson(String body, Class<T> type, String context) throws MelCloudCommException {
        try {
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

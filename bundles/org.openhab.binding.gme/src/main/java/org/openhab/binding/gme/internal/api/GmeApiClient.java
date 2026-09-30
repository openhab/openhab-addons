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
package org.openhab.binding.gme.internal.api;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpResponseException;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.gme.internal.model.GmeAuthResponse;
import org.openhab.binding.gme.internal.model.GmeGranularity;
import org.openhab.binding.gme.internal.model.GmePriceEntry;
import org.openhab.binding.gme.internal.model.GmeRequestDataResponse;

import com.google.gson.Gson;

/**
 * Client for the GME market data API.
 *
 * @author Andrea Riela - Initial contribution
 */
@NonNullByDefault
public class GmeApiClient {

    private static final String BASE_URL = "https://api.mercatoelettrico.org/request/api/v1";
    private static final int REQUEST_TIMEOUT = 10000;
    private static final DateTimeFormatter GME_DATE_FORMAT = DateTimeFormatter.BASIC_ISO_DATE;

    private final HttpClient httpClient;
    private final Gson gson = new Gson();

    public GmeApiClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public String authenticate(String login, String password)
            throws InterruptedException, TimeoutException, ExecutionException {
        String body = gson.toJson(Map.of("Login", login, "Password", password));

        ContentResponse response;
        try {
            response = httpClient.newRequest(BASE_URL + "/Auth").method(HttpMethod.POST)
                    .content(new StringContentProvider(body), "application/json")
                    .timeout(REQUEST_TIMEOUT, TimeUnit.MILLISECONDS).send();
        } catch (ExecutionException e) {
            throw translateHttpResponseException(e, "GME authentication");
        }

        if (response.getStatus() != HttpStatus.OK_200) {
            throw new GmeApiException("GME authentication returned HTTP status " + response.getStatus(),
                    response.getStatus());
        }

        GmeAuthResponse authResponse = gson.fromJson(response.getContentAsString(), GmeAuthResponse.class);

        if (authResponse == null || !authResponse.success) {
            String reason = authResponse != null && authResponse.reason != null ? authResponse.reason
                    : "unknown reason";
            throw new IllegalStateException("GME authentication failed: " + reason);
        }

        String token = authResponse.token;
        if (token == null || token.isBlank()) {
            throw new IllegalStateException("GME authentication failed: missing token");
        }

        return token;
    }

    /**
     * Retrieves the complete MGP zonal price dataset for the requested day.
     *
     * The returned list contains PUN and all market zones. Consumers can
     * select the required zone locally without performing another API call.
     */
    public List<GmePriceEntry> requestMarketPrices(LocalDate date, String token)
            throws InterruptedException, TimeoutException, ExecutionException, IOException {
        return requestMarketPrices(date, token, GmeGranularity.PT60);
    }

    public List<GmePriceEntry> requestMarketPrices(LocalDate date, String token, GmeGranularity granularity)
            throws InterruptedException, TimeoutException, ExecutionException, IOException {
        String dateValue = date.format(GME_DATE_FORMAT);

        Map<String, Object> bodyObject = Map.of("Platform", "PublicMarketResults", "Segment", "MGP", "DataName",
                "ME_ZonalPrices", "IntervalStart", dateValue, "IntervalEnd", dateValue, "Attributes",
                Map.of("GranularityType", granularity.apiValue()));

        String body = gson.toJson(bodyObject);

        ContentResponse response;
        try {
            response = httpClient.newRequest(BASE_URL + "/RequestData").method(HttpMethod.POST)
                    .header(HttpHeader.AUTHORIZATION, "Bearer " + token)
                    .content(new StringContentProvider(body), "application/json")
                    .timeout(REQUEST_TIMEOUT, TimeUnit.MILLISECONDS).send();
        } catch (ExecutionException e) {
            throw translateHttpResponseException(e, "GME RequestData");
        }

        if (response.getStatus() != HttpStatus.OK_200) {
            throw new GmeApiException("GME RequestData returned HTTP status " + response.getStatus(),
                    response.getStatus());
        }

        GmeRequestDataResponse requestResponse = gson.fromJson(response.getContentAsString(),
                GmeRequestDataResponse.class);

        if (requestResponse == null) {
            throw new IllegalStateException("GME RequestData returned an empty response");
        }

        String contentResponse = requestResponse.contentResponse;
        if (contentResponse == null || contentResponse.isBlank()) {
            throw new IllegalStateException("GME RequestData returned no content");
        }

        return parseMarketPriceContentResponse(contentResponse, granularity);
    }

    /**
     * Backward-compatible helper for PUN prices.
     */
    public List<GmePriceEntry> requestPun(LocalDate date, String token)
            throws InterruptedException, TimeoutException, ExecutionException, IOException {
        return requestMarketPrices(date, token).stream().filter(price -> "PUN".equals(price.zone())).toList();
    }

    /**
     * Helper for a specific market zone.
     */
    public List<GmePriceEntry> requestZonal(LocalDate date, String marketZone, String token)
            throws InterruptedException, TimeoutException, ExecutionException, IOException {
        return requestMarketPrices(date, token).stream().filter(price -> marketZone.equals(price.zone())).toList();
    }

    /**
     * Jetty can fail an HTTP request before returning a ContentResponse when
     * the remote server sends an authentication challenge that does not fully
     * comply with the HTTP specification, for example a 401 response without
     * a WWW-Authenticate header.
     *
     * In that situation send() completes exceptionally with an
     * HttpResponseException wrapped by ExecutionException.
     *
     * Translate that exception into GmeApiException while preserving the HTTP
     * status code, so GmeAuthManager can recognize 401/403 responses,
     * invalidate the cached token and authenticate again.
     */
    private ExecutionException translateHttpResponseException(ExecutionException exception, String operation) {
        Throwable cause = exception.getCause();

        if (cause instanceof HttpResponseException httpResponseException) {
            Response response = httpResponseException.getResponse();
            int status = response.getStatus();

            throw new GmeApiException(operation + " returned HTTP status " + status, status);
        }

        return exception;
    }

    static List<GmePriceEntry> parseMarketPriceContentResponse(String contentResponse) throws IOException {
        return parseMarketPriceContentResponse(contentResponse, GmeGranularity.PT60);
    }

    static List<GmePriceEntry> parseMarketPriceContentResponse(String contentResponse, GmeGranularity granularity)
            throws IOException {
        byte[] zipData = Base64.getDecoder().decode(contentResponse);

        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(zipData), StandardCharsets.UTF_8)) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                if (!entry.isDirectory() && entry.getName().endsWith(".json")) {
                    List<GmePriceEntry> entries = GmePriceDataParser
                            .parse(new InputStreamReader(zip, StandardCharsets.UTF_8), granularity);

                    return entries.stream().filter(price -> "MGP".equals(price.market())).toList();
                }
            }
        }

        throw new IllegalStateException("GME ZIP response did not contain a JSON file");
    }

    /**
     * Kept for compatibility with existing tests and callers.
     */
    static List<GmePriceEntry> parsePunContentResponse(String contentResponse) throws IOException {
        return parseMarketPriceContentResponse(contentResponse).stream().filter(price -> "PUN".equals(price.zone()))
                .toList();
    }
}

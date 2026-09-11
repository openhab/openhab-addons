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
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.gme.internal.model.GmeAuthResponse;
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

        ContentResponse response = httpClient.newRequest(BASE_URL + "/Auth").method(HttpMethod.POST)
                .content(new StringContentProvider(body), "application/json")
                .timeout(REQUEST_TIMEOUT, TimeUnit.MILLISECONDS).send();

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

    public List<GmePriceEntry> requestPun(LocalDate date, String token)
            throws InterruptedException, TimeoutException, ExecutionException, IOException {
        String dateValue = date.format(GME_DATE_FORMAT);

        Map<String, Object> bodyObject = Map.of("Platform", "PublicMarketResults", "Segment", "MGP", "DataName",
                "ME_ZonalPrices", "IntervalStart", dateValue, "IntervalEnd", dateValue, "Attributes",
                Map.of("GranularityType", "PT60"));

        String body = gson.toJson(bodyObject);

        ContentResponse response = httpClient.newRequest(BASE_URL + "/RequestData").method(HttpMethod.POST)
                .header(HttpHeader.AUTHORIZATION, "Bearer " + token)
                .content(new StringContentProvider(body), "application/json")
                .timeout(REQUEST_TIMEOUT, TimeUnit.MILLISECONDS).send();

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

        return parsePunContentResponse(contentResponse);
    }

    static List<GmePriceEntry> parsePunContentResponse(String contentResponse) throws IOException {
        byte[] zipData = Base64.getDecoder().decode(contentResponse);

        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(zipData), StandardCharsets.UTF_8)) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                if (!entry.isDirectory() && entry.getName().endsWith(".json")) {
                    List<GmePriceEntry> entries = GmePriceDataParser
                            .parse(new InputStreamReader(zip, StandardCharsets.UTF_8));

                    return entries.stream().filter(price -> "MGP".equals(price.market()))
                            .filter(price -> "PUN".equals(price.zone())).toList();
                }
            }
        }

        throw new IllegalStateException("GME ZIP response did not contain a JSON file");
    }
}

/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.icloud.internal;

import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.openhab.binding.icloud.internal.utilities.CustomCookieStore;
import org.openhab.binding.icloud.internal.utilities.ListUtil;
import org.openhab.binding.icloud.internal.utilities.Pair;
import org.openhab.core.storage.Storage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 *
 * Class to handle iCloud API session information for accessing the API.
 *
 * The implementation of this class is inspired by https://github.com/picklepete/pyicloud.
 *
 * @author Simon Spielmann Initial contribution
 */
public class ICloudSession {

    private final Logger logger = LoggerFactory.getLogger(ICloudSession.class);

    private final HttpClient client;

    private List<Pair<String, String>> headers = new ArrayList<>();

    private ICloudSessionData data = new ICloudSessionData();

    private final Gson gson = new GsonBuilder().create();

    private Storage<String> stateStorage;

    private final static String SESSION_DATA_KEY = "SESSION_DATA";

    /**
     * The constructor.
     *
     * @param stateStorage Storage to persist session state.
     */
    public ICloudSession(Storage<String> stateStorage) {

        String storedData = stateStorage.get(SESSION_DATA_KEY);
        if (storedData != null) {
            this.data = this.gson.fromJson(storedData, ICloudSessionData.class);
        }
        this.stateStorage = stateStorage;
        this.client = HttpClient.newBuilder().version(Version.HTTP_1_1).followRedirects(Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(20))
                .cookieHandler(new CookieManager(new CustomCookieStore(), CookiePolicy.ACCEPT_ALL)).build();
    }

    /**
     * Invoke an HTTP POST request to the given url and body.
     *
     * @param url URL to call.
     * @param body Body for the request
     * @param overrideHeaders  If not null the given headers are used instead of the standard headers set via
     *            {@link #setDefaultHeaders(Pair...)}
     * @return Result body as {@link String}.
     * @throws IOException if I/O error occurred
     * @throws InterruptedException if this blocking request was interrupted
     */
    public String post(String url, String body, List<Pair<String, String>> overrideHeaders)
            throws IOException, InterruptedException {

        return request("POST", url, body, overrideHeaders);
    }

    /**
     * Invoke an HTTP GET request to the given url.
     *
     * @param url URL to call.
     * @param overrideHeaders  If not null the given headers are used to replace corresponding entries of the standard
     *            headers set via
     *            {@link #setDefaultHeaders(Pair...)}
     * @return Result body as {@link String}.
     * @throws IOException if I/O error occurred
     * @throws InterruptedException if this blocking request was interrupted
     */
    public String get(String url, List<Pair<String, String>> overrideHeaders) throws IOException, InterruptedException {

        return request("GET", url, null, overrideHeaders);
    }

    private String request(String method, String url, String body, List<Pair<String, String>> overrideHeaders)
            throws IOException, InterruptedException {
        logger.debug("iCloud request {} {}.", method, url);

        Builder builder = HttpRequest.newBuilder().uri(URI.create(url));

        List<Pair<String, String>> requestHeaders = ListUtil.replaceEntries(this.headers, overrideHeaders);

        for (Pair<String, String> header : requestHeaders) {
            builder.header(header.getKey(), header.getValue());
        }

        if (body != null) {
            builder.method(method, BodyPublishers.ofString(body));
        }

        HttpRequest request = builder.build();

        logger.trace("Calling {}\nHeaders -----\n{}\nBody -----\n{}\n------\n", url, request.headers(), body);

        HttpResponse<?> response = this.client.send(request, BodyHandlers.ofString());
        logger.trace("Result {} {}\nHeaders -----\n{}\nBody -----\n{}\n------\n", url, response.statusCode(),
                response.headers().toString(), response.body().toString());

        if (response.statusCode() >= 300) {
            // Error Handling pyicloud 99-162
            throw new ICloudAPIResponseException(url, response.statusCode());

        }

        // Store headers to reuse authentication
        this.data.accountCountry = response.headers().firstValue("X-Apple-ID-Account-Country")
                .orElse(getAccountCountry());
        this.data.sessionId = response.headers().firstValue("X-Apple-ID-Session-Id").orElse(getSessionId());
        this.data.sessionToken = response.headers().firstValue("X-Apple-Session-Token").orElse(getSessionToken());
        this.data.trustToken = response.headers().firstValue("X-Apple-TwoSV-Trust-Token").orElse(getTrustToken());
        this.data.scnt = response.headers().firstValue("scnt").orElse(getScnt());

        this.stateStorage.put(SESSION_DATA_KEY, this.gson.toJson(this.data));

        return response.body().toString();
    }

    /**
     * Sets default HTTP headers, for HTTP requests.
     *
     * @param headers HTTP headers to use for requests
     */
    @SafeVarargs
    public final void setDefaultHeaders(Pair<String, String>... headers) {
        this.headers = Arrays.asList(headers);
    }

    /**
     * @return scnt
     */
    public String getScnt() {

        return this.data.scnt;
    }

    /**
     * @return sessionId
     */
    public String getSessionId() {

        return this.data.sessionId;
    }

    /**
     * @return sessionToken
     */
    public String getSessionToken() {

        return this.data.sessionToken;
    }

    /**
     * @return trustToken
     */
    public String getTrustToken() {

        return this.data.trustToken;
    }

    /**
     * @return {@code true} if session token is not empty.
     */
    public boolean hasToken() {

        return this.data.sessionToken != null && !this.data.sessionToken.isEmpty();
    }

    /**
     * @return accountCountry
     */
    public String getAccountCountry() {

        return this.data.accountCountry;
    }

    /**
     *
     * Internal class to encapsulate data required for iCloud authentication.
     *
     * @author Simon Spielmann Initial Contribution
     */
    private class ICloudSessionData {
        String scnt;

        String sessionId;

        String sessionToken;

        String trustToken;

        String accountCountry;
    }
}

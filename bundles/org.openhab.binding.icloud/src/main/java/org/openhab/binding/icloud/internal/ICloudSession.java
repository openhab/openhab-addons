/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
import java.util.Iterator;
import java.util.List;

import org.openhab.binding.icloud.internal.utilities.CustomCookieStore;
import org.openhab.binding.icloud.internal.utilities.Pair;
import org.openhab.core.storage.Storage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 *
 * TODO
 *
 * @author Simon Spielmann
 */
public class ICloudSession {

    private final static Logger LOGGER = LoggerFactory.getLogger(ICloudSession.class);

    private final HttpClient client;

    private final CustomCookieStore cookieStore;

    private final List<Pair<String, String>> headers = new ArrayList();

    private ICloudSessionData data = new ICloudSessionData();

    private final Gson gson = new GsonBuilder().create();

    private Storage<String> stateStorage;

    private final static String SESSION_DATA_KEY = "SESSION_DATA";

    public ICloudSession(Storage<String> stateStorage) {

        String storedData = stateStorage.get(SESSION_DATA_KEY);
        if (storedData != null) {
            this.data = this.gson.fromJson(storedData, ICloudSessionData.class);
        }
        this.stateStorage = stateStorage;
        this.cookieStore = new CustomCookieStore(stateStorage);
        this.client = HttpClient.newBuilder().version(Version.HTTP_1_1).followRedirects(Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(20))
                .cookieHandler(new CookieManager(this.cookieStore, CookiePolicy.ACCEPT_ALL)).build();
    }

    public String post(String url, String kwargs, List<Pair<String, String>> overrideHeaders)
            throws IOException, InterruptedException {

        return request("POST", url, kwargs, overrideHeaders);
    }

    public String get(String url, String kwargs, List<Pair<String, String>> overrideHeaders)
            throws IOException, InterruptedException {

        return request("GET", url, kwargs, overrideHeaders);
    }

    private String request(String method, String url, String kwargs, List<Pair<String, String>> overrideHeaders)
            throws IOException, InterruptedException {

        Builder builder = HttpRequest.newBuilder().uri(URI.create(url));

        List<Pair<String, String>> requestHeaders = this.headers;
        if (overrideHeaders != null) {
            requestHeaders = overrideHeaders;
        }

        for (Pair<String, String> header : requestHeaders) {
            builder.header(header.key, header.value);
        }

        if (kwargs != null) {
            builder.method(method, BodyPublishers.ofString(kwargs));
        }

        HttpRequest request = builder.build();

        LOGGER.debug("Calling {}\nHeaders -----\n{}\nBody -----\n{}\n------\n", url, request.headers(), kwargs);

        HttpResponse response = this.client.send(request, BodyHandlers.ofString());
        LOGGER.debug("Result {} {}\nHeaders -----\n{}\nBody -----\n{}\n------\n", url, response.statusCode(),
                response.headers().toString(), response.body().toString());

        // TODO Error Handling pyicloud 99-162
        if (response.statusCode() >= 300) {
            throw new ICloudAPIResponseException();
            /*
             * 826 if (response.statusCode() == 421 || response.statusCode() == 450 || response.statusCode() == 500) {
             * throw
             * new ICloudAPIResponseException();
             */
        }

        this.data.accountCountry = response.headers().firstValue("X-Apple-ID-Account-Country")
                .orElse(getAccountCountry());
        this.data.sessionId = response.headers().firstValue("X-Apple-ID-Session-Id").orElse(getSessionId());
        this.data.sessionToken = response.headers().firstValue("X-Apple-Session-Token").orElse(getSessionToken());
        this.data.trustToken = response.headers().firstValue("X-Apple-TwoSV-Trust-Token").orElse(getTrustToken());
        this.data.scnt = response.headers().firstValue("scnt").orElse(getScnt());

        this.stateStorage.put(SESSION_DATA_KEY, this.gson.toJson(this.data));
        this.cookieStore.saveState();

        return response.body().toString();
    }

    /**
     * Update headers, remove existing keys and set given
     *
     * @return headers
     */
    public void updateHeaders(Pair<String, String>... headers) {

        updateList(this.headers, headers);
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
     * @return
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

    // TODO refactor this
    public static void updateList(List<Pair<String, String>> originalList, Pair<String, String>... replacements) {

        for (Pair<String, String> newHeader : replacements) {
            Iterator<Pair<String, String>> it = originalList.iterator();
            boolean found = false;
            while (it.hasNext()) {
                Pair<String, String> existingHeader = it.next();
                if (existingHeader.key.equals(newHeader.key)) {
                    if (found) {
                        it.remove();
                    } else {
                        existingHeader.value = newHeader.value;
                        found = true;
                    }
                }
            }
            if (!found) {
                originalList.add(newHeader);
            }
        }
    }

    /**
     *
     * TODO
     *
     * @author Simon Spielmann
     */
    class ICloudSessionData {
        /**
         *
         */
        String scnt;
        /**
         *
         */
        String sessionId;
        /**
         *
         */
        String sessionToken;
        /**
         *
         */
        String trustToken;
        /**
         *
         */
        String accountCountry;
    }
}

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
package org.openhab.binding.kostalinverter.internal.thirdgeneration;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpVersion;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * The {@link ThirdGenerationHttpHelper} is handling http communication with the device
 * handlers.
 *
 * @author René Stakemeier - Initial contribution
 */
final class ThirdGenerationHttpHelper {

    private ThirdGenerationHttpHelper() {
    }

    // base URL of the web api
    private static final String WEB_API = "/api/v1";
    // GSON handler
    private static final Gson GSON = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();

    /**
     * Helper function to execute a HTTP post request
     *
     * @param httpClient httpClient to use for communication
     * @param url IP or hostname or the device
     * @param resource web API resource to post to
     * @param parameters the JSON content to post
     * @return the HTTP response for the created post request
     * @throws ExecutionException Error during the execution of the http request
     * @throws TimeoutException Connection timed out
     * @throws InterruptedException Connection interrupted
     */
    static ContentResponse executeHttpPost(HttpClient httpClient, String url, String resource, JsonObject parameters)
            throws InterruptedException, TimeoutException, ExecutionException {
        return executeHttpPost(httpClient, url, resource, parameters, null);
    }

    /**
     * Helper function to execute a HTTP post request
     *
     * @param httpClient httpClient to use for communication
     * @param url IP or hostname or the device
     * @param resource web API resource to post to
     * @param sessionId optional session ID
     * @param parameters the JSON content to post
     * @return the HTTP response for the created post request
     * @throws ExecutionException Error during the execution of the http request
     * @throws TimeoutException Connection timed out
     * @throws InterruptedException Connection interrupted
     */
    static ContentResponse executeHttpPost(HttpClient httpClient, String url, String resource, JsonElement parameters,
            @Nullable String sessionId) throws InterruptedException, TimeoutException, ExecutionException {
        Request response = httpClient.newRequest(String.format("%s/%s%s", url, WEB_API, resource), 80).scheme("http")
                .agent("Jetty HTTP client").version(HttpVersion.HTTP_1_1).method(HttpMethod.POST)
                .header(HttpHeader.ACCEPT, "application/json").header(HttpHeader.CONTENT_TYPE, "application/json")
                .timeout(5, TimeUnit.SECONDS);
        response.content(new StringContentProvider(parameters.toString()));
        if (sessionId != null) {
            response.header(HttpHeader.AUTHORIZATION, String.format("Session %s", sessionId));
        }
        return response.send();
    }

    /**
     * Helper function to execute a HTTP get request
     *
     * @param httpClient httpClient to use for communication
     * @param url IP or hostname or the device
     * @param resource web API resource to get
     * @return the HTTP response for the created get request
     * @throws ExecutionException Error during the execution of the http request
     * @throws TimeoutException Connection timed out
     * @throws InterruptedException Connection interrupted
     */
    static ContentResponse executeHttpGet(HttpClient httpClient, String url, String resource)
            throws InterruptedException, TimeoutException, ExecutionException {
        return executeHttpGet(httpClient, url, resource, null);
    }

    /**
     * Helper function to execute a HTTP get request
     *
     * @param httpClient httpClient to use for communication
     * @param url IP or hostname or the device
     * @param resource web API resource to get
     * @param sessionId optional session ID
     * @return the HTTP response for the created get request
     * @throws ExecutionException Error during the execution of the http request
     * @throws TimeoutException Connection timed out
     * @throws InterruptedException Connection interrupted
     * @throws Exception thrown if there are communication problems
     */
    static ContentResponse executeHttpGet(HttpClient httpClient, String url, String resource,
            @Nullable String sessionId) throws InterruptedException, TimeoutException, ExecutionException {
        Request response = httpClient.newRequest(String.format("%s/%s%s", url, WEB_API, resource), 80).scheme("http")
                .agent("Jetty HTTP client").version(HttpVersion.HTTP_1_1).method(HttpMethod.GET)
                .header(HttpHeader.ACCEPT, "application/json").header(HttpHeader.CONTENT_TYPE, "application/json")
                .timeout(5, TimeUnit.SECONDS);
        if (sessionId != null) {
            response.header(HttpHeader.AUTHORIZATION, String.format("Session %s", sessionId));
        }
        return response.send();
    }

    /**
     * Helper to extract the JsonArray from a HTTP response.
     * Use only, if you expect a JsonArray and no other types (e.g. JSON array)!
     *
     * @param reponse the HTTP response
     * @return the JSON object
     */
    static JsonArray getJsonArrayFromResponse(ContentResponse reponse) {
        return GSON.fromJson(reponse.getContentAsString(), JsonArray.class);
    }

    /**
     * Helper to extract the JSON object from a HTTP response.
     * Use only, if you expect a JSON object and no other types (e.g. JSON array)!
     *
     * @param reponse the HTTP response
     * @return the JSON object
     */
    static JsonObject getJsonObjectFromResponse(ContentResponse reponse) {
        return GSON.fromJson(reponse.getContentAsString(), JsonObject.class);
    }
}

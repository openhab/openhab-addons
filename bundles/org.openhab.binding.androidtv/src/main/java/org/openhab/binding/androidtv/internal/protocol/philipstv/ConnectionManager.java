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
package org.openhab.binding.androidtv.internal.protocol.philipstv;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The {@link ConnectionManager} is responsible for handling https GETs and POSTs to the Philips
 * TVs.
 *
 * @author Benjamin Meyer - Initial contribution
 * @author Ben Rosenblum - Merged into AndroidTV
 */
@NonNullByDefault
public class ConnectionManager {

    private static final String TARGET_URI_MSG = "Target Uri is: {}";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    // Cannot use jetty in OH2.4 due to 9.4.11.v20180605 version with digest auth bug
    // https://github.com/eclipse/jetty.project/issues/1555
    private final CloseableHttpClient httpClient;

    private final HttpHost httpHost;

    public ConnectionManager(CloseableHttpClient httpClient, HttpHost httpHost) {
        this.httpClient = httpClient;
        this.httpHost = httpHost;
        OBJECT_MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public String doHttpsGet(String path) throws IOException {
        String uri = httpHost.toURI() + path;
        logger.debug(TARGET_URI_MSG, uri);
        HttpGet httpGet = new HttpGet(uri);
        String jsonContent = "";
        try (CloseableHttpClient client = httpClient; //
                CloseableHttpResponse response = client.execute(httpHost, httpGet)) {
            validateResponse(response, uri);
            jsonContent = getJsonFromResponse(response);
        } catch (HttpHostConnectException e) {
            logger.debug("HttpHostConnectException when getting {}", uri);
        }
        return jsonContent;
    }

    public String doHttpsPost(String path, String json) throws IOException {
        String uri = httpHost.toURI() + path;
        logger.debug(TARGET_URI_MSG, uri);
        HttpPost httpPost = new HttpPost(uri);
        httpPost.setHeader("Content-type", "application/json");
        httpPost.setEntity(new StringEntity(json));
        String jsonContent = "";
        try (CloseableHttpClient client = httpClient; //
                CloseableHttpResponse response = client.execute(httpHost, httpPost)) {
            validateResponse(response, uri);
            jsonContent = getJsonFromResponse(response);
        } catch (HttpHostConnectException e) {
            logger.debug("HttpHostConnectException when getting {}", uri);
        }
        return jsonContent;
    }

    private void validateResponse(CloseableHttpResponse response, String uri) throws HttpResponseException {
        if (response == null) {
            throw new HttpResponseException(0, String.format("The response for the request to %s was empty.", uri));
        } else if (response.getStatusLine().getStatusCode() == 401) {
            throw new HttpResponseException(401, "The given username/password combination is invalid.");
        }
    }

    private String getJsonFromResponse(HttpResponse response) throws IOException {
        String jsonContent = EntityUtils.toString(response.getEntity());
        logger.trace("----------------------------------------");
        logger.trace("{}", response.getStatusLine());
        logger.trace("{}", jsonContent);
        return jsonContent;
    }

    public byte[] doHttpsGetForImage(String path) throws IOException {
        String uri = httpHost.toURI() + path;
        logger.debug(TARGET_URI_MSG, uri);
        HttpGet httpGet = new HttpGet(uri);
        try (CloseableHttpClient client = httpClient;
                CloseableHttpResponse response = client.execute(httpHost, httpGet)) {
            if ((response != null) && (response.getStatusLine().getStatusCode() == 401)) {
                throw new HttpResponseException(401, "The given username/password combination is invalid.");
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            if (response != null) {
                response.getEntity().writeTo(baos);
            }
            return baos.toByteArray();
        }
    }
}

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
package org.openhab.binding.enphase.internal.handler;

import java.net.HttpCookie;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.FormContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.util.Fields;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openhab.binding.enphase.internal.dto.EntrezJwtDTO;
import org.openhab.binding.enphase.internal.dto.EntrezJwtDTO.EntrezJwtBodyDTO;
import org.openhab.binding.enphase.internal.dto.EntrezJwtDTO.EntrezJwtHeaderDTO;
import org.openhab.binding.enphase.internal.exception.EntrezConnectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

/**
 * Connector logic for connecting to Entrez server
 *
 * @author Joe Inkenbrandt - Initial contribution
 */
@NonNullByDefault
public class EntrezConnector {

    private static final String SESSION_COOKIE_NAME = "SESSION";
    private static final String ELEMENT_ID_JWT_TOKEN = "#JWTToken";
    private static final String LOGIN_URL = "https://entrez.enphaseenergy.com/login";
    private static final String TOKEN_URL = "https://entrez.enphaseenergy.com/entrez_tokens";

    private final Logger logger = LoggerFactory.getLogger(EntrezConnector.class);
    private final Gson gson = new GsonBuilder().create();
    private final HttpClient httpClient;

    private static final long CONNECT_TIMEOUT_SECONDS = 10;

    public EntrezConnector(final HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public String retrieveJwt(final String username, final String password, final String siteId, final String serialNum)
            throws EntrezConnectionException {
        final String session = login(username, password);
        final Fields fields = new Fields();
        fields.put("Site", siteId);
        fields.put("serialNum", serialNum);

        final URI uri = URI.create(TOKEN_URL);
        logger.trace("Retrieving jwt from '{}'", uri);
        final Request request = httpClient.newRequest(uri).method(HttpMethod.POST)
                .cookie(new HttpCookie(SESSION_COOKIE_NAME, session)).content(new FormContentProvider(fields))
                .timeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS);

        final ContentResponse response = send(request);
        final String contentAsString = response.getContentAsString();
        final Document document = Jsoup.parse(contentAsString);
        final Elements elements = document.select(ELEMENT_ID_JWT_TOKEN);
        final Element first = elements.first();

        if (first == null) {
            logger.debug("Could not select element '{}' in received data from entrez site. Received data: {}",
                    ELEMENT_ID_JWT_TOKEN, contentAsString);
            throw new EntrezConnectionException("Could not parse data from entrez site");
        }
        return first.text();
    }

    public EntrezJwtDTO processJwt(final String jwt) throws EntrezConnectionException {
        try {
            final String[] parts = jwt.split("\\.", 0);
            if (parts.length < 2) {
                logger.debug("Could not split data into 2 parts. Recevied data: {}", jwt);
                throw new EntrezConnectionException("Could not parse data from entrez site");
            }
            final EntrezJwtHeaderDTO header = gson.fromJson(
                    new String(Base64.getUrlDecoder().decode(parts[0]), StandardCharsets.UTF_8),
                    EntrezJwtHeaderDTO.class);
            final EntrezJwtBodyDTO body = gson.fromJson(
                    new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8),
                    EntrezJwtBodyDTO.class);

            return new EntrezJwtDTO(header, body);
        } catch (JsonSyntaxException | IllegalArgumentException e) {
            throw new EntrezConnectionException("Could not parse data from entrez site:", e);
        }
    }

    private String login(final String username, final String password) throws EntrezConnectionException {
        final Fields fields = new Fields();
        fields.put("username", username);
        fields.put("password", password);

        final URI uri = URI.create(LOGIN_URL);
        logger.trace("Retrieving session id from '{}'", uri);
        final Request request = httpClient.newRequest(uri).method(HttpMethod.POST)
                .content(new FormContentProvider(fields)).timeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        final ContentResponse response = send(request);

        if (response.getStatus() == 200 && response.getHeaders().contains(HttpHeader.SET_COOKIE)) {
            final List<HttpCookie> cookies = HttpCookie.parse(response.getHeaders().get(HttpHeader.SET_COOKIE));

            for (final HttpCookie c : cookies) {
                if (SESSION_COOKIE_NAME.equals(c.getName())) {
                    return c.getValue();
                }
            }
        }
        logger.debug("Failed to login to Entrez portal. Portal returned status: {}. Response from Entrez portal: {}",
                response.getStatus(), response.getContentAsString());
        throw new EntrezConnectionException(
                "Could not login to Entrez JWT Portal. Status code:" + response.getStatus());
    }

    private ContentResponse send(final Request request) throws EntrezConnectionException {
        try {
            return request.send();
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new EntrezConnectionException("Interrupted");
        } catch (final TimeoutException e) {
            logger.debug("TimeoutException: {}", e.getMessage());
            throw new EntrezConnectionException("Connection timeout: ", e);
        } catch (final ExecutionException e) {
            logger.debug("ExecutionException: {}", e.getMessage(), e);
            throw new EntrezConnectionException("Could not retrieve data: ", e.getCause());
        }
    }
}

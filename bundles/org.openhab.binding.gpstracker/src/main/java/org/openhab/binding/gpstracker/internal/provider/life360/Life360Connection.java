/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.gpstracker.internal.provider.life360;

import com.google.gson.Gson;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.FormContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.util.Fields;
import org.openhab.binding.gpstracker.internal.config.Life360Config;
import org.openhab.binding.gpstracker.internal.message.life360.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

/**
 * The {@link Life360Connection} is a API helper for Life360.
 *
 * @author Gabor Bicskei - Initial contribution
 */
public class Life360Connection {
    /**
     * Class logger
     */
    private final Logger logger = LoggerFactory.getLogger(Life360Connection.class);

    private static final String URI_TOKEN = "https://api.life360.com/v3/oauth2/token.json";

    private static final String URI_CIRCLES = "https://api.life360.com/v3/circles.json";

    private static final String URI_PLACES = "https://api.life360.com/v3/circles/%s/places.json";

    private static final String URI_CIRCLE = "https://api.life360.com/v3/circles/%s";

    private HttpClient httpClient;

    private final Gson gson = new Gson();

    private String accessToken;

    private Life360StatusListener statusListener;

    public Life360Connection(HttpClient httpClient, Life360StatusListener statusListener) {
        try {
            this.httpClient = httpClient;
            this.statusListener = statusListener;
        } catch (Exception e) {
            logger.error("Error starting HTTP client", e);
        }
    }

    public void authenticate(Life360Config config) {
        try {
            Fields fields = new Fields();
            fields.add("grant_type", "password");
            fields.add("username", config.getUserName());
            fields.add("password", config.getPassword());

            Request request = httpClient.POST(URI_TOKEN).content(new FormContentProvider(fields));
            request.header("Authorization", "Basic " + config.getAuthToken());

            ContentResponse response = request.send();
            if (response.getStatus() == HttpStatus.OK_200) {
                String contentStr = response.getContentAsString();
                AccessTokenResponse tokenMsg = gson.fromJson(contentStr, AccessTokenResponse.class);
                accessToken = tokenMsg.getAccessToken();
                statusListener.online();
            } else {
                statusListener.error("Failed to get access token.");
            }
        } catch (Exception e) {
            logger.error("Authentication failed", e);
            statusListener.error(e.getMessage());
        }
    }

    public Set<String> loadCircleIds() {
        try {
            Request request = httpClient.newRequest(URI_CIRCLES).header("Authorization", "Bearer " + accessToken).method(HttpMethod.GET);
            ContentResponse response = request.send();

            CircleListResponse circleList = gson.fromJson(response.getContentAsString(), CircleListResponse.class);
            return circleList.getCircles().stream().map(CirclesItem::getId).collect(Collectors.toSet());
        } catch (Exception e) {
            logger.error("Error loading Life360 information", e);
            statusListener.error("Load");
        }
        return Collections.emptySet();
    }

    public CircleDetailResponse loadCircleDetails(String cId) throws InterruptedException, TimeoutException, ExecutionException {
        Request circleDetailRequest = httpClient.newRequest(String.format(URI_CIRCLE, cId)).header("Authorization", "Bearer " + accessToken).method(HttpMethod.GET);
        ContentResponse circleDetailResponse = circleDetailRequest.send();
        return gson.fromJson(circleDetailResponse.getContentAsString(), CircleDetailResponse.class);
    }

    private List<PlacesItem> loadPlaces(String cId) throws InterruptedException, TimeoutException, ExecutionException {
        Request placesRequest = httpClient.newRequest(String.format(URI_PLACES, cId)).header("Authorization", "Bearer " + accessToken).method(HttpMethod.GET);
        ContentResponse placesResponse = placesRequest.send();
        PlaceListResponse places = gson.fromJson(placesResponse.getContentAsString(), PlaceListResponse.class);
        return places.getPlaces();
    }
}

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
package org.openhab.binding.sncf.internal.handler;

import static org.eclipse.jetty.http.HttpMethod.GET;
import static org.eclipse.jetty.http.HttpStatus.OK_200;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.http.HttpHeader;
import org.openhab.binding.sncf.internal.SncfException;
import org.openhab.binding.sncf.internal.discovery.SncfDiscoveryService;
import org.openhab.binding.sncf.internal.dto.Passage;
import org.openhab.binding.sncf.internal.dto.Passages;
import org.openhab.binding.sncf.internal.dto.PlaceNearby;
import org.openhab.binding.sncf.internal.dto.PlacesNearby;
import org.openhab.binding.sncf.internal.dto.SncfAnswer;
import org.openhab.binding.sncf.internal.dto.StopPoint;
import org.openhab.binding.sncf.internal.dto.StopPoints;
import org.openhab.core.cache.ExpiringCacheMap;
import org.openhab.core.library.types.PointType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link SncfBridgeHandler} is handles connection and communication toward
 * SNCF API
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class SncfBridgeHandler extends BaseBridgeHandler {
    public static final String JSON_CONTENT_TYPE = "application/json";

    public static final String SERVICE_URL = "https://api.sncf.com/v1/coverage/sncf/";

    private final Logger logger = LoggerFactory.getLogger(SncfBridgeHandler.class);
    private final ExpiringCacheMap<String, @Nullable String> cache = new ExpiringCacheMap<>(Duration.ofMinutes(1));
    private final HttpClient httpClient;

    private final Gson gson;
    private @NonNullByDefault({}) String apiId;

    public SncfBridgeHandler(Bridge bridge, Gson gson, HttpClient httpClient) {
        super(bridge);
        this.httpClient = httpClient;
        this.gson = gson;
    }

    @Override
    public void initialize() {
        logger.debug("Initializing SNCF API bridge handler.");
        apiId = (String) getConfig().get("apiID");
        if (apiId != null && !apiId.isBlank()) {
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "@text/null-or-empty-api-key");
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("SNCF API Bridge is read-only and does not handle commands");
    }

    private <T extends SncfAnswer> T getResponseFromCache(String url, Class<T> objectClass) throws SncfException {
        String answer = cache.putIfAbsentAndGet(url, () -> getResponse(url));
        try {
            if (answer != null) {
                @Nullable
                T response = gson.fromJson(answer, objectClass);
                if (response == null) {
                    throw new SncfException("Unable to deserialize API answer");
                }
                if (response.message != null) {
                    throw new SncfException(response.message);
                }
                return response;
            } else {
                throw new SncfException(String.format("Unable to get api answer for url : %s", url));
            }
        } catch (JsonSyntaxException e) {
            throw new SncfException(e);
        }
    }

    private @Nullable String getResponse(String url) {
        try {
            logger.debug("SNCF Api request: url = '{}'", url);
            ContentResponse contentResponse = httpClient.newRequest(url).method(GET).timeout(10, TimeUnit.SECONDS)
                    .header(HttpHeader.AUTHORIZATION, apiId).send();
            int httpStatus = contentResponse.getStatus();
            String content = contentResponse.getContentAsString();
            logger.debug("SNCF Api response: status = {}, content = '{}'", httpStatus, content);
            if (httpStatus == OK_200) {
                return content;
            }
            logger.debug("SNCF Api server responded with status code {}: {}", httpStatus, content);
        } catch (TimeoutException | ExecutionException e) {
            logger.debug("Execution occured : {}", e.getMessage(), e);
        } catch (InterruptedException e) {
            logger.debug("Execution interrupted : {}", e.getMessage(), e);
            Thread.currentThread().interrupt();
        }
        return null;
    }

    public @Nullable List<PlaceNearby> discoverNearby(PointType location, int distance) throws SncfException {
        String url = String.format(Locale.US, "%scoord/%.5f;%.5f/places_nearby?distance=%d&type[]=stop_point&count=100",
                SERVICE_URL, location.getLongitude().floatValue(), location.getLatitude().floatValue(), distance);
        PlacesNearby places = getResponseFromCache(url, PlacesNearby.class);
        return places.placesNearby;
    }

    public Optional<StopPoint> stopPointDetail(String stopPointId) throws SncfException {
        String url = String.format("%sstop_points/%s", SERVICE_URL, stopPointId);
        List<StopPoint> points = getResponseFromCache(url, StopPoints.class).stopPoints;
        return points != null && !points.isEmpty() ? Optional.ofNullable(points.get(0)) : Optional.empty();
    }

    public Optional<Passage> getNextPassage(String stopPointId, String expected) throws SncfException {
        String url = String.format("%sstop_points/%s/%s?disable_geojson=true&count=1", SERVICE_URL, stopPointId,
                expected);
        List<Passage> passages = getResponseFromCache(url, Passages.class).passages;
        return passages != null && !passages.isEmpty() ? Optional.ofNullable(passages.get(0)) : Optional.empty();
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(SncfDiscoveryService.class);
    }
}

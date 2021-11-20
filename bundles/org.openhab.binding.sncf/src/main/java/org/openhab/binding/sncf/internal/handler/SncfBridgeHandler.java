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
package org.openhab.binding.sncf.internal.handler;

import static org.eclipse.jetty.http.HttpMethod.GET;
import static org.eclipse.jetty.http.HttpStatus.*;

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
import org.openhab.core.i18n.LocationProvider;
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
    private final LocationProvider locationProvider;
    private final ExpiringCacheMap<String, String> cache = new ExpiringCacheMap<>(Duration.ofMinutes(1));
    private final HttpClient httpClient;

    private final Gson gson;
    private @NonNullByDefault({}) String apiId;

    public SncfBridgeHandler(Bridge bridge, Gson gson, LocationProvider locationProvider, HttpClient httpClient) {
        super(bridge);
        this.locationProvider = locationProvider;
        this.httpClient = httpClient;
        this.gson = gson;
    }

    @Override
    public void initialize() {
        logger.debug("Initializing SNCF API bridge handler.");
        apiId = (String) getConfig().get("apiID");
        if (apiId != null && apiId.length() != 0) {
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
        String answer = cache.get(url);
        if (answer == null) {
            final String newAnswer = getResponse(url);
            cache.put(url, () -> newAnswer);
            answer = newAnswer;
        }
        @Nullable
        T response = gson.fromJson(answer, objectClass);
        if (response == null) {
            throw new SncfException("Unable to deserialize API answer");
        }
        if (response.message != null) {
            throw new SncfException(response.message);
        }
        return response;
    }

    private String getResponse(String url) throws SncfException {
        try {
            logger.debug("SNCF Api request: URL = '{}'", url);
            ContentResponse contentResponse = httpClient.newRequest(url).method(GET).timeout(10, TimeUnit.SECONDS)
                    .header("Authorization", apiId).send();
            int httpStatus = contentResponse.getStatus();
            String content = contentResponse.getContentAsString();
            logger.debug("SNCF Api response: status = {}, content = '{}'", httpStatus, content);
            switch (httpStatus) {
                case OK_200:
                    return content;
                case BAD_REQUEST_400:
                case UNAUTHORIZED_401:
                case NOT_FOUND_404:
                    logger.debug("SNCF Api server responded with status code {}: {}", httpStatus, content);
                    throw new SncfException(content);
                default:
                    logger.debug("SNCF Api server responded with status code {}: {}", httpStatus, content);
                    throw new SncfException(content);
            }
        } catch (TimeoutException | ExecutionException e) {
            logger.debug("Exception occurred during execution: {}", e.getLocalizedMessage(), e);
            throw new SncfException(e.getLocalizedMessage(), e.getCause());
        } catch (InterruptedException e) {
            logger.debug("Execution interrupted: {}", e.getLocalizedMessage(), e);
            Thread.currentThread().interrupt();
            throw new SncfException(e.getLocalizedMessage(), e.getCause());
        }
    }

    public @Nullable List<PlaceNearby> discoverNearby(PointType location, int distance) throws SncfException {
        String URL = String.format(Locale.US,
                "%s/coord/%.5f;%.5f/places_nearby?distance=%d&type[]=stop_point&count=100", SERVICE_URL,
                location.getLongitude().floatValue(), location.getLatitude().floatValue(), distance);
        PlacesNearby places = getResponseFromCache(URL, PlacesNearby.class);
        return places.placesNearby;
    }

    public Optional<StopPoint> stopPointDetail(String stopPointId) throws SncfException {
        String URL = String.format(Locale.US, "%s/stop_points/%s", SERVICE_URL, stopPointId);
        List<StopPoint> points = getResponseFromCache(URL, StopPoints.class).stopPoints;
        return points != null ? Optional.ofNullable(points.get(0)) : Optional.empty();
    }

    public Optional<Passage> getNextPassage(String stopPointId, String expected) throws SncfException {
        String URL = String.format(Locale.US, "%s/stop_points/%s/%s?disable_geojson=true&count=1", SERVICE_URL,
                stopPointId, expected);
        List<Passage> passages = getResponseFromCache(URL, Passages.class).passages;
        return passages != null ? passages.size() > 0 ? Optional.ofNullable(passages.get(0)) : Optional.empty()
                : Optional.empty();
    }

    public LocationProvider getLocationProvider() {
        return locationProvider;
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(SncfDiscoveryService.class);
    }
}

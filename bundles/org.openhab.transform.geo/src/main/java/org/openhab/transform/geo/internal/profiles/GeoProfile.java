/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.transform.geo.internal.profiles;

import static org.openhab.transform.geo.internal.GeoConstants.*;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.library.types.PointType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.profiles.ProfileCallback;
import org.openhab.core.thing.profiles.ProfileContext;
import org.openhab.core.thing.profiles.ProfileTypeUID;
import org.openhab.core.thing.profiles.StateProfile;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.util.DurationUtils;
import org.openhab.transform.geo.internal.config.GeoConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Profile for geo coding and reverse geo coding
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class GeoProfile implements StateProfile {
    private final Logger logger = LoggerFactory.getLogger(GeoProfile.class);
    private final ProfileCallback callback;
    private final GeoConfig configuration;
    private final HttpClient httpClient;

    private final ScheduledExecutorService scheduler;
    private ReverseGeocoding lastState;
    private Instant lastResolveTime = Instant.MIN;
    private Duration resolveDuration;
    private String language;
    private @Nullable ScheduledFuture<?> resolverJob;

    public GeoProfile(final ProfileCallback callback, final ProfileContext context, final HttpClient client,
            final LocaleProvider locale) {
        this.callback = callback;
        this.httpClient = client;
        this.scheduler = context.getExecutorService();

        this.configuration = context.getConfiguration().as(GeoConfig.class);
        if (!configuration.language.isBlank()) {
            language = configuration.language;
        } else {
            language = locale.getLocale().getDisplayLanguage() + "-" + locale.getLocale().getCountry();
        }
        try {
            resolveDuration = DurationUtils.parse(configuration.resolveDuration);
        } catch (IllegalArgumentException e) {
            resolveDuration = Duration.ofMinutes(1);
            logger.warn("Could not parse duration '{}', using default of 1 minute.", configuration.resolveDuration);
        }
        logger.debug("GeoProfile created with language: {} and resolve duration: {}", language, resolveDuration);
        lastState = new ReverseGeocoding(PointType.valueOf("0,0"), language, httpClient);
    }

    @Override
    public ProfileTypeUID getProfileTypeUID() {
        return PROFILE_TYPE_UID;
    }

    @Override
    public void onStateUpdateFromItem(State state) {
        processState(state);
    }

    @Override
    public void onStateUpdateFromHandler(State state) {
        processState(state);
    }

    /**
     * Processes the incoming state and schedules reverse geocoding according to configured duration
     *
     * @param location to be resolved
     */
    private void processState(State location) {
        if (location instanceof PointType point) {
            synchronized (this) {
                lastState = new ReverseGeocoding(point, language, httpClient);
                Instant now = Instant.now();
                Instant nextResolveTime = lastResolveTime.plus(resolveDuration);
                if (resolverJob == null) {
                    // no job running so let's check last resolve timestamp
                    if (now.isAfter(nextResolveTime)) {
                        // resolve duration passed, do immediate resolve
                        logger.trace("Resolve now.");
                        resolverJob = scheduler.schedule(this::resolveState, 0, TimeUnit.SECONDS);
                    } else {
                        // schedule resolving for the future
                        long delaySeconds = Duration.between(now, nextResolveTime).toSeconds();
                        logger.trace("Resolve in {} seconds.", delaySeconds);
                        resolverJob = scheduler.schedule(this::resolveState, delaySeconds, TimeUnit.SECONDS);
                    }
                } else {
                    logger.trace("Resolve job already scheduled, skipping new scheduling.");
                }
            }
        }
    }

    /**
     * Callback function of the resolverJob to perform reverse geocoding on the last stored state
     */
    private void resolveState() {
        ReverseGeocoding localLastState;
        synchronized (this) {
            localLastState = lastState;
            lastResolveTime = Instant.now();
            resolverJob = null;
        }
        // do reverse geocoding and double check for success before sending update
        localLastState.doResolve();
        if (localLastState.isResolved()) {
            callback.sendUpdate(StringType.valueOf(localLastState.getAddress()));
        } else {
            logger.debug("Could not resolve address for location: {}", localLastState.toString());
        }
    }

    @Override
    public void onCommandFromItem(Command command) {
        search(command);
    }

    @Override
    public void onCommandFromHandler(Command command) {
        search(command);
    }

    private void search(Command command) {
        if (command instanceof StringType string) {
            try {
                String searchString = URLEncoder.encode(string.toString(), StandardCharsets.UTF_8.toString());
                ContentResponse response = httpClient.newRequest(String.format(SEARCH_URL, searchString))
                        .header("Accept-Language", language).header("User-Agent", "openHAB Geo Transformation Service")
                        .timeout(10, TimeUnit.SECONDS).send();
                if (response.getStatus() == HttpStatus.OK_200) {
                    String jsonResponse = response.getContentAsString();
                    PointType gpsCoordinates = Geocoding.parse(jsonResponse);
                    if (gpsCoordinates != null) {
                        callback.sendCommand(gpsCoordinates);
                    } else {
                        logger.debug("Geo search doesn't provide coordinates {}", jsonResponse);
                    }
                } else {
                    String errorMessage = "HTTP error: " + response.getStatus();
                    logger.debug("Geo search error {}", errorMessage);
                }
            } catch (InterruptedException | TimeoutException | ExecutionException | UnsupportedEncodingException e) {
                String errorMessage = e.getMessage();
                logger.debug("Geo search exception {}", errorMessage);
            }
        } else {
            logger.trace("No possible geo search for command {}", command.toFullString());
        }
    }
}

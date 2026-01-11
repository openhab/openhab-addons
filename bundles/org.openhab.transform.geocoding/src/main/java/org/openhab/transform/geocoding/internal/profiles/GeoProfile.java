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
package org.openhab.transform.geocoding.internal.profiles;

import static org.openhab.transform.geocoding.internal.GeoProfileConstants.GEOCODING_PROFILE_TYPE_UID;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
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
import org.openhab.transform.geocoding.internal.config.GeoProfileConfig;
import org.openhab.transform.geocoding.internal.provider.BaseGeoResolver;
import org.openhab.transform.geocoding.internal.provider.GeoResolverFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link GeoProfile} handles the general profile behavior without knowing the used provider. The provider is
 * evaluated in super class {@link GeoResolverFactory}.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class GeoProfile extends GeoResolverFactory implements StateProfile {
    private final Logger logger = LoggerFactory.getLogger(GeoProfile.class);
    private final ProfileCallback callback;

    private final ScheduledExecutorService scheduler;
    private BaseGeoResolver lastState;
    private Instant lastResolveTime = Instant.MIN;
    private Duration refreshInterval;
    private String language;
    private @Nullable ScheduledFuture<?> resolverJob;

    public GeoProfile(final ProfileCallback callback, final ProfileContext context, final HttpClient client,
            final LocaleProvider locale) {
        super(context.getConfiguration().as(GeoProfileConfig.class), client);
        this.callback = callback;
        this.scheduler = context.getExecutorService();

        if (!configuration.language.isBlank()) {
            language = configuration.language;
        } else {
            language = locale.getLocale().getLanguage() + "-" + locale.getLocale().getCountry();
        }
        try {
            refreshInterval = DurationUtils.parse(configuration.resolveInterval);
            // ensure minimal refresh interval of 1 minute
            if (refreshInterval.getSeconds() < 60) {
                refreshInterval = Duration.ofMinutes(1);
            }
        } catch (IllegalArgumentException e) {
            // fallback to default interval of 5 minutes
            refreshInterval = Duration.ofMinutes(5);
            logger.warn("Could not parse interval '{}', using default interval {}", configuration.resolveInterval,
                    refreshInterval);
        }
        logger.debug("GeoProfile created with language: {} and resolve interval: {}", language, refreshInterval);
        lastState = super.createResolver(PointType.valueOf("0,0"));
    }

    @Override
    public ProfileTypeUID getProfileTypeUID() {
        return GEOCODING_PROFILE_TYPE_UID;
    }

    @Override
    public void onStateUpdateFromItem(State state) {
        // no operation
    }

    @Override
    public void onStateUpdateFromHandler(State state) {
        processState(state);
    }

    /**
     * Processes the incoming state and schedules reverse geocoding according to configured interval
     *
     * @param location to be resolved
     */
    private void processState(State location) {
        if (location instanceof PointType point) {
            synchronized (this) {
                lastState = super.createResolver(point);
                Instant now = Instant.now();
                Instant nextResolveTime = lastResolveTime.plus(refreshInterval);
                if (resolverJob == null) {
                    // no job running so let's check last resolve timestamp
                    if (now.isAfter(nextResolveTime)) {
                        // resolve interval passed, do immediate resolve
                        logger.trace("Resolve now.");
                        resolverJob = scheduler.schedule(this::doResolve, 0, TimeUnit.SECONDS);
                    } else {
                        // schedule resolving for the future
                        long delaySeconds = Duration.between(now, nextResolveTime).toSeconds();
                        logger.trace("Resolve in {} seconds.", delaySeconds);
                        resolverJob = scheduler.schedule(this::doResolve, delaySeconds, TimeUnit.SECONDS);
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
    private void doResolve() {
        BaseGeoResolver localLastState;
        synchronized (this) {
            localLastState = lastState;
            lastResolveTime = Instant.now();
            resolverJob = null;
        }
        // do reverse geocoding and double check for success before sending update
        localLastState.resolve();
        if (localLastState.isResolved()) {
            callback.sendUpdate(StringType.valueOf(localLastState.getResolved()));
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
        // no operation
    }

    /**
     * Perform search for the given command string and take the first relevant result as geo coordinates
     *
     * @param command string with the search query
     */
    private void search(Command command) {
        if (command instanceof StringType string) {
            BaseGeoResolver geoSearch = createResolver(string);
            geoSearch.resolve();
            if (geoSearch.isResolved()) {
                String geoCoordinates = geoSearch.getResolved();
                PointType point = PointType.valueOf(geoCoordinates);
                logger.trace("Send coordinates {} for address {}", point.toFullString(), command.toFullString());
                callback.handleCommand(point);
            } else {
                logger.debug("Geo search could not resolve coordinates for command {}", command.toFullString());
            }
        } else {
            logger.trace("No possible geo search for command {}", command.toFullString());
        }
    }
}

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
package org.openhab.binding.pirateweather.internal.handler;

import static org.openhab.binding.pirateweather.internal.PirateWeatherBindingConstants.*;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.pirateweather.internal.config.PirateWeatherAPIConfiguration;
import org.openhab.binding.pirateweather.internal.connection.PirateWeatherCommunicationException;
import org.openhab.binding.pirateweather.internal.connection.PirateWeatherConfigurationException;
import org.openhab.binding.pirateweather.internal.connection.PirateWeatherConnection;
import org.openhab.binding.pirateweather.internal.dto.PirateWeatherJsonWeatherData;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.library.types.PointType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PirateWeatherAPIHandler} is responsible for accessing the Pirate Weather API.
 *
 * @author Scott Hanson - Initial contribution
 */
@NonNullByDefault
public class PirateWeatherAPIHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(PirateWeatherAPIHandler.class);

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_WEATHER_API);

    private static final long INITIAL_DELAY_IN_SECONDS = 15;

    private @Nullable ScheduledFuture<?> refreshJob;
    private @Nullable PirateWeatherConnection connection;
    private @Nullable PirateWeatherAPIConfiguration config;

    private final HttpClient httpClient;
    private final LocaleProvider localeProvider;

    public PirateWeatherAPIHandler(Bridge bridge, HttpClient httpClient, LocaleProvider localeProvider) {
        super(bridge);
        this.httpClient = httpClient;
        this.localeProvider = localeProvider;
    }

    @Override
    public void initialize() {
        logger.debug("Initialize Pirate Weather API handler '{}'.", getThing().getUID());
        PirateWeatherAPIConfiguration config = this.config = getConfigAs(PirateWeatherAPIConfiguration.class);

        if (config.apikey.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.conf-error-missing-apikey");
            return;
        }
        int refreshInterval = config.refreshInterval;
        if (refreshInterval < 1) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.conf-error-not-supported-refreshInterval");
            return;
        }

        if (config.language.isBlank()) {
            String language = localeProvider.getLocale().getLanguage();
            if (PirateWeatherAPIConfiguration.SUPPORTED_LANGUAGES.contains(language)) {
                logger.debug("Language set to '{}'.", language);
                Configuration editConfig = editConfiguration();
                editConfig.put(CONFIG_LANGUAGE, language);
                updateConfiguration(editConfig);
            } else {
                logger.debug("Language not supported '{}', Using Default Language '{}'.", language, DEFAULT_LANGUAGE);
                Configuration editConfig = editConfiguration();
                editConfig.put(CONFIG_LANGUAGE, DEFAULT_LANGUAGE);
                updateConfiguration(editConfig);
            }
        }

        updateStatus(ThingStatus.UNKNOWN);
        connection = new PirateWeatherConnection(config, httpClient);

        ScheduledFuture<?> localRefreshJob = refreshJob;
        if (localRefreshJob == null || localRefreshJob.isCancelled()) {
            logger.debug("Start refresh job at interval {} min.", refreshInterval);
            refreshJob = scheduler.scheduleWithFixedDelay(this::updateThings, INITIAL_DELAY_IN_SECONDS,
                    TimeUnit.MINUTES.toSeconds(refreshInterval), TimeUnit.SECONDS);
        }
    }

    @Override
    public void dispose() {
        logger.debug("Dispose Pirate Weather API handler '{}'.", getThing().getUID());
        ScheduledFuture<?> localRefreshJob = refreshJob;
        if (localRefreshJob != null && !localRefreshJob.isCancelled()) {
            logger.debug("Stop refresh job.");
            if (localRefreshJob.cancel(true)) {
                refreshJob = null;
            }
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            updateThings();
        } else {
            logger.debug("The Pirate Weather binding is a read-only binding and cannot handle command '{}'.", command);
        }
    }

    @Override
    public void childHandlerInitialized(ThingHandler childHandler, Thing childThing) {
        scheduler.schedule(() -> {
            if (childThing.getHandler() instanceof PirateWeatherWeatherAndForecastHandler pirateChildHandler) {
                pirateChildHandler.updateData();
            }
        }, INITIAL_DELAY_IN_SECONDS, TimeUnit.SECONDS);
    }

    private void updateThings() {
        for (Thing childThing : getThing().getThings()) {
            if (!childThing.isEnabled()) {
                continue;
            }
            if (childThing.getHandler() instanceof PirateWeatherWeatherAndForecastHandler childHandler) {
                childHandler.updateData();
            }
        }
    }

    public PirateWeatherAPIConfiguration getPirateWeatherAPIConfig() {
        PirateWeatherAPIConfiguration cfg = config;
        if (cfg == null) {
            throw new IllegalStateException("Handler not yet initialized");
        }
        return cfg;
    }

    public @Nullable PirateWeatherJsonWeatherData getWeatherData(PointType location)
            throws PirateWeatherCommunicationException, PirateWeatherConfigurationException {
        PirateWeatherConnection connection = this.connection;
        if (connection == null) {
            throw new PirateWeatherCommunicationException("Pirate Weather connection is not initialized");
        }

        try {
            var data = connection.getWeatherData(location);
            updateStatus(ThingStatus.ONLINE);
            return data;
        } catch (PirateWeatherConfigurationException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
            throw e;
        } catch (PirateWeatherCommunicationException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            throw e;
        }
    }
}

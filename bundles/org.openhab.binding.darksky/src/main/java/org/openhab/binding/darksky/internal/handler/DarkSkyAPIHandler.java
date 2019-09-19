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
package org.openhab.binding.darksky.internal.handler;

import static org.openhab.binding.darksky.internal.DarkSkyBindingConstants.*;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.i18n.LocaleProvider;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.darksky.internal.config.DarkSkyAPIConfiguration;
import org.openhab.binding.darksky.internal.connection.DarkSkyConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DarkSkyAPIHandler} is responsible for accessing the Dark Sky API.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public class DarkSkyAPIHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(DarkSkyAPIHandler.class);

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_WEATHER_API);

    private static final long INITIAL_DELAY_IN_SECONDS = 15;

    private @Nullable ScheduledFuture<?> refreshJob;

    private final HttpClient httpClient;
    private final LocaleProvider localeProvider;
    private @NonNullByDefault({}) DarkSkyConnection connection;

    // keeps track of the parsed config
    private @NonNullByDefault({}) DarkSkyAPIConfiguration config;

    public DarkSkyAPIHandler(Bridge bridge, HttpClient httpClient, LocaleProvider localeProvider) {
        super(bridge);
        this.httpClient = httpClient;
        this.localeProvider = localeProvider;
    }

    @Override
    public void initialize() {
        logger.debug("Initialize Dark Sky API handler '{}'.", getThing().getUID());
        config = getConfigAs(DarkSkyAPIConfiguration.class);

        boolean configValid = true;
        if (StringUtils.trimToNull(config.getApikey()) == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.conf-error-missing-apikey");
            configValid = false;
        }
        int refreshInterval = config.getRefreshInterval();
        if (refreshInterval < 10) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.conf-error-not-supported-refreshInterval");
            configValid = false;
        }
        String language = config.getLanguage();
        if (language != null) {
            language = StringUtils.trimToEmpty(language);
            if (!DarkSkyAPIConfiguration.SUPPORTED_LANGUAGES.contains(language.toLowerCase())) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "@text/offline.conf-error-not-supported-language");
                configValid = false;
            }
        } else {
            language = localeProvider.getLocale().getLanguage();
            if (DarkSkyAPIConfiguration.SUPPORTED_LANGUAGES.contains(language)) {
                logger.debug("Language set to '{}'.", language);
                Configuration editConfig = editConfiguration();
                editConfig.put(CONFIG_LANGUAGE, language);
                updateConfiguration(editConfig);
            }
        }

        if (configValid) {
            connection = new DarkSkyConnection(this, httpClient);

            updateStatus(ThingStatus.UNKNOWN);

            if (refreshJob == null || refreshJob.isCancelled()) {
                logger.debug("Start refresh job at interval {} min.", refreshInterval);
                refreshJob = scheduler.scheduleWithFixedDelay(this::updateThings, INITIAL_DELAY_IN_SECONDS,
                        TimeUnit.MINUTES.toSeconds(refreshInterval), TimeUnit.SECONDS);
            }
        }
    }

    @Override
    public void dispose() {
        logger.debug("Dispose Dark Sky API handler '{}'.", getThing().getUID());
        if (refreshJob != null && !refreshJob.isCancelled()) {
            logger.debug("Stop refresh job.");
            if (refreshJob.cancel(true)) {
                refreshJob = null;
            }
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            scheduler.schedule(this::updateThings, INITIAL_DELAY_IN_SECONDS, TimeUnit.SECONDS);
        } else {
            logger.debug("The Dark Sky binding is a read-only binding and cannot handle command '{}'.", command);
        }
    }

    @Override
    public void childHandlerInitialized(ThingHandler childHandler, Thing childThing) {
        scheduler.schedule(() -> {
            updateThing((DarkSkyWeatherAndForecastHandler) childHandler, childThing);
            determineBridgeStatus();
        }, INITIAL_DELAY_IN_SECONDS, TimeUnit.SECONDS);
    }

    @Override
    public void childHandlerDisposed(ThingHandler childHandler, Thing childThing) {
        determineBridgeStatus();
    }

    private void determineBridgeStatus() {
        ThingStatus status = ThingStatus.OFFLINE;
        for (Thing thing : getThing().getThings()) {
            if (ThingStatus.ONLINE.equals(thing.getStatus())) {
                status = ThingStatus.ONLINE;
                break;
            }
        }
        updateStatus(status);
    }

    private void updateThings() {
        ThingStatus status = ThingStatus.OFFLINE;
        for (Thing thing : getThing().getThings()) {
            if (ThingStatus.ONLINE.equals(updateThing((DarkSkyWeatherAndForecastHandler) thing.getHandler(), thing))) {
                status = ThingStatus.ONLINE;
            }
        }
        updateStatus(status);
    }

    private ThingStatus updateThing(@Nullable DarkSkyWeatherAndForecastHandler handler, Thing thing) {
        if (handler != null && connection != null) {
            handler.updateData(connection);
            return thing.getStatus();
        } else {
            logger.debug("Cannot update weather data of thing '{}' as location handler is null.", thing.getUID());
            return ThingStatus.OFFLINE;
        }
    }

    public DarkSkyAPIConfiguration getDarkSkyAPIConfig() {
        return config;
    }
}

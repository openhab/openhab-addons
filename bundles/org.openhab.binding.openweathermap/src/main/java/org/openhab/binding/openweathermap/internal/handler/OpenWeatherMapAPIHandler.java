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
package org.openhab.binding.openweathermap.internal.handler;

import static org.openhab.binding.openweathermap.internal.OpenWeatherMapBindingConstants.*;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.openweathermap.internal.config.OpenWeatherMapAPIConfiguration;
import org.openhab.binding.openweathermap.internal.connection.OpenWeatherMapConnection;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.util.ThingHandlerHelper;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link OpenWeatherMapAPIHandler} is responsible for accessing the OpenWeatherMap API.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public class OpenWeatherMapAPIHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(OpenWeatherMapAPIHandler.class);

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(THING_TYPE_WEATHER_API);

    private static final long INITIAL_DELAY_IN_SECONDS = 15;

    private @Nullable ScheduledFuture<?> refreshJob;

    private final HttpClient httpClient;
    private final LocaleProvider localeProvider;
    private @NonNullByDefault({}) OpenWeatherMapConnection connection;

    // keeps track of the parsed config
    private @NonNullByDefault({}) OpenWeatherMapAPIConfiguration config;

    public OpenWeatherMapAPIHandler(Bridge bridge, HttpClient httpClient, LocaleProvider localeProvider) {
        super(bridge);
        this.httpClient = httpClient;
        this.localeProvider = localeProvider;
    }

    @Override
    public void initialize() {
        logger.debug("Initialize OpenWeatherMap API handler '{}'.", getThing().getUID());
        config = getConfigAs(OpenWeatherMapAPIConfiguration.class);

        boolean configValid = true;
        if (config.apikey.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.conf-error-missing-apikey");
            configValid = false;
        }
        int refreshInterval = config.refreshInterval;
        if (refreshInterval < 1) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.conf-error-not-supported-refreshInterval");
            configValid = false;
        }
        String language = config.language;
        if (language != null && !(language = language.trim()).isEmpty()) {
            if (!OpenWeatherMapAPIConfiguration.SUPPORTED_LANGUAGES.contains(language.toLowerCase())) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "@text/offline.conf-error-not-supported-language");
                configValid = false;
            }
        } else {
            language = localeProvider.getLocale().getLanguage();
            if (OpenWeatherMapAPIConfiguration.SUPPORTED_LANGUAGES.contains(language)) {
                logger.debug("Language set to '{}'.", language);
                Configuration editConfig = editConfiguration();
                editConfig.put(CONFIG_LANGUAGE, language);
                updateConfiguration(editConfig);
            }
        }

        if (configValid) {
            connection = new OpenWeatherMapConnection(this, httpClient);

            updateStatus(ThingStatus.UNKNOWN);

            ScheduledFuture<?> localRefreshJob = refreshJob;
            if (localRefreshJob == null || localRefreshJob.isCancelled()) {
                logger.debug("Start refresh job at interval {} min.", refreshInterval);
                refreshJob = scheduler.scheduleWithFixedDelay(this::updateThings, INITIAL_DELAY_IN_SECONDS,
                        TimeUnit.MINUTES.toSeconds(refreshInterval), TimeUnit.SECONDS);
            }
        }
    }

    @Override
    public void dispose() {
        logger.debug("Dispose OpenWeatherMap API handler '{}'.", getThing().getUID());
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
            scheduler.schedule(this::updateThings, INITIAL_DELAY_IN_SECONDS, TimeUnit.SECONDS);
        } else {
            logger.debug("The OpenWeatherMap binding is a read-only binding and cannot handle command '{}'.", command);
        }
    }

    @Override
    public void childHandlerInitialized(ThingHandler childHandler, Thing childThing) {
        scheduler.schedule(() -> {
            updateThing((AbstractOpenWeatherMapHandler) childHandler, childThing);
            determineBridgeStatus();
        }, INITIAL_DELAY_IN_SECONDS, TimeUnit.SECONDS);
    }

    @Override
    public void childHandlerDisposed(ThingHandler childHandler, Thing childThing) {
        determineBridgeStatus();
    }

    private void determineBridgeStatus() {
        ThingStatus status = ThingStatus.ONLINE;
        List<Thing> childs = getThing().getThings().stream().filter(Thing::isEnabled).collect(Collectors.toList());
        if (!childs.isEmpty()) {
            status = ThingStatus.OFFLINE;
            for (Thing thing : childs) {
                if (ThingStatus.ONLINE.equals(thing.getStatus())) {
                    status = ThingStatus.ONLINE;
                    break;
                }
            }
        }
        updateStatus(status);
    }

    private void updateThings() {
        ThingStatus status = ThingStatus.ONLINE;
        List<Thing> childs = getThing().getThings().stream().filter(Thing::isEnabled).collect(Collectors.toList());
        if (!childs.isEmpty()) {
            status = ThingStatus.OFFLINE;
            for (Thing thing : childs) {
                if (ThingStatus.ONLINE.equals(updateThing((AbstractOpenWeatherMapHandler) thing.getHandler(), thing))) {
                    status = ThingStatus.ONLINE;
                }
            }
        }
        updateStatus(status);
    }

    private ThingStatus updateThing(@Nullable AbstractOpenWeatherMapHandler handler, Thing thing) {
        if (handler != null && ThingHandlerHelper.isHandlerInitialized(handler) && connection != null) {
            handler.updateData(connection);
            return thing.getStatus();
        } else {
            logger.debug("Cannot update weather data of thing '{}' as location handler is null.", thing.getUID());
            return ThingStatus.OFFLINE;
        }
    }

    public OpenWeatherMapAPIConfiguration getOpenWeatherMapAPIConfig() {
        return config;
    }
}

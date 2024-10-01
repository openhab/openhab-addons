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
package org.openhab.binding.radiobrowser.internal;

import static org.openhab.binding.radiobrowser.internal.RadioBrowserBindingConstants.*;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.radiobrowser.internal.api.ApiException;
import org.openhab.binding.radiobrowser.internal.api.RadioBrowserApi;
import org.openhab.binding.radiobrowser.internal.api.RadioBrowserJson.Country;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link RadioBrowserHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Matthew Skinner - Initial contribution
 */
@NonNullByDefault
public class RadioBrowserHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final LocaleProvider localeProvider;
    public final RadioBrowserStateDescriptionProvider stateDescriptionProvider;
    public RadioBrowserConfiguration config = new RadioBrowserConfiguration();
    private RadioBrowserApi radioBrowserApi;
    private @Nullable ScheduledFuture<?> reconnectFuture = null;

    public RadioBrowserHandler(Thing thing, HttpClient httpClient,
            RadioBrowserStateDescriptionProvider stateDescriptionProvider, LocaleProvider localeProvider) {
        super(thing);
        this.localeProvider = localeProvider;
        this.stateDescriptionProvider = stateDescriptionProvider;
        radioBrowserApi = new RadioBrowserApi(this, httpClient);
    }

    public void setChannelState(String channelToUpdate, State valueOf) {
        updateState(channelToUpdate, valueOf);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        try {
            if (command instanceof RefreshType) {
                switch (channelUID.getId()) {
                    case CHANNEL_STATION:
                        radioBrowserApi.updateStations();
                        return;
                }
            } else if (command instanceof StringType) {
                switch (channelUID.getId()) {
                    case CHANNEL_LANGUAGE:
                        radioBrowserApi.setLanguage(command.toString());
                        return;
                    case CHANNEL_COUNTRY:
                        radioBrowserApi.setCountry(command.toString());
                        return;
                    case CHANNEL_STATE:
                        radioBrowserApi.setState(command.toString());
                        return;
                    case CHANNEL_GENRE:
                        radioBrowserApi.setGenre(command.toString());
                        return;
                    case CHANNEL_RECENT:
                    case CHANNEL_STATION:
                        radioBrowserApi.selectStation(command.toString());
                        return;
                }
            }
        } catch (ApiException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            Future<?> future = reconnectFuture;
            if (future == null) {
                // reconnect every 3 mins, but try in 30 seconds time in case its only 1 of 5 servers down.
                reconnectFuture = scheduler.scheduleWithFixedDelay(this::reconnect, 30, 180, TimeUnit.SECONDS);
            }
        }
    }

    private void reconnect() {
        try {
            // Will look up and randomly connect to one of the servers
            radioBrowserApi.initialize();
            updateStatus(ThingStatus.ONLINE);
            updateState(CHANNEL_STATION, new StringType());
            updateState(CHANNEL_STATE, new StringType());
            updateState(CHANNEL_LANGUAGE, new StringType());
            updateState(CHANNEL_GENRE, new StringType());
            String countryCode = localeProvider.getLocale().getCountry();
            Country localCountry = radioBrowserApi.countryMap.get(countryCode);
            if (localCountry != null) {
                updateState(CHANNEL_COUNTRY, new StringType(localCountry.name));
                radioBrowserApi.setCountry(countryCode);
            } else {
                logger.debug(
                        "The binding could not auto discover your country, check openHAB has a country setup in the settings");
            }

            Future<?> future = reconnectFuture;
            if (future != null) {
                future.cancel(false);// don't interrupt as we are running it right now
                reconnectFuture = null;
            }
        } catch (ApiException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    private boolean buildFilters() {
        if (!config.filters.contains("=") || config.filters.startsWith("?") || config.filters.contains(" ")
                || config.filters.startsWith(" ")) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Please update your filters config to the correct key=value,key2=value2 format");
            return false;
        }
        String builtFilters = "";
        List<String> filterList = Arrays.asList(config.filters.split(","));
        for (String filter : filterList) {
            if (builtFilters.isEmpty()) {
                builtFilters = "?" + filter;
            } else {
                builtFilters = builtFilters + "&" + filter;
            }
        }
        // over write the config with fixed copy, existing code keep working
        config.filters = builtFilters;
        return true;
    }

    @Override
    public void initialize() {
        config = getConfigAs(RadioBrowserConfiguration.class);
        updateStatus(ThingStatus.UNKNOWN);
        if (buildFilters()) {
            // First time connecting, try again in 60 seconds to try another random server out of 5? possible ones
            reconnectFuture = scheduler.scheduleWithFixedDelay(this::reconnect, 0, 60, TimeUnit.SECONDS);
        }
    }

    @Override
    public void dispose() {
        Future<?> future = reconnectFuture;
        if (future != null) {
            future.cancel(true);
            reconnectFuture = null;
        }
    }
}

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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.radiobrowser.internal.api.ApiException;
import org.openhab.binding.radiobrowser.internal.api.RadioBrowserApi;
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
    private final HttpClient httpClient;
    public final RadioBrowserStateDescriptionProvider stateDescriptionProvider;
    public RadioBrowserConfiguration config = new RadioBrowserConfiguration();
    private RadioBrowserApi radioBrowserApi;

    public RadioBrowserHandler(Thing thing, HttpClient httpClient,
            RadioBrowserStateDescriptionProvider stateDescriptionProvider) {
        super(thing);
        this.httpClient = httpClient;
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
                    case CHANNEL_COUNTRY:

                        return;
                    case CHANNEL_LANGUAGE:

                        return;
                    case CHANNEL_STATE:

                        return;
                    case CHANNEL_STATION:
                        radioBrowserApi.updateStations();
                        return;
                    case CHANNEL_STREAM:

                        return;
                    case CHANNEL_FAVORITES:

                        return;
                    case CHANNEL_ADD_FAVORITE:

                        return;
                    case CHANNEL_REMOVE_FAVORITE:

                        return;
                }
            } else if (command instanceof StringType) {
                switch (channelUID.getId()) {
                    case CHANNEL_COUNTRY:
                        radioBrowserApi.setCountry(command.toString());
                        return;
                    case CHANNEL_LANGUAGE:
                        radioBrowserApi.setLanguage(command.toString());
                        return;
                    case CHANNEL_STATE:

                        return;
                    case CHANNEL_STATION:
                        radioBrowserApi.selectStation(command.toString());
                        return;
                    case CHANNEL_STREAM:

                        return;
                    case CHANNEL_FAVORITES:

                        return;
                    case CHANNEL_ADD_FAVORITE:

                        return;
                    case CHANNEL_REMOVE_FAVORITE:

                        return;
                }
            }
        } catch (ApiException e) {
            // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    @Override
    public void initialize() {
        config = getConfigAs(RadioBrowserConfiguration.class);
        updateStatus(ThingStatus.UNKNOWN);

        scheduler.execute(() -> {
            try {
                radioBrowserApi.initialize();
                updateStatus(ThingStatus.ONLINE);
                updateState(CHANNEL_LANGUAGE, new StringType());
                updateState(CHANNEL_COUNTRY, new StringType());
                updateState(CHANNEL_STATION, new StringType());
                updateState(CHANNEL_STATE, new StringType());
                updateState(CHANNEL_STREAM, new StringType());
                updateState(CHANNEL_ICON, new StringType());
                updateState(CHANNEL_NAME, new StringType());
            } catch (ApiException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            }
        });
    }

    @Override
    public void dispose() {
    }
}

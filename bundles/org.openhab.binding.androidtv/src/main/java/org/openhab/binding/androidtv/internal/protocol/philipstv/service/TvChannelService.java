/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.androidtv.internal.protocol.philipstv.service;

import static org.openhab.binding.androidtv.internal.protocol.philipstv.ConnectionManager.OBJECT_MAPPER;
import static org.openhab.binding.androidtv.internal.protocol.philipstv.PhilipsTvBindingConstants.CHANNEL_TV_CHANNEL;
import static org.openhab.binding.androidtv.internal.protocol.philipstv.PhilipsTvBindingConstants.GET_AVAILABLE_TV_CHANNEL_LIST_PATH;
import static org.openhab.binding.androidtv.internal.protocol.philipstv.PhilipsTvBindingConstants.TV_CHANNEL_PATH;
import static org.openhab.binding.androidtv.internal.protocol.philipstv.PhilipsTvBindingConstants.TV_NOT_LISTENING_MSG;
import static org.openhab.binding.androidtv.internal.protocol.philipstv.PhilipsTvBindingConstants.TV_OFFLINE_MSG;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.openhab.binding.androidtv.internal.protocol.philipstv.ConnectionManager;
import org.openhab.binding.androidtv.internal.protocol.philipstv.PhilipsTVConnectionManager;
import org.openhab.binding.androidtv.internal.protocol.philipstv.service.api.PhilipsTvService;
import org.openhab.binding.androidtv.internal.protocol.philipstv.service.model.channel.AvailableTvChannelsDto;
import org.openhab.binding.androidtv.internal.protocol.philipstv.service.model.channel.ChannelDto;
import org.openhab.binding.androidtv.internal.protocol.philipstv.service.model.channel.ChannelListDto;
import org.openhab.binding.androidtv.internal.protocol.philipstv.service.model.channel.TvChannelDto;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service for handling commands regarding setting or retrieving the TV channel
 *
 * @author Benjamin Meyer - Initial contribution
 */
public class TvChannelService implements PhilipsTvService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    // Name , ccid of TV Channel
    private Map<String, String> availableTvChannels;

    private final PhilipsTVConnectionManager handler;

    private final ConnectionManager connectionManager;

    public TvChannelService(PhilipsTVConnectionManager handler, ConnectionManager connectionManager) {
        this.handler = handler;
        this.connectionManager = connectionManager;
    }

    @Override
    public void handleCommand(String channel, Command command) {
        try {
            synchronized (this) {
                if (isTvChannelListEmpty()) { // TODO: avoids multiple inits at startup
                    availableTvChannels = getAvailableTvChannelListFromTv();
                    handler.updateChannelStateDescription(CHANNEL_TV_CHANNEL, availableTvChannels.keySet().stream()
                            .collect(Collectors.toMap(Function.identity(), Function.identity())));
                }
            }
            if (command instanceof RefreshType) {
                // Get current tv channel name
                String tvChannelName = getCurrentTvChannel();
                handler.postUpdateChannel(CHANNEL_TV_CHANNEL, new StringType(tvChannelName));
            } else if (command instanceof StringType) {
                if (availableTvChannels.containsKey(command.toString())) {
                    switchTvChannel(command);
                } else {
                    logger.warn(
                            "The given TV Channel with Name: {} couldn't be found in the local Channel List from the TV.",
                            command);
                }
            } else {
                logger.warn("Unknown command: {} for Channel {}", command, channel);
            }
        } catch (Exception e) {
            if (isTvOfflineException(e)) {
                logger.warn("Could not execute command for TV Channels, the TV is offline.");
                handler.postUpdateThing(ThingStatus.OFFLINE, ThingStatusDetail.NONE, TV_OFFLINE_MSG);
            } else if (isTvNotListeningException(e)) {
                handler.postUpdateThing(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        TV_NOT_LISTENING_MSG);
            } else {
                logger.warn("Error occurred during handling of command for TV Channels: {}", e.getMessage(), e);
            }
        }
    }

    private boolean isTvChannelListEmpty() {
        return (availableTvChannels == null) || availableTvChannels.isEmpty();
    }

    private Map<String, String> getAvailableTvChannelListFromTv() throws IOException {
        AvailableTvChannelsDto availableTvChannelsDto = OBJECT_MAPPER.readValue(
                connectionManager.doHttpsGet(GET_AVAILABLE_TV_CHANNEL_LIST_PATH), AvailableTvChannelsDto.class);

        ConcurrentMap<String, String> tvChannelsMap = availableTvChannelsDto.getChannel().stream()
                .collect(Collectors.toConcurrentMap(ChannelDto::getName, ChannelDto::getCcid, (c1, c2) -> c1));

        logger.debug("TV Channels added: {}", tvChannelsMap.size());
        if (logger.isTraceEnabled()) {
            tvChannelsMap.keySet().forEach(app -> logger.trace("TV Channel found: {}", app));
        }
        return tvChannelsMap;
    }

    private String getCurrentTvChannel() throws IOException {
        TvChannelDto tvChannelDto = OBJECT_MAPPER.readValue(connectionManager.doHttpsGet(TV_CHANNEL_PATH),
                TvChannelDto.class);
        return Optional.ofNullable(tvChannelDto.getChannel()).map(ChannelDto::getName).orElse("NA");
    }

    private void switchTvChannel(Command command) throws IOException {
        TvChannelDto tvChannelDto = new TvChannelDto();

        ChannelDto channelDto = new ChannelDto();
        channelDto.setCcid(availableTvChannels.get(command.toString()));
        tvChannelDto.setChannel(channelDto);

        ChannelListDto channelListDto = new ChannelListDto();
        channelListDto.setId("allter");
        channelListDto.setVersion("30");
        tvChannelDto.setChannelList(channelListDto);

        String switchTvChannelJson = OBJECT_MAPPER.writeValueAsString(tvChannelDto);
        logger.debug("Switch TV Channel json: {}", switchTvChannelJson);
        connectionManager.doHttpsPost(TV_CHANNEL_PATH, switchTvChannelJson);
    }

    public void clearAvailableTvChannelList() {
        if (availableTvChannels != null) {
            availableTvChannels.clear();
        }
    }
}

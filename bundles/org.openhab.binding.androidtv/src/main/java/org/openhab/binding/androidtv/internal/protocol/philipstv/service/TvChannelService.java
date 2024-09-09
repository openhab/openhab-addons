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
package org.openhab.binding.androidtv.internal.protocol.philipstv.service;

import static org.openhab.binding.androidtv.internal.AndroidTVBindingConstants.CHANNEL_TV_CHANNEL;
import static org.openhab.binding.androidtv.internal.protocol.philipstv.ConnectionManager.OBJECT_MAPPER;
import static org.openhab.binding.androidtv.internal.protocol.philipstv.PhilipsTVBindingConstants.*;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.androidtv.internal.protocol.philipstv.ConnectionManager;
import org.openhab.binding.androidtv.internal.protocol.philipstv.PhilipsTVConnectionManager;
import org.openhab.binding.androidtv.internal.protocol.philipstv.service.api.PhilipsTVService;
import org.openhab.binding.androidtv.internal.protocol.philipstv.service.model.channel.AvailableTvChannelsDTO;
import org.openhab.binding.androidtv.internal.protocol.philipstv.service.model.channel.ChannelDTO;
import org.openhab.binding.androidtv.internal.protocol.philipstv.service.model.channel.ChannelListDTO;
import org.openhab.binding.androidtv.internal.protocol.philipstv.service.model.channel.TvChannelDTO;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;

/**
 * Service for handling commands regarding setting or retrieving the TV channel
 *
 * @author Benjamin Meyer - Initial contribution
 * @author Ben Rosenblum - Merged into AndroidTV
 */
@NonNullByDefault
public class TvChannelService implements PhilipsTVService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    // Name , ccid of TV Channel
    private @Nullable Map<String, String> availableTvChannels;

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
                if (isTvChannelListEmpty()) {
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
        try {
            AvailableTvChannelsDTO availableTvChannelsDTO = OBJECT_MAPPER.readValue(
                    connectionManager.doHttpsGet(GET_AVAILABLE_TV_CHANNEL_LIST_PATH), AvailableTvChannelsDTO.class);

            ConcurrentMap<String, String> tvChannelsMap = availableTvChannelsDTO.getChannel().stream()
                    .collect(Collectors.toConcurrentMap(ChannelDTO::getName, ChannelDTO::getCcid, (c1, c2) -> c1));

            logger.debug("TV Channels added: {}", tvChannelsMap.size());
            if (logger.isTraceEnabled()) {
                tvChannelsMap.keySet().forEach(app -> logger.trace("TV Channel found: {}", app));
            }
            return tvChannelsMap;
        } catch (InvalidFormatException e) {
            logger.debug("TV Channels loading failed: {}", e.getMessage(), e);
        }
        return Collections.emptyMap();
    }

    private String getCurrentTvChannel() throws IOException {
        TvChannelDTO tvChannelDTO = OBJECT_MAPPER.readValue(connectionManager.doHttpsGet(TV_CHANNEL_PATH),
                TvChannelDTO.class);
        ChannelDTO channel = tvChannelDTO.getChannel();
        return channel != null ? channel.getName() : "NA";
    }

    private void switchTvChannel(Command command) throws IOException {
        ChannelDTO channelDTO = new ChannelDTO();
        channelDTO.setCcid(availableTvChannels.get(command.toString()));

        ChannelListDTO channelListDTO = new ChannelListDTO();
        channelListDTO.setId("allter");
        channelListDTO.setVersion("30");

        TvChannelDTO tvChannelDTO = new TvChannelDTO(channelDTO, channelListDTO);
        String switchTvChannelJson = OBJECT_MAPPER.writeValueAsString(tvChannelDTO);
        logger.debug("Switch TV Channel json: {}", switchTvChannelJson);
        connectionManager.doHttpsPost(TV_CHANNEL_PATH, switchTvChannelJson);
    }

    public void clearAvailableTvChannelList() {
        if (availableTvChannels != null) {
            availableTvChannels.clear();
        }
    }
}

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

import static org.openhab.binding.androidtv.internal.AndroidTVBindingConstants.*;
import static org.openhab.binding.androidtv.internal.protocol.philipstv.ConnectionManager.OBJECT_MAPPER;
import static org.openhab.binding.androidtv.internal.protocol.philipstv.PhilipsTVBindingConstants.*;
import static org.openhab.binding.androidtv.internal.protocol.philipstv.service.KeyPress.KEY_MUTE;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.androidtv.internal.protocol.philipstv.ConnectionManager;
import org.openhab.binding.androidtv.internal.protocol.philipstv.PhilipsTVConnectionManager;
import org.openhab.binding.androidtv.internal.protocol.philipstv.service.api.PhilipsTVService;
import org.openhab.binding.androidtv.internal.protocol.philipstv.service.model.keypress.KeyPressDTO;
import org.openhab.binding.androidtv.internal.protocol.philipstv.service.model.volume.VolumeDTO;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link VolumeService} is responsible for handling volume commands, which are sent to the
 * volume channel or mute channel.
 *
 * @author Benjamin Meyer - Initial contribution
 * @author Ben Rosenblum - Merged into AndroidTV
 */
@NonNullByDefault
public class VolumeService implements PhilipsTVService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final PhilipsTVConnectionManager handler;

    private final ConnectionManager connectionManager;

    public VolumeService(PhilipsTVConnectionManager handler, ConnectionManager connectionManager) {
        this.handler = handler;
        this.connectionManager = connectionManager;
    }

    @Override
    public void handleCommand(String channel, Command command) {
        try {
            if (command instanceof RefreshType) {
                VolumeDTO volumeDTO = getVolume();
                handler.postUpdateChannel(CHANNEL_VOLUME, new PercentType(volumeDTO.getCurrentVolume()));
                handler.postUpdateChannel(CHANNEL_MUTE, volumeDTO.isMuted() ? OnOffType.ON : OnOffType.OFF);
            } else if (CHANNEL_VOLUME.equals(channel) && command instanceof PercentType) {
                setVolume((PercentType) command);
                handler.postUpdateChannel(CHANNEL_VOLUME, (PercentType) command);
            } else if (CHANNEL_MUTE.equals(channel) && command instanceof OnOffType) {
                setMute();
            } else {
                logger.warn("Unknown command: {} for Channel {}", command, channel);
            }
        } catch (Exception e) {
            if (isTvOfflineException(e)) {
                handler.postUpdateThing(ThingStatus.OFFLINE, ThingStatusDetail.NONE, TV_OFFLINE_MSG);
            } else if (isTvNotListeningException(e)) {
                handler.postUpdateThing(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        TV_NOT_LISTENING_MSG);
            } else {
                logger.warn("Error during handling the VolumeService command {} for Channel {}: {}", command, channel,
                        e.getMessage(), e);
            }
        }
    }

    private VolumeDTO getVolume() throws IOException {
        String jsonContent = connectionManager.doHttpsGet(VOLUME_PATH);
        return OBJECT_MAPPER.readValue(jsonContent, VolumeDTO.class);
    }

    private void setVolume(PercentType volumeToSet) throws IOException {
        VolumeDTO volumeDTO = new VolumeDTO();
        volumeDTO.setMuted(false);
        volumeDTO.setCurrentVolume(volumeToSet.intValue());
        String volumeJson = OBJECT_MAPPER.writeValueAsString(volumeDTO);
        logger.debug("Set json volume: {}", volumeJson);
        connectionManager.doHttpsPost(VOLUME_PATH, volumeJson);
    }

    private void setMute() throws IOException {
        // We just sent the KEY_MUTE and dont bother what was actually requested
        KeyPressDTO keyPressDTO = new KeyPressDTO(KEY_MUTE);
        String muteJson = OBJECT_MAPPER.writeValueAsString(keyPressDTO);
        logger.debug("Set json mute state: {}", muteJson);
        connectionManager.doHttpsPost(KEY_CODE_PATH, muteJson);
    }
}

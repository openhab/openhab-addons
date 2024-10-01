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

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.androidtv.internal.protocol.philipstv.ConnectionManager;
import org.openhab.binding.androidtv.internal.protocol.philipstv.PhilipsTVConnectionManager;
import org.openhab.binding.androidtv.internal.protocol.philipstv.service.api.PhilipsTVService;
import org.openhab.binding.androidtv.internal.protocol.philipstv.service.model.TvSettingsUpdateDTO;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service for handling commands regarding the TV picture settings
 *
 * @author Benjamin Meyer - Initial contribution
 * @author Ben Rosenblum - Merged into AndroidTV
 */
@NonNullByDefault
public class TvPictureService implements PhilipsTVService {

    private static final int SHARPNESS_NODE_ID = 2131230851;
    private static final int CONTRAST_NODE_ID = 2131230850;
    private static final int BRIGHTNESS_NODE_ID = 2131230852;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final PhilipsTVConnectionManager handler;

    private final ConnectionManager connectionManager;

    public TvPictureService(PhilipsTVConnectionManager handler, ConnectionManager connectionManager) {
        this.handler = handler;
        this.connectionManager = connectionManager;
    }

    @Override
    public void handleCommand(String channel, Command command) {
        try {
            if (CHANNEL_BRIGHTNESS.equals(channel) && command instanceof PercentType) {
                setBrightness(((PercentType) command).intValue());
            } else if (CHANNEL_BRIGHTNESS.equals(channel) && command instanceof RefreshType) {
                int currentBrightness = getBrightness();
                handler.postUpdateChannel(CHANNEL_BRIGHTNESS, new PercentType(currentBrightness));
            } else if (CHANNEL_CONTRAST.equals(channel) && command instanceof PercentType) {
                setContrast(((PercentType) command).intValue());
            } else if (CHANNEL_CONTRAST.equals(channel) && command instanceof RefreshType) {
                int currentContrast = getContrast();
                handler.postUpdateChannel(CHANNEL_CONTRAST, new PercentType(currentContrast));
            } else if (CHANNEL_SHARPNESS.equals(channel) && command instanceof PercentType) {
                setSharpness(((PercentType) command).intValue());
            } else if (CHANNEL_SHARPNESS.equals(channel) && command instanceof RefreshType) {
                int currentSharpness = getSharpness();
                handler.postUpdateChannel(CHANNEL_SHARPNESS, new PercentType(currentSharpness));
            } else {
                if (!(command instanceof RefreshType)) {
                    logger.warn("Unknown command: {} for Channel {}", command, channel);
                }
            }
        } catch (Exception e) {
            if (isTvOfflineException(e)) {
                handler.postUpdateThing(ThingStatus.OFFLINE, ThingStatusDetail.NONE, TV_OFFLINE_MSG);
            } else if (isTvNotListeningException(e)) {
                handler.postUpdateThing(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        TV_NOT_LISTENING_MSG);
            } else {
                logger.warn("Error during handling the TvPicture command {} for Channel {}: {}", command, channel,
                        e.getMessage(), e);
            }
        }
    }

    private int getBrightness() throws IOException {
        String getBrightnessJson = ServiceUtil.createTvSettingsRetrievalJson(BRIGHTNESS_NODE_ID);
        logger.debug("Post Tv Picture retrieval brightness json: {}", getBrightnessJson);
        return (int) OBJECT_MAPPER.readValue(connectionManager.doHttpsPost(CURRENT_SETTINGS_PATH, getBrightnessJson),
                TvSettingsUpdateDTO.class).getValues().get(0).getValue().getData().getValue();
    }

    private void setBrightness(int brightness) throws IOException {
        String tvPictureBrightnessJson = ServiceUtil.createTvSettingsUpdateJson(BRIGHTNESS_NODE_ID, brightness);
        logger.debug("Post Tv Picture brightness json: {}", tvPictureBrightnessJson);
        connectionManager.doHttpsPost(UPDATE_SETTINGS_PATH, tvPictureBrightnessJson);
    }

    private int getContrast() throws IOException {
        String getContrastJson = ServiceUtil.createTvSettingsRetrievalJson(CONTRAST_NODE_ID);
        logger.debug("Post Tv Picture retrieval contrast json: {}", getContrastJson);
        return (int) OBJECT_MAPPER.readValue(connectionManager.doHttpsPost(CURRENT_SETTINGS_PATH, getContrastJson),
                TvSettingsUpdateDTO.class).getValues().get(0).getValue().getData().getValue();
    }

    private void setContrast(int contrast) throws IOException {
        String tvPictureContrastJson = ServiceUtil.createTvSettingsUpdateJson(CONTRAST_NODE_ID, contrast);
        logger.debug("Post Tv Picture contrast json: {}", tvPictureContrastJson);
        connectionManager.doHttpsPost(UPDATE_SETTINGS_PATH, tvPictureContrastJson);
    }

    private int getSharpness() throws IOException {
        String getSharpnessJson = ServiceUtil.createTvSettingsRetrievalJson(SHARPNESS_NODE_ID);
        logger.debug("Post Tv Picture retrieval sharpness json: {}", getSharpnessJson);
        return (int) OBJECT_MAPPER.readValue(connectionManager.doHttpsPost(CURRENT_SETTINGS_PATH, getSharpnessJson),
                TvSettingsUpdateDTO.class).getValues().get(0).getValue().getData().getValue();
    }

    private void setSharpness(int sharpness) throws IOException {
        String tvPictureSharpnessJson = ServiceUtil.createTvSettingsUpdateJson(SHARPNESS_NODE_ID, sharpness / 10);
        logger.debug("Post Tv Picture brightness json: {}", tvPictureSharpnessJson);
        connectionManager.doHttpsPost(UPDATE_SETTINGS_PATH, tvPictureSharpnessJson);
    }
}

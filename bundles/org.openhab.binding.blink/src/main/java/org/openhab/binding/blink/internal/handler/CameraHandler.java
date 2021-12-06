/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
 * <p>
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 * <p>
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 * <p>
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.blink.internal.handler;

import java.io.IOException;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.blink.internal.config.CameraConfiguration;
import org.openhab.binding.blink.internal.service.CameraService;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.RawType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.Gson;

import static org.openhab.binding.blink.internal.BlinkBindingConstants.*;

/**
 * The {@link CameraHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Matthias Oesterheld - Initial contribution
 */
@NonNullByDefault
public class CameraHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(CameraHandler.class);

    @NonNullByDefault({}) CameraConfiguration config;
    CameraService cameraService;

    public CameraHandler(Thing thing, HttpClientFactory httpClientFactory, Gson gson) {
        super(thing);
        this.cameraService = new CameraService(httpClientFactory.getCommonHttpClient(), gson);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        try {
            if (getBridge() == null || getBridge().getHandler() == null) {
                logger.warn("Cannot handle commands of blink things without a bridge: {}",
                        thing.getUID().getAsString());
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "no bridge");
                return;
            }
            AccountHandler accountHandler = (AccountHandler) getBridge().getHandler();
            if (CHANNEL_CAMERA_TEMPERATURE.equals(channelUID.getId())) {
                if (command instanceof RefreshType) {
                    double temp = accountHandler.getTemperature(config);
                    updateState(CHANNEL_CAMERA_TEMPERATURE, new DecimalType(temp));
                }
            } else if (CHANNEL_CAMERA_BATTERY.equals(channelUID.getId())) {
                if (command instanceof RefreshType) {
                    updateState(CHANNEL_CAMERA_BATTERY, accountHandler.getBattery(config));
                }
            } else if (CHANNEL_CAMERA_MOTIONDETECTION.equals(channelUID.getId())) {
                if (command instanceof RefreshType) {
                    updateState(CHANNEL_CAMERA_MOTIONDETECTION, accountHandler.getMotionDetection(config, false));
                } else if (command instanceof OnOffType) {
                    OnOffType cmd = (OnOffType) command;
                    boolean enable = (cmd == OnOffType.ON);
                    cameraService.motionDetection(accountHandler.getBlinkAccount(), config, enable);
                    // enable/disable is an async command in the api, changes might not be reflected in updateState
                    updateState(CHANNEL_CAMERA_MOTIONDETECTION, cmd);
                }
            } else if (CHANNEL_CAMERA_SETTHUMBNAIL.equals(channelUID.getId())) {
                if (command instanceof RefreshType)
                    updateState(CHANNEL_CAMERA_SETTHUMBNAIL, OnOffType.OFF);
                if (command instanceof OnOffType && command == OnOffType.ON) {
                    cameraService.createThumbnail(accountHandler.getBlinkAccount(), config);
                    updateState(CHANNEL_CAMERA_SETTHUMBNAIL, OnOffType.OFF);
                }
            } else if (CHANNEL_CAMERA_GETTHUMBNAIL.equals(channelUID.getId())) {
                if (command instanceof RefreshType) {
                    String imagePath = accountHandler.getCameraState(config, true).thumbnail;
                    updateState(CHANNEL_CAMERA_GETTHUMBNAIL,
                            new RawType(cameraService.getThumbnail(accountHandler.getBlinkAccount(), imagePath),
                                    "image/jpeg"));
                }
            }
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    @Override
    public void initialize() {
        config = getConfigAs(CameraConfiguration.class);
        updateStatus(ThingStatus.ONLINE);
    }
}

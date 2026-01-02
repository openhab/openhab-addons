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
package org.openhab.binding.blink.internal.handler;

import static org.openhab.binding.blink.internal.BlinkBindingConstants.*;

import java.io.IOException;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.blink.internal.config.CameraConfiguration;
import org.openhab.binding.blink.internal.dto.BlinkEvents;
import org.openhab.binding.blink.internal.service.CameraService;
import org.openhab.binding.blink.internal.servlet.ThumbnailServlet;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.RawType;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.net.NetworkAddressService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.osgi.service.http.HttpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link CameraHandler} is responsible for initializing camera thing and handling commands, which are
 * sent to one of the camera's channels.
 *
 * @author Matthias Oesterheld - Initial contribution
 * @author Robert T. Brown (-rb) - support Blink Authentication changes in 2025 (OAUTHv2)
 * @author Volker Bier - add support for Doorbells
 *
 */
@NonNullByDefault
public class CameraHandler extends BaseThingHandler implements EventListener {

    private final Logger logger = LoggerFactory.getLogger(CameraHandler.class);

    @NonNullByDefault({})
    CameraConfiguration config;
    @NonNullByDefault({})
    AccountHandler accountHandler;
    private final HttpService httpService;
    private final NetworkAddressService networkAddressService;
    CameraService cameraService;

    @Nullable
    ThumbnailServlet thumbnailServlet;

    String lastThumbnailPath = "";

    public CameraHandler(Thing thing, HttpService httpService, NetworkAddressService networkAddressService,
            HttpClientFactory httpClientFactory, Gson gson) {
        super(thing);
        this.httpService = httpService;
        this.networkAddressService = networkAddressService;
        this.cameraService = new CameraService(httpClientFactory.getCommonHttpClient(), gson);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Handling command {} {} for camera {}", channelUID.getId(), command.toFullString(),
                thing.getUID().getAsString());
        try {
            if (CHANNEL_CAMERA_TEMPERATURE.equals(channelUID.getId())) {
                if (command instanceof RefreshType) {
                    double temp = accountHandler.getTemperature(config);
                    updateState(CHANNEL_CAMERA_TEMPERATURE, new QuantityType<>(temp, ImperialUnits.FAHRENHEIT));
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
                    if (config.cameraType == CameraConfiguration.CameraType.CAMERA) {
                        Long cmdId = cameraService.motionDetection(accountHandler.getBlinkAccount(), config, enable);
                        cameraService.watchCommandStatus(scheduler, accountHandler.getBlinkAccount(), config.networkId,
                                cmdId, this::asyncCommandFinished);
                    } else if (config.cameraType == CameraConfiguration.CameraType.DOORBELL) {
                        String result = cameraService.motionDetectionDoorbell(accountHandler.getBlinkAccount(), config,
                                enable);
                        logger.debug("Returned from doorbell arm/disarm: {}", result); // {"id":200445162,"network_id":5xx2,"state":"done"}

                    } else {
                        String result = cameraService.motionDetectionOwl(accountHandler.getBlinkAccount(), config,
                                enable);
                        logger.debug("Returned from owl arm/disarm: {}", result); // {"id":200445162,"network_id":5xx2,"state":"done"}
                    }
                }
            } else if (CHANNEL_CAMERA_SETTHUMBNAIL.equals(channelUID.getId())) {
                if (command instanceof RefreshType) {
                    updateState(CHANNEL_CAMERA_SETTHUMBNAIL, OnOffType.OFF);
                } else if (command == OnOffType.ON) {
                    if (config.cameraType == CameraConfiguration.CameraType.CAMERA) {
                        Long cmdId = cameraService.createThumbnail(accountHandler.getBlinkAccount(), config);
                        cameraService.watchCommandStatus(scheduler, accountHandler.getBlinkAccount(), config.networkId,
                                cmdId, this::setThumbnailFinished);
                    }
                    if (config.cameraType == CameraConfiguration.CameraType.DOORBELL) {
                        String result = cameraService.createThumbnailDoorbell(accountHandler.getBlinkAccount(), config);
                        setThumbnailFinished(true);
                        logger.debug("Returned from doorbell createThumbnail: {}", result); // {"id":200445162,"network_id":5xx2,"command":"thumbnail","state":"new"}
                    } else {
                        String result = cameraService.createThumbnailOwl(accountHandler.getBlinkAccount(), config);
                        setThumbnailFinished(true);
                        logger.debug("Returned from owl createThumbnail: {}", result); // {"id":200445162,"network_id":5xx2,"command":"thumbnail","state":"new"}
                    }
                }
            } else if (CHANNEL_CAMERA_GETTHUMBNAIL.equals(channelUID.getId())) {
                if (command instanceof RefreshType) {
                    String imagePath = accountHandler.getCameraState(config, false).thumbnail;
                    lastThumbnailPath = imagePath;
                    updateState(CHANNEL_CAMERA_GETTHUMBNAIL, new RawType(
                            cameraService.getThumbnail(accountHandler.getBlinkAccount(), imagePath), "image/jpeg"));
                }
            }
        } catch (IOException e) {
            lastThumbnailPath = "";
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Command Failed");
        }
    }

    public byte[] getThumbnail() throws IOException {
        String imagePath = accountHandler.getCameraState(config, true).thumbnail;
        return cameraService.getThumbnail(accountHandler.getBlinkAccount(), imagePath);
    }

    @Override
    public void initialize() {
        Configuration configuration = editConfiguration();
        if (!configuration.containsKey(PROPERTY_CAMERA_TYPE)) {
            logger.debug("CameraConfiguration: there is no camera type, adding 'CAMERA'.  uhhhh, why???");
            configuration.put(PROPERTY_CAMERA_TYPE, CameraConfiguration.CameraType.CAMERA);
            updateConfiguration(configuration);
        }
        config = getConfigAs(CameraConfiguration.class);
        logger.debug("Initializing camera {}", thing.getUID().getAsString());
        logger.debug("Read camera configuration {}", config);

        @Nullable
        Bridge bridge = getBridge();
        if (bridge == null || bridge.getHandler() == null) {
            logger.warn("Cannot handle commands of blink things without a bridge: {}", thing.getUID().getAsString());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "no bridge");
            return;
        }
        accountHandler = (AccountHandler) bridge.getHandler();

        if (thumbnailServlet == null) {
            try {
                logger.debug("Registering thumbnail servlet");
                thumbnailServlet = new ThumbnailServlet(httpService, this);
                Map<String, String> properties = editProperties();
                String scheme = "http://";
                int port = 8080;
                String imageUrl = scheme + networkAddressService.getPrimaryIpv4HostAddress() + ":" + port
                        + "/blink/thumbnail/" + thing.getUID().getId();
                properties.put("thumbnail", imageUrl);
                logger.debug("Registered thumbnail servlet at {}", imageUrl);
                updateProperties(properties);
            } catch (IllegalStateException e) {
                logger.warn("Failed to create thumbnail servlet", e);
            }
        }

        if (config.cameraType.equals(CameraConfiguration.CameraType.DOORBELL)) {
            logger.debug("Removing unapplicable channels for doorbell");
            ThingBuilder thingBuilder = editThing();
            thingBuilder.withoutChannel(new ChannelUID(this.thing.getUID(), CHANNEL_CAMERA_TEMPERATURE));
            updateThing(thingBuilder.build());
        }
        if (config.cameraType.equals(CameraConfiguration.CameraType.OWL)) {
            logger.debug("Removing unapplicable channels for owl");
            ThingBuilder thingBuilder = editThing();
            thingBuilder.withoutChannel(new ChannelUID(this.thing.getUID(), CHANNEL_CAMERA_BATTERY));
            thingBuilder.withoutChannel(new ChannelUID(this.thing.getUID(), CHANNEL_CAMERA_TEMPERATURE));
            updateThing(thingBuilder.build());
        }

        // set the status to UNKNOWN temporarily and let the background refresh task decide the real status.
        updateStatus(ThingStatus.UNKNOWN);
    }

    @Override
    public void handleHomescreenUpdate() {
        if (config == null) {
            return;
        }
        logger.trace("Camera {} checking for state updates", config.cameraId);
        try {
            if (config.cameraType == CameraConfiguration.CameraType.CAMERA) {
                updateState(CHANNEL_CAMERA_TEMPERATURE,
                        new QuantityType<>(accountHandler.getTemperature(config), ImperialUnits.FAHRENHEIT));
                updateState(CHANNEL_CAMERA_BATTERY, accountHandler.getBattery(config));
            } else if (config.cameraType == CameraConfiguration.CameraType.DOORBELL) {
                updateState(CHANNEL_CAMERA_BATTERY, accountHandler.getBattery(config));
            }
            updateState(CHANNEL_CAMERA_MOTIONDETECTION, accountHandler.getMotionDetection(config, false));
            String imagePath = accountHandler.getCameraState(config, false).thumbnail;
            if (!lastThumbnailPath.equals(imagePath)) {
                logger.debug("Loading NEW thumbnail during refresh of camera {} ({})", thing.getLabel(),
                        config.cameraId);
                lastThumbnailPath = imagePath;
                updateState(CHANNEL_CAMERA_GETTHUMBNAIL, new RawType(
                        cameraService.getThumbnail(accountHandler.getBlinkAccount(), imagePath), "image/jpeg"));
            }
            updateStatus(ThingStatus.ONLINE);
        } catch (IOException e) {
            lastThumbnailPath = "";
            // if the camera can't be updated, then set the camera itself offline.
            // One scenario why this happens is if the user deletes a camera in the Blink App--we still
            // have a CameraThing, but it can no longer be found in the "homescreen" list of legit cameras.
            // It also appears that cameras with dead batteries are no longer in Blink's list via the API,
            // although the Blink app shows dead cameras as "offline, click here to troubleshoot".
            // We will do the same--show it as OFFLINE (but we don't offer to help troubleshoot).
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Camera state could not be updated (e.g. battery dead / unplugged / deleted)");
        }
    }

    @Override
    public void handleMediaEvent(BlinkEvents.Media mediaEvent) {
        if (mediaEvent.isNewEvent()) {
            if (mediaEvent.isCamera(config)) {
                logger.debug("Triggering motion event for camera {}", config.cameraId);
                logger.debug("Thumbnail from the motion event is at; {}", mediaEvent.thumbnail);
                triggerChannel(CHANNEL_CAMERA_MOTIONDETECTION);
            } else {
                logger.debug("Media event for a non-camera? event: {}", mediaEvent);
            }
        }
    }

    @Override
    public void dispose() {
        logger.debug("Disposing camera service");
        cameraService.dispose();
        logger.debug("Trying to dispose thumbnail servlet");
        disposeServlet();
        super.dispose();
    }

    private void disposeServlet() {
        ThumbnailServlet servlet = this.thumbnailServlet;
        if (servlet != null) {
            logger.debug("Disposing thumbnail servlet");
            servlet.dispose();
        }
        this.thumbnailServlet = null;
    }

    private void setThumbnailFinished(boolean success) {
        updateState(CHANNEL_CAMERA_SETTHUMBNAIL, OnOffType.OFF);
        asyncCommandFinished(success);
    }

    private void asyncCommandFinished(boolean success) {
        if (success) {
            accountHandler.getDevices(true); // trigger refresh of homescreen
        }
    }
}

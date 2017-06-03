/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.vera.handler;

import static org.openhab.binding.vera.VeraBindingConstants.*;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder;
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.vera.config.VeraSceneConfiguration;
import org.openhab.binding.vera.controller.json.Scene;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link VeraSceneHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Dmitriy Ponomarev
 */
public class VeraSceneHandler extends BaseThingHandler {
    private Logger logger = LoggerFactory.getLogger(getClass());

    private VeraSceneConfiguration mConfig;

    public VeraSceneHandler(Thing thing) {
        super(thing);
    }

    private class Initializer implements Runnable {
        @Override
        public void run() {
            try {
                VeraBridgeHandler veraBridgeHandler = getVeraBridgeHandler();
                if (veraBridgeHandler != null && veraBridgeHandler.getThing().getStatus().equals(ThingStatus.ONLINE)) {
                    ThingStatusInfo statusInfo = veraBridgeHandler.getThing().getStatusInfo();
                    logger.debug("Change scene status to bridge status: {}", statusInfo);

                    // Set thing status to bridge status
                    updateStatus(statusInfo.getStatus(), statusInfo.getStatusDetail(), statusInfo.getDescription());

                    logger.debug("Add channels");
                    Scene scene = veraBridgeHandler.getController().getScene(mConfig.getSceneId());
                    if (scene != null) {
                        logger.debug("Found {} scene", scene.getName());
                        updateLabelAndLocation(scene.getName(), scene.getRoomName());
                        addSceneAsChannel(scene);
                    }
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR,
                            "Controller is not online");
                }
            } catch (Exception e) {
                logger.error("Error occurred when adding scene as channel: {}", e);
                if (getThing().getStatus() == ThingStatus.ONLINE) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR,
                            "Error occurred when adding scene as channel: " + e.getMessage());
                }
            }
        }
    };

    protected synchronized VeraBridgeHandler getVeraBridgeHandler() {
        Bridge bridge = getBridge();
        if (bridge == null) {
            return null;
        }
        ThingHandler handler = bridge.getHandler();
        if (handler instanceof VeraBridgeHandler) {
            return (VeraBridgeHandler) handler;
        } else {
            return null;
        }
    }

    private VeraSceneConfiguration loadAndCheckConfiguration() {
        VeraSceneConfiguration config = getConfigAs(VeraSceneConfiguration.class);
        if (config.getSceneId() == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Couldn't create scene, sceneId is missing.");
            return null;
        }
        return config;
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Vera scene handler ...");
        updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.CONFIGURATION_PENDING,
                "Checking configuration and bridge...");
        mConfig = loadAndCheckConfiguration();
        if (mConfig != null) {
            logger.debug("Configuration complete: {}", mConfig);
            scheduler.schedule(new Initializer(), 2, TimeUnit.SECONDS);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "SceneId required!");
        }
    }

    @Override
    public void dispose() {
        logger.debug("Dispose Vera scene handler ...");
        if (mConfig.getSceneId() != null) {
            mConfig.setSceneId(null);
        }
        super.dispose();
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        // Only called if status ONLINE or OFFLINE
        logger.debug("Vera bridge status changed: {}", bridgeStatusInfo);

        if (bridgeStatusInfo.getStatus().equals(ThingStatus.OFFLINE)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "Bridge status is offline.");
        } else if (bridgeStatusInfo.getStatus().equals(ThingStatus.ONLINE)) {
            // Initialize thing, if all OK the status of scene thing will be ONLINE
            scheduler.execute(new Initializer());
        }
    }

    private void updateLabelAndLocation(String label, String location) {
        if (!label.equals(thing.getLabel()) || !location.equals(thing.getLocation())) {
            logger.debug("Set location to {}", location);
            ThingBuilder thingBuilder = editThing();
            if (!label.equals(thing.getLabel())) {
                thingBuilder.withLabel(thing.getLabel());
            }
            if (!location.equals(thing.getLocation())) {
                thingBuilder.withLocation(location);
            }
            updateThing(thingBuilder.build());
        }
    }

    private class ScenePolling implements Runnable {
        @Override
        public void run() {
            for (Channel channel : getThing().getChannels()) {
                if (isLinked(channel.getUID().getId())) {
                    refreshChannel(channel);
                } else {
                    logger.debug("Polling for scene: {} not possible (channel {} not linked", thing.getLabel(),
                            channel.getLabel());
                }
            }
        }
    };

    protected void refreshAllChannels() {
        scheduler.execute(new ScenePolling());
    }

    private void refreshChannel(Channel channel) {
        VeraBridgeHandler veraBridgeHandler = getVeraBridgeHandler();
        if (veraBridgeHandler == null || !veraBridgeHandler.getThing().getStatus().equals(ThingStatus.ONLINE)) {
            logger.debug("Vera bridge handler not found or not ONLINE.");
            return;
        }

        // Check scene id associated with channel
        String sceneId = channel.getProperties().get(SCENE_CONFIG_ID);
        if (sceneId == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE,
                    "Not found sceneId for channel: " + channel.getChannelTypeUID());
            logger.debug("Vera scene disconnected: {}", thing.getLabel());
            return;
        }

        Scene scene = veraBridgeHandler.getController().getScene(sceneId);
        if (scene == null) {
            updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE, "Channel refresh for sceneId: " + sceneId
                    + " with channel: " + channel.getChannelTypeUID() + " failed!");
            logger.debug("Vera scene disconnected: {}", sceneId);
            return;
        }

        updateLabelAndLocation(scene.getName(), scene.getRoomName());
        ThingStatusInfo statusInfo = veraBridgeHandler.getThing().getStatusInfo();
        updateStatus(statusInfo.getStatus(), statusInfo.getStatusDetail(), statusInfo.getDescription());
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        logger.debug("Vera scene channel linked: {}", channelUID);
        VeraBridgeHandler veraBridgeHandler = getVeraBridgeHandler();
        if (veraBridgeHandler == null || !veraBridgeHandler.getThing().getStatus().equals(ThingStatus.ONLINE)) {
            logger.debug("Vera bridge handler not found or not ONLINE.");
            return;
        }
        super.channelLinked(channelUID);
    }

    @Override
    public void channelUnlinked(ChannelUID channelUID) {
        logger.debug("Vera scene channel unlinked: {}", channelUID);
        VeraBridgeHandler veraBridgeHandler = getVeraBridgeHandler();
        if (veraBridgeHandler == null || !veraBridgeHandler.getThing().getStatus().equals(ThingStatus.ONLINE)) {
            logger.debug("Vera bridge handler not found or not ONLINE.");
            return;
        }
        super.channelUnlinked(channelUID);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, final Command command) {
        logger.debug("Handle command for channel: {} with command: {}", channelUID.getId(), command.toString());

        VeraBridgeHandler veraBridgeHandler = getVeraBridgeHandler();
        if (veraBridgeHandler == null || !veraBridgeHandler.getThing().getStatus().equals(ThingStatus.ONLINE)) {
            logger.debug("Vera bridge handler not found or not ONLINE.");
            return;
        }

        final Channel channel = getThing().getChannel(channelUID.getId());
        final String sceneId = channel.getProperties().get(SCENE_CONFIG_ID);
        if (sceneId != null) {
            if (command instanceof RefreshType) {
                logger.debug("Handle command: RefreshType");
                refreshChannel(channel);
            } else {
                if (command instanceof OnOffType) {
                    if (command == OnOffType.ON) {
                        logger.debug("Handle command: OnOffType");
                        veraBridgeHandler.getController().runScene(sceneId);
                        updateState(channelUID, OnOffType.OFF);
                    }
                } else {
                    logger.warn("Unknown command type: {}, {}, {}, {}", command, sceneId);
                }
            }
        } else {
            logger.warn("Not found sceneId {}", sceneId);
        }
    }

    protected synchronized void addSceneAsChannel(Scene scene) {
        if (scene != null) {
            logger.debug("Add scene as channel: {}", scene.getName());

            HashMap<String, String> properties = new HashMap<>();
            properties.put(SCENE_CONFIG_ID, scene.getId());

            addChannel("sceneButton", "Switch", "Run scene", properties);
        }
    }

    private synchronized void addChannel(String id, String acceptedItemType, String label,
            HashMap<String, String> properties) {
        String channelId = id + "-" + properties.get(SCENE_CONFIG_ID);
        boolean channelExists = false;
        // Check if a channel for this scene exist.
        List<Channel> channels = getThing().getChannels();
        for (Channel channel : channels) {
            if (channelId.equals(channel.getUID().getId())) {
                channelExists = true;
            }
        }
        if (!channelExists) {
            ThingBuilder thingBuilder = editThing();
            ChannelTypeUID channelTypeUID = new ChannelTypeUID(BINDING_ID, id);
            ChannelBuilder channelBuilder = ChannelBuilder.create(new ChannelUID(getThing().getUID(), channelId),
                    acceptedItemType);
            channelBuilder.withType(channelTypeUID);
            channelBuilder.withLabel(label);
            channelBuilder.withProperties(properties);
            thingBuilder.withChannel(channelBuilder.build());
            thingBuilder.withLabel(thing.getLabel());
            updateThing(thingBuilder.build());
        }
    }
}

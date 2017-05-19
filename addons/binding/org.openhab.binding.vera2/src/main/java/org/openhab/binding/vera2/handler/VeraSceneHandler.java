/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.vera2.handler;

import static org.openhab.binding.vera2.VeraBindingConstants.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
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
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.vera2.VeraBindingConstants;
import org.openhab.binding.vera2.config.VeraSceneConfiguration;
import org.openhab.binding.vera2.controller.Vera.json.Scene;
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

    private ScenePolling scenePolling;
    private ScheduledFuture<?> pollingJob;
    private VeraSceneConfiguration mConfig = null;

    public VeraSceneHandler(Thing thing) {
        super(thing);
        scenePolling = new ScenePolling();
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

                    try {
                        logger.debug("Add channels");
                        Scene scene = veraBridgeHandler.getController().getScene(mConfig.getSceneId());
                        if (scene != null) {
                            logger.debug("Finded {} scene", scene.name);
                            addSceneAsChannel(scene);
                        }
                    } catch (Exception e) {
                        logger.error("{}", e.getMessage());
                        if (getThing().getStatus() == ThingStatus.ONLINE) {
                            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR,
                                    "Error occurred when adding scene as channel: " + mConfig.getSceneId());
                        }
                    }

                    // Initialize scene polling
                    if (pollingJob == null || pollingJob.isCancelled()) {
                        logger.debug("Starting polling job at intervall {}",
                                veraBridgeHandler.getVeraBridgeConfiguration().getPollingInterval());
                        pollingJob = scheduler.scheduleAtFixedRate(scenePolling, 10,
                                veraBridgeHandler.getVeraBridgeConfiguration().getPollingInterval(), TimeUnit.SECONDS);
                    } else {
                        // Called when thing or bridge updated ...
                        logger.debug("Polling is already active");
                    }
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR,
                            "Scenes not loaded");
                }
            } catch (Exception e) {
                logger.error("{}", e.getMessage());
                if (getThing().getStatus() == ThingStatus.ONLINE) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR,
                            "Error occurred when adding scene as channel.");
                }
            }
        }
    };

    /**
     * Remove all linked items from openHAB connector observer list
     */
    private class Disposer implements Runnable {
        @Override
        public void run() {
            // Vera bridge have to be ONLINE because configuration is needed
            VeraBridgeHandler veraBridgeHandler = getVeraBridgeHandler();
            if (veraBridgeHandler == null || !veraBridgeHandler.getThing().getStatus().equals(ThingStatus.ONLINE)) {
                logger.debug("Vera bridge handler not found or not ONLINE.");
                // status update will remove finally
                updateStatus(ThingStatus.REMOVED);
                return;
            }
            // status update will remove finally
            updateStatus(ThingStatus.REMOVED);
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
        setLocation();
        logger.debug("Initializing Vera scene handler ...");
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING,
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
        if (pollingJob != null && !pollingJob.isCancelled()) {
            pollingJob.cancel(true);
            pollingJob = null;
        }
        super.dispose();
    }

    @Override
    public void handleRemoval() {
        logger.debug("Handle removal Vera scene ...");
        scheduler.execute(new Disposer());
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

    private class ScenePolling implements Runnable {
        @Override
        public void run() {
            // logger.debug("Starting polling for scene: {}", getThing().getLabel());
            for (Channel channel : getThing().getChannels()) {
                // logger.debug("Checking link state of channel: {}", channel.getLabel());
                if (isLinked(channel.getUID().getId())) {
                    // logger.debug("Refresh items that linked with channel: {}", channel.getLabel());
                    try {
                        refreshChannel(channel);
                    } catch (Throwable t) {
                        if (t instanceof Exception) {
                            logger.error("Error occurred when performing polling:{}", t.getMessage());
                        } else if (t instanceof Error) {
                            logger.error("Error occurred when performing polling:{}", t.getMessage());
                        } else {
                            logger.error("Error occurred when performing polling: Unexpected error");
                        }
                        if (getThing().getStatus() == ThingStatus.ONLINE) {
                            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE,
                                    "Error occurred when performing polling.");
                        }
                    }
                } else {
                    logger.debug("Polling for scene: {} not possible (channel {} not linked", thing.getLabel(),
                            channel.getLabel());
                }
            }
            refreshLastUpdate();
        }
    };

    private synchronized void setLocation() {
        Map<String, String> properties = getThing().getProperties();
        // Load location from properties
        String location = properties.get(VeraBindingConstants.PROP_ROOM);
        if (location != null && !location.equals("") && getThing().getLocation() == null) {
            logger.debug("Set location to {}", location);
            ThingBuilder thingBuilder = editThing();
            thingBuilder.withLocation(location);
            thingBuilder.withLabel(thing.getLabel());
            updateThing(thingBuilder.build());
        }
    }

    protected void refreshLastUpdate() {
        // logger.debug("Refresh last update for scene");
        DateFormat formatter = new SimpleDateFormat("dd.MM.yyyy hh:mm:ss");
        updateProperty(PROP_LAST_UPDATE, formatter.format(Calendar.getInstance().getTime()));
    }

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
            logger.debug("Vera scene disconnected");
            return;
        }

        Scene scene = veraBridgeHandler.getController().getScene(sceneId);
        if (scene == null) {
            logger.debug("Vera scene not found.");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "Channel refresh for sceneId: " + sceneId
                    + " with channel: " + channel.getChannelTypeUID() + " failed!");
        } else if (!getThing().getStatus().equals(ThingStatus.ONLINE)) {
            ThingStatusInfo statusInfo = veraBridgeHandler.getThing().getStatusInfo();
            updateStatus(statusInfo.getStatus(), statusInfo.getStatusDetail(), statusInfo.getDescription());
        }
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        logger.debug("Vera scene channel linked: {}", channelUID);
        VeraBridgeHandler veraBridgeHandler = getVeraBridgeHandler();
        if (veraBridgeHandler == null || !veraBridgeHandler.getThing().getStatus().equals(ThingStatus.ONLINE)) {
            logger.debug("Vera bridge handler not found or not ONLINE.");
            return;
        }
        super.channelLinked(channelUID); // performs a refresh command
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
    public void handleUpdate(ChannelUID channelUID, State newState) {
        // Refresh update time
        logger.debug("Handle update for channel: {} with new state: {}", channelUID.getId(), newState.toString());

        refreshLastUpdate();
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
            try {
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
            } catch (UnsupportedOperationException e) {
                logger.warn("Unknown command: {}", e.getMessage());
            }
        } else {
            logger.warn("Not found sceneId {}", sceneId);
        }
    }

    protected synchronized void addSceneAsChannel(Scene scene) {
        if (scene != null) {
            logger.debug("Add scene as channel: {}", scene.name);

            HashMap<String, String> properties = new HashMap<>();
            properties.put(SCENE_CONFIG_ID, scene.id);

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
            if (channel.getUID().getId().equals(channelId)) {
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

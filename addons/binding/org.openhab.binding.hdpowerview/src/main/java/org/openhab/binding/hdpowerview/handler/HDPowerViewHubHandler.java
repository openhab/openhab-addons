/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
/**
e * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.hdpowerview.handler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.hdpowerview.HDPowerViewBindingConstants;
import org.openhab.binding.hdpowerview.config.HDPowerViewHubConfiguration;
import org.openhab.binding.hdpowerview.config.HDPowerViewShadeConfiguration;
import org.openhab.binding.hdpowerview.internal.HDPowerViewWebTargets;
import org.openhab.binding.hdpowerview.internal.api.responses.Scenes;
import org.openhab.binding.hdpowerview.internal.api.responses.Scenes.Scene;
import org.openhab.binding.hdpowerview.internal.api.responses.Shades;
import org.openhab.binding.hdpowerview.internal.api.responses.Shades.Shade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonParseException;

/**
 * The {@link HDPowerViewHubHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Andy Lintner - Initial contribution
 */
public class HDPowerViewHubHandler extends BaseBridgeHandler {

    private Logger logger = LoggerFactory.getLogger(HDPowerViewHubHandler.class);

    private long refreshInterval;

    private final Client client = ClientBuilder.newClient();
    private HDPowerViewWebTargets webTargets;
    private ScheduledFuture<?> pollFuture;

    private final ChannelTypeUID sceneChannelTypeUID = new ChannelTypeUID(HDPowerViewBindingConstants.BINDING_ID,
            HDPowerViewBindingConstants.CHANNELTYPE_SCENE_ACTIVATE);

    public HDPowerViewHubHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        Channel channel = getThing().getChannel(channelUID.getId());
        if (channel != null && sceneChannelTypeUID.equals(channel.getChannelTypeUID())) {
            if (command.equals(OnOffType.ON)) {
                webTargets.activateScene(Integer.parseInt(channelUID.getId()));
            }
        }
    }

    @Override
    public void initialize() {
        logger.debug("Initializing HDPowerView HUB");
        HDPowerViewHubConfiguration config = getConfigAs(HDPowerViewHubConfiguration.class);
        if (config.host == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Host address must be set");
        }
        webTargets = new HDPowerViewWebTargets(client, config.host);
        refreshInterval = config.refresh;

        schedulePoll();
    }

    public HDPowerViewWebTargets getWebTargets() {
        return webTargets;
    }

    @Override
    public void handleRemoval() {
        super.handleRemoval();
        stopPoll();
    }

    @Override
    public void dispose() {
        super.dispose();
        stopPoll();
    }

    void pollNow() {
        if (thingIsInitialized()) {
            schedulePoll();
        }
    }

    private void schedulePoll() {
        if (pollFuture != null) {
            pollFuture.cancel(false);
        }
        logger.debug("Scheduling poll for 500ms out, then every {} ms", refreshInterval);
        pollFuture = scheduler.scheduleAtFixedRate(pollingRunnable, 500, refreshInterval, TimeUnit.MILLISECONDS);
    }

    private synchronized void stopPoll() {
        if (pollFuture != null && !pollFuture.isCancelled()) {
            pollFuture.cancel(true);
            pollFuture = null;
        }
    }

    private synchronized void poll() {
        try {
            logger.debug("Polling for state");
            pollShades();
            pollScenes();
        } catch (IOException e) {
            logger.debug("Could not connect to bridge", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, e.getMessage());
        } catch (Exception e) {
            logger.warn("Unexpected error connecting to bridge", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    private void pollShades() throws IOException {
        Shades shades = webTargets.getShades();
        updateStatus(ThingStatus.ONLINE);
        if (shades != null) {
            Map<Integer, Thing> things = getThingsByShadeId();
            logger.debug("Found {} shades", things.size());
            for (Shade shade : shades.shadeData) {
                Thing thing = things.get(shade.id);
                if (thing != null) {
                    HDPowerViewShadeHandler handler = ((HDPowerViewShadeHandler) thing.getHandler());
                    if (handler != null) {
                        logger.debug("Handling update for shade {}", shade.id);
                        handler.onReceiveUpdate(shade);
                    } else {
                        logger.debug("Skipping shade with no handler {}", shade.id);
                    }
                } else {
                    logger.debug("Skipping non-bound shade {}", shade.id);
                }
            }
        } else {
            logger.warn("No response to shade poll");
        }
    }

    private void pollScenes() throws JsonParseException, IOException {
        Scenes scenes = webTargets.getScenes();
        if (scenes != null) {
            logger.debug("Received {} scenes", scenes.sceneIds.size());
            Map<Integer, Channel> channels = getSceneChannelsById();
            for (Scene scene : scenes.sceneData) {
                // Remove existing scene from the map
                Channel existingChannel = channels.remove(scene.id);
                if (existingChannel == null) {
                    // Create the new scene
                    ChannelUID channelUID = new ChannelUID(getThing().getUID(), Integer.toString(scene.id, 10));
                    Channel channel = ChannelBuilder.create(channelUID, "Switch").withType(sceneChannelTypeUID)
                            .withLabel(scene.getName()).withDescription("Activates the scene " + scene.getName())
                            .build();
                    updateThing(editThing().withChannel(channel).build());
                    logger.debug("Created new channel for scene {}", scene.id);
                } else {
                    logger.debug("Skipping existing scene {}", scene.id);
                }
            }

            // Remove any previously created channels that no longer exist
            if (!channels.isEmpty()) {
                logger.debug("Removing {} existing scenes", channels.size());
                List<Channel> allChannels = new ArrayList<>(getThing().getChannels());
                allChannels.removeAll(channels.values());
                updateThing(editThing().withChannels(allChannels).build());
            }

        } else {
            logger.warn("No response to scene poll");
        }
    }

    private Map<Integer, Thing> getThingsByShadeId() {
        Map<Integer, Thing> ret = new HashMap<>();
        for (Thing thing : getThing().getThings()) {
            if (thing.getThingTypeUID().equals(HDPowerViewBindingConstants.THING_TYPE_SHADE)) {
                Integer id = thing.getConfiguration().as(HDPowerViewShadeConfiguration.class).id;
                ret.put(id, thing);
            }
        }
        return ret;
    }

    private Map<Integer, Channel> getSceneChannelsById() {
        Map<Integer, Channel> ret = new HashMap<>();
        for (Channel channel : getThing().getChannels()) {
            if (channel.getChannelTypeUID().equals(sceneChannelTypeUID)) {
                Integer id = Integer.parseInt(channel.getUID().getId());
                ret.put(id, channel);
            }
        }
        return ret;
    }

    private Runnable pollingRunnable = new Runnable() {

        @Override
        public void run() {
            poll();
        }

    };

}

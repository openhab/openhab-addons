/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.hdpowerview.internal.handler;

import static org.openhab.binding.hdpowerview.internal.HDPowerViewBindingConstants.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.ProcessingException;
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
import org.openhab.binding.hdpowerview.internal.HDPowerViewBindingConstants;
import org.openhab.binding.hdpowerview.internal.HDPowerViewWebTargets;
import org.openhab.binding.hdpowerview.internal.HubMaintenanceException;
import org.openhab.binding.hdpowerview.internal.api.responses.Scenes;
import org.openhab.binding.hdpowerview.internal.api.responses.Scenes.Scene;
import org.openhab.binding.hdpowerview.internal.api.responses.Shades;
import org.openhab.binding.hdpowerview.internal.api.responses.Shades.ShadeData;
import org.openhab.binding.hdpowerview.internal.config.HDPowerViewHubConfiguration;
import org.openhab.binding.hdpowerview.internal.config.HDPowerViewShadeConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonParseException;

/**
 * The {@link HDPowerViewHubHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Andy Lintner - Initial contribution
 * @author Andrew Fiddian-Green - Added support for secondary rail positions
 */
public class HDPowerViewHubHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(HDPowerViewHubHandler.class);

    private long refreshInterval;

    private final Client client = ClientBuilder.newClient();
    private HDPowerViewWebTargets webTargets;
    private ScheduledFuture<?> pollFuture;

    private final ChannelTypeUID sceneChannelTypeUID = new ChannelTypeUID(HDPowerViewBindingConstants.BINDING_ID,
            HDPowerViewBindingConstants.CHANNELTYPE_SCENE_ACTIVATE);

    private final Runnable refreshShadeCacheTask = () -> {
        refreshShadeCache();
    };

    public HDPowerViewHubHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (CHANNEL_HUB_REFRESH_CACHE.equals(channelUID.getId()) && command.equals(OnOffType.ON)) {
            new Thread(refreshShadeCacheTask).start();
            return;
        }
        Channel channel = getThing().getChannel(channelUID.getId());
        if (channel != null && sceneChannelTypeUID.equals(channel.getChannelTypeUID())) {
            if (command.equals(OnOffType.ON)) {
                try {
                    webTargets.activateScene(Integer.parseInt(channelUID.getId()));
                } catch (HubMaintenanceException e) {
                    logger.debug("Hub temporariliy down for maintenance");
                } catch (NumberFormatException | ProcessingException e) {
                    logger.debug("Unexpected error {}", e.getMessage());
                }
            }
        }
    }

    @Override
    public void initialize() {
        logger.debug("Initializing hub");
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
        if (isInitialized()) {
            schedulePoll();
        }
    }

    private void schedulePoll() {
        if (pollFuture != null) {
            pollFuture.cancel(false);
        }
        logger.debug("Scheduling poll for 500ms out, then every {} ms", refreshInterval);
        pollFuture = scheduler.scheduleWithFixedDelay(this::poll, 500, refreshInterval, TimeUnit.MILLISECONDS);
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
        } catch (JsonParseException e) {
            logger.warn("Bridge returned a bad JSON response: {}", e.getMessage());
        } catch (ProcessingException e) {
            logger.warn("Error connecting to bridge: {}", e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, e.getMessage());
        } catch (HubMaintenanceException e) {
            logger.debug("Hub temporariliy down for maintenance");
        }
    }

    private void pollShades() throws JsonParseException, ProcessingException, HubMaintenanceException {
        Shades shades = webTargets.getShades();
        updateStatus(ThingStatus.ONLINE);

        if (shades == null) {
            throw new JsonParseException("Missing 'shades' element");
        }
        List<ShadeData> shadeData = shades.shadeData;
        if (shadeData == null) {
            throw new JsonParseException("Missing 'shades.shadeData' element");
        }
        logger.debug("Received data for {} shades", shadeData.size());

        Map<String, ShadeData> shadeIdData = getShadeDataById(shadeData);
        List<Thing> things = getThing().getThings();
        if (shadeIdData.size() != things.size()) {
            logger.debug("Shade count in hub ({}) != Shade count in binding ({})", shadeIdData.size(), things.size());
        }

        for (Thing thing : things) {
            String thingId = thing.getConfiguration().as(HDPowerViewShadeConfiguration.class).id;
            HDPowerViewShadeHandler thingHandler = ((HDPowerViewShadeHandler) thing.getHandler());
            if (thingHandler == null) {
                logger.debug("Shade '{}' missing handler", thingId);
                continue;
            }
            if (!shadeIdData.containsKey(thingId)) {
                thingHandler.onReceiveUpdate(null);
                logger.debug("Shade '{}' not found in hub", thingId);
                continue;
            }
            logger.debug("Updating shade '{}'", thingId);
            thingHandler.onReceiveUpdate(shadeIdData.get(thingId));
        }
    }

    private void pollScenes() throws JsonParseException, ProcessingException, HubMaintenanceException {
        Scenes scenes = webTargets.getScenes();

        if (scenes == null) {
            throw new JsonParseException("Missing 'scenes' element");
        }
        List<Scene> sceneData = scenes.sceneData;
        if (sceneData == null) {
            throw new JsonParseException("Missing 'scenes.sceneData' element");
        }
        logger.debug("Received data for {} scenes", sceneData.size());

        Map<Integer, Channel> channels = getSceneChannelsById();

        for (Scene scene : sceneData) {
            // remove existing scenes from the map
            if (channels.remove(scene.id) == null) {
                // Create the new scene
                ChannelUID channelUID = new ChannelUID(getThing().getUID(), Integer.toString(scene.id, 10));
                Channel channel = ChannelBuilder.create(channelUID, "Switch").withType(sceneChannelTypeUID)
                        .withLabel(scene.getName()).withDescription("Activates the scene " + scene.getName()).build();
                updateThing(editThing().withChannel(channel).build());
                logger.debug("Creating new channel for scene '{}'", scene.id);
                continue;
            }
            logger.debug("Keeping channel for existing scene '{}'", scene.id);
        }

        // Remove any previously created channels that no longer exist
        if (!channels.isEmpty()) {
            logger.debug("Removing {} orphan scene channels", channels.size());
            List<Channel> allChannels = new ArrayList<>(getThing().getChannels());
            allChannels.removeAll(channels.values());
            updateThing(editThing().withChannels(allChannels).build());
        }
    }

    private Map<String, Thing> getThingsByShadeId() {
        Map<String, Thing> ret = new HashMap<>();
        for (Thing thing : getThing().getThings()) {
            String id = thing.getConfiguration().as(HDPowerViewShadeConfiguration.class).id;
            ret.put(id, thing);
        }
        return ret;
    }

    private Map<String, ShadeData> getShadeDataById(List<ShadeData> shadeData) {
        Map<String, ShadeData> ret = new HashMap<>();
        if (shadeData != null) {
            for (ShadeData shade : shadeData) {
                ret.put(shade.id, shade);
            }
        }
        return ret;
    }

    private Map<Integer, Channel> getSceneChannelsById() {
        Map<Integer, Channel> ret = new HashMap<>();
        for (Channel channel : getThing().getChannels()) {
            if (sceneChannelTypeUID.equals(channel.getChannelTypeUID())) {
                Integer id = Integer.parseInt(channel.getUID().getId());
                ret.put(id, channel);
            }
        }
        return ret;
    }

    private void refreshShadeCache() {
        Map<String, Thing> things = getThingsByShadeId();
        for (String shadeId : things.keySet()) {
            try {
                webTargets.refreshShade(shadeId);
            } catch (HubMaintenanceException e) {
                logger.debug("Hub temporariliy down for maintenance");
            } catch (ProcessingException e) {
                logger.debug("Unexpected error {}", e.getMessage());
            }
        }
    }
}

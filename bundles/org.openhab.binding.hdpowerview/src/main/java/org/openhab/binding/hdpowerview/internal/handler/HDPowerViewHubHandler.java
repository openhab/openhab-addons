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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
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
 * The {@link HDPowerViewHubHandler} is responsible for handling commands, which
 * are sent to one of the channels.
 *
 * @author Andy Lintner - Initial contribution
 * @author Andrew Fiddian-Green - Added support for secondary rail positions
 */
@NonNullByDefault
public class HDPowerViewHubHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(HDPowerViewHubHandler.class);

    private long refreshInterval;
    private long hardRefreshInterval;

    private final Client client = ClientBuilder.newClient();
    private @Nullable HDPowerViewWebTargets webTargets;
    private @Nullable ScheduledFuture<?> pollFuture;
    private @Nullable ScheduledFuture<?> hardRefreshFuture;

    private final ChannelTypeUID sceneChannelTypeUID = new ChannelTypeUID(HDPowerViewBindingConstants.BINDING_ID,
            HDPowerViewBindingConstants.CHANNELTYPE_SCENE_ACTIVATE);

    public HDPowerViewHubHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (RefreshType.REFRESH.equals(command)) {
            requestRefreshShades();
            return;
        }

        Channel channel = getThing().getChannel(channelUID.getId());
        if (channel != null && sceneChannelTypeUID.equals(channel.getChannelTypeUID())) {
            if (OnOffType.ON.equals(command)) {
                try {
                    HDPowerViewWebTargets webTargets = this.webTargets;
                    if (webTargets == null) {
                        throw new ProcessingException("Web targets not initialized");
                    }
                    webTargets.activateScene(Integer.parseInt(channelUID.getId()));
                } catch (HubMaintenanceException e) {
                    // exceptions are logged in HDPowerViewWebTargets
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
        String host = config.host;

        if (host == null || host.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Host address must be set");
            return;
        }

        webTargets = new HDPowerViewWebTargets(client, host);
        refreshInterval = config.refresh;
        hardRefreshInterval = config.hardRefresh;
        schedulePoll();
    }

    public @Nullable HDPowerViewWebTargets getWebTargets() {
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

    private void schedulePoll() {
        ScheduledFuture<?> future = this.pollFuture;
        if (future != null) {
            future.cancel(false);
        }
        logger.debug("Scheduling poll for 5000ms out, then every {}ms", refreshInterval);
        this.pollFuture = scheduler.scheduleWithFixedDelay(this::poll, 5000, refreshInterval, TimeUnit.MILLISECONDS);

        future = this.hardRefreshFuture;
        if (future != null) {
            future.cancel(false);
        }
        if (hardRefreshInterval > 0) {
            logger.debug("Scheduling hard refresh every {}minutes", hardRefreshInterval);
            this.hardRefreshFuture = scheduler.scheduleWithFixedDelay(this::requestRefreshShades, 1,
                    hardRefreshInterval, TimeUnit.MINUTES);
        }
    }

    private synchronized void stopPoll() {
        ScheduledFuture<?> future = this.pollFuture;
        if (future != null) {
            future.cancel(true);
        }
        this.pollFuture = null;

        future = this.hardRefreshFuture;
        if (future != null) {
            future.cancel(true);
        }
        this.hardRefreshFuture = null;
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
            // exceptions are logged in HDPowerViewWebTargets
        }
    }

    private void pollShades() throws JsonParseException, ProcessingException, HubMaintenanceException {
        HDPowerViewWebTargets webTargets = this.webTargets;
        if (webTargets == null) {
            throw new ProcessingException("Web targets not initialized");
        }

        Shades shades = webTargets.getShades();
        if (shades == null) {
            throw new JsonParseException("Missing 'shades' element");
        }

        List<ShadeData> shadesData = shades.shadeData;
        if (shadesData == null) {
            throw new JsonParseException("Missing 'shades.shadeData' element");
        }

        updateStatus(ThingStatus.ONLINE);
        logger.debug("Received data for {} shades", shadesData.size());

        Map<String, ShadeData> idShadeDataMap = getIdShadeDataMap(shadesData);
        Map<Thing, String> thingIdMap = getThingIdMap();
        for (Entry<Thing, String> item : thingIdMap.entrySet()) {
            Thing thing = item.getKey();
            String shadeId = item.getValue();
            ShadeData shadeData = idShadeDataMap.get(shadeId);
            updateShadeThing(shadeId, thing, shadeData);
        }
    }

    private void updateShadeThing(String shadeId, Thing thing, @Nullable ShadeData shadeData) {
        HDPowerViewShadeHandler thingHandler = ((HDPowerViewShadeHandler) thing.getHandler());
        if (thingHandler == null) {
            logger.debug("Shade '{}' handler not initialized", shadeId);
            return;
        }
        if (shadeData == null) {
            logger.debug("Shade '{}' has no data in hub", shadeId);
        } else {
            logger.debug("Updating shade '{}'", shadeId);
        }
        thingHandler.onReceiveUpdate(shadeData);
    }

    private void pollScenes() throws JsonParseException, ProcessingException, HubMaintenanceException {
        HDPowerViewWebTargets webTargets = this.webTargets;
        if (webTargets == null) {
            throw new ProcessingException("Web targets not initialized");
        }

        Scenes scenes = webTargets.getScenes();
        if (scenes == null) {
            throw new JsonParseException("Missing 'scenes' element");
        }

        List<Scene> sceneData = scenes.sceneData;
        if (sceneData == null) {
            throw new JsonParseException("Missing 'scenes.sceneData' element");
        }
        logger.debug("Received data for {} scenes", sceneData.size());

        Map<String, Channel> idChannelMap = getIdChannelMap();
        for (Scene scene : sceneData) {
            // remove existing scene channel from the map
            String sceneId = Integer.toString(scene.id);
            if (idChannelMap.containsKey(sceneId)) {
                idChannelMap.remove(sceneId);
                logger.debug("Keeping channel for existing scene '{}'", sceneId);
            } else {
                // create a new scene channel
                ChannelUID channelUID = new ChannelUID(getThing().getUID(), sceneId);
                Channel channel = ChannelBuilder.create(channelUID, "Switch").withType(sceneChannelTypeUID)
                        .withLabel(scene.getName()).withDescription("Activates the scene " + scene.getName()).build();
                updateThing(editThing().withChannel(channel).build());
                logger.debug("Creating new channel for scene '{}'", sceneId);
            }
        }

        // remove any previously created channels that no longer exist
        if (!idChannelMap.isEmpty()) {
            logger.debug("Removing {} orphan scene channels", idChannelMap.size());
            List<Channel> allChannels = new ArrayList<>(getThing().getChannels());
            allChannels.removeAll(idChannelMap.values());
            updateThing(editThing().withChannels(allChannels).build());
        }
    }

    private Map<Thing, String> getThingIdMap() {
        Map<Thing, String> ret = new HashMap<>();
        for (Thing thing : getThing().getThings()) {
            String id = thing.getConfiguration().as(HDPowerViewShadeConfiguration.class).id;
            if (id != null && !id.isEmpty()) {
                ret.put(thing, id);
            }
        }
        return ret;
    }

    private Map<String, ShadeData> getIdShadeDataMap(List<ShadeData> shadeData) {
        Map<String, ShadeData> ret = new HashMap<>();
        for (ShadeData shade : shadeData) {
            if (shade.id != 0) {
                ret.put(Integer.toString(shade.id), shade);
            }
        }
        return ret;
    }

    private Map<String, Channel> getIdChannelMap() {
        Map<String, Channel> ret = new HashMap<>();
        for (Channel channel : getThing().getChannels()) {
            if (sceneChannelTypeUID.equals(channel.getChannelTypeUID())) {
                ret.put(channel.getUID().getId(), channel);
            }
        }
        return ret;
    }

    private void requestRefreshShades() {
        Map<Thing, String> thingIdMap = getThingIdMap();
        for (Entry<Thing, String> item : thingIdMap.entrySet()) {
            Thing thing = item.getKey();
            ThingHandler handler = thing.getHandler();
            if (handler instanceof HDPowerViewShadeHandler) {
                ((HDPowerViewShadeHandler) handler).requestRefreshShade();
            } else {
                String shadeId = item.getValue();
                logger.debug("Shade '{}' handler not initialized", shadeId);
            }
        }
    }
}

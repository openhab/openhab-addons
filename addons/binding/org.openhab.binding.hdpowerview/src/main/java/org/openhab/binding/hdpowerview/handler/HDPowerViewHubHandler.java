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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.hdpowerview.HDPowerViewBindingConstants;
import org.openhab.binding.hdpowerview.config.HDPowerViewHubConfiguration;
import org.openhab.binding.hdpowerview.config.HDPowerViewSceneConfiguration;
import org.openhab.binding.hdpowerview.config.HDPowerViewShadeConfiguration;
import org.openhab.binding.hdpowerview.internal.HDPowerViewWebTargets;
import org.openhab.binding.hdpowerview.internal.api.responses.Scenes;
import org.openhab.binding.hdpowerview.internal.api.responses.Scenes.Scene;
import org.openhab.binding.hdpowerview.internal.api.responses.Shades;
import org.openhab.binding.hdpowerview.internal.api.responses.Shades.Shade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    public HDPowerViewHubHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

    }

    @Override
    public void initialize() {
        HDPowerViewHubConfiguration config = getConfigAs(HDPowerViewHubConfiguration.class);
        webTargets = new HDPowerViewWebTargets(client, config.ipAddress);
        refreshInterval = config.refresh;

        updateStatus(ThingStatus.OFFLINE);

        schedulePoll();

        // Note: When initialization can NOT be done set the status with more details for further
        // analysis. See also class ThingStatusDetail for all available status details.
        // Add a description to give user information to understand why thing does not work
        // as expected. E.g.
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
        // "Can not access device as username and/or password are invalid");
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
        schedulePoll();
    }

    private void schedulePoll() {
        if (pollFuture != null) {
            pollFuture.cancel(false);
        }
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
            Shades shades = webTargets.getShades();
            if (shades != null) {
                Map<Integer, Thing> things = getThingsByShadeId();
                for (Shade shade : shades.shadeData) {
                    Thing thing = things.get(shade.id);
                    if (thing != null) {
                        HDPowerViewShadeHandler handler = ((HDPowerViewShadeHandler) thing.getHandler());
                        if (handler != null) {
                            handler.onReceiveUpdate(shade);
                        }
                    }
                }
                updateStatus(ThingStatus.ONLINE);
            }
            Scenes scenes = webTargets.getScenes();
            if (scenes != null) {
                Map<Integer, Thing> things = getThingsBySceneId();
                for (Scene scene : scenes.sceneData) {
                    Thing thing = things.get(scene.id);
                    if (thing != null) {
                        HDPowerViewSceneHandler handler = ((HDPowerViewSceneHandler) thing.getHandler());
                        if (handler != null) {
                            handler.setOnline();
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("Could not connect to bridge", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, e.getMessage());
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

    private Map<Integer, Thing> getThingsBySceneId() {
        Map<Integer, Thing> ret = new HashMap<>();
        for (Thing thing : getThing().getThings()) {
            if (thing.getThingTypeUID().equals(HDPowerViewBindingConstants.THING_TYPE_SCENE)) {
                Integer id = thing.getConfiguration().as(HDPowerViewSceneConfiguration.class).id;
                ret.put(id, thing);
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

/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.heos.handler;

import static org.openhab.binding.heos.HeosBindingConstants.PROP_PID;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.heos.internal.api.HeosFacade;
import org.openhab.binding.heos.internal.api.HeosSystem;
import org.openhab.binding.heos.internal.resources.HeosConstants;
import org.openhab.binding.heos.internal.resources.HeosPlayer;

/**
 * The {@link HeosPlayerHandler} handles the actions for a HEOS player.
 * Channel commands are received and send to the dedicated channels
 *
 * @author Johannes Einig - Initial contribution
 */
public class HeosPlayerHandler extends HeosThingBaseHandler {

    private final String pid;
    private HeosPlayer player = new HeosPlayer();

    public HeosPlayerHandler(Thing thing, HeosSystem heos, HeosFacade api) {
        super(thing, heos, api);
        pid = thing.getConfiguration().get(PROP_PID).toString();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        super.handleCommand(channelUID, command);
    }

    @Override
    public void initialize() {
        api.registerforChangeEvents(this);
        // Because initialization can take longer a scheduler with an extra thread is created
        scheduler.schedule(() -> {
            initChannelHandlerFactory();
            player = heos.getPlayerState(pid);
            if (!player.isOnline()) {
                setStatusOffline();
                bridge.setThingStatusOffline(thing.getUID());
                return;
            }
            // Adding the favorite channel to the player
            if (bridge.isLoggedin()) {
                updateThingChannels(channelManager.addFavoriteChannels(heos.getFavorites()));
            }
            updateStatus(ThingStatus.ONLINE);
        }, 3, TimeUnit.SECONDS);
    }

    @Override
    public PercentType getNotificationSoundVolume() {
        return PercentType.valueOf(player.getLevel());
    }

    @Override
    public void setNotificationSoundVolume(PercentType volume) {
        api.setVolume(volume.toString(), pid);
    }

    @Override
    public void playerStateChangeEvent(String pid, String event, String command) {
        if (this.pid.equals(pid)) {
            handleThingStateUpdate(event, command);
        }
    }

    @Override
    public void playerMediaChangeEvent(String pid, Map<String, String> info) {
        if (this.pid.equals(pid)) {
            player.updateMediaInfo(info);
            handleThingMediaUpdate(info);
        }
    }

    @Override
    public void bridgeChangeEvent(String event, String result, String command) {
        if (HeosConstants.USER_CHANGED.equals(command)) {
            updateThingChannels(channelManager.addFavoriteChannels(heos.getFavorites()));
        }
    }

    @Override
    public void setStatusOffline() {
        api.unregisterforChangeEvents(this);
        updateStatus(ThingStatus.OFFLINE);
    }

    @Override
    public void setStatusOnline() {
        this.initialize();
    }
}

/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.heos.handler;

import static org.openhab.binding.heos.internal.resources.HeosConstants.PID;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.heos.internal.api.HeosFacade;
import org.openhab.binding.heos.internal.api.HeosSystem;
import org.openhab.binding.heos.internal.resources.HeosPlayer;

/**
 * The {@link HeosPlayerHandler} handles the actions for a HEOS player.
 * Channel commands are received and send to the dedicated channels
 *
 * @author Johannes Einig - Initial contribution
 */
public class HeosPlayerHandler extends HeosThingBaseHandler {

    private String pid;
    private HeosPlayer player = new HeosPlayer();

    public HeosPlayerHandler(Thing thing, HeosSystem heos, HeosFacade api) {
        super(thing, heos, api);
        pid = thing.getConfiguration().get(PID).toString();
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
            updateStatus(ThingStatus.ONLINE);
        }, 3, TimeUnit.SECONDS);
    }

    @Override
    public PercentType getNotificationSoundVolume() {
        //updateHeosThingState();
        return PercentType.valueOf(player.getLevel());
    }

    @Override
    public void setNotificationSoundVolume(PercentType volume) {
        api.setVolume(volume.toString(), pid);
    }

    @Override
    public void playerStateChangeEvent(String pid, String event, String command) {
        if (pid.equals(this.pid)) {
            handleThingStateUpdate(event, command);
        }
    }

    @Override
    public void playerMediaChangeEvent(String pid, Map<String, String> info) {
        if (pid.equals(this.pid)) {
            this.player.updateMediaInfo(info);
            handleThingMediaUpdate(info);
        }
    }

    @Override
    public void bridgeChangeEvent(String event, String result, String command) {
        // Do nothing
    }

    @Override
    @SuppressWarnings("null")
    public void setStatusOffline() {
        api.unregisterforChangeEvents(this);
        updateStatus(ThingStatus.OFFLINE);
    }

    @Override
    public void setStatusOnline() {
        this.initialize();
    }

    @Override
    protected void updateHeosThingState() {
        player = heos.getPlayerState(pid);
    }

}

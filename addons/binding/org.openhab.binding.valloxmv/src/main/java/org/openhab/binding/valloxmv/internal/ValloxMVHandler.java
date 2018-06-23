/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.valloxmv.internal;

import java.math.BigDecimal;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ValloxMVHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Björn Brings - Initial contribution
 */
public class ValloxMVHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(ValloxMVHandler.class);
    ScheduledFuture<?> updateTasks;
    ValloxMVWebSocket vows_send;

    /**
     * Refresh interval in seconds.
     */
    private int updateInterval;
    private long lastUpdate;

    /**
     * IP of vallox ventilation unit web interface.
     */
    public ValloxMVHandler(@NonNull Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            if (lastUpdate > System.currentTimeMillis() + updateInterval * 1000) {
                vows_send.request(null, null);
            }
        } else {
            if (channelUID.getId().equals(ValloxMVBindingConstants.CHANNEL_STATE)) {
                try {
                    int cmd = Integer.parseInt(command.toString());
                    if ((cmd == ValloxMVBindingConstants.STATE_FIREPLACE)
                            || (cmd == ValloxMVBindingConstants.STATE_ATHOME)
                            || (cmd == ValloxMVBindingConstants.STATE_AWAY)
                            || (cmd == ValloxMVBindingConstants.STATE_BOOST)) {
                        logger.debug("Changing state to: {}", command.toString());
                        // Open WebSocket
                        vows_send.request(channelUID, command.toString());
                        vows_send.request(null, null);
                    }
                } catch (NumberFormatException nfe) {
                    // Other commands like refresh
                    return;
                }
            } else if (channelUID.getId().equals(ValloxMVBindingConstants.CHANNEL_ONOFF)) {
                if (command.toString() == "ON") {
                    vows_send.request(channelUID, "0");
                } else if (command.toString() == "OFF") {
                    vows_send.request(channelUID, "5");
                }
            }
        }
    }

    @Override
    public void initialize() {
        logger.debug("Start initializing!");

        String ip = getConfigAs(ValloxMVConfig.class).getIp();
        vows_send = new ValloxMVWebSocket(ValloxMVHandler.this, ip);

        BigDecimal bdUpdateInterval = getConfigAs(ValloxMVConfig.class).getUpdateinterval();
        if (bdUpdateInterval == null || bdUpdateInterval.compareTo(new BigDecimal(15)) == -1) {
            updateInterval = 60;
        } else {
            updateInterval = bdUpdateInterval.intValue();
        }

        try {
            scheduleUpdates();
        } catch (Exception e) {
            logger.error("Error: {}", e.getMessage().toString());
        }
    }

    private void scheduleUpdates() throws Exception {
        logger.debug("Schedule vallox update every {} sec", updateInterval);

        String ip = getConfigAs(ValloxMVConfig.class).getIp();
        logger.debug("Connecting to ip: {}", ip);
        // Open WebSocket
        ValloxMVWebSocket vows = new ValloxMVWebSocket(ValloxMVHandler.this, ip);

        updateTasks = scheduler.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                // Do a pure read request to websocket interface
                vows.request(null, null);
            }
        }, 0, updateInterval, TimeUnit.SECONDS);
    }

    public void dataUpdated() {
        lastUpdate = System.currentTimeMillis();
    }

    @Override
    public void dispose() {
        updateTasks.cancel(true);
    }

    @Override
    protected void updateState(ChannelUID uid, State dt) {
        super.updateState(uid, dt);
    }

    @Override
    protected void updateStatus(ThingStatus ts, ThingStatusDetail statusDetail) {
        super.updateStatus(ts, statusDetail);
    }

    @Override
    protected void updateStatus(ThingStatus ts) {
        super.updateStatus(ts);

    }
}
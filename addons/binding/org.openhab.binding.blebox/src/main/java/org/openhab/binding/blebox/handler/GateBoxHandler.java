/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.blebox.handler;

import java.math.BigDecimal;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.blebox.BleboxBindingConstants;
import org.openhab.binding.blebox.devices.GateBox;
import org.openhab.binding.blebox.internal.BleboxDeviceConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link GateBoxHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Szymon Tokarski - Initial contribution
 */
public class GateBoxHandler extends BaseThingHandler {

    private Logger logger = LoggerFactory.getLogger(GateBoxHandler.class);
    private GateBox gateBox;

    Runnable runnable = new Runnable() {
        @Override
        public void run() {

            try {
                if (gateBox != null) {

                    GateBox.StateResponse state = gateBox.GetStatus();

                    if (state != null) {
                        updateState(BleboxBindingConstants.CHANNEL_POSITION, state.GetPosition());
                        // updateState(BleboxBindingConstants.CHANNEL_COLOR, state.GetColor());

                        if (getThing().getStatus() == ThingStatus.OFFLINE) {
                            updateStatus(ThingStatus.ONLINE);
                        }
                    } else {
                        updateStatus(ThingStatus.OFFLINE);
                    }

                }
            } catch (Exception e) {
                logger.info("Polling device state failed: {}", e.toString());
            }
        }
    };
    private ScheduledFuture<?> pollingJob;

    public GateBoxHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

        switch (channelUID.getId()) {
            case BleboxBindingConstants.CHANNEL_POSITION:

                if (command instanceof UpDownType) {
                    UpDownType upDownCommand = (UpDownType) command;

                    if (upDownCommand == UpDownType.UP) {
                        gateBox.SetPosition(PercentType.HUNDRED);
                    } else if (upDownCommand == UpDownType.DOWN) {
                        gateBox.SetPosition(PercentType.ZERO);
                    }
                }

                break;
        }

    }

    @Override
    public void initialize() {

        final String ipAddress = (String) getConfig().get(BleboxDeviceConfiguration.IP);

        if (ipAddress != null) {
            gateBox = new GateBox(ipAddress);
            updateStatus(ThingStatus.ONLINE);

            int pollingInterval = BleboxDeviceConfiguration.DEFAULT_POLL_INTERVAL;

            try {
                Object pollingIntervalConfig = getConfig().get(BleboxDeviceConfiguration.POLL_INTERVAL);
                if (pollingIntervalConfig != null) {
                    pollingInterval = ((BigDecimal) pollingIntervalConfig).intValue();
                } else {
                    logger.info("Polling interval not configured for this device. Using default value: {}s",
                            pollingInterval);
                }
            } catch (NumberFormatException ex) {
                logger.info("Wrong configuration value for polling interval. Using default value: {}s",
                        pollingInterval);
            }

            pollingJob = scheduler.scheduleAtFixedRate(runnable, 0, pollingInterval, TimeUnit.SECONDS);
        }

    }

    @Override
    public void dispose() {
        pollingJob.cancel(true);
    }
}

/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.blebox.handler;

import static org.openhab.binding.blebox.BleboxBindingConstants.CHANNEL_BRIGHTNESS;

import java.math.BigDecimal;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.blebox.devices.Dimmer;
import org.openhab.binding.blebox.internal.BleboxDeviceConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DimmerHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Szymon Tokarski - Initial contribution
 */
public class DimmerHandler extends BaseThingHandler {

    private Logger logger = LoggerFactory.getLogger(DimmerHandler.class);
    private Dimmer dimmer;

    Runnable runnable = new Runnable() {
        @Override
        public void run() {

            try {
                if (dimmer != null) {
                    PercentType brightness = dimmer.GetBrightness();

                    if (brightness != null) {
                        updateState(CHANNEL_BRIGHTNESS, brightness);

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

    public DimmerHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (channelUID.getId().equals(CHANNEL_BRIGHTNESS)) {

            if (command instanceof PercentType) {
                dimmer.SetBrightness((PercentType) command);
            } else if (command instanceof OnOffType) {
                if (((OnOffType) command).equals(OnOffType.ON)) {
                    dimmer.SetBrightness(PercentType.HUNDRED);
                } else {
                    dimmer.SetBrightness(PercentType.ZERO);
                }
            }
        }
    }

    @Override
    public void initialize() {

        final String ipAddress = (String) getConfig().get(BleboxDeviceConfiguration.IP);

        if (ipAddress != null) {
            dimmer = new Dimmer(ipAddress);
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

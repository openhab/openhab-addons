/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.blebox.handler;

import java.math.BigDecimal;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.blebox.BleboxBindingConstants;
import org.openhab.binding.blebox.devices.LightBox;
import org.openhab.binding.blebox.internal.BleboxDeviceConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link LightBoxHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Szymon Tokarski - Initial contribution
 */
public class LightBoxHandler extends BaseThingHandler {
    private Logger logger = LoggerFactory.getLogger(LightBoxHandler.class);
    private LightBox lightBox;

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            try {
                if (lightBox != null) {
                    LightBox.StateResponse state = lightBox.getStatus();

                    if (state != null) {
                        updateState(BleboxBindingConstants.CHANNEL_BRIGHTNESS, state.getWhiteBrightness());
                        updateState(BleboxBindingConstants.CHANNEL_COLOR, state.getColor());

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

    public LightBoxHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        switch (channelUID.getId()) {
            case BleboxBindingConstants.CHANNEL_COLOR:
                if (command instanceof HSBType) {
                    HSBType hsbCommand = (HSBType) command;
                    if (hsbCommand.getBrightness().intValue() == 0) {
                        // lightState = LightStateConverter.toOnOffLightState(OnOffType.OFF);
                        lightBox.setColor(HSBType.BLACK);
                    } else {
                        lightBox.setColor(hsbCommand);
                    }
                }
                break;
            case BleboxBindingConstants.CHANNEL_BRIGHTNESS:
                if (command instanceof PercentType) {
                    // lightState = LightStateConverter.toBrightnessLightState((PercentType) command);
                    lightBox.setWhiteBrightness((PercentType) command);
                } else if (command instanceof OnOffType) {
                    lightBox.setWhiteBrightness((OnOffType) command);
                }
                break;
        }
    }

    @Override
    public void initialize() {
        final String ipAddress = (String) getConfig().get(BleboxDeviceConfiguration.IP);

        if (ipAddress != null) {
            lightBox = new LightBox(ipAddress);
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

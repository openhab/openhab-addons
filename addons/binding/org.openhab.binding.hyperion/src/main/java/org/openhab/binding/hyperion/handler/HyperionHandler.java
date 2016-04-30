/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.hyperion.handler;

import static org.openhab.binding.hyperion.HyperionBindingConstants.*;

import java.awt.Color;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.UnknownHostException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.hyperion.internal.protocol.HyperionConnection;
import org.openhab.binding.hyperion.internal.protocol.HyperionStateListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HyperionHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Daniel Walters - Initial contribution
 */
public class HyperionHandler extends BaseThingHandler implements HyperionStateListener {

    private Logger logger = LoggerFactory.getLogger(HyperionHandler.class);
    private HyperionConnection server;
    private ScheduledFuture<?> refreshHandler;

    public HyperionHandler(Thing thing) {
        super(thing);

    }

    @Override
    public void initialize() {
        logger.debug("Initializing Hyperion thing handler.");
        try {
            String address = (String) thing.getConfiguration().get(PROP_HOST);
            int port = ((BigDecimal) thing.getConfiguration().get(PROP_PORT)).intValue();
            int refreshInterval = ((BigDecimal) thing.getConfiguration().get(PROP_POLL_FREQUENCY)).intValue();

            server = new HyperionConnection(address, port);
            server.addListener(this);
            refreshHandler = scheduler.scheduleWithFixedDelay(new Runnable() {
                @Override
                public void run() {
                    server.synchronize();
                }
            }, 0, refreshInterval, TimeUnit.SECONDS);

            updateStatus(ThingStatus.ONLINE);
        } catch (UnknownHostException e) {
            logger.error("Could not resolve host: {}", e.getMessage());
            updateStatus(ThingStatus.OFFLINE);
        }
    }

    @Override
    public void dispose() {
        logger.debug("Disposing of Hyperion thing handler.");
        refreshHandler.cancel(true);
        server.removeListener(this);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        try {
            if (channelUID.getId().equals(CHANNEL_BRIGHTNESS)) {
                handleBrightness(command);
            } else if (channelUID.getId().equals(CHANNEL_COLOR)) {
                handleColor(command);
            } else if (channelUID.getId().equals(CHANNEL_CLEAR_ALL)) {
                handleClearAll(command);
            } else if (channelUID.getId().equals(CHANNEL_CLEAR)) {
                handleClear(command);
            } else if (channelUID.getId().equals(CHANNEL_EFFECT)) {
                handleEffect(command);
            }
        } catch (IOException e) {
            logger.error("Unable to send command: {}", command.toString());
        }
    }

    private void handleEffect(Command command) throws IOException {
        if (command instanceof StringType) {
            StringType effect = (StringType) command;
            int priority = ((BigDecimal) thing.getConfiguration().get(PROP_PRIORITY)).intValue();
            server.setEffect(effect.toString(), priority);
            logger.debug("channel {} set to {}", CHANNEL_EFFECT, effect.toString());
        } else {
            logger.warn("Channel {} unable to process command {}", CHANNEL_EFFECT, command.toString());
        }

    }

    private void handleBrightness(Command command) throws IOException {
        if (command instanceof PercentType) {
            PercentType percent = (PercentType) command;
            double value = percent.doubleValue() / 100.0;
            server.setValueGain(value);
            logger.debug("channel {} set to {}", CHANNEL_BRIGHTNESS, value);
        } else if (command instanceof OnOffType) {
            OnOffType onOff = (OnOffType) command;
            double value = onOff == OnOffType.ON ? 1.0 : 0.0;
            server.setValueGain(value);
            logger.debug("channel {} set to {}", CHANNEL_BRIGHTNESS, value);
        } else if (command instanceof IncreaseDecreaseType) {
            // get current brightness
            // increment/decrement current brightness by 5
            // set new brightness
            // double currentGain = server.getValueGain();
            // double newGain = currentGain + 0.05;

            // server.setValue(newGain);
        } else {
            logger.warn("Channel {} unable to process command {}", CHANNEL_BRIGHTNESS, command.toString());
        }
    }

    private void handleColor(Command command) throws IOException {
        if (command instanceof HSBType) {
            HSBType color = (HSBType) command;
            Color c = new Color(color.getRGB());
            int r = c.getRed();
            int g = c.getGreen();
            int b = c.getBlue();

            int priority = ((BigDecimal) thing.getConfiguration().get(PROP_PRIORITY)).intValue();
            server.setColor(r, g, b, priority);
            logger.debug("Channel {} set to {}", CHANNEL_COLOR, command.toString());
        } else {
            logger.warn("Channel {} unable to process command {}", CHANNEL_COLOR, command.toString());
        }
    }

    private void handleClearAll(Command command) throws IOException {
        server.clearAll();
        logger.debug("Channel {} set to {}", CHANNEL_CLEAR, "clear all");
    }

    private void handleClear(Command command) throws IOException {
        int priority = ((BigDecimal) thing.getConfiguration().get(PROP_PRIORITY)).intValue();
        server.clearPriority(priority);
        logger.debug("Channel {} set to {}", CHANNEL_CLEAR, "clear");
    }

    @Override
    public void stateChanged(String property, Object oldValue, Object newValue) {
        if (HyperionConnection.PROPERTY_VALUEGAIN.equals(property)) {
            int intValue = (int) Math.round((double) newValue * 100);
            PercentType percentType = new PercentType(intValue);
            updateState(CHANNEL_BRIGHTNESS, percentType);
        } else {
            logger.warn("Ignoring change to {}", property);
        }
    }
}

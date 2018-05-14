/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.jeelink.internal.pca301;

import static org.openhab.binding.jeelink.JeeLinkBindingConstants.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.jeelink.internal.JeeLinkHandler;
import org.openhab.binding.jeelink.internal.JeeLinkSensorHandler;
import org.openhab.binding.jeelink.internal.ReadingPublisher;
import org.openhab.binding.jeelink.internal.config.Pca301SensorConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for a EC3000 sensor thing.
 *
 * @author Volker Bier - Initial contribution
 */
public class Pca301SensorHandler extends JeeLinkSensorHandler<Pca301Reading> {
    private final Logger logger = LoggerFactory.getLogger(Pca301SensorHandler.class);

    private JeeLinkHandler bridge;
    private OnOffType state;
    private final AtomicInteger channel = new AtomicInteger(-1);

    private ScheduledFuture<?> retry;
    private int sendCount;

    public Pca301SensorHandler(Thing thing) {
        super(thing);
    }

    @Override
    public Class<Pca301Reading> getReadingClass() {
        return Pca301Reading.class;
    }

    @Override
    public void initialize() {
        super.initialize();

        bridge = (JeeLinkHandler) getBridge().getHandler();

        Pca301SensorConfig cfg = getConfigAs(Pca301SensorConfig.class);
        sendCount = cfg.sendCount;

        logger.debug("initilized handler for thing {} ({}): sendCount = {}", getThing().getLabel(),
                getThing().getUID().getId(), sendCount);
    }

    @Override
    public void dispose() {
        cancelRetry();
    }

    @Override
    public synchronized void handleCommand(ChannelUID channelUid, Command command) {
        logger.debug("received command for thing {} ({}): {}", getThing().getLabel(), getThing().getUID().getId(),
                command);

        if (channelUid.getIdWithoutGroup().equals(SWITCHING_STATE_CHANNEL)) {
            if (command instanceof OnOffType) {
                sendCommandRetry((OnOffType) command);
            } else {
                sendCommand(command);
            }
        } else if (command != RefreshType.REFRESH) {
            logger.warn("Unsupported command {} for channel {} of sensor with id {}.", command,
                    channelUid.getIdWithoutGroup(), this.id);
        }
    }

    @Override
    public ReadingPublisher<Pca301Reading> createPublisher() {
        return new ReadingPublisher<Pca301Reading>() {
            @Override
            public void publish(Pca301Reading reading) {
                if (reading != null) {
                    channel.set(reading.getChannel());

                    BigDecimal current = new BigDecimal(reading.getCurrent()).setScale(1, RoundingMode.HALF_UP);
                    state = reading.isOn() ? OnOffType.ON : OnOffType.OFF;

                    updateState(CURRENT_WATT_CHANNEL, new DecimalType(current));
                    updateState(CONSUMPTION_CHANNEL, new DecimalType(reading.getTotal()));
                    updateState(SWITCHING_STATE_CHANNEL, state);

                    logger.debug("updated states for thing {} ({}): state={}, current={}, total={}",
                            getThing().getLabel(), getThing().getUID().getId(), state, current, reading.getTotal());
                }
            }

            @Override
            public void dispose() {
            }
        };
    }

    private void sendCommand(Command command) {
        int chan = channel.get();

        if (chan != -1) {
            if (command == RefreshType.REFRESH) {
                bridge.getConnection().sendCommands(chan + ",4," + id.replaceAll("-", ",") + ",0,255,255,255,255s");
            } else if (command instanceof OnOffType) {
                bridge.getConnection().sendCommands(chan + ",5," + id.replaceAll("-", ",") + ","
                        + (command == OnOffType.ON ? "1" : "2") + ",255,255,255,255s");
            } else {
                logger.warn("Unsupported command {} for sensor with id {}.", command, this.id);
            }
        } else if (command != RefreshType.REFRESH && !(command instanceof OnOffType)) {
            logger.warn("Could not send command {} for sensor with id {}. Ignoring command.", command, this.id);
        }
    }

    private synchronized void sendCommandRetry(OnOffType command) {
        cancelRetry();

        retry = scheduler.scheduleWithFixedDelay(new Runnable() {
            int remainingRetries = sendCount;

            @Override
            public void run() {
                if (state == null) {
                    logger.debug("skip sending of command (current state not yet known) for thing {} ({}): {}",
                            getThing().getLabel(), getThing().getUID().getId(), command);
                } else if ((state != command && remainingRetries > 0)) {
                    logger.debug("sending command for thing {} ({}) attempt {}/{}: {}", getThing().getLabel(),
                            getThing().getUID().getId(), (sendCount - remainingRetries + 1), sendCount, command);

                    sendCommand(command);
                    remainingRetries--;
                } else if (state != command) {
                    logger.debug("giving up command for thing {} ({}): {}", getThing().getLabel(),
                            getThing().getUID().getId(), command);

                    cancelRetry();
                }
            }
        }, 0, 2, TimeUnit.SECONDS);
    }

    private synchronized void cancelRetry() {
        if (retry != null) {
            retry.cancel(true);
            retry = null;
        }
    }
}

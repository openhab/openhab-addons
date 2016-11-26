/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.edimax.internal.handler;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.edimax.EdimaxBindingConstants;
import org.openhab.binding.edimax.internal.commands.GetCurrent;
import org.openhab.binding.edimax.internal.commands.GetPower;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link Edimax2101Handler} is responsible for handling commands for the
 * Edimax 2101, which are sent to one of the channels.
 *
 * @author Falk Harnisch - Initial contribution
 */
public class Edimax2101Handler extends EdimaxHandler {

    private final Logger logger = LoggerFactory.getLogger(Edimax2101Handler.class);

    private ScheduledFuture<?> pollingJob;

    /**
     * Constructor the only calls the super constructor.
     *
     * @param thing The thing
     */
    public Edimax2101Handler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        super.handleCommand(channelUID, command);
        try {
            if (EdimaxBindingConstants.CURRENT.equals(channelUID.getId())) {
                if (command instanceof RefreshType) {
                    final DecimalType current = new DecimalType(getCurrent());
                    logger.debug("Current: {}", current);
                    updateState(channelUID, current);
                }
            }
            if (EdimaxBindingConstants.POWER.equals(channelUID.getId())) {
                if (command instanceof RefreshType) {
                    final DecimalType power = new DecimalType(getPower());
                    logger.debug("Power: {}", power);
                    updateState(channelUID, power);
                }
            }
            updateStatus(ThingStatus.ONLINE);
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    @Override
    public void initialize() {
        super.initialize();
        pollingJob = scheduler.scheduleWithFixedDelay(() -> refreshValues(), 0, 30, TimeUnit.SECONDS);
    }

    @Override
    public void dispose() {
        if (pollingJob != null) {
            pollingJob.cancel(true);
        }
    }

    /**
     * Sends command to the channel to refresh current and power.
     */
    private void refreshValues() {
        Channel currentChannel = getThing().getChannel(EdimaxBindingConstants.CURRENT);
        if (currentChannel != null) {
            handleCommand(currentChannel.getUID(), RefreshType.REFRESH);
        }
        Channel powerChannel = getThing().getChannel(EdimaxBindingConstants.POWER);
        if (powerChannel != null) {
            handleCommand(powerChannel.getUID(), RefreshType.REFRESH);
        }
    }

    /**
     * Returns the current.
     *
     * @return Current in Ampere
     * @throws IOException if communication to device fails
     */
    public BigDecimal getCurrent() throws IOException {
        return new GetCurrent().executeCommand(configuration);
    }

    /**
     * Gets the actual power.
     *
     * @return Actual power in Watt
     * @throws IOException if communication to device fails
     */
    public BigDecimal getPower() throws IOException {
        return new GetPower().executeCommand(configuration);
    }
}

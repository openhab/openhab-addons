/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.gmailparadoxparser.internal;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.gmailparadoxparser.internal.model.ParadoxPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ParadoxPartitionHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Konstantin_Polihronov - Initial contribution
 */
@NonNullByDefault
public class ParadoxPartitionHandler extends BaseThingHandler {

    private static final int INITIAL_DELAY = 60; // sec
    private static final int DEFAULT_REFRESH_INTERVAL = 60; // sec

    private final Logger logger = LoggerFactory.getLogger(ParadoxPartitionHandler.class);
    private ScheduledFuture<?> schedule;

    @Nullable
    private ParadoxPartitionConfiguration config;

    public ParadoxPartitionHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Command " + command + " received on ChannelID: " + channelUID);
        retrieveDataFromCache();
    }

    @SuppressWarnings("null")
    private void retrieveDataFromCache() {
        logger.debug("Getting from cache partitition with ID: " + config.partitionId);
        ParadoxPartition paradoxPartition = ParadoxStatesCache.getInstance().get(config.partitionId);
        if (paradoxPartition != null) {
            logger.debug(paradoxPartition.toString() + " updating state to " + paradoxPartition.getState());
            updateState(GmailParadoxParserBindingConstants.STATE, new StringType(paradoxPartition.getState()));
            updateState(GmailParadoxParserBindingConstants.ACTIVATED_BY,
                    new StringType(paradoxPartition.getActivatedBy()));
            updateState(GmailParadoxParserBindingConstants.TIME, new StringType(paradoxPartition.getTime()));
        }
    }

    @Override
    protected void updateState(ChannelUID channelUID, State state) {
        super.updateState(channelUID, state);
        logger.debug("ChannelUID: " + channelUID + " updated state to " + state);
    }

    @Override
    public void initialize() {
        config = getConfigAs(ParadoxPartitionConfiguration.class);
        logger.debug("Start initializing - " + thing.getLabel() + ":" + config.partitionId);

        schedule = scheduler.scheduleWithFixedDelay(() -> {
            retrieveDataFromCache();
        }, INITIAL_DELAY, DEFAULT_REFRESH_INTERVAL, TimeUnit.SECONDS);

        updateStatus(ThingStatus.ONLINE);
        logger.debug("Finished initializing - " + thing.getLabel() + ":" + config.partitionId);
    }

    @Override
    public void dispose() {
        if (schedule != null) {
            boolean cancelingResult = schedule.cancel(true);
            String cancelingSuccessful = cancelingResult ? "successful" : "failed";
            logger.debug("Canceling schedule of " + schedule.toString() + " in class " + getClass().getName()
                    + cancelingSuccessful);
        }
        super.dispose();
    }

}

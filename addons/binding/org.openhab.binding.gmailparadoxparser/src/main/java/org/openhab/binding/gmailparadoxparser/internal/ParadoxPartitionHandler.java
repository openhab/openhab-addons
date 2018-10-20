/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.gmailparadoxparser.internal;

import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
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

    private static final int INITIAL_DELAY = 15; // sec
    private static final int DEFAULT_REFRESH_INTERVAL = 60; // sec

    private final Logger logger = LoggerFactory.getLogger(ParadoxPartitionHandler.class);

    @Nullable
    private ParadoxPartitionConfiguration config;

    public ParadoxPartitionHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        retrieveDataFromCache();
    }

    @SuppressWarnings("null")
    private void retrieveDataFromCache() {
        ParadoxPartition paradoxPartition = ParadoxStatesCache.getInstance().get(config.partitionId);
        if (paradoxPartition != null) {
            updateState(GmailParadoxParserBindingConstants.STATE, new StringType(paradoxPartition.getState()));
        }
    }

    @Override
    protected void updateState(ChannelUID channelUID, State state) {
        super.updateState(channelUID, state);
        logger.debug("ChannelUID: " + channelUID + " updated state to " + state);
    }

    @Override
    public void initialize() {
        logger.debug("Start initializing!");
        config = getConfigAs(ParadoxPartitionConfiguration.class);
        updateStatus(ThingStatus.ONLINE);

        scheduler.scheduleAtFixedRate(() -> {
            retrieveDataFromCache();
        }, INITIAL_DELAY, DEFAULT_REFRESH_INTERVAL, TimeUnit.SECONDS);

        logger.debug("Finished initializing!");
    }

}

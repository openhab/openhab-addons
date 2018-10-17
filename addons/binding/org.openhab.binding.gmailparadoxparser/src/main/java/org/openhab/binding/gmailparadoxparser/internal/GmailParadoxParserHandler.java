/**
 * Copyright (c) 2014,2018 by the respective copyright holders.
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
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
import org.openhab.binding.gmailparadoxparser.model.ParadoxPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link GmailParadoxParserHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Konstantin_Polihronov - Initial contribution
 */
@NonNullByDefault
public class GmailParadoxParserHandler extends BaseThingHandler {

    private static final int INITIAL_DELAY = 1; // sec
    private static final int DEFAULT_REFRESH_INTERVAL = 60; // sec

    private final Logger logger = LoggerFactory.getLogger(GmailParadoxParserHandler.class);

    @Nullable
    private GmailParadoxParserConfiguration config;

    @SuppressWarnings("null")
    public GmailParadoxParserHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        refreshData();
    }

    @SuppressWarnings("null")
    private void refreshData() {
        ParadoxPartition paradoxPartition = StatesCache.getInstance().get(config.partitionId);
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
        config = getConfigAs(GmailParadoxParserConfiguration.class);
        updateStatus(ThingStatus.ONLINE);
        updateStatus(ThingStatus.UNINITIALIZED);

        scheduler.scheduleAtFixedRate(() -> {
            refreshData();
        }, INITIAL_DELAY, DEFAULT_REFRESH_INTERVAL, TimeUnit.SECONDS);

        logger.debug("Finished initializing!");

        // Note: When initialization can NOT be done set the status with more details for further
        // analysis. See also class ThingStatusDetail for all available status details.
        // Add a description to give user information to understand why thing does not work as expected. E.g.
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
        // "Can not access device as username and/or password are invalid");
    }

}

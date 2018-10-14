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

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
import org.openhab.binding.gmailparadoxparser.gmail.adapter.GmailAdapter;
import org.openhab.binding.gmailparadoxparser.gmail.adapter.MailAdapter;
import org.openhab.binding.gmailparadoxparser.gmail.adapter.MailParser;
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

    private static final int INITIAL_DELAY = 10; // sec
    private static final int DEFAULT_REFRESH_INTERVAL = 60; // sec

    private final Logger logger = LoggerFactory.getLogger(GmailParadoxParserHandler.class);
    private MailAdapter mailAdapter;

    @Nullable
    private GmailParadoxParserConfiguration config;
    private static Set<ParadoxPartition> partitionsStates = new HashSet<ParadoxPartition>();

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
        try {
            List<String> retrievedMessages = mailAdapter
                    .retrieveAllMessagesContentsAndMarkAllRead(MailAdapter.QUERY_UNREAD);
            Set<ParadoxPartition> partitionsUpdatedStates = MailParser.getInstance()
                    .parseToParadoxPartitionStates(retrievedMessages);
            updateCachePartitionsState(partitionsUpdatedStates);
            for (ParadoxPartition partitionState : partitionsStates) {
                if (config.partitionId.equals(partitionState.getPartition())) {
                    updateState(GmailParadoxParserBindingConstants.PARTITION_1_ID,
                            new StringType(partitionState.getState()));
                }
            }
        } catch (IOException e) {
            logger.debug(e.getMessage());
        }
    }

    @Override
    protected void updateState(String channelUID, State state) {
        logger.debug("ChannelUID: " + channelUID + ", " + config.partitionId + "updated state to " + state);
        super.updateState(channelUID, state);
    }

    private void updateCachePartitionsState(Set<ParadoxPartition> partitionsUpdatedStates) {
        if (partitionsUpdatedStates.isEmpty()) {
            logger.debug("Received empty set. Nothing to update.");
            return;
        }

        for (ParadoxPartition paradoxPartition : partitionsUpdatedStates) {
            if (partitionsStates.contains(paradoxPartition)) {
                partitionsStates.remove(paradoxPartition);
            }
            partitionsStates.add(paradoxPartition);
        }
    }

    @Override
    public void initialize() {
        logger.debug("Start initializing!");
        config = getConfigAs(GmailParadoxParserConfiguration.class);
        try {
            mailAdapter = new GmailAdapter(logger);
            updateStatus(ThingStatus.ONLINE);
        } catch (IOException | GeneralSecurityException e) {
            logger.trace(e.getMessage(), e);
            updateStatus(ThingStatus.UNINITIALIZED);
            return;
        }

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

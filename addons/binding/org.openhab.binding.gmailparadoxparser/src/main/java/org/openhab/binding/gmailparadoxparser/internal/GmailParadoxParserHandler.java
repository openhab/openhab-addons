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

import static org.openhab.binding.gmailparadoxparser.internal.GmailParadoxParserBindingConstants.PARTITION_CHANNEL_ID;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
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

    private final Logger logger = LoggerFactory.getLogger(GmailParadoxParserHandler.class);
    private MailAdapter mailAdapter;

    @Nullable
    private GmailParadoxParserConfiguration config;

    @SuppressWarnings("null")
    public GmailParadoxParserHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (PARTITION_CHANNEL_ID.equals(channelUID.getId())) {
            if (command instanceof RefreshType) {
                refreshData();
            }
        }
    }

    private void refreshData() {
        try {
            List<String> retrievedMessages = mailAdapter
                    .retrieveAllMessagesContentsAndMarkAllRead(MailAdapter.QUERY_UNREAD);
            Set<ParadoxPartition> partitionsStates = MailParser.getInstance()
                    .parseToParadoxPartitionStates(retrievedMessages);
            for (ParadoxPartition state : partitionsStates) {
                logger.debug(state.toString());
            }
        } catch (IOException e) {
            logger.debug(e.getMessage());
        }
    }

    @Override
    public void initialize() {
        logger.debug("Start initializing!");
        config = getConfigAs(GmailParadoxParserConfiguration.class);
        try {
            mailAdapter = new GmailAdapter(logger);
        } catch (IOException | GeneralSecurityException e) {
            logger.trace(e.getMessage(), e);
            updateStatus(ThingStatus.UNINITIALIZED);
            return;
        }
        updateStatus(ThingStatus.ONLINE);

        scheduler.scheduleAtFixedRate(() -> {
            refreshData();
        }, 30, 15, TimeUnit.SECONDS);

        logger.debug("Finished initializing!");

        // Note: When initialization can NOT be done set the status with more details for further
        // analysis. See also class ThingStatusDetail for all available status details.
        // Add a description to give user information to understand why thing does not work as expected. E.g.
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
        // "Can not access device as username and/or password are invalid");
    }

}

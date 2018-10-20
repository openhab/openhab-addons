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
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ParadoxCommunicationHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Konstantin_Polihronov - Initial contribution
 */
@NonNullByDefault
public class ParadoxCommunicationHandler extends BaseThingHandler {

    private static final int INITIAL_DELAY = 1; // sec
    private static final int DEFAULT_REFRESH_INTERVAL = 60; // sec

    private final Logger logger = LoggerFactory.getLogger(ParadoxCommunicationHandler.class);

    @Nullable
    private ParadoxCommunicationConfiguration config;

    public ParadoxCommunicationHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    private void refreshData() {
        ParadoxStatesCache.getInstance().refresh();
    }

    @SuppressWarnings("null")
    @Override
    public void initialize() {
        logger.debug("Start initializing!");
        config = getConfigAs(ParadoxCommunicationConfiguration.class);
        ParadoxStatesCache.getInstance().initialize();
        updateStatus(ThingStatus.ONLINE);

        logger.debug("Scheduling cache update. Initial delay: " + INITIAL_DELAY + ". Refresh interval: "
                + config.refresh + ".");
        scheduler.scheduleAtFixedRate(() -> {
            refreshData();
        }, INITIAL_DELAY, config.refresh, TimeUnit.SECONDS);

        logger.debug("Finished initializing!");
    }

}

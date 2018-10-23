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
    ScheduledFuture<?> schedule;

    @Nullable
    private ParadoxCommunicationConfiguration config;

    public ParadoxCommunicationHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Command " + command + " received on ChannelID: " + channelUID);
    }

    private void refreshData() {
        ParadoxStatesCache.getInstance().refresh();
    }

    @SuppressWarnings("null")
    @Override
    public void initialize() {
        logger.debug("Start initializing - " + thing.getLabel());

        config = getConfigAs(ParadoxCommunicationConfiguration.class);
        updateStatus(ThingStatus.ONLINE);

        ParadoxStatesCache.getInstance().initialize();

        logger.debug("Scheduling cache update. Initial delay: " + INITIAL_DELAY + ". Refresh interval: "
                + config.refresh + ".");
        schedule = scheduler.scheduleWithFixedDelay(() -> {
            refreshData();
        }, INITIAL_DELAY, config.refresh, TimeUnit.SECONDS);

        logger.debug("Finished initializing - " + thing.getLabel());
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

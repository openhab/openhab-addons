/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.paradoxalarm.internal.handlers;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.openhab.binding.paradoxalarm.internal.model.ParadoxPanel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link EntityBaseHandler} abstract handler class that contains common logic for entities.
 *
 * @author Konstantin Polihronov - Initial contribution
 */
public abstract class EntityBaseHandler extends BaseThingHandler {

    private static final long INITIAL_DELAY_SECONDS = 15;
    private static final int MAX_WAIT_TIME_MILLIS = 60000;
    private long timeStamp;

    private final Logger logger = LoggerFactory.getLogger(EntityBaseHandler.class);

    protected EntityConfiguration config;
    private ScheduledFuture<?> delayedSchedule;

    public EntityBaseHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        logger.trace("Start initializing. {}", thing.getUID());
        updateStatus(ThingStatus.UNKNOWN);

        config = getConfigAs(EntityConfiguration.class);

        timeStamp = System.currentTimeMillis();
        delayedSchedule = scheduler.schedule(this::initializeDelayed, INITIAL_DELAY_SECONDS, TimeUnit.SECONDS);
    }

    private void initializeDelayed() {
        logger.debug("Start initializeDelayed() in {}", getThing().getUID());
        ParadoxPanel panel = ParadoxPanel.getInstance();
        // Asynchronous update not yet done
        if (panel.getPanelInformation() == null) {
            // Retry until reach MAX_WAIT_TIME
            if (System.currentTimeMillis() - timeStamp <= MAX_WAIT_TIME_MILLIS) {
                logger.debug("Panel information is null. Scheduling initializeDelayed() to be executed again in {} sec",
                        INITIAL_DELAY_SECONDS);
                delayedSchedule = scheduler.schedule(this::initializeDelayed, INITIAL_DELAY_SECONDS, TimeUnit.SECONDS);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR,
                        "Panel is not updating the information in " + MAX_WAIT_TIME_MILLIS
                                + " ms. Giving up. Cannot update entity=" + this + ".");
            }

            // Asynchronous update done but panel is not supported
        } else if (!panel.isPanelSupported()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Panel is not supported. Cannot update entity=" + this + ".");
            // All OK
        } else {
            updateStatus(ThingStatus.ONLINE);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (ThingStatus.OFFLINE == getThing().getStatus()) {
            logger.debug("Received {} command but {} is OFFLINE with the following detailed status {}", command,
                    getThing().getUID(), getThing().getStatusInfo());
            return;
        }

        if (command instanceof RefreshType) {
            updateEntity();
        }
    }

    @Override
    public void dispose() {
        if (delayedSchedule != null) {
            boolean cancelingResult = delayedSchedule.cancel(true);
            String cancelingSuccessful = cancelingResult ? "successful" : "failed";
            logger.debug("Canceling schedule of {} is {}", delayedSchedule, cancelingSuccessful);
        }
    }

    protected abstract void updateEntity();

    protected int calculateEntityIndex() {
        return Math.max(0, config.getId() - 1);
    }
}

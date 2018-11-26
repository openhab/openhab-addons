/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.paradoxalarm.internal;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.paradoxalarm.internal.model.ParadoxPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link EntityBaseHandler} abstract handler class that contains common logic for entities.
 *
 * @author Konstantin_Polihronov - Initial contribution
 */
public abstract class EntityBaseHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(ParadoxPartitionHandler.class);
    private static final long INITIAL_DELAY = 15;

    protected EntityConfiguration config;
    private ScheduledFuture<?> refreshEntitySchedule;

    public EntityBaseHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        logger.debug("Start initializing!");
        updateStatus(ThingStatus.UNKNOWN);

        initializeConfig(getThing().getThingTypeUID());

        ParadoxPanel panel = ParadoxPanel.getInstance();
        if (panel == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "Unable to find panel instance.");
        } else if (!panel.isPanelSupported()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Panel " + panel.getPanelInformation().getPanelType().name() + " is not supported.");
        } else {
            updateEntity();
            updateStatus(ThingStatus.ONLINE);
        }
        createSchedules();
        logger.debug("Finished initializing!");
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (ParadoxAlarmBindingConstants.PARTITION_THING_TYPE_UID.equals(channelUID.getId())
                || ParadoxAlarmBindingConstants.ZONE_THING_TYPE_UID.equals(channelUID.getId())) {
            if (command instanceof RefreshType) {
                updateEntity();
            }
        }
    }

    @Override
    public void dispose() {
        cancelSchedules();
    }

    protected abstract void updateEntity();

    protected void initializeConfig(ThingTypeUID thingTypeUID) {
        config = getConfigAs(EntityConfiguration.class);
    }

    protected void createSchedules() {
        createRefreshSchedule();
    }

    protected void cancelSchedules() {
        cancelRefreshSchedule();
    }

    private void createRefreshSchedule() {
        logger.debug("Scheduling thing update{}. Refresh interval: {}sec.", config.getRefresh());
        refreshEntitySchedule = scheduler.scheduleWithFixedDelay(() -> {
            updateEntity();
        }, INITIAL_DELAY, config.getRefresh(), TimeUnit.SECONDS);
    }

    private void cancelRefreshSchedule() {
        if (refreshEntitySchedule != null) {
            boolean cancelingResult = refreshEntitySchedule.cancel(true);
            String cancelingSuccessful = cancelingResult ? "successful" : "failed";
            logger.debug("Canceling schedule of " + refreshEntitySchedule.toString() + " in class "
                    + getClass().getName() + cancelingSuccessful);
        }

    }

    protected int calculateEntityIndex() {
        int index = config.getId() - 1;
        if (index < 0) {
            index = 0;
        }
        return index;
    }
}

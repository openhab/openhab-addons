/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.paradoxalarm.internal.exceptions.ParadoxBindingException;
import org.openhab.binding.paradoxalarm.internal.model.ParadoxPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link EntityBaseHandler} abstract handler class that contains common logic for entities.
 *
 * @author Konstantin_Polihronov - Initial contribution
 */
public abstract class EntityBaseHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(EntityBaseHandler.class);
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

        try {
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
            logger.debug("Finished initializing!");
        } catch (ParadoxBindingException e) {
            logger.error("Unable to retrieve/create Paradox panel instance. Exception: {}", e);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            updateEntity();
        }
    }

    @Override
    public void dispose() {
    }

    protected void initializeConfig(ThingTypeUID thingTypeUID) {
        config = getConfigAs(EntityConfiguration.class);
    }

    protected abstract void updateEntity();

    protected int calculateEntityIndex() {
        int index = config.getId() - 1;
        if (index < 0) {
            index = 0;
        }
        return index;
    }
}
